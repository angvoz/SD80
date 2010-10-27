/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.services;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.CodeBufferUnderflowException;
import org.eclipse.cdt.debug.edc.disassembler.DisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.EDCInstruction;
import org.eclipse.cdt.debug.edc.disassembler.EDCInstructionFunctionInfo;
import org.eclipse.cdt.debug.edc.disassembler.EDCMixedInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.EDCServicesMessages;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IModules.AddressRange;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;


public class Disassembly extends AbstractEDCService implements IDisassembly {

	/**
	 * @param classNames
	 *            the type names the service will be registered under. See
	 *            AbstractDsfService#register for details. We tack on base DSF's
	 *            IDisassembly and this class to the list if not provided.
	 * @since 2.0
	 */
	public Disassembly(DsfSession session, String[] classNames) {
		super(session,
				massageClassNames(classNames, 
						new String[] { IDisassembly.class.getName(), Disassembly.class.getName() }));
	}

	/**
	 * @return IStatus.ERROR containing NLS string indicating disassembler not yet available
	 * @since 2.0
	 */
	public static IStatus statusNoDisassembler() {
		return new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
						  EDCServicesMessages.Disassembly_NoDisassemblerYet, null);
	}

	/**
	 * @param memoryAt 1st location of unreadable memory
	 * @param memoryLength length of unreadable memory
	 * @return IStatus.ERROR containing formatted message with location & length
	 */
	private static IStatus statusCannotReadMemory(String memoryAt, Integer memoryLength) {
		return new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
						  cantReadMemory(memoryAt, memoryLength.toString()), null);
	}

	/**
	 * string for use in both status for error return and in
	 * pseudo-instruction used in error recovery mode for disassembly view
	 * @param memoryAt 1st location of unreadable memory
	 * @param memoryLength length of unreadable memory (incoming null will "unknown" for length)
	 * @return formatted message with location & length
	 */
	private static String cantReadMemory(String memoryAt, String memoryLength) {
		memoryLength = (memoryLength == null) ? "unknown" : memoryLength;
		return MessageFormat.format(EDCServicesMessages.Disassembly_CannotReadMemoryAt,
									memoryAt, memoryLength);
	}

	/**
	 * check each byte of incoming MemoryByte[] array to see if readable;
	 * fills the RequestMonitor status in case of unreadable bytes.
	 * @param memBytes data to translate
	 * @param start address of first byte of memBytes
	 * @param rm with which to set status in case of error
	 * @return code buffer translated from incoming memByte array, null if any are unreadable
	 * @since 2.0
	 */
	public static ByteBuffer translateMemoryBytes(MemoryByte[] memBytes,
			IAddress start, RequestMonitor rm) {
		byte[] bytes = new byte[memBytes.length];
		for (int i = 0; i < memBytes.length; i++) {
			// check each byte
			if (!memBytes[i].isReadable()) {
				rm.setStatus(statusCannotReadMemory(start.add(i).toHexAddressString(), 
													((Integer)(memBytes.length-i))));
				rm.done();
				return null;
			}
			bytes[i] = memBytes[i].getValue();
		}
		return ByteBuffer.wrap(bytes);
	}

	/**
	 * return a buffer of instruction code whose readability matches the
	 * first byte of the data for as long as all such bytes are the same
	 * @param memBytes data to translate
	 * @return a code buffer either full of readable bytes
	 * 		or empty representing the unreadable region
	 */
	private ByteBuffer translateMemoryBytes(List<MemoryByte> memBytes) {
		byte[] bytes = new byte[memBytes.size()];
		boolean firstIsReadable = memBytes.get(0).isReadable();
		int count = 0;
		for (MemoryByte memByte : memBytes) {
			// check each byte
			if (memByte.isReadable() != firstIsReadable)
				break;
			bytes[count++] = firstIsReadable ? memByte.getValue() : 0;
		}

		return ByteBuffer.wrap(bytes, 0, count);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.math.BigInteger, java.math.BigInteger, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getInstructions(final IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			final DataRequestMonitor<IInstruction[]> drm) {

		// FIXME: ignoring null startAddress and null endAddress semantics

		ITargetEnvironment env = getTargetEnvironmentService();
		final IDisassembler disassembler = (env != null) ? env.getDisassembler() : null;
		if (disassembler == null) {
			drm.setStatus(statusNoDisassembler());
			drm.done();
			return;
		}

		DsfServicesTracker services = getServicesTracker();
		if (services == null) // could be null if async invoked as or after debug session ends
			return;

		IMemory memoryService = services.getService(IMemory.class);
		if (memoryService == null) // could be null if async invoked as or after debug session ends
			return;

		final int size = endAddress.intValue() - startAddress.intValue() + 16;

		final IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);
		final IAddress start = new Addr64(startAddress);

		memoryService.getMemory(mem_dmc, start, 0, 1, size,
								new DataRequestMonitor<MemoryByte[]>(getExecutor(), drm) {
			/**
			 * overridden to create a non-error status plus pseudoInstruction data-set
			 * in the DRM requested by DisassemblyBackendDsf, where a DRM.status of
			 * ERROR is turned into an "invalid" block in its document map.  Such
			 * blocks are repeatedly re-requested ... causing scrolling oddities & performance issues.
			 * @see failedMemoryDsfPseudoInstructions
			 */
			@Override
			protected void handleError() {
				IStatus s = getStatus();
		    	Throwable e = s.getException();
		    	if (e instanceof MemoryError && s.getMessage().contains("Fail to read memory")) {
					drm.setData(failedMemoryDsfPseudoInstructions(start, size, s.getMessage()));
					drm.done();
				} else {
					super.handleError();
		        }
		        
		    }

		    @Override
			protected void handleSuccess() {
				List<MemoryByte> memBytes = Arrays.asList(getData());
				Map<String, Object> options = new HashMap<String, Object>();
				try {
					ArrayList<IInstruction> instrs
					  = fillDisassemblyViewInstructions(memBytes, start, context,
														disassembler, options);
					drm.setData(instrs.toArray(new IInstruction[instrs.size()]));
				} catch (CoreException e) {
					drm.setStatus(e.getStatus());
				}
				drm.done();
			}
		});

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.lang.String, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getInstructions(final IDisassemblyDMContext context, String filename, int linenum, final int lines,
			final DataRequestMonitor<IInstruction[]> drm) {

		// FIXME: ignoring "lines" semantics

		IModules modulesService = getServicesTracker().getService(IModules.class);

		ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		filename = EDCLaunch.getLaunchForSession(getSession().getId()).getCompilationPath(filename);

		modulesService.calcAddressInfo(sym_dmc, filename, linenum, 0,
									   new DataRequestMonitor<AddressRange[]>(getExecutor(), drm) {
			@Override
			protected void handleSuccess() {
				AddressRange[] addr_ranges = getData();

				IAddress start = addr_ranges[0].getStartAddress();
				IAddress end = start.add(lines * 4); // kind of arbitrary end
														// address hint.

				getInstructions(context, start.getValue(), end.getValue(), drm);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getMixedInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.math.BigInteger, java.math.BigInteger, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getMixedInstructions(final IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			final DataRequestMonitor<IMixedInstruction[]> drm) {

		// FIXME: ignoring null startAddress and null endAddress semantics

		DsfServicesTracker services = getServicesTracker();
		if (services == null) // could be null if async invoked as or after debug session ends
			return;

		IEDCSymbols symbolsService = services.getService(IEDCSymbols.class);
		if (symbolsService == null) // could be null if async invoked as or after debug session ends
			return;

		IEDCModules modulesService = services.getService(IEDCModules.class);
		if (modulesService == null) // could be null if async invoked as or after debug session ends
			return;

		final ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		// These are absolute runtime addresses.
		final IAddress end = new Addr64(endAddress);
		final IAddress start
		  = getStartAddressForLineEntryContainingAddress(symbolsService, modulesService, sym_dmc,
				  										 new Addr64(startAddress), end);

		ILineEntry startEntry = symbolsService.getLineEntryForAddress(sym_dmc, start);

		if (startEntry == null) {
			// startAddress has no source
			getInstructions(context, startAddress, endAddress,
							new DataRequestMonitor<IInstruction[]>(getExecutor(), drm) {
				@Override
				protected void handleSuccess() {
					IMixedInstruction[] ret = new IMixedInstruction[1];
					ret[0] = new EDCMixedInstruction("unknown", 0, getData()); //$NON-NLS-1$
					drm.setData(ret);
					drm.done();
				}
			});
		} else { // there is source for start address.

			final IEDCModuleDMContext module = modulesService.getModuleByAddress(sym_dmc, start);
			final List<ILineEntry> codeLines = symbolsService.getLineEntriesForAddressRange(sym_dmc, start, end);

			getInstructions(context, startAddress, endAddress,
							new DataRequestMonitor<IInstruction[]>(getExecutor(), drm) {
				@Override
				protected void handleSuccess() {
					List<IMixedInstruction> mixedInstructions = new ArrayList<IMixedInstruction>();

					mixSource(mixedInstructions, null, module, codeLines, getData());

					drm.setData(mixedInstructions.toArray(new IMixedInstruction[mixedInstructions.size()]));
					drm.done();
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getMixedInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.lang.String, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getMixedInstructions(final IDisassemblyDMContext context, String filename, final int linenum,
			final int lines, final DataRequestMonitor<IMixedInstruction[]> drm) {

		// FIXME: ignoring "lines" semantics

		final ITargetEnvironment env = getTargetEnvironmentService();
		final IDisassembler disassembler = (env != null) ? env.getDisassembler() : null;
		if (disassembler == null) {
			drm.setStatus(statusNoDisassembler());
			drm.done();
			return;
		}

		IModules modulesService = getServicesTracker().getService(IModules.class);

		ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		filename = EDCLaunch.getLaunchForSession(getSession().getId()).getCompilationPath(filename);

		modulesService.calcAddressInfo(sym_dmc, filename, linenum, 0,
									   new DataRequestMonitor<AddressRange[]>(getExecutor(), drm) {
			@Override
			protected void handleSuccess() {
				AddressRange[] addr_ranges = getData();

				IAddress start = addr_ranges[0].getStartAddress();
				IAddress end = start.add(lines * 4); // kind of arbitrary end  address hint.
				getMixedInstructions(context, start.getValue(), end.getValue(), drm);
			}
		});
	}

	private static EDCInstruction pseudoInstruction(IAddress address, int size, String pseudoMnemonic) {
		DisassembledInstruction pseudoInstruction = new DisassembledInstruction();
		pseudoInstruction.setAddress(address);
		pseudoInstruction.setSize(size);
		pseudoInstruction.setMnemonics(pseudoMnemonic);
		return new EDCInstruction(pseudoInstruction);
	}

	/**
	 * used in failedMemoryDsfPseudoInstructions() below as chunk boundary for
	 * each of the pseudoInstructions in a larger chunk of retrieved memory.
	 */
	private static final int asmFence = 0x20;

	/**
	 * this utility function creates pseudo-mnemonics indicating failed memory
	 * read. it was refactored from memoryService.getMemory().handleError() so
	 * that it could be utilized by subclass override getInstructions() methods
	 * making the same memoryService.getMemory() call.
	 * <p>
	 * <i>background:</i>
	 * <p>
	 * as of 2010.oct.01, EDC memoryService no longer caches blocks of memory that
	 * cannot be read (a correct change, given that this was blocking the caching
	 * of good memory on the boundaries of such blocks, causing other problems).
	 * <p>
	 * when this change was made, Disassembly#fillDisassemblyViewInstructions()
	 * stopped getting reached through memoryService.getMemory().handleSuccess() .
	 * therefore, actual bad blocks of memory were not getting filled with pseudo
	 * mnemonics indicating bad memory .  in other words, when "Fail to read memory"
	 * errors from TCF caused invocation of memoryService.getMemory().handleFailure()
	 * ... and thus eventually also handleError() ... the result was that
	 * DsfBackendDisassembly would fill the DisassemblyDocument with "invalid"
	 * sections based upon address but no size.  it's algorithm then later attempts
	 * to fill any missing/invalid sections corresponding with the document.  this
	 * results in a visual anomaly where "Unable to retrieve disassembly" would be
	 * populated in the DisassemblyView one block at a time, until there would be a 
	 * large, mostly useless portion of the document view populated with the same
	 * message repeated once for every byte the user had attempted to scroll to.
	 * <p>
	 * the handleError() override implementations that call this utility function
	 * solve the problem whereby DisassemblyBackendDsf interprets DRM.status as
	 * "invalid" sections in its the DisassemblyDocument it is associated with.
	 * <p>
	 * the point of this re-factored code is to create "fences" on regular
	 * boundaries so that chunks of failed memory always get placed on similar
	 * boundaries, thus drastically ameliorating the occurrence of small "invalid"
	 * chunks in the DisassemblyDocument map between chunks of pseudoInstructions
	 * that DisassemblyBackendDsf considers "valid".
	 * <p>
	 * in user terms, this means that scrolling in the view is more consistent
	 * and even, with better performance thanks to fewer attempts to re-retrieve
	 * memory for small "invalid" sections in its map at the boundaries of
	 * previously inserted pseudo-instructions.
	 * 
	 * @param size size of the chunk to break up
	 * @param start location of memory chunk
	 * @param msg message from the target agent
	 * @return array containing 1 or more pseudo-instructions, mostly on asmFence boundaries
	 * @since 2.0
	 */
	protected static IInstruction[] failedMemoryDsfPseudoInstructions(
			IAddress start, final int size, String msg) {
		ArrayList<IInstruction> pseudoInstr = new ArrayList<IInstruction>();
		int offset = 0, chunkSize = Math.min(size, asmFence - start.getValue().intValue() % asmFence);
		do {
			pseudoInstr.add(pseudoInstruction(start.add(offset), chunkSize,
											  msg + "..[length=" + chunkSize + ']'));
			offset += chunkSize;
			chunkSize = Math.min(asmFence, size-offset);
		} while (offset < size);
		return pseudoInstr.toArray(new IInstruction[pseudoInstr.size()]);
	}

	/**
	 * Creates the array of instructions to be used to fill the disassembly view.
	 * for a range containing any unreadable instructions, it will create a
	 * fake instruction consisting of the address, the entire unreadable range,
	 * and a message to fill in the mnemonics section about the unreadable range.
	 * @param memBytes the buffer containing the bytes to be disassembled
	 * @param start starting address corresponding to the buffer
	 * @param context context for disassembly
	 * @param disassembler the disassembler object to use
	 * @param options to be passed to the disassembleInstructions() call
	 * @return array of IInstruction
	 * @throws CoreException can be thrown by disassembleInstructions().
	 * @since 2.0
	 */
	protected ArrayList<IInstruction> fillDisassemblyViewInstructions(
			final List<MemoryByte> memBytes, final IAddress start,
			final IDisassemblyDMContext context, final IDisassembler disassembler,
			Map<String, Object> options)
			throws CoreException {
		ArrayList<IInstruction> ret = new ArrayList<IInstruction>();
		for (int offset = 0, last = memBytes.size(); offset < last ;) {
			ByteBuffer codeBuf = translateMemoryBytes(memBytes.subList(offset, last));
			int codeBufSize = codeBuf.limit();
			IAddress block = start.add(offset);
			if (memBytes.get(offset).isReadable()) {
				try {
					List<IDisassembledInstruction> insts
					  = disassembler.disassembleInstructions(block, block.add(codeBufSize),
							  								 codeBuf, options, context);
					if (insts.size() == 0)
						break;
					for (int i = 0; i < insts.size(); i++) {
						ret.add(new EDCInstruction(insts.get(i)));
						offset += insts.get(i).getSize();
					}
				} catch (CodeBufferUnderflowException e) {
					if (offset == 0 && codeBufSize == last) {
						// nothing in entire block can be disassembled;
						// at least tell the Disassembly view code this much
						ret.add(pseudoInstruction(start, codeBufSize,
												  "Buffer Underflow during disassembly")); //$NON-NLS-1$
					}
					offset += codeBufSize;
				}
			} else {	// this will only occur when the target supports partial bad blocks
				ret.add(pseudoInstruction(block, codeBufSize,
										  cantReadMemory(block.toHexAddressString(),
			  					   						 ((Integer)codeBufSize).toString())));
				offset += codeBufSize;
			}
		}
		return ret;
	}

	/**
	 * whereas the default implementation of getMixedInstructions() gets and
	 * processes all the disassembly in one pass, some variants are known to need
	 * to override the default implementation to break the retrieval into chunks.
	 * <p>
	 * this portion of the mixing is still the same within those chunks, though,
	 * and so has been extracted and protected for use by extending variant classes.
	 * @param mixedInstructions the growing list of instructions being processed
	 * @param wholeFunctionName name for whole function; if null, will be calculated per line
	 * @param module the module for this block of code
	 * @param codeLines list of ILineEntry containing info for mixing
	 * @param instructions the instructions to be mixed with the source
	 * @since 2.0
	 */
	protected void mixSource(List<IMixedInstruction> mixedInstructions,
			final EDCInstructionFunctionInfo wholeBlockInfo,
			final IEDCModuleDMContext module,
			final List<ILineEntry> codeLines,
			final IInstruction[] instructions) {

		List<IInstruction> instsForLine = new ArrayList<IInstruction>();

		IPath filePath = null;
		String osString = null;

		EDCInstructionFunctionInfo functionInfo = wholeBlockInfo;
		int k = 0, instsCnt = instructions.length, lineCnt = codeLines.size();
		for (int i = 0; i < lineCnt && k < instsCnt; i++) {
			// Now map the instructions to source lines to generate
			// MixedInstructions.
			instsForLine.clear();
			ILineEntry line = codeLines.get(i);

			if (wholeBlockInfo == null && module != null) {
				functionInfo = new EDCInstructionFunctionInfo(module, line);
			}

			while (k < instsCnt) {
				EDCInstruction inst = (EDCInstruction)instructions[k];
				IAddress linkAddress = module.toLinkAddress(new Addr64(inst.getAdress()));
				if (linkAddress.compareTo(line.getHighAddress()) >= 0)
					break;

				if (functionInfo != null) {
					inst.setFunctionName(functionInfo.getFunctionName());
					IAddress functionBase = functionInfo.getFunctionStartAddress();
					if (functionBase != null) {
						inst.setOffset(functionBase.distanceTo(linkAddress).intValue());
					}
				}
				instsForLine.add(inst);
				k++;
			}
			if (line.getFilePath() != filePath) {
				filePath = line.getFilePath();
				osString = (filePath != null) ? filePath.toOSString() : null;
			}
			mixedInstructions.add(new EDCMixedInstruction(osString, line.getLineNumber(),
								  						  instsForLine.toArray(new IInstruction[instsForLine.size()])));
		}
	}

	/**
	 * disassembly utility function to find the first address for a line-entry 
	 * for an address contained by that line-entry
	 * @param symbolsService
	 * @param modulesService
	 * @param sym_dmc symbol context used to retrieve LineEntry for address
	 * @param initialStartAddress the address of interest
	 * @param endAddress the last address of a range to search
	 * @return the first address of an associated LineEntry if it can be found, else the initialStartAddress
	 * @since 2.0
	 */
	protected static IAddress getStartAddressForLineEntryContainingAddress(final IEDCSymbols symbolsService,
			final IEDCModules modulesService, final ISymbolDMContext sym_dmc, final IAddress initialStartAddress,
			final IAddress endAddress) {
		assert symbolsService != null && modulesService != null;
		if (symbolsService == null || modulesService == null)
			return initialStartAddress;

		if (sym_dmc != null) {
			// in case the caller requested a start that falls somewhere other than the
			// boundary of an instruction, back up to that boundary for the first instruction
			if (symbolsService.getLineEntryForAddress(sym_dmc, initialStartAddress) != null) {
				IEDCModuleDMContext module = modulesService.getModuleByAddress(sym_dmc, initialStartAddress);
				if (module != null) {
					List<ILineEntry> allLines
					  = symbolsService.getLineEntriesForAddressRange(sym_dmc, initialStartAddress, endAddress);
					if (allLines != null && !allLines.isEmpty())
					{
						return module.toRuntimeAddress(allLines.get(0).getLowAddress());
					}
				}
			}
		}
		return initialStartAddress;
	}
}

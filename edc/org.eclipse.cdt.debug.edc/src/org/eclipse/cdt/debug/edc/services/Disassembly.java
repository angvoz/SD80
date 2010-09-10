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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.EDCInstruction;
import org.eclipse.cdt.debug.edc.disassembler.EDCMixedInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.EDCServicesMessages;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;

public class Disassembly extends AbstractEDCService implements IDisassembly {

	public Disassembly(DsfSession session) {
		super(session, new String[] { IDisassembly.class.getName(), Disassembly.class.getName() });
	}

	/**
	 * @return NLS-ized string indicating the there's no disassembler, yet
	 * @since 2.0
	 */
	protected IStatus statusNoDisassembler() {
		return new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
						  EDCServicesMessages.Disassembly_NoDisassemblerYet, null);
	}

	/**
	 * @return NLS-ized string indicating cannot read memory, yet
	 * @since 2.0
	 */
	protected IStatus statusCannotReadMemory(String memoryAt) {
		return new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
						  MessageFormat.format(EDCServicesMessages.Disassembly_CannotReadMemoryAt, memoryAt),
						  null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.math.BigInteger, java.math.BigInteger, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			final DataRequestMonitor<IInstruction[]> drm) {

		// FIXME: ignoring null startAddress and null endAddress semantics

		final IDisassembler disassembler = getTargetEnvironmentService().getDisassembler();
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

		long size = endAddress.longValue() - startAddress.longValue() + 16;

		final IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);
		final IAddress start = new Addr64(startAddress);
		final IAddress end = new Addr64(endAddress);

		memoryService.getMemory(mem_dmc, start, 0, 1, (int) size,
								new DataRequestMonitor<MemoryByte[]>(getExecutor(), drm) {
			@Override
			protected void handleSuccess() {
				MemoryByte[] memBytes = getData();
				final byte[] bytes = new byte[memBytes.length];
				for (int i = 0; i < memBytes.length; i++) {
					// check each byte
					if (!memBytes[i].isReadable()) {
						drm.setStatus(statusCannotReadMemory(start.add(i).getValue().toString(16)));
						drm.done();
						return;
					}
					bytes[i] = memBytes[i].getValue();
				}

				ByteBuffer codeBuf = ByteBuffer.wrap(bytes);

				Map<String, Object> options = new HashMap<String, Object>();
				try {
					List<IDisassembledInstruction> insts
					  = disassembler.disassembleInstructions(start, end, codeBuf, options);

					IInstruction[] ret = new IInstruction[insts.size()];
					for (int i = 0; i < insts.size(); i++) {
						ret[i] = new EDCInstruction(insts.get(i));
					}

					drm.setData(ret);
					drm.done();
				} catch (CoreException e) {
					drm.setStatus(e.getStatus());
					drm.done();
				}
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
	public void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
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

					mixSource(mixedInstructions, module, codeLines, getData());

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

		final IDisassembler disassembler = getTargetEnvironmentService().getDisassembler();
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

	/**
	 * whereas the default implementation of getMixedInstructions() gets and
	 * processes all the disassembly in one pass, some variants are known to need
	 * to override the default implementation to break the retrieval into chunks.
	 * this portion of the mixing is still the same within those chunks, though,
	 * and so has been extracted and protected for use by extending variant classes.
	 * @param mixedInstructions the growing list of instructions being processed
	 * @param module the module for this block of code
	 * @param codeLines list of ILineEntry containing info for mixing
	 * @param instructions the instructions to be mixed with the source
	 * @since 2.0
	 */
	protected void mixSource(List<IMixedInstruction> mixedInstructions,
			final IEDCModuleDMContext module, final List<ILineEntry> codeLines,
			final IInstruction[] instructions) {

		List<IInstruction> instsForLine = new ArrayList<IInstruction>();

		int k = 0, instsCnt = instructions.length, lineCnt = codeLines.size();
		for (int i = 0; i < lineCnt && k < instsCnt; i++) {
			// Now map the instructions to source lines to generate
			// MixedInstructions.
			//
			instsForLine.clear();
			ILineEntry line = codeLines.get(i);

			while (k < instsCnt
					&& module.toLinkAddress(new Addr64(instructions[k].getAdress()))
											.compareTo(line.getHighAddress()) < 0) {
				instsForLine.add(instructions[k]);
				k++;
			}
			mixedInstructions.add(new EDCMixedInstruction(line.getFilePath().toOSString(), line.getLineNumber(),
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
	protected IAddress getStartAddressForLineEntryContainingAddress(final IEDCSymbols symbolsService,
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

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
package org.eclipse.cdt.debug.edc.internal.arm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.EDCInstructionFunctionInfo;
import org.eclipse.cdt.debug.edc.disassembler.EDCMixedInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.DisassemblerARM.IDisassemblerOptionsARM;
import org.eclipse.cdt.debug.edc.services.Disassembly;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.services.IEDCSymbols;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;


public class ARMDisassembly extends Disassembly {

	public ARMDisassembly(DsfSession session) {
		super(session, new String[] {ARMDisassembly.class.getName()});
	}

	/**
	 * overrides the default implementation in order to first break the
	 * whole requested range into a collection of ranges whose primary
	 * characteristic is whether the range is ARM or Thumb mode; this
	 * acts as a cache for each range so the disassembler doesn't have
	 * to perform a full look-up for every byte it disassembles.
	 */
	@Override
	public void getInstructions(final IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			final DataRequestMonitor<IInstruction[]> drm) {

		// FIXME: ignoring null startAddress and null endAddress semantics

		getInstructions(context, new Addr64(startAddress), new Addr64(endAddress), null, drm);
	}

	/**
	 * private version of getInstructions() that takes a pre-calculated RangeAndMode
	 * @param context
	 * @param start
	 * @param end
	 * @param preCalculatedRangeAndMode
	 * 			non-null arg will be used; null arg will forces use of result of getModeAndRange(startAddress, endAddress)
	 * @param drm
	 * @see getInstructions(IDisassemblyDMContext, BigInteger, BigInteger, DataRequestMonitor<IInstruction[]>)
	 */
	private void getInstructions(final IDisassemblyDMContext context, final IAddress start, final IAddress end,
			final RangeAndMode preCalculatedRangeAndMode, final DataRequestMonitor<IInstruction[]> drm) {

		final TargetEnvironmentARM envARM = (TargetEnvironmentARM)getTargetEnvironmentService();
		final IDisassembler disassembler = (envARM != null ) ? envARM.getDisassembler() : null;
		if (disassembler == null) {
			drm.setStatus(statusNoDisassembler());
			drm.done();
			return;
		}

		final DsfServicesTracker services = getServicesTracker();
		if (services == null) // could be null if async invoked as or after debug session ends
			return;

		IMemory memoryService = services.getService(IMemory.class);
		if (memoryService == null) // could be null if async invoked as or after debug session ends
			return;

		final IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);

		final List<RangeAndMode> fullRange;
		if (preCalculatedRangeAndMode != null) {
			// the caller (likely getMixedInstructions() already determined the range
			(fullRange = new ArrayList<RangeAndMode>(1)).add(preCalculatedRangeAndMode);
		} else {
			// figure out the range and mode for the whole range first
			fullRange = getModeAndRange(context, start, end);
		}

		// use the adjusted start in case getModeAndRange() backed up for alignment
		final IAddress actualStart;
		if (fullRange.size() > 0) {
			actualStart = fullRange.get(0).getStartAddress();			
		} else {
			actualStart = start;
		}

		final int size = actualStart.distanceTo(end).intValue() + 16;
		
		memoryService.getMemory(mem_dmc, actualStart, 0, 1, size,
								new DataRequestMonitor<MemoryByte[]>(getExecutor(),	drm) {
			/**
			 * overridden to create a non-error status plus pseudoInstruction data-set
			 * in the DRM requested by DisassemblyBackendDsf, where a DRM.status of
			 * ERROR is turned into an "invalid" block in its document map.  Such
			 * blocks are repeatedly re-requested ... causing scrolling oddities & performance issues.
			 * @see Disassembly#failedMemoryDsfPseudoInstructions
			 */
			@Override
		    protected void handleError() {
				IStatus s = getStatus();
		    	Throwable e = s.getException();
		    	if (e instanceof MemoryError && s.getMessage().contains("Fail to read memory.")) {
					drm.setData(failedMemoryDsfPseudoInstructions(actualStart, size, s.getMessage()));
					drm.done();
				} else {
					super.handleError();
		        }
		    }

			@Override
			protected void handleSuccess() {
				List<MemoryByte> memBytes = Arrays.asList(getData());

				Map<String, Object> options = new HashMap<String, Object>();

				final List<IInstruction> result = new ArrayList<IInstruction>();
				final CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), drm) {

					@Override
					protected void handleSuccess() {
						drm.setData(result.toArray(new IInstruction[result.size()]));
						drm.done();
					}
				};

				crm.setDoneCount(fullRange.size());

// always true, and compiler is generating false-positive null-check warning for envARM
//				if (envARM.isLittleEndian(null))
					options.put(IDisassemblerOptionsARM.ENDIAN_MODE, 2);

				// for each range disassemble it
				for (RangeAndMode rangeAndMode : fullRange) {
					IAddress rStart = rangeAndMode.getStartAddress();
					int rOff = actualStart.distanceTo(rStart).intValue();
					int rLen = actualStart.distanceTo(rangeAndMode.getEndAddress()).intValue();
					try {
						result.addAll(
						  fillDisassemblyViewInstructions(rangeAndMode.isThumbMode(),
														  memBytes.subList(rOff, rLen),
														  rStart, context,
														  disassembler, options));
					} catch (CoreException e) {
						crm.setStatus(e.getStatus());
					}
					crm.done();
				}
			}
		});
	}

	/**
	 * overrides the default implementation in order to first break the
	 * whole range into a collection of ranges with the primary
	 * characteristic being whether the range is ARM or Thumb mode;
	 * also takes advantage of the RangeAndMode list to further break
	 * the whole range into chunks based upon whether they have symbols
	 * and then also into ranges with function boundaries.
	 */
	@Override
	public void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress,
			BigInteger endAddress, final DataRequestMonitor<IMixedInstruction[]> drm) {

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

		// These are absolute runtime addresses.
		ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);
		IAddress end = new Addr64(endAddress);
		IAddress start
		  = getStartAddressForLineEntryContainingAddress(symbolsService, modulesService, sym_dmc,
			  											 new Addr64(startAddress), end);

		// figure out the range and mode for the whole range first
		List<RangeAndMode> fullRange = getModeAndRange(context, start, end);
		final List<IMixedInstruction> result = new ArrayList<IMixedInstruction>();

		final CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), drm) {

			@Override
			protected void handleSuccess() {
				drm.setData(result.toArray(new IMixedInstruction[result.size()]));
				drm.done();
			}
		};

		crm.setDoneCount(fullRange.size());
		// for each range disassemble it
		for (final RangeAndMode rangeAndMode : fullRange) {
			IAddress rangeStart = rangeAndMode.getStartAddress();
			IAddress rangeEnd = rangeAndMode.getEndAddress();

			if (rangeAndMode.hasSymbols()) {
				// there is source for this range
				final List<ILineEntry> codeLines
				  = symbolsService.getLineEntriesForAddressRange(sym_dmc, rangeStart, rangeEnd);
				if (codeLines != null && !codeLines.isEmpty()) {
					final IEDCModuleDMContext module
					  = modulesService.getModuleByAddress(sym_dmc, rangeStart);

					// Get absolute runtime address of the line
					start = module.toRuntimeAddress(codeLines.get(0).getLowAddress());
					getInstructions(context, start, rangeEnd, rangeAndMode,
									new DataRequestMonitor<IInstruction[]>(getExecutor(), crm) {
						@Override
						protected void handleSuccess() {
							mixSource(result, rangeAndMode.getFunctionInfo(), module,
									  codeLines, getData());
							crm.done();
						}
					});

					continue;
				}
			}

			// no source for this range, just get the disassembly for it
			getInstructions(context, rangeStart, rangeEnd, rangeAndMode,
							new DataRequestMonitor<IInstruction[]>(getExecutor(), crm) {
				@Override
				protected void handleSuccess() {
					result.add(new EDCMixedInstruction("unknown", 0, getData()));//$NON-NLS-1$
					crm.done();
				}
			});
		}
	}

	/**
	 * pseudo-override of {@link Disassembly#fillDisassemblyViewInstructions},
	 * with the additional up-front boolean isThumbMode
	 */
	private ArrayList<IInstruction> fillDisassemblyViewInstructions(boolean isThumbMode,
			final List<MemoryByte> memBytes,
			final IAddress start, final IDisassemblyDMContext context,
			final IDisassembler disassembler, Map<String, Object> options)
			throws CoreException {
		options.put(IDisassemblerOptionsARM.DISASSEMBLER_MODE, isThumbMode ? 2 : 1);

		ArrayList<IInstruction> result
		  = super.fillDisassemblyViewInstructions(memBytes, start, context, disassembler, options);

		/*
		 * nok.bz.12231: DSF-disassembly will occasionally request a block at a
		 * location that has no symbols and happens to start at the 2nd byte of
		 * the 2-byte BL/BLX ; this has only been observed when scrolling up or
		 * jumping to a location, and in such a case, if we just throw the 1st
		 * instruction away, the next scroll/jump will normally acquire a block
		 * starting further away and properly disassemble the BL/BLX.
		 * 
		 * safety checks before throwing away an invalid opcode:
		 * - only check if there are at least 2 instructions (thus preventing a
		 *   loop when DSF-disassembly is forced to collect only 1 instruction)
		 * - if 2nd instruction is not an invalid opcode (thus preventing a loop 
		 *   of empty retrievals when a block contains only invalid instructions)
		 */

		if (isThumbMode && result.size() >= 2
			&& result.get(0).getInstruction().contains(IDisassembler.INVALID_OPCODE)
			&& !result.get(1).getInstruction().contains(IDisassembler.INVALID_OPCODE)) {
			result.remove(0);
		}
		return result;
	}

	private static IAddress alignBack(IAddress addr, boolean thumbMode) {
		if (addr == null)
			throw new IllegalArgumentException();
		int rem = addr.getValue().intValue() % (thumbMode ? 2 : 4);
		return (rem != 0) ? addr.add(-rem) : addr;
	}

	private static IAddress alignForward(IAddress addr, boolean thumbMode) {
		if (addr == null)
			throw new IllegalArgumentException();
		int mod = (thumbMode ? 2 : 4);
		int rem = addr.getValue().intValue() % mod;
		return (rem != 0) ? addr.add(mod-rem) : addr;
	}

	/**
	 * take a range of addresses and break it into a list of RangeAndMode entries,
	 * with the attributes {startAddr, endAddr, [boolean ThumbMode], [boolean hasSymbols] [functionInfo]}
	 * <br><b>NOTE:</b> first address of first item in value returned
	 * may be before start address passed in
	 * @param context execution context for determining ThumbMode and symbols content
	 * @param start address to start checking, inclusive
	 * @param end address at end of range to check, exclusive
	 * @return List of RangeAndMode entries with ThumbMode and hasSymbols attributes
	 */
	private List<RangeAndMode> getModeAndRange(IDisassemblyDMContext context, IAddress start, IAddress end) {
		IExecutionDMContext exeDMC = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
		IEDCModules modules = getServicesTracker().getService(IEDCModules.class);
		TargetEnvironmentARM env = (TargetEnvironmentARM)getTargetEnvironmentService();

		final List<RangeAndMode> result = new ArrayList<RangeAndMode>();

		IEDCSymbols symbolsService = getServicesTracker().getService(IEDCSymbols.class);
		final ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		boolean thumbMode = (env != null) ? env.isThumbMode(exeDMC, start, false) : false;
		start = alignBack(start, thumbMode);	// align back so the requested address is included

		int counter = 0;
		while (start.add(counter).compareTo(end) < 0) {
			IAddress preAlignedRangeStart = start.add(counter);
			thumbMode = (env != null) ? env.isThumbMode(exeDMC, preAlignedRangeStart, false) : false;
			IAddress rangeStart = alignForward(preAlignedRangeStart, thumbMode);
			counter += preAlignedRangeStart.distanceTo(rangeStart).intValue();

			ILineEntry startEntry = symbolsService.getLineEntryForAddress(sym_dmc, rangeStart);

			RangeAndMode range = new RangeAndMode(rangeStart, null, thumbMode, (startEntry != null));

			if (range.hasSymbols()) {
				// figure out mode and end address
				range.setEndAddress(end);

				IEDCModuleDMContext module = modules.getModuleByAddress(sym_dmc, rangeStart);
				if (module != null) {
					IEDCSymbolReader reader = module.getSymbolReader();
					if (reader != null) {
						IScope scope = reader.getModuleScope().getScopeAtAddress(module.toLinkAddress(rangeStart));
						while (scope != null && !(scope instanceof IFunctionScope)) {
							scope = scope.getParent();
						}

						if (scope != null) {
							IFunctionScope function = (IFunctionScope)scope;
							IAddress functionEndAddress
							  = module.toRuntimeAddress(function.getHighAddress());
							EDCInstructionFunctionInfo info
							  = new EDCInstructionFunctionInfo(scope.getName(),
									  						   function.getLowAddress());
							range.setFunctionInfo(info);
							if (functionEndAddress.compareTo(end) < 0) {
								range.setEndAddress(functionEndAddress);
							}
						}
					}
				}

				counter += rangeStart.distanceTo(range.getEndAddress()).intValue();
			} else {
				// got source file but no symbols;
				// search for the next line with symbols
				while (start.add(counter).compareTo(end) < 0) {
					startEntry = symbolsService.getLineEntryForAddress(sym_dmc, start.add(counter));
					if (startEntry != null) {
						range.setEndAddress(start.add(counter));
						break;
					}
					// even in ARM mode, always just count forward 2, in case next thumbMode section
					// somehow strangely but legally starts on a non-4byte boundary.
					// will re-align next ARM section above as necessary.
					counter = counter + 2;
				}
				if (range.getEndAddress() == null)
					range.setEndAddress(end);
			}

			result.add(range);
		}
		return result;
	}
}

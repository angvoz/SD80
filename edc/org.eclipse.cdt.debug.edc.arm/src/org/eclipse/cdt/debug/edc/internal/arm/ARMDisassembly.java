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
import org.eclipse.debug.core.model.MemoryByte;

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

		int size = start.distanceTo(end).intValue() + 16;

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

		memoryService.getMemory(mem_dmc, actualStart, 0, 1, size,
								new DataRequestMonitor<MemoryByte[]>(getExecutor(),	drm) {
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
					options.put(IDisassemblerOptionsARM.DISASSEMBLER_MODE,
								rangeAndMode.isThumbMode() ? 2 : 1);

					IAddress rStart = rangeAndMode.getStartAddress();
					int rOff = actualStart.distanceTo(rStart).intValue();
					int rLen = actualStart.distanceTo(rangeAndMode.getEndAddress()).intValue();
					try {
						result.addAll(
						  fillDisassemblyViewInstructions(memBytes.subList(rOff, rLen),
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

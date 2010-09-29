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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.EDCInstruction;
import org.eclipse.cdt.debug.edc.disassembler.EDCMixedInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembledInstruction;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler;
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

	boolean currentlyProcessingThumbMode = false;

	public ARMDisassembly(DsfSession session) {
		super(session, new String[] {ARMDisassembly.class.getName()});
	}

	/**
	 * overrides the default implmentation in order to first break the
	 * whole range into a collection of ranges with the defining
	 * characteristic being whether the range is ARM or Thumb mode
	 */
	@Override
	public void getInstructions(final IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
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
								new DataRequestMonitor<MemoryByte[]>(getExecutor(),	drm) {
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

				// figure out the range and mode for the whole range first
				List<RangeAndMode> fullRange = getModeAndRange(context, start, end);

				final List<IInstruction> result = new ArrayList<IInstruction>();
				final CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), drm) {

					@Override
					protected void handleSuccess() {
						drm.setData(result.toArray(new IInstruction[result.size()]));
						drm.done();
					}
				};

				crm.setDoneCount(fullRange.size());
				
				// for each range disassemble it
				for (RangeAndMode rangeAndMode : fullRange) {
					currentlyProcessingThumbMode = rangeAndMode.isThumbMode();

					if (currentlyProcessingThumbMode)
						options.put("DisassemblerMode", 2);//$NON-NLS-1$
					else
						options.put("DisassemblerMode", 1);//$NON-NLS-1$
	
					if (getTargetEnvironmentService().isLittleEndian(null))
						options.put("EndianMode", 2);//$NON-NLS-1$
	
					try {
						int rangeOffs = rangeAndMode.getStartAddress().getValue().subtract(start.getValue()).intValue();
						ByteBuffer rangeBytes
						  = ByteBuffer.wrap(codeBuf.array(), rangeOffs, codeBuf.capacity() - rangeOffs);
						List<IDisassembledInstruction> insts
						  = disassembler.disassembleInstructions(rangeAndMode.getStartAddress(), rangeAndMode.getEndAddress(), 
								  								 rangeBytes, options);
	
						for (int i = 0; i < insts.size(); i++) {
							result.add(new EDCInstruction(insts.get(i)));
						}
	
						crm.done();
					} catch (CoreException e) {
						crm.setStatus(e.getStatus());
						crm.done();
						break;
					}
				}
			}
		});

	}

	/**
	 * overrides the default implmentation in order to first break the
	 * whole range into a collection of ranges with the primary
	 * characteristic being whether the range is ARM or Thumb mode;
	 * also takes advantage of the RangeAndMode list to further break
	 * the whole range into chunks based upon whether they have symbols
	 * and then also into ranges with function boundaries.
	 */
	@Override
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

		// These are absolute runtime addresses.
		final ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);
		final IAddress end = new Addr64(endAddress);
		final IAddress start
		  = this.getStartAddressForLineEntryContainingAddress(symbolsService, modulesService, sym_dmc,
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
		for (RangeAndMode range : fullRange) {
			currentlyProcessingThumbMode = range.isThumbMode();
			IAddress rangeStart = range.getStartAddress(), rangeEnd = range.getEndAddress();

			if (range.hasSymbols()) {
				// there is source for this range
				final List<ILineEntry> codeLines
				  = symbolsService.getLineEntriesForAddressRange(sym_dmc, rangeStart, rangeEnd);
				if (codeLines != null && !codeLines.isEmpty()) {
					final IEDCModuleDMContext module = modulesService.getModuleByAddress(sym_dmc, rangeStart);

					// Get absolute runtime address of the line
					startAddress = module.toRuntimeAddress(codeLines.get(0).getLowAddress()).getValue();
					getInstructions(context, startAddress, rangeEnd.getValue(),
									new DataRequestMonitor<IInstruction[]>(getExecutor(), crm) {
						@Override
						protected void handleSuccess() {
							mixSource(result, module, codeLines, getData());
							crm.done();
						}
					});

					continue;
				}
			}

			// no source for this range, just get the disassembly for it
			getInstructions(context, rangeStart.getValue(), rangeEnd.getValue(),
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
	 * take a range of addresses and break it into a list of RangeAndMode entries,
	 * with the attributes {[boolean ThumbMode] [boolean hasSymbols]}
	 * @param context execution context for determining ThumbMode and symbols content
	 * @param start
	 * @param end
	 * @return List of RangeAndMode entries with ThumbMode and hasSymbols attributes
	 */
	private List<RangeAndMode> getModeAndRange(IDisassemblyDMContext context, IAddress start, IAddress end) {
		IExecutionDMContext exeDMC = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
		IEDCModules modules = getServicesTracker().getService(IEDCModules.class);

		final List<RangeAndMode> result = new ArrayList<RangeAndMode>();

		int counter = 0;
		IEDCSymbols symbolsService = getServicesTracker().getService(IEDCSymbols.class);
		final ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		while (start.add(counter).compareTo(end) < 0) {
			RangeAndMode range = new RangeAndMode(null, null, false, false);

			boolean thumbMode = ((TargetEnvironmentARM)getTargetEnvironmentService())
								.isThumbMode(exeDMC, start.add(counter), false);
			range.setThumbMode(thumbMode);
			range.setStartAddress(start.add(counter));

			ILineEntry startEntry = symbolsService.getLineEntryForAddress(sym_dmc, start.add(counter));
			if (startEntry != null) {
				// figure out mode and end address
				range.setHasSymbols(true);
				range.setEndAddress(end);

				IEDCModuleDMContext module = modules.getModuleByAddress(sym_dmc, start.add(counter));
				if (module != null) {
					IEDCSymbolReader reader = module.getSymbolReader();
					if (reader != null) {
						IScope scope = reader.getModuleScope().getScopeAtAddress(module.toLinkAddress(start.add(counter)));
						while (scope != null && !(scope instanceof IFunctionScope)) {
							scope = scope.getParent();
						}

						if (scope != null) {
							IAddress functionEndAddress = module.toRuntimeAddress(((IFunctionScope) scope)
									.getHighAddress());
							if (functionEndAddress.compareTo(end) < 0) {
								range.setEndAddress(functionEndAddress);
							}
						}
					}
				}

				counter += (range.getEndAddress().getValue().intValue() - range.getStartAddress().getValue().intValue());

				result.add(range);

			} else {
				// got source file but no symbols;
				// search for the next line with symbols
				while (start.add(counter).compareTo(end) < 0) {
					startEntry = symbolsService.getLineEntryForAddress(sym_dmc, start.add(counter));
					if (startEntry != null) {
						range.setEndAddress(start.add(counter));
						break;
					}
					counter = counter + 2;
				}
				if (range.getEndAddress() == null)
					range.setEndAddress(end);

				result.add(range);
			}

		}
		return result;
	}
}

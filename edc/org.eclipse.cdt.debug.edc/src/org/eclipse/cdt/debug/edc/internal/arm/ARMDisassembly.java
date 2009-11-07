/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
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
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.disassembler.DisassembledInstruction;
import org.eclipse.cdt.debug.edc.internal.disassembler.EDCInstruction;
import org.eclipse.cdt.debug.edc.internal.disassembler.EDCMixedInstruction;
import org.eclipse.cdt.debug.edc.internal.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Disassembly;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RangeAndMode;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.internal.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILineEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IInstruction;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IMixedInstruction;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.AddressRange;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;

public class ARMDisassembly extends Disassembly {

	boolean thumbMode = false;

	public ARMDisassembly(DsfSession session) {
		super(session);
	}

	@Override
	public void getInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			final DataRequestMonitor<IInstruction[]> drm) {
		final IDisassembler disassembler = getTargetEnvironmentService().getDisassembler();
		if (disassembler == null) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
					"No disassembler is available yet.", null));
			drm.done();
			return;
		}

		long size = endAddress.longValue() - startAddress.longValue() + 16;

		IMemory memoryService = getServicesTracker().getService(IMemory.class);

		final IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(context, IMemoryDMContext.class);
		final IAddress start = new Addr64(startAddress);
		final IAddress end = new Addr64(endAddress);

		memoryService.getMemory(mem_dmc, start, 0, 1, (int) size, new DataRequestMonitor<MemoryByte[]>(getExecutor(),
				drm) {

			@Override
			protected void handleSuccess() {
				MemoryByte[] memBytes = getData();
				final byte[] bytes = new byte[memBytes.length];
				for (int i = 0; i < memBytes.length; i++)
					bytes[i] = memBytes[i].getValue();

				ByteBuffer codeBuf = ByteBuffer.wrap(bytes);

				Map<String, Object> options = new HashMap<String, Object>();

				if (thumbMode)
					options.put("DisassemblerMode", 2);

				if (getTargetEnvironmentService().isLittleEndian(null))
					options.put("EndianMode", 2);

				try {
					List<DisassembledInstruction> insts = disassembler.disassembleInstructions(start, end, codeBuf,
							options);

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

	@Override
	public void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			final DataRequestMonitor<IMixedInstruction[]> drm) {

		// These are absolute runtime addresses.
		final IAddress start = new Addr64(startAddress);
		final IAddress end = new Addr64(endAddress);

		Symbols symbolsService = getServicesTracker().getService(Symbols.class);
		final ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

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
		for (int i = 0; i < fullRange.size(); i++) {
			if (fullRange.get(i).isThumbMode())
				thumbMode = true;
			else
				thumbMode = false;

			if (fullRange.get(i).hasSymbols()) {
				// there is source for this range
				final List<ILineEntry> codeLines = symbolsService.getLineEntriesForAddressRange(sym_dmc, start, end);
				if (codeLines == null || codeLines.isEmpty()) {

					getInstructions(context, fullRange.get(i).getStartAddress().getValue(), fullRange.get(i)
							.getEndAddress().getValue(), new DataRequestMonitor<IInstruction[]>(getExecutor(), crm) {

						@Override
						protected void handleSuccess() {
							result.add(new EDCMixedInstruction("unknown", 0, getData()));
							crm.done();
						}
					});

					return;

				}

				ILineEntry startEntry = symbolsService.getLineEntryForAddress(sym_dmc, start);
				ILineEntry endEntry = symbolsService.getLineEntryForAddress(sym_dmc, end);

				final String srcFile = startEntry != null ? startEntry.getFilePath().toOSString() : endEntry
						.getFilePath().toOSString();

				Modules modulesService = getServicesTracker().getService(Modules.class);
				final ModuleDMC module = modulesService.getModuleByAddress(sym_dmc, start);

				// Get absolute runtime address of the line
				startAddress = module.toRuntimeAddress(codeLines.get(0).getLowAddress()).getValue();
				getInstructions(context, startAddress, fullRange.get(i).getEndAddress().getValue(),
						new DataRequestMonitor<IInstruction[]>(getExecutor(), crm) {

							@Override
							protected void handleSuccess() {
								IInstruction[] instructions = getData();
								int instsCnt = instructions.length;

								int lineCnt = codeLines.size();
								List<IInstruction> instsForLine = new ArrayList<IInstruction>();

								int k = 0;
								for (int i = 0; i < lineCnt && k < instsCnt; i++) {
									// Now map the instructions to source lines
									// to generate
									// MixedInstructions.
									instsForLine.clear();
									ILineEntry line = codeLines.get(i);

									while (k < instsCnt
											&& module.toLinkAddress(new Addr64(instructions[k].getAdress())).compareTo(
													line.getLowAddress()) < 0) {
										instsForLine.add(instructions[k]);
										k++;
									}

									result.add(new EDCMixedInstruction(srcFile, line.getLineNumber(), instsForLine
											.toArray(new IInstruction[instsForLine.size()])));
								}
								crm.done();
							}
						});
			} else {
				// startAddress has no source
				getInstructions(context, startAddress, endAddress, new DataRequestMonitor<IInstruction[]>(
						getExecutor(), drm) {
					@Override
					protected void handleSuccess() {
						result.add(new EDCMixedInstruction("unknown", 0, getData()));
						crm.done();
					}
				});

			}

		}
	}

	@Override
	public void getMixedInstructions(final IDisassemblyDMContext context, final String filename, final int linenum,
			final int lines, final DataRequestMonitor<IMixedInstruction[]> drm) {

		final IDisassembler disassembler = getTargetEnvironmentService().getDisassembler();
		if (disassembler == null) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
					"No disassembler is available yet.", null));
			drm.done();
			return;
		}

		IModules modulesService = getServicesTracker().getService(IModules.class);

		ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		modulesService.calcAddressInfo(sym_dmc, filename, linenum, 0, new DataRequestMonitor<AddressRange[]>(
				getExecutor(), drm) {

			@Override
			protected void handleSuccess() {
				AddressRange[] addr_ranges = getData();

				IAddress start = addr_ranges[0].getStartAddress();
				IAddress end = start.add(lines * 4); // kind of arbitrary end
														// address hint.
				getMixedInstructions(context, start.getValue(), end.getValue(), drm);
			}
		});
	}

	private List<RangeAndMode> getModeAndRange(IDisassemblyDMContext context, IAddress start, IAddress end) {
		ExecutionDMC exeDMC = DMContexts.getAncestorOfType(context, ExecutionDMC.class);
		Modules modules = getServicesTracker().getService(Modules.class);

		final List<RangeAndMode> result = new ArrayList<RangeAndMode>();

		int counter = 0;
		Symbols symbolsService = getServicesTracker().getService(Symbols.class);
		final ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		while (start.add(counter).compareTo(end) < 0) {
			RangeAndMode range = new RangeAndMode(null, null, false, false);
			ILineEntry startEntry = symbolsService.getLineEntryForAddress(sym_dmc, start.add(counter));
			if (startEntry != null) {
				// figure out mode and end address
				range.setHasSymbols(true);

				boolean thumbMode = ((TargetEnvironmentARM) getTargetEnvironmentService()).isThumbMode(exeDMC, start
						.add(counter), false);
				range.setThumbMode(thumbMode);
				range.setStartAddress(start.add(counter));
				range.setEndAddress(end);

				ModuleDMC module = modules.getModuleByAddress(sym_dmc, start.add(counter));
				if (module != null) {
					IEDCSymbolReader reader = module.getSymbolReader();
					if (reader != null) {
						IScope scope = reader.getScopeAtAddress(module.toLinkAddress(start.add(counter)));
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

				counter = counter
						+ (range.getEndAddress().getValue().intValue() - range.getStartAddress().getValue().intValue());

				result.add(range);

			} else {
				// got source file but no symbols, search for the next line with
				// symbols
				range.setStartAddress(start.add(counter));
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

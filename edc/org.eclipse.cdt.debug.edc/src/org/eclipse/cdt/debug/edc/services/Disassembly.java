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
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
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

public class Disassembly extends AbstractEDCService implements IDisassembly {

	public Disassembly(DsfSession session) {
		super(session, new String[] { IDisassembly.class.getName(), Disassembly.class.getName() });
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.math.BigInteger, java.math.BigInteger, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
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
				try {
					List<IDisassembledInstruction> insts = disassembler.disassembleInstructions(start, end, codeBuf,
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.lang.String, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getInstructions(final IDisassemblyDMContext context, String filename, int linenum, final int lines,
			final DataRequestMonitor<IInstruction[]> drm) {

		// FIXME: ignoring "lines" semantics
		
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

				getInstructions(context, start.getValue(), end.getValue(), drm);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getMixedInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.math.BigInteger, java.math.BigInteger, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getMixedInstructions(IDisassemblyDMContext context, BigInteger startAddress, BigInteger endAddress,
			final DataRequestMonitor<IMixedInstruction[]> drm) {

		// These are absolute runtime addresses.
		final IAddress start = new Addr64(startAddress);
		final IAddress end = new Addr64(endAddress);

		IEDCSymbols symbolsService = getServicesTracker().getService(Symbols.class);
		final ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

		ILineEntry startEntry = symbolsService.getLineEntryForAddress(sym_dmc, start);

		if (startEntry == null) {
			// startAddress has no source
			getInstructions(context, startAddress, endAddress, new DataRequestMonitor<IInstruction[]>(getExecutor(),
					drm) {
				@Override
				protected void handleSuccess() {
					IMixedInstruction[] ret = new IMixedInstruction[1];
					ret[0] = new EDCMixedInstruction("unknown", 0, getData());
					drm.setData(ret);
					drm.done();
				}
			});
		} else { // there is source for start address.

			final List<ILineEntry> codeLines = symbolsService.getLineEntriesForAddressRange(sym_dmc, start, end);

			final String srcFile = startEntry.getFilePath().toOSString();

			IEDCModules modulesService = getServicesTracker().getService(Modules.class);
			final IEDCModuleDMContext module = modulesService.getModuleByAddress(sym_dmc, start);

			// Get absolute runtime address of the line
			// 
			startAddress = module.toRuntimeAddress(codeLines.get(0).getLowAddress()).getValue();

			getInstructions(context, startAddress, endAddress, new DataRequestMonitor<IInstruction[]>(getExecutor(),
					drm) {

				@Override
				protected void handleSuccess() {
					IInstruction[] instructions = getData();
					int instsCnt = instructions.length;

					int lineCnt = codeLines.size();
					List<IMixedInstruction> mixedInstructions = new ArrayList<IMixedInstruction>();

					List<IInstruction> instsForLine = new ArrayList<IInstruction>();

					int k = 0;
					for (int i = 0; i < lineCnt && k < instsCnt; i++) {
						// Now map the instructions to source lines to generate
						// MixedInstructions.
						//
						instsForLine.clear();
						ILineEntry line = codeLines.get(i);

						while (k < instsCnt
								&& module.toLinkAddress(new Addr64(instructions[k].getAdress())).compareTo(
										line.getHighAddress()) < 0) {
							instsForLine.add(instructions[k]);
							k++;
						}

						mixedInstructions.add(new EDCMixedInstruction(srcFile, line.getLineNumber(), instsForLine
								.toArray(new IInstruction[instsForLine.size()])));
					}

					drm.setData(mixedInstructions.toArray(new IMixedInstruction[mixedInstructions.size()]));
					drm.done();
				}
			});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.debug.service.IDisassembly#getMixedInstructions(org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext, java.lang.String, int, int, org.eclipse.cdt.dsf.concurrent.DataRequestMonitor)
	 */
	public void getMixedInstructions(final IDisassemblyDMContext context, final String filename, final int linenum,
			final int lines, final DataRequestMonitor<IMixedInstruction[]> drm) {

		// FIXME: ignoring "lines" semantics
		
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
}

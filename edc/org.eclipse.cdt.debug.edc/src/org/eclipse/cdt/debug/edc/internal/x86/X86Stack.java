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
package org.eclipse.cdt.debug.edc.internal.x86;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.MemoryUtils;
import org.eclipse.cdt.debug.edc.internal.services.dsf.DMContext;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Registers;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Stack;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

public class X86Stack extends Stack {

	static int nextStackFrameID = 100;

	public X86Stack(DsfSession session) {
		super(session, new String[] { IStack.class.getName(), Stack.class.getName(), X86Stack.class.getName() });
	}

	@Override
	protected List<Map<String, Object>> computeStackFrames(ExecutionDMC context) {

		ArrayList<Map<String, Object>> frames = new ArrayList<Map<String, Object>>();
		HashMap<String, Object> properties = new HashMap<String, Object>();

		Modules modules = getServicesTracker().getService(Modules.class);
		Memory memoryService = getServicesTracker().getService(Memory.class);
		Registers registersService = getServicesTracker().getService(Registers.class);

		try {
			long eipValue = Long.valueOf(registersService.getRegisterValue(context, "EIP"), 16);
			long espValue = Long.valueOf(registersService.getRegisterValue(context, "ESP"), 16);
			long ebpValue = Long.valueOf(registersService.getRegisterValue(context, "EBP"), 16);
			ArrayList<MemoryByte> memBuffer = new ArrayList<MemoryByte>();
			IStatus memGetStatus = memoryService.getMemory(context, new Addr64(Long.toString(eipValue)), memBuffer, 4,
					1);
			if (memGetStatus.isOK()) {

				long baseAddress = 0;
				long instructionAddress = 0;
				long prev_baseAddress = 0;

				if (memBuffer.get(0).getValue() == 0x55)
					baseAddress = espValue - 4;
				else if (memBuffer.get(0).getValue() == 0x89 && memBuffer.get(1).getValue() == 0xe5)
					baseAddress = espValue;
				else
					baseAddress = ebpValue;
				instructionAddress = eipValue;

				if (baseAddress == 0) {
					properties = new HashMap<String, Object>();
					properties.put(DMContext.PROP_ID, Integer.toString(nextStackFrameID++));
					properties.put(StackFrameDMC.PROP_LEVEL, 1);
					properties.put(StackFrameDMC.BASE_ADDR, Long.valueOf(instructionAddress));
					properties.put(StackFrameDMC.IP_ADDR, Long.valueOf(instructionAddress));
					frames.add(properties);
				} else {
					while (baseAddress != 0) {
						int level = frames.size() + 1;

						memBuffer = new ArrayList<MemoryByte>();
						memGetStatus = memoryService.getMemory(context, new Addr64(Long.toString(instructionAddress)),
								memBuffer, 2, 1);

						// Bail out if not in any executable
						ModuleDMC module = modules.getModuleByAddress(context.getSymbolDMContext(), new Addr64(Long
								.toString(instructionAddress)));

						if (!memGetStatus.isOK() || module == null) {
							if (frames.size() == 0) {
								properties = new HashMap<String, Object>();
								properties.put(DMContext.PROP_ID, Integer.toString(nextStackFrameID++));
								properties.put(StackFrameDMC.PROP_LEVEL, level);
								properties.put(StackFrameDMC.BASE_ADDR, Long.valueOf(instructionAddress));
								properties.put(StackFrameDMC.IP_ADDR, Long.valueOf(instructionAddress));
								frames.add(properties);
							}
							break;
						}

						if (prev_baseAddress == baseAddress)
							break;

						prev_baseAddress = baseAddress;

						properties = new HashMap<String, Object>();
						properties.put(DMContext.PROP_ID, Integer.toString(nextStackFrameID++));
						properties.put(StackFrameDMC.PROP_LEVEL, level);
						properties.put(StackFrameDMC.BASE_ADDR, Long.valueOf(baseAddress));
						properties.put(StackFrameDMC.IP_ADDR, Long.valueOf(instructionAddress));
						if (module == null)
							properties.put(StackFrameDMC.MODULE_NAME, "");
						else
							properties.put(StackFrameDMC.MODULE_NAME, module.getName());

						if (frames.size() == 0 && memBuffer.get(0).getValue() == 0x55) {
							memBuffer = new ArrayList<MemoryByte>();
							memoryService.getMemory(context, new Addr64(Long.toString(espValue)), memBuffer, 4, 1);
							instructionAddress = MemoryUtils.convertByteArrayToUnsignedLong(
									memBuffer.subList(0, 4).toArray(new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN)
									.longValue();
							baseAddress = ebpValue;
						} else if (frames.size() == 0
								&& (memBuffer.get(0).getValue() == 0x89 && memBuffer.get(1).getValue() == 0xE5)) {
							memBuffer = new ArrayList<MemoryByte>();
							memoryService.getMemory(context, new Addr64(Long.toString(espValue + 4)), memBuffer, 4, 1);
							instructionAddress = MemoryUtils.convertByteArrayToUnsignedLong(
									memBuffer.subList(0, 4).toArray(new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN)
									.longValue();
							baseAddress = ebpValue;
						} else {
							memBuffer = new ArrayList<MemoryByte>();
							memoryService.getMemory(context, new Addr64(Long.toString(baseAddress)), memBuffer, 8, 1);
							baseAddress = MemoryUtils.convertByteArrayToUnsignedLong(
									memBuffer.subList(0, 4).toArray(new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN)
									.longValue();
							instructionAddress = MemoryUtils.convertByteArrayToUnsignedLong(
									memBuffer.subList(4, 8).toArray(new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN)
									.longValue();
						}
						frames.add(properties);
					}
				}

			}
		} catch (Exception e) { // catch any exception
			EDCDebugger.getMessageLogger().logAndShowError("Exception happened in computing stack frames.", e);
		}

		return frames;
	}

}

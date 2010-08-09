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
package org.eclipse.cdt.debug.edc.x86;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.MemoryUtils;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.HostOS;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.IEDCMemory;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

public class X86Stack extends Stack {

	static class X86PreservedRegisters {

		// the values of registers stored in this frame
		// note: only integer variables tracked
		BigInteger registers[] = new BigInteger[8];
		
		public Map<Integer, BigInteger> getPreservedRegisters() {
			Map<Integer, BigInteger> map = new HashMap<Integer, BigInteger>();
			for (int i = 0; i < registers.length; i++) {
				if (registers[i] != null)
					map.put(i, registers[i]);
			}
			return map;
		}
	}
	static int nextStackFrameID = 100;

	public X86Stack(DsfSession session) {
		super(session, new String[] { IStack.class.getName(), Stack.class.getName(), X86Stack.class.getName() });
	}

	/**
	 * Analyze the stack frame and try to decide what the base address (EBP or ESP),
	 * return address, and preserved registers are.
	 * 
	 * If we are at the beginning of a function, the PUSH %EBP / MOV %ESP,%EBP
	 * instructions are a clue as to where the return address and previous base address have
	 * been placed.  
	 * 
	 * We may also have a jump into a DLL (a jmp *0xNNNN address) which is not yet a new
	 * stack frame.
	 * 
	 * Otherwise, we assume that the current value of EBP is the base
	 * address.  (TODO: optimized code might use EBP as a variable, so if it is far enough
	 * out of range of ESP, we must quit here.)  
	 */
	@Override
	protected List<Map<String, Object>> computeStackFrames(IEDCExecutionDMC context, int startIndex, int endIndex) {

		ArrayList<Map<String, Object>> frames = new ArrayList<Map<String, Object>>();

		IEDCModules modules = getServicesTracker().getService(IEDCModules.class);
		IEDCMemory memoryService = getServicesTracker().getService(IEDCMemory.class);
		Registers registersService = getServicesTracker().getService(Registers.class);

		try {
			long eipValue = Long.valueOf(registersService.getRegisterValue(context, "EIP"), 16);
			long espValue = Long.valueOf(registersService.getRegisterValue(context, "ESP"), 16);
			long ebpValue = Long.valueOf(registersService.getRegisterValue(context, "EBP"), 16);
			
			long baseAddress = ebpValue;
			long instructionAddress = eipValue;
			long returnAddress = 0;
			long previousBaseAddress = 0;
			long previousPtrToBaseAddress = 0;

			while (true) {
				int level = frames.size();
				
				HashMap<String, Object> properties = new HashMap<String, Object>();
				
				IEDCModuleDMContext module = modules.getModuleByAddress(context.getSymbolDMContext(), new Addr64(Long
						.toString(instructionAddress)));
				
				boolean isFramePushed = true;
				boolean detected = false;
				X86PreservedRegisters preserved = new X86PreservedRegisters();
				
				ArrayList<MemoryByte> memBuffer = new ArrayList<MemoryByte>();
				IStatus memGetStatus = memoryService.getMemory(context, new Addr64(Long.toString(eipValue)), memBuffer, 4,
						1);
				if (memGetStatus.isOK()) {

					byte op1 = memBuffer.get(0).getValue();
					byte op2 = memBuffer.size() > 1 ? memBuffer.get(1).getValue() : 0;
					
					if (level == 0) {
						// the current instruction is special because it may be
						// before the frame has been established
						if (op1 == 0x55) {
							// push %ebp -- only return address is on stack; base address is still %ebp
							detected = true;
							isFramePushed = false;
							
							previousBaseAddress = ebpValue;
							returnAddress = readAddress(context, memoryService, espValue);
						}
						else if (op1 == (byte) 0x89 && op2 == (byte) 0xe5) {
							// mov %esp, %ebp -- only return address and ebp are on stack; base address is still %ebp
							detected = true;
							isFramePushed = false;
							
							previousBaseAddress = ebpValue;
							returnAddress = readAddress(context, memoryService, espValue + 4);
						}
						else if (op1 == (byte) 0xff && op2 == (byte) 0x25) {
							// jmp *0xNNNNN -- likely we're in an __imp_xxx thunk, and the new frame is not pushed
							detected = true;
							isFramePushed = false;
							
							previousBaseAddress = ebpValue;
							returnAddress = readAddress(context, memoryService, espValue);
						}
					}
					if (!detected) {
						// assume we're inside the function already
						preserved.registers[X86Registers.EBP] = BigInteger.valueOf(level == 0 ? ebpValue : previousPtrToBaseAddress);
						
						previousPtrToBaseAddress = baseAddress;
						previousBaseAddress = readAddress(context, memoryService, baseAddress);
						returnAddress = readAddress(context, memoryService, baseAddress + 4);
					}
				}
			
				properties = new HashMap<String, Object>();
				properties.put(IEDCDMContext.PROP_ID, Integer.toString(nextStackFrameID++));
				properties.put(StackFrameDMC.LEVEL_INDEX, level);
				properties.put(StackFrameDMC.BASE_ADDR, Long.valueOf(baseAddress));
				properties.put(StackFrameDMC.INSTRUCTION_PTR_ADDR, Long.valueOf(instructionAddress));
				if (module != null) properties.put(StackFrameDMC.MODULE_NAME, module.getName());
				if (level == 0) properties.put(StackFrameDMC.IN_PROLOGUE, !isFramePushed);
				properties.put(StackFrameDMC.PRESERVED_REGISTERS, preserved.getPreservedRegisters());
				frames.add(properties);
				
				// avoid recursive loop
				if (level > 0 && baseAddress == previousBaseAddress) {
					properties.put(StackFrameDMC.ROOT_FRAME, true);
					break;
				}
				
				baseAddress = previousBaseAddress;
				instructionAddress = returnAddress;
				
				// Bail out when we hit the top of the stack frame 
				// (but not always if a module is unrecognized; this can happen in Linux/gdbserver sometimes -- TODO)
				if (baseAddress == 0 || (module == null && !HostOS.IS_UNIX)) {
					properties.put(StackFrameDMC.ROOT_FRAME, true);
					break;
				}
			}
		} catch (Exception e) { // catch any exception
			EDCDebugger.getMessageLogger().logError("Exception happened in computing stack frames.", e);
		}

		return frames;
	}

	private long readAddress(IEDCExecutionDMC context, IEDCMemory memoryService, long addr) {
		ArrayList<MemoryByte> memBuffer = new ArrayList<MemoryByte>();
		IStatus status = memoryService.getMemory(context, new Addr64(Long.toString(addr)), memBuffer, 4, 1);
		if (!status.isOK())
			return 0;
		return MemoryUtils.convertByteArrayToUnsignedLong(
				memBuffer.subList(0, 4).toArray(new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN)
				.longValue();
	}

}

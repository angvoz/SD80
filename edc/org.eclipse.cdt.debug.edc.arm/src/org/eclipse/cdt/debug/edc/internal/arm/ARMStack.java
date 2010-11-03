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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.MemoryUtils;
import org.eclipse.cdt.debug.edc.internal.PersistentCache;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.IEDCMemory;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.services.IEDCSymbols;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

public class ARMStack extends Stack {

	/** IModuleDMContext attribute: list of IAddress which are known or suspected to be thumb */
	public static final String THUMB_ADDRESSES = "thumbAddresses";
	
	private static final int MAX_FRAMES = 30;
	private static final String FUNCTION_START_ADDRESS_CACHE = "_function_start_address";
	private static final String FUNCTION_END_ADDRESS_CACHE = "_function_end_address";

	/**
	 * Container for spilled registers.
	 */
	private class ARMSpilledRegisters  {

		/**
		 * General purpose registers R0-R12. Note that the addresses are the
		 * memory location where the register values are stored in the stack.
		 */
		public IAddress registers[] = new IAddress[13];

		/**
		 * Stack pointer (R13)
		 */
		public IAddress SP;

		/**
		 * Link register (R14)
		 */
		public IAddress LR;

		/**
		 * Address of the link register on the stack
		 */
		public IAddress LRAddress;

		/**
		 * Address where prolog is finished
		 */
		public IAddress firstInstructionAfterProlog;

		public boolean isValid() {
			// as long as the LR and SP are set then we're OK
			return LR != null && SP != null;
		}
		
		public Map<Integer, BigInteger> getPreservedRegisters() {
			Map<Integer, BigInteger> map = new HashMap<Integer, BigInteger>();
			for (int i = 0; i < registers.length; i++) {
				if (registers[i] != null) {
					map.put(i, registers[i].getValue());
				}
			}
			return map;
		}

		public IAddress fillLRFromStack(IEDCExecutionDMC context) {
			// get the real value of the link register
			IEDCMemory memoryService = getServicesTracker().getService(IEDCMemory.class);
			ArrayList<MemoryByte> byteArray = new ArrayList<MemoryByte>(4);
			IStatus status = memoryService.getMemory(context, LRAddress, byteArray, 4, 1);
			if (!status.isOK()) {
				return null;
			}

			// check each byte
			for (int i = 0; i < byteArray.size(); i++) {
				if (!byteArray.get(i).isReadable())
					return null;
			}

			LR = new Addr64(MemoryUtils.convertByteArrayToUnsignedLong(byteArray.toArray(new MemoryByte[4]),
																	   MemoryUtils.LITTLE_ENDIAN));
			return LR;
		}
	}
	
	public ARMStack(DsfSession session) {
		super(session, new String[] { ARMStack.class.getName() });
	}

	@Override
	protected List<EdcStackFrame> computeStackFrames(IEDCExecutionDMC context, int startIndex, int endIndex) {

		ArrayList<EdcStackFrame> frames = new ArrayList<EdcStackFrame>();
		
		Registers registersService = getServicesTracker().getService(Registers.class);
		IAddress pcValue = new Addr64(registersService.getRegisterValue(context, ARMRegisters.PC), 16);

		// get the SP and LR values based on the processor mode
		String spName, lrName;
		BigInteger cpsrValue = new BigInteger(registersService.getRegisterValue(context, ARMRegisters.CPSR), 16);
		switch (cpsrValue.and(BigInteger.valueOf(0x0000001FL)).intValue()) {
		case 0x11:
			spName = ARMRegisters.SP_fiq;
			lrName = ARMRegisters.LR_fiq;
			break;
		case 0x12:
			spName = ARMRegisters.SP_irq;
			lrName = ARMRegisters.LR_irq;
			break;
		case 0x13:
			spName = ARMRegisters.SP_svc;
			lrName = ARMRegisters.LR_svc;
			break;
		case 0x17:
			spName = ARMRegisters.SP_abt;
			lrName = ARMRegisters.LR_abt;
			break;
		case 0x1B:
			spName = ARMRegisters.SP_und;
			lrName = ARMRegisters.LR_und;
			break;
		case 0x1F:
			spName = ARMRegisters.SP_sys;
			lrName = ARMRegisters.LR_sys;
			break;
		case 0x10:
		default:
			spName = ARMRegisters.SP;
			lrName = ARMRegisters.LR;
			break;
		}

		IAddress spValue = new Addr64(registersService.getRegisterValue(context, spName), 16);
		IAddress lrValue = new Addr64(registersService.getRegisterValue(context, lrName), 16);

		int frameCount = 0;
		HashMap<String, Object> properties = null;
		
		if (endIndex == ALL_FRAMES)
			endIndex = MAX_FRAMES;

		Set<IAddress> thumbAddresses = null;

		IEDCSymbols symbols = getServicesTracker().getService(IEDCSymbols.class);
		PersistentCache armPluginCache = ARMPlugin.getDefault().getCache();

		do {
			IAddress functionStartAddress = null;
			IAddress functionEndAddress = null;
			String moduleName = "Unknown";

			// see if the PC is in an executable that we know about
			IEDCModuleDMContext module = getModule(context, pcValue);
			
			if (module != null) {
				moduleName = module.getName();

				{ @SuppressWarnings("unchecked")
					Set<IAddress> s = (Set<IAddress>)module.getProperty(THUMB_ADDRESSES);
					thumbAddresses = s;
				}
				if (thumbAddresses == null) {
					thumbAddresses = new TreeSet<IAddress>();
					module.setProperty(THUMB_ADDRESSES, thumbAddresses);
				}

				IEDCSymbolReader reader = module.getSymbolReader();
				if (reader != null && reader.getSymbolFile() != null)
				{
					String symbolFileOSString = reader.getSymbolFile().toOSString();
					long modDate = reader.getModificationDate();

					functionStartAddress
					  = getFunctionStartAddress(pcValue, context, symbols, armPluginCache,
												module, symbolFileOSString, modDate);

					if (frameCount == 0) {
						functionEndAddress
						  = getFunctionEndAddress(pcValue, context, symbols, armPluginCache,
												  module, symbolFileOSString, modDate);
					}
				}
			} else {
				// null module; hang thumb addresses off process context

				IDMContext[] parents = context.getParents();
				if (parents != null) {
					IEDCDMContext rootProcessDMC = null;
					for (int i = parents.length-1 ; i >= 0; i--) {
						if (parents[i] instanceof IEDCDMContext && parents[i] instanceof IProcessDMContext) {
							rootProcessDMC = (IEDCDMContext) parents[i];
							break;
						}
					}
					if (rootProcessDMC != null) {
						{ @SuppressWarnings("unchecked")
							Set<IAddress> s = (Set<IAddress>) rootProcessDMC.getProperty(THUMB_ADDRESSES);
							thumbAddresses = s;
						}
						if (thumbAddresses == null) {
							thumbAddresses = new TreeSet<IAddress>();
							rootProcessDMC.setProperty(THUMB_ADDRESSES, thumbAddresses);
						}
					}
				}
			}

			boolean thumbMode
			  = ((TargetEnvironmentARM)getTargetEnvironmentService()).isThumbMode(context, pcValue,
																				  frameCount == 0);

			// mask off the thumb bit
			pcValue = new Addr64(pcValue.getValue().clearBit(0)); 
			
			if (thumbAddresses != null && thumbMode)
				thumbAddresses.add(pcValue);

			// add this frame
			long baseAddress = functionStartAddress == null
								? pcValue.getValue().longValue()
								: functionStartAddress.getValue().longValue();

			properties = new HashMap<String, Object>();
			properties.put(IEDCDMContext.PROP_ID, context.getID() + "." + Integer.toString(frameCount));
			properties.put(StackFrameDMC.LEVEL_INDEX, frameCount);
			properties.put(StackFrameDMC.BASE_ADDR, baseAddress);
			properties.put(StackFrameDMC.INSTRUCTION_PTR_ADDR, pcValue.getValue().longValue());
			properties.put(StackFrameDMC.MODULE_NAME, moduleName);
			frames.add(new EdcStackFrame(properties));

			if (functionStartAddress == null) {
				// either module at address not known or we don't have symbols
				// for it, so try to find the prolog by parsing the preceding
				// instructions
				functionStartAddress = findProlog(context, pcValue, thumbMode);
			}

			if (functionStartAddress != null) {
				ARMSpilledRegisters spilledRegs = null;
	
				/*
				 * This first test is for epilog; when instruction stepping
				 * (or hitting a breakpoint) in thumb epilog, the PC could
				 * be in the middle of a multi-instr mod to the SP.
				 *
				 * The test case looks like this:
				 *		add sp,#0xXXX
				 *		add sp,#0xXXX
				 *		pop (rX,RX,...,pc)
				 *
				 * so, instead of relying on parsed prolog, parse the epilog
				 * to find out where it's popping the PC from on the stack,
				 * and re-adjust the live SP and other preserved registers along the way.
				 */
				if (frameCount == 0 && thumbMode && functionEndAddress != null && module != null) {
					ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);
					final List<ILineEntry> code
					  = symbols.getLineEntriesForAddressRange(sym_dmc, functionStartAddress, functionEndAddress);
					IAddress lastAddrBeforeEpilog
					  = (code.size() == 0 ? null : module.toRuntimeAddress(code.get(code.size()-1).getLowAddress()));
					if (lastAddrBeforeEpilog != null && pcValue.compareTo(lastAddrBeforeEpilog) > 0) {
						spilledRegs = parseThumbEpilog(context, pcValue, spValue, functionEndAddress);
					}
				}
	
				// if spilledRegs is still null, thumb epilog parsing
				// didn't occur.  the prolog still needs to be parsed
				// to figure out where the LR was stored on the stack
				if (spilledRegs == null)
					spilledRegs = parseProlog(context, functionStartAddress, pcValue, spValue, lrValue, thumbMode);

				// parsing prolog also failed ... run away, run away!
				if (spilledRegs == null)
					break;
	
				// remember spilled registers in case debug info is missing
				properties.put(StackFrameDMC.PRESERVED_REGISTERS, spilledRegs.getPreservedRegisters());
				// identify whether we're not yet inside a new frame
				if (frameCount == 0 && (spilledRegs.firstInstructionAfterProlog == null 
						|| pcValue.compareTo(spilledRegs.firstInstructionAfterProlog) < 0)) {
					properties.put(StackFrameDMC.IN_PROLOGUE, true);
				}
				
				pcValue = spilledRegs.LR;
				spValue = spilledRegs.SP;

			} else if (frameCount == 0) {
				// still don't know where the prolog is for sure.  there may not be
				// one at all.  assume that the currnet SP and LR are correct since we're
				// at the first frame.
				pcValue = lrValue;

			} else if (spName.compareTo(ARMRegisters.SP) != 0) {
				// if we're not in user mode, we're probably handling an exception.
				// try crawling from user mode sp/lr since we've gone as far as we
				// can go on the kernel side
				
				spValue = new Addr64(registersService.getRegisterValue(context, ARMRegisters.SP), 16);
				pcValue = new Addr64(registersService.getRegisterValue(context, ARMRegisters.LR), 16);

			} else {
				// still don't know where the prolog is so no way to tell where
				// the LR is saved on the stack (if at all)
				break;
			}

			if (pcValue.isZero())
				break;

			frameCount++;

		// keep going until we reach the maximum number of frames
		} while (frameCount < endIndex);

		if (properties != null) {
			properties.put(StackFrameDMC.ROOT_FRAME, true);
		}
		
		return frames;
	}

	private class AddressMapping extends HashMap<IAddress, IAddress> {
		private static final long serialVersionUID = -4200139520138410612L;
	}

	private IAddress getFunctionEndAddress(IAddress pcValue,
				IEDCExecutionDMC context, IEDCSymbols symbols, PersistentCache pCache,
				IEDCModuleDMContext module, String keyPrefix, long modDate) {
		// Check the persistent cache
		String key = keyPrefix + FUNCTION_END_ADDRESS_CACHE;
		AddressMapping cachedMapping = pCache.getCachedData(key, AddressMapping.class, modDate);
		IAddress addr = getRuntimeAddressFromCache(pcValue, module, cachedMapping);

		if (addr == null) {
			IFunctionScope scope = getNonInlineFunctionAtAddress(context, pcValue, symbols);
			if (scope != null) {
				addr = getAndCacheRuntimeAddress(pcValue, scope.getHighAddress(), key,
												 cachedMapping, pCache, module, modDate);
			}
		}
		return addr;
	}

	private IAddress getFunctionStartAddress(IAddress pcValue,
				IEDCExecutionDMC context, IEDCSymbols symbols, PersistentCache pCache,
				IEDCModuleDMContext module, String keyPrefix, long modDate) {
		// Check the persistent cache
		String key = keyPrefix + FUNCTION_START_ADDRESS_CACHE;
		AddressMapping cachedMapping = pCache.getCachedData(key, AddressMapping.class, modDate);
		IAddress addr = getRuntimeAddressFromCache(pcValue, module, cachedMapping);

		if (addr == null) {
			IFunctionScope scope = getNonInlineFunctionAtAddress(context, pcValue, symbols);
			if (scope != null) {
				addr = getAndCacheRuntimeAddress(pcValue, scope.getLowAddress(), key,
												 cachedMapping, pCache, module, modDate);
			}
		}
		return addr;
	}

	private IAddress getAndCacheRuntimeAddress(IAddress pcValueRuntimeAddress,
			IAddress mapLinkAddress, String cacheKey, AddressMapping cachedMapping,
			PersistentCache armPluginCache, IEDCModuleDMContext module, long modDate) {
		// put it in the cache
		if (cachedMapping == null)
			cachedMapping = new AddressMapping();
		cachedMapping.put(module.toLinkAddress(pcValueRuntimeAddress), mapLinkAddress);
		armPluginCache.putCachedData(cacheKey, cachedMapping, modDate);
		return module.toRuntimeAddress(mapLinkAddress);
	}

	protected IEDCModuleDMContext getModule(IEDCExecutionDMC context, IAddress address) {
		IEDCModules modules = getServicesTracker().getService(IEDCModules.class);
		return modules.getModuleByAddress(context.getSymbolDMContext(), address);
	}

	private static IFunctionScope getNonInlineFunctionAtAddress(
			IEDCExecutionDMC context, IAddress pcValue, IEDCSymbols symbols) {
		IFunctionScope scope = symbols.getFunctionAtAddress(context.getSymbolDMContext(), pcValue);
		while (scope != null && scope.getParent() instanceof IFunctionScope)
			scope = (IFunctionScope) scope.getParent();
		return scope;
	}

	private static IAddress getRuntimeAddressFromCache(IAddress pcValueRuntimeAddress,
			IEDCModuleDMContext module, AddressMapping mapping) {
		if (mapping != null)
		{
			IAddress mapLinkAddress = mapping.get(module.toLinkAddress(pcValueRuntimeAddress));
			if (mapLinkAddress != null)
				return module.toRuntimeAddress(mapLinkAddress);
		}
		return null;
	}

	private IAddress findProlog(IEDCExecutionDMC context, IAddress pcValue, boolean thumbMode) {
		// read memory back from the PC so we can parse the instructions
		IEDCMemory memoryService = getServicesTracker().getService(IEDCMemory.class);

		int instructionSize = thumbMode ? 2 : 4;
		
		long bytesToRead = 128 * instructionSize; // max 128 instructions
		
		// for cases where the PC is small, only read back to 0x0
		if (bytesToRead > pcValue.getValue().longValue()) {
			bytesToRead = pcValue.getValue().longValue();
		}
		ArrayList<MemoryByte> byteArray = new ArrayList<MemoryByte>();
		IStatus status = memoryService.getMemory(context, pcValue.add(-bytesToRead).add(instructionSize), byteArray, (int)bytesToRead, 1);
		if (!status.isOK()) {
			return null;
		}

		// check each byte
		for (int i = 0; i < byteArray.size(); i++) {
			if (!byteArray.get(i).isReadable())
				return null;
		}

		List<BigInteger> instructions = new ArrayList<BigInteger>();
		int index = 0;
		while (index < bytesToRead) {
			instructions.add(MemoryUtils.convertByteArrayToUnsignedLong(byteArray.subList(index, index + instructionSize)
					.toArray(new MemoryByte[instructionSize]), MemoryUtils.LITTLE_ENDIAN));
			index += instructionSize;
		}

		return thumbMode ? findThumbProlog(pcValue, instructions) : findArmProlog(pcValue, instructions);
	}

	private IAddress findArmProlog(IAddress pcValue, List<BigInteger> instructions) {
		IAddress prologAddress = pcValue;

		// parse backwards
		for (int i = instructions.size() - 1; i >= 0; i--) {
			BigInteger instruction = instructions.get(i);

			// look for a stmfd instruction that saves the LR on the stack
			if (isStmfdInstruction(instruction) && instruction.testBit(14)) {
				return prologAddress;
			}
			
			// look for a str instruction that saves the LR on the stack
			if (isStrLrToStackInstruction(instruction)) {
				return prologAddress;
			}

			// special handling of SWI
			if (isSWIInstruction(instruction)) {
				return prologAddress;
			}

			// special handling of NOP
			if (instruction.longValue() == 0xE1A00000L) {
				return prologAddress;
			}

			// bail if we find an epilog (mov pc, lr)
			if (instruction.longValue() == 0x0E1A0F0EL) {
				break;
			}

			prologAddress = prologAddress.add(-4);
		}

		return null;
	}

	private IAddress findThumbProlog(IAddress pcValue, List<BigInteger> instructions) {
		IAddress prologAddress = pcValue;

		// parse backwards
		for (int i = instructions.size() - 1; i >= 0; i--) {
			BigInteger instruction = instructions.get(i);

			if (isPushInstruction(instruction)) {
				return prologAddress;
			}

			// bail if we find an epilog (bx lr)
			if ((instruction.intValue() & 0xFFF8L) == 0x4770L) {
				break;
			}

			prologAddress = prologAddress.add(-2);
		}

		return null;
	}

	private ARMSpilledRegisters parseProlog(IEDCExecutionDMC context, IAddress prologAddress, IAddress pcValue,
			IAddress spValue, IAddress lrValue, boolean thumbMode) {
		// read memory from the prolog address to the pc, or 20 bytes, whichever
		// is less
		IEDCMemory memoryService = getServicesTracker().getService(IEDCMemory.class);
		ArrayList<MemoryByte> byteArray = new ArrayList<MemoryByte>();
		int bytesToRead = prologAddress.distanceTo(pcValue).min(BigInteger.valueOf(20)).intValue();
		if (bytesToRead > 0) {
			// the PC is not at the start of the prolog, so parse from the start
			// of the prolog to the PC, or 20 bytes, whichever is less.
			IStatus status = memoryService.getMemory(context, prologAddress, byteArray, bytesToRead, 1);
			if (!status.isOK()) {
				return null;
			}

			// check each byte
			for (int i = 0; i < byteArray.size(); i++) {
				if (!byteArray.get(i).isReadable())
					return null;
			}

			List<BigInteger> instructions = new ArrayList<BigInteger>();
			int index = 0;
			while (index < bytesToRead) {
				if (thumbMode) {
					instructions.add(MemoryUtils.convertByteArrayToUnsignedLong(byteArray.subList(index, index + 2)
							.toArray(new MemoryByte[2]), MemoryUtils.LITTLE_ENDIAN));
					index += 2;
				} else {
					instructions.add(MemoryUtils.convertByteArrayToUnsignedLong(byteArray.subList(index, index + 4)
							.toArray(new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN));
					index += 4;
				}
			}

			// look for prolog instructions. if found, figure out the LR and SP
			// values and return
			ARMSpilledRegisters spilledRegs
			  = thumbMode ? parseThumbProlog(context, instructions, spValue, prologAddress)
						  : parseArmProlog(context, instructions, spValue, lrValue, prologAddress);

			if (spilledRegs != null) {
				return spilledRegs;
			}
		}

		// we're either at the start of the prolog, or there is no prolog for
		// this function (leaf function), so just use the real LR and SP
		ARMSpilledRegisters spilledRegs = new ARMSpilledRegisters();
		spilledRegs.SP = spValue;
		spilledRegs.LR = lrValue;

		if (pcValue.equals(spilledRegs.LR)) {
			// prevent recursive loop
			return null;
		}

		return spilledRegs;
	}

	private ARMSpilledRegisters parseArmProlog(IEDCExecutionDMC context, List<BigInteger> instructions, 
			IAddress spValue, IAddress lrValue, IAddress pcAddress) {
		ARMSpilledRegisters spilledRegs = new ARMSpilledRegisters();

		IAddress currentSP = spValue;

		for (BigInteger instruction : instructions) {
			// point to PC of next instruction
			pcAddress = pcAddress.add(4);
			
			if (isStmfdInstruction(instruction)) {
				// figure out how many registers are being stored
				BigInteger regBits = instruction.and(BigInteger.valueOf(0x0000FFFFL));
				int regCount = 0;
				for (int i = 0; i <= 15; i++) {
					if (regBits.testBit(i)) {
						regCount++;
					}
				}

				// save off R0-R12 if needed
				IAddress registerLocation = currentSP;
				for (int i = 0; i < spilledRegs.registers.length; i++) {
					if (regBits.testBit(i)) {
						spilledRegs.registers[i] = registerLocation;
						registerLocation = registerLocation.add(4);
					} else {
						// if it's already been pushed then update its location
						if (spilledRegs.registers[i] != null) {
							spilledRegs.registers[i] = spilledRegs.registers[i].add(4 * regCount);
						}
					}
				}

				// save off the stack pointer register if needed
				if (instruction.testBit(13)) {
					spilledRegs.SP = registerLocation;
				} else {
					// if it's already been pushed then update its location
					if (spilledRegs.SP != null) {
						spilledRegs.SP = spilledRegs.SP.add(4 * regCount);
					}
				}

				// save off the link register if needed
				if (instruction.testBit(14)) {
					spilledRegs.LRAddress = registerLocation;
				} else {
					// if it's already been pushed then update its location
					if (spilledRegs.LRAddress != null) {
						spilledRegs.LRAddress = spilledRegs.LRAddress.add(4 * regCount);
					}
				}

				if (instruction.testBit(21)) {
					// write back enabled (STM(1) only) - update the SP
					// if the stack pointer has already been updated, just
					// adjust it here
					if (spilledRegs.SP != null) {
						spilledRegs.SP = spilledRegs.SP.add(4 * regCount);
					} else {
						// the previous SP is 4 times the number of saved
						// registers away from the current SP
						spilledRegs.SP = currentSP.add(4 * regCount);
					}
				}

				spilledRegs.firstInstructionAfterProlog = pcAddress;
			} else if ((instruction.longValue() & 0x01E00000L) == 0x00400000L
					&& (instruction.longValue() & 0x0C000000L) == 0x00000000L) {
				// sub instruction - does it modify the SP?
				if ((instruction.longValue() & 0x0000F000L) == 0x0000D000L) {
					Integer shifter_operand = null;

					// bit 25 is the I bit for immediate shift
					if (instruction.testBit(25)) {
						int immed_8 = instruction.and(BigInteger.valueOf(0x000000FFL)).intValue();
						int rotate_imm = instruction.and(BigInteger.valueOf(0x00000F00L)).shiftRight(7).intValue(); // rotate_imm * 2

						// shifter_operand = immed_8 Rotate_Right (rotate_imm * 2)
						shifter_operand = immed_8 >> rotate_imm | immed_8 << (32 - rotate_imm);
					} else {
						// TODO register operand, but doesn't seem to be used by
						// any compilers for prologs
						// shifter_operand = ;
					}

					if (shifter_operand != null) {
						// update the spilled registers
						for (int i = 0; i < spilledRegs.registers.length; i++) {
							IAddress address = spilledRegs.registers[i];
							if (address != null) {
								// register was spilled so update its location
								spilledRegs.registers[i] = address.add(shifter_operand);
							}
						}

						// update the LR and SP as well
						if (spilledRegs.LRAddress != null) {
							spilledRegs.LRAddress = spilledRegs.LRAddress.add(shifter_operand);
						} else {
							// SP is modified, but LR is not spilled
							if (spilledRegs.LR == null) {
								// set it to the real LR so we can spill the SP
								spilledRegs.LR = lrValue;
							}
						}

						if (spilledRegs.SP != null) {
							spilledRegs.SP = spilledRegs.SP.add(shifter_operand);
						} else {
							spilledRegs.SP = currentSP.add(shifter_operand);
						}
					}
				}
				spilledRegs.firstInstructionAfterProlog = pcAddress;
				
			} else if (isSWIInstruction(instruction)) {
				// get the user mode LR and SP. note that the ones we've already
				// read are in supervisor mode since we're in an exception
				Registers registersService = getServicesTracker().getService(Registers.class);
				spilledRegs.SP = new Addr64(registersService.getRegisterValue(context, ARMRegisters.SP), 16);
				spilledRegs.LR = new Addr64(registersService.getRegisterValue(context, ARMRegisters.LR), 16);
				
				spilledRegs.firstInstructionAfterProlog = pcAddress;
				
			} else if (isStrLrToStackInstruction(instruction)) {
				// get the location where the LR is stored and set it
				getLRAddrOnStack(context, instruction.intValue(), spValue, spilledRegs);
			}
		}

		if (spilledRegs.LRAddress != null && spilledRegs.LR == null) {
			spilledRegs.fillLRFromStack(context);
		}

		if (spilledRegs.isValid()) {
			return spilledRegs;
		}

		return null;
	}

	/**
	 * @param context
	 * @param pcValue
	 * @param spValue
	 * @return
	 */
	private ARMSpilledRegisters parseThumbEpilog(final IEDCExecutionDMC context, IAddress pcValue,
			IAddress spValue, final IAddress functionEndAddress) {
		if (context == null || pcValue == null || spValue == null)
			throw new IllegalArgumentException("null argument passed to parseThumbProlog");

		IEDCMemory memoryService = getServicesTracker().getService(IEDCMemory.class);

		// get the instruction before the PC to see if SP is already changed
		pcValue = pcValue.add(-2);
		int bytesToRead = (functionEndAddress != null) ? pcValue.distanceTo(functionEndAddress).intValue() : 16;
		bytesToRead -= bytesToRead % 2;	// get only an even amount
		ArrayList<MemoryByte> byteArray = new ArrayList<MemoryByte>(bytesToRead);

		IStatus status = memoryService.getMemory(context, pcValue, byteArray, bytesToRead, 1);
		if (!status.isOK()) {
			return null;
		}

		// don't bother parsing rest of epilog if SP hasn't changed, yet.
		// just return null and rely on caller to parse current epilog
		BigInteger priorInstruction
		  = MemoryUtils.convertByteArrayToUnsignedLong(
					byteArray.subList(0, 2).toArray(new MemoryByte[2]),
					MemoryUtils.LITTLE_ENDIAN);
		if (!(isAdd7ChangesSP(priorInstruction) || isSub4ChangesSP(priorInstruction)
				   || isAdd4WithSPDestination(priorInstruction)))
			return null;

		List<BigInteger> instructions = new ArrayList<BigInteger>();

		// check each byte
		for (int i = 2; i < byteArray.size(); i++) {
			if (!byteArray.get(i).isReadable())
				break;
			if (i % 2 == 0)
				instructions.add(
						MemoryUtils.convertByteArrayToUnsignedLong(
								byteArray.subList(i, i + 2).toArray(new MemoryByte[2]),
								MemoryUtils.LITTLE_ENDIAN));
		}

		ARMSpilledRegisters spilledRegs = new ARMSpilledRegisters();

		for (BigInteger instruction : instructions) {
			if (isPopInstruction(instruction)) {
				// pop instruction. figure out how many registers were being stored
				BigInteger regBits = instruction.and(BigInteger.valueOf(0x01FFL));

				// save off R0-R7 if needed
				for (int i = 0; i <= 7; i++) {
					if (regBits.testBit(i)) {
						spilledRegs.registers[i] = spValue;
						spValue = spValue.add(4);
					}
				}

				// the location of the pushed LR, popped into PC
				if (instruction.testBit(8)) {
					spilledRegs.LRAddress = spValue;
					spValue = spValue.add(4);
				}

			} else if (isAdd7ChangesSP(instruction) || isSub4ChangesSP(instruction)) {
				BigInteger immed_7 = instruction.and(BigInteger.valueOf(0x007FL)).shiftLeft(2);
				if (isSub4ChangesSP(instruction))
					immed_7 = immed_7.negate();

				spValue = spValue.add(immed_7);

			} else {
				return null;
			}
		}

		spilledRegs.SP = spValue;
		
		if (spilledRegs.LRAddress != null) {
			spilledRegs.fillLRFromStack(context);
		}

		if (spilledRegs.isValid()) {
			return spilledRegs;
		}

		return null;
	}

	private ARMSpilledRegisters parseThumbProlog(IEDCExecutionDMC context, List<BigInteger> instructions,
			IAddress spValue, IAddress prologAddress) {

		ARMSpilledRegisters spilledRegs = new ARMSpilledRegisters();

		IAddress currentSP = spValue;

		IAddress pcAddress = prologAddress;
		
		for (BigInteger instruction : instructions) {
			// point to next instruction
			pcAddress = pcAddress.add(2);
			
			if (isPushInstruction(instruction)) {
				// push instruction. figure out how many registers are being
				// stored
				BigInteger regBits = instruction.and(BigInteger.valueOf(0x01FFL));
				int regCount = 0;
				for (int i = 0; i <= 8; i++) {
					if (regBits.testBit(i)) {
						regCount++;
					}
				}

				// save off R0-R7 if needed
				IAddress registerLocation = currentSP;
				for (int i = 0; i <= 7; i++) {
					if (regBits.testBit(i)) {
						spilledRegs.registers[i] = registerLocation;
						registerLocation = registerLocation.add(4);
					} else {
						// if it's already been pushed then update its location
						if (spilledRegs.registers[i] != null) {
							spilledRegs.registers[i] = spilledRegs.registers[i].add(4 * regCount);
						}
					}
				}

				// save off the link register if needed
				if (instruction.testBit(8)) {
					spilledRegs.LRAddress = registerLocation;
				} else {
					// if it's already been pushed then update its location
					if (spilledRegs.LRAddress != null) {
						spilledRegs.LRAddress = spilledRegs.LRAddress.add(4 * regCount);
					}
				}

				// if the stack pointer has already been updated, just adjust it
				// here
				if (spilledRegs.SP != null) {
					spilledRegs.SP = spilledRegs.SP.add(4 * regCount);
				} else {
					// the previous SP is 4 times the number of saved registers
					// away from the current SP
					spilledRegs.SP = currentSP.add(4 * regCount);
				}

				spilledRegs.firstInstructionAfterProlog = pcAddress;

			} else if (isSub4ChangesSP(instruction) || isAdd7ChangesSP(instruction)) {
				BigInteger immed_7 = instruction.and(BigInteger.valueOf(0x007FL)).shiftLeft(2);
				if (isAdd7ChangesSP(instruction))
					immed_7 = immed_7.negate();
				for (int i = 0; i < spilledRegs.registers.length; i++) {
					IAddress address = spilledRegs.registers[i];
					if (address != null) {
						// register was spilled so update its location
						spilledRegs.registers[i] = address.add(immed_7);
					}
				}

				// update the LR and SP as well
				if (spilledRegs.LRAddress != null) {
					spilledRegs.LRAddress = spilledRegs.LRAddress.add(immed_7);
				}

				if (spilledRegs.SP != null) {
					spilledRegs.SP = spilledRegs.SP.add(immed_7);
				}

				spilledRegs.firstInstructionAfterProlog = pcAddress;

			} else if (isAdd4WithSPDestination(instruction)) {
				// add (4) with the SP as the destination register
				// get the source register number
				int sourceReg = instruction.shiftRight(3).and(BigInteger.valueOf(0x000F)).intValue();

				/*
				 	Here's an example of a prolog that uses an add to the SP with another register
				 	
					push {r4,r7,lr}
					cpy  r7,sp
					ldr  r4,[pc,#320]
					add  sp,r4
					
					Note that is could technically use any instruction(s) to fill the value
					of r4 before doing the add, but then we'd have to support all Thumb instructions.
					This is the only known signature of this case, so we'll only look for an LDR (3)
					instruction with the source of the add as the destination.  Other instructions
					can be added on a case by case basis as needed.
				 */

				IAddress instAddr = prologAddress;
				
				for (BigInteger inst : instructions) {
					// look for an LDR (3)
					if ((inst.intValue() & 0xF800L) == 0x4800L) {
						// is the destination register the right one?
						if (inst.and(BigInteger.valueOf(0x0700L)).shiftRight(8).intValue() == sourceReg) {
							int immed_8 = inst.and(BigInteger.valueOf(0x00FF)).intValue();
							
							// calculate the address - (PC & 0xFFFFFFFC) + (immed_8 * 4)
							IAddress dataAddr = new Addr64(instAddr.getValue().and(BigInteger.valueOf(0xFFFFFFFCL)));
							dataAddr = dataAddr.add(immed_8 * 4);
							
							// not sure why as the docs don't say anything about it, but in practice the
							// data is really in (PC & 0xFFFFFFFC) + (immed_8 * 4) + 4.  I validated this
							// by stepping over the ldr instruction and checking the value of the destination
							// register.
							dataAddr = dataAddr.add(4);
							
							IEDCMemory memoryService = getServicesTracker().getService(IEDCMemory.class);
							ArrayList<MemoryByte> byteArray = new ArrayList<MemoryByte>(4);
							IStatus status = memoryService.getMemory(context, dataAddr, byteArray, 4, 1);
							
							boolean validRead = status.isOK();
							if (validRead) {
								// check each byte
								for (int i = 0; i < byteArray.size(); i++) {
									if (!byteArray.get(i).isReadable()) {
										validRead = false;
										break;
									}
								}
							}

							if (validRead) {
								// note that we must treat this as a signed int
								int sourceRegValue = MemoryUtils.convertByteArrayToInt(byteArray.toArray(new MemoryByte[4]), MemoryUtils.LITTLE_ENDIAN);

								// update the spilled registers
								for (int i = 0; i < spilledRegs.registers.length; i++) {
									IAddress address = spilledRegs.registers[i];
									if (address != null) {
										// register was spilled so update its location
										spilledRegs.registers[i] = address.add(-sourceRegValue);
									}
								}

								// update the LR and SP as well
								if (spilledRegs.LRAddress != null) {
									spilledRegs.LRAddress = spilledRegs.LRAddress.add(-sourceRegValue);
								}

								if (spilledRegs.SP != null) {
									spilledRegs.SP = spilledRegs.SP.add(-sourceRegValue);
								}
							}

							spilledRegs.firstInstructionAfterProlog = instAddr.add(2);

							break;
						}
					}
					
					instAddr = instAddr.add(2);
				}
			}
		}

		if (spilledRegs.LRAddress != null && spilledRegs.LR == null) {
			spilledRegs.fillLRFromStack(context);
		}

		if (spilledRegs.isValid()) {
			return spilledRegs;
		}

		return null;
	}

	private boolean isAdd4WithSPDestination(BigInteger instruction) {
		return (instruction.intValue() & 0xFF87L) == 0x4485L;
	}
	
	private boolean isAdd7ChangesSP(BigInteger instruction) {
		return (instruction.intValue() & 0xFF80L) == 0xB000L;
	}

	private boolean isPopInstruction(BigInteger instruction) {
		return (instruction.intValue() & 0xFE00L) == 0xBC00L;
	}

	private boolean isPushInstruction(BigInteger instruction) {
		return (instruction.intValue() & 0xFE00L) == 0xB400L;
	}

	private boolean isStmfdInstruction(BigInteger instruction) {
		if ((instruction.longValue() & 0x0F800000L) == 0x09000000L) {
			// stmfd instruction. is the SP the destination?
			if ((instruction.longValue() & 0x000F0000L) == 0x000D0000L) {
				return true;
			}
		}

		return false;
	}

	private boolean isSub4ChangesSP(BigInteger instruction) {
		return (instruction.intValue() & 0xFF80L) == 0xB080L;
	}
	
	private boolean isStrLrToStackInstruction(BigInteger instruction) {
		// is this a str instruction with LR the source and SP the destination?
		// TODO: handle case where SP is in the Rm field of the str instruction
		if (   ((instruction.longValue() & 0x04000000L) == 0x04000000L)
			&& ((instruction.longValue() & 0x08500000L) == 0x0L)
			&& ((instruction.longValue() & 0x0000F000L) == 0x0000E000L)
			&& ((instruction.longValue() & 0x000F0000L) == 0x000D0000L)) {
			return true;
		}
		return false;
	}

	private boolean isSWIInstruction(BigInteger instruction) {
		return (instruction.longValue() & 0xFF000000L) == 0xEF000000L;
	}
	
	// Get LR value from str instruction with an SP-relative destination
	private void getLRAddrOnStack(IEDCExecutionDMC context, int instruction, IAddress spValue, ARMSpilledRegisters spilledRegs) {
		int regOffset = (instruction >> 25) & 1;
		int addOffset = (instruction >> 23) & 1;
		int updateSP  = (instruction >> 21) & 1;

		// TODO: handle offset in register (non-immediate) cases
		IAddress lrAddress = new Addr64(spValue.getValue());
		if (regOffset == 0) {
			// offset is add/subtract 12-bit immediate, not in register
			// Note: the code assumes the str condition passes
			if (updateSP == 1) {
				// SP contains the address of LR +/- the offset, so adjust it
				if (addOffset == 1) {
					spValue = spValue.add(-(instruction & 0xfff));
				} else {
					spValue = spValue.add(instruction & 0xfff);
				}
			} else {
				if (addOffset == 1) {
					lrAddress = lrAddress.add(instruction & 0xfff);
				} else {
					lrAddress = lrAddress.add(-(instruction & 0xfff));
				}
			}
		}
		
		spilledRegs.LRAddress = lrAddress;
		spilledRegs.SP = new Addr64(spValue.getValue());
	}
}

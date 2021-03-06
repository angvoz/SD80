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

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IAddressExpressionEvaluator;
import org.eclipse.cdt.debug.edc.arm.IARMSymbol;
import org.eclipse.cdt.debug.edc.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.AddressExpressionEvaluatorARM;
import org.eclipse.cdt.debug.edc.internal.arm.disassembler.DisassemblerARM;
import org.eclipse.cdt.debug.edc.services.AbstractTargetEnvironment;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.IEDCModuleDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCModules;
import org.eclipse.cdt.debug.edc.services.IEDCSymbols;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.edc.symbols.ISymbol;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;

/**
 * ITargetEnvironment service for ARM.
 */
public class TargetEnvironmentARM extends AbstractTargetEnvironment implements ITargetEnvironment {

	/*
	 * Breakpoint SWI instruction: SWI &9F0001 #define BREAKINST_ARM 0xef9f0001
	 * #define BREAKINST_THUMB 0xdf00
	 * 
	 * TODO: Or use these ? New breakpoints - use an undefined instruction. The
	 * ARM architecture reference manual guarantees that the following
	 * instruction space will produce an undefined instruction exception on all
	 * CPUs:
	 * 
	 * ARM: xxxx 0111 1111 xxxx xxxx xxxx 1111 xxxx Thumb: 1101 1110 xxxx xxxx
	 * #define BREAKINST_ARM 0xe7f001f0 #define BREAKINST_THUMB 0xde01
	 */
	final static byte[] sARMBreakpointInstruction = new byte[] { 0x01, 0, (byte) 0x9f, (byte) 0xef };
	final static byte[] sThumbBreakpointInstruction = new byte[] { 0, (byte) 0xdf, (byte) 0xef };
	private static final String IS_THUMB_MODE = "_is_thumb_mode";

	private IDisassembler disassembler = null;

	private IAddressExpressionEvaluator aeEvaluator = null;

	private HashMap<Integer, Integer> basicTypeSizes;
	private HashMap<IPath, ARMElf> readerToArmElfMap;

	public TargetEnvironmentARM(DsfSession session, ILaunch launch) {
		super(session, new String[] { TargetEnvironmentARM.class.getName() },
				launch);
		readerToArmElfMap = new HashMap<IPath, ARMElf>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.debug.edc.internal.services.dsf.ITargetEnvironment#
	 * getArchitecture()
	 */
	public String getArchitecture() {
		return ARCH_ARM;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.debug.edc.internal.services.dsf.ITargetEnvironment#
	 * getBreakpointInstruction()
	 */
	public byte[] getBreakpointInstruction(IDMContext context, IAddress address) {
		if (isThumbMode(context, address, false)) {
			return sThumbBreakpointInstruction;
		}
		return sARMBreakpointInstruction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.debug.edc.internal.services.dsf.ITargetEnvironment#
	 * addAdditionalBreakpointProperties(java.util.Map)
	 */
	public void updateBreakpointProperties(IDMContext context, IAddress address, Map<String, Object> properties) {
		if (isThumbMode(context, address, false)) {
			properties.put("THUMB_BREAKPOINT", Boolean.TRUE); //$NON-NLS-1$
			// twiddle bit to ensure it's legal for TRK
			properties.put(org.eclipse.tm.tcf.services.IBreakpoints.PROP_LOCATION, address.getValue().clearBit(0).toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.cdt.debug.edc.internal.services.dsf.ITargetEnvironment#
	 * getDisassembler()
	 */
	public IDisassembler getDisassembler() {
		if (disassembler == null) {
			disassembler = new DisassemblerARM(this);
		}

		return disassembler;
	}

	public String getOS() {
		return OS_UNKNOWN;
	}

	public String getPCRegisterID() {
		return ARMRegisters.PC;
	}

	public String getProperty(String propertyKey) {
		return null;
	}

	public boolean isLittleEndian(IDMContext context) {
		return true;
	}

	public int getLongestInstructionLength() {
		return 4;
	}

	public IAddressExpressionEvaluator getAddressExpressionEvaluator() {
		if (aeEvaluator == null)
			aeEvaluator = new AddressExpressionEvaluatorARM();

		return aeEvaluator;
	}

	@SuppressWarnings("unchecked")
	public boolean isThumbMode(IDMContext context, IAddress address, boolean useCPSR) {

		IEDCExecutionDMC exeDMC = DMContexts.getAncestorOfType(context, IEDCExecutionDMC.class);

		if (useCPSR) {
			// bit 5 of the CPSR tells us the current processor mode. this is
			// the most reliable way of getting the processor mode, but only
			// works when we're getting the mode for the PC address (or very
			// close to it).  other addresses could be in either mode.
			Registers registersService = getService(Registers.class);
			try {
				return new BigInteger(registersService.getRegisterValue(exeDMC, ARMRegisters.CPSR), 16).testBit(5);
			} catch (CoreException e) {
				EDCDebugger.getMessageLogger().logException(e);
				return false;
			}
		}

		// if the address is question has bit 0 set then it is a thumb address
		if (address.getValue().testBit(0)) {
			return true;
		}
		
		// if the address is question is two-byte aligned then it must be thumb
		if (address.getValue().testBit(1)) {
			return true;
		}
		
		// see if the PC is in an executable that we know about
		IEDCModules modules = getService(IEDCModules.class);
		IEDCModuleDMContext module = modules.getModuleByAddress(exeDMC.getSymbolDMContext(), address);
		if (module != null) {
			
			// first, see if we have cached info from earlier stack crawls
			Set<IAddress> thumbAddresses = (Set<IAddress>) module.getProperty(ARMStack.THUMB_ADDRESSES);
			if (thumbAddresses != null) {
				if (thumbAddresses.contains(address))
					return true;
			}
			
			IEDCSymbolReader reader = module.getSymbolReader();
			IAddress linkAddress = module.toLinkAddress(address);
			
			Map<IAddress, Boolean> cachedValues = new HashMap<IAddress, Boolean>();
			String cacheKey = null;
			if (reader != null)
			{
				cacheKey = reader.getSymbolFile().toOSString() + IS_THUMB_MODE;
				cachedValues = ARMPlugin.getDefault().getCache().getCachedData(cacheKey, Collections.synchronizedMap(new HashMap<IAddress, Boolean>()), reader.getModificationDate());
				Boolean cachedValue = cachedValues.get(linkAddress);
				if (cachedValue != null)
					return cachedValue;
			}

			// see if we have symbolics for the module
			IEDCSymbols symbols = getService(IEDCSymbols.class);
			IFunctionScope scope = symbols.getFunctionAtAddress(exeDMC.getSymbolDMContext(), address);
			if (scope != null) {
				IAddress functionStartAddress = module.toRuntimeAddress(scope.getLowAddress());
				// if the address is question has bit 0 set then it is a
				// thumb address
				if (functionStartAddress.getValue().testBit(0)) {
					cachedValues.put(linkAddress, true);
					return true;
				}
			}

			if (reader != null) {
				// see if we can get the mode from the symbol tab.
				ISymbol symbol = reader.getSymbolAtAddress(module.toLinkAddress(address));
				if (symbol != null && symbol instanceof IARMSymbol) {
					if (((IARMSymbol)symbol).isThumbAddress()) {
						cachedValues.put(linkAddress, true);
						return true;
					}

					ARMElf armElf = findOrLoadARMElf(reader.getSymbolFile(), reader.getByteOrder());
					if (armElf != null) {
						String mappingSymbol = armElf.getMappingSymbolAtAddress(symbol.getAddress());
						if (mappingSymbol != null) {
							if (mappingSymbol.startsWith("$t")) { //$NON-NLS-1$
								cachedValues.put(linkAddress, true);
								return true;
							} else if (mappingSymbol.startsWith("$a")) { //$NON-NLS-1$
								cachedValues.put(linkAddress, false);
								return false;
							}
						}
						cachedValues.put(linkAddress, false);
					}
				}
			}
		} else {
			// no module?  try the rootmost execution DMC
	
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
					Set<IAddress> thumbAddresses
					  = (Set<IAddress>) rootProcessDMC.getProperty(ARMStack.THUMB_ADDRESSES);
					if (thumbAddresses != null && thumbAddresses.contains(address))
						return true;
				}
			}
		}
		
		// TODO we have no other way of finding the mode, so check the pref?  we'd need a pref for EDC ARM
		// debugger for default mode (arm/thumb) when the mode cannot be determined.

		return false;
	}

	/**
	 * Cache the ARM/Thumb symbol table information for a given sym file, instead of
	 * re-reading it on every step.
	 * @param symbolFile
	 * @param byteOrder 
	 * @return ARMElf instance or null
	 */
	private ARMElf findOrLoadARMElf(IPath symbolFile, ByteOrder byteOrder) {
		
		ARMElf armElf = readerToArmElfMap.get(symbolFile);
		if (armElf == null) {
			try {
				armElf = new ARMElf(symbolFile.toOSString(), byteOrder == ByteOrder.LITTLE_ENDIAN);
			} catch (IOException e) {
				ARMPlugin.getMessageLogger().logError("Failed to load ARM/Thumb symbol mapping", e);
			}
			readerToArmElfMap.put(symbolFile, armElf);
		}
		return armElf;
	}

	public Map<Integer, Integer> getBasicTypeSizes() {
		if (this.basicTypeSizes == null) {
			this.basicTypeSizes = new HashMap<Integer, Integer>();
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_CHAR, 1);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_CHAR_SIGNED, 1);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_CHAR_UNSIGNED, 1);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_SHORT, 2);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_SHORT_UNSIGNED, 2);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_INT, 4);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_INT_UNSIGNED, 4);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG, 4);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG_UNSIGNED, 4);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG_LONG, 8);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG_LONG_UNSIGNED, 8);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_FLOAT, 4);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_FLOAT_COMPLEX, 8);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_DOUBLE, 8);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_DOUBLE_COMPLEX, 16);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG_DOUBLE, 12);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG_DOUBLE_COMPLEX, 24);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_BOOL, 1);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_BOOL_C9X, 1);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_WCHAR_T, 2);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_POINTER, 4);
		}

		return this.basicTypeSizes;
	}

	public int getEnumSize() {
		return 4;
	}

	public int getPointerSize() {
		return 4;
	}

	public boolean isCharSigned() {
		return false;
	}

	public int getMemoryCacheMinimumBlockSize() {
		// this looks the optimal after some trials.
		return 64;
	}
}

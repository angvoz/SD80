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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IAddressExpressionEvaluator;
import org.eclipse.cdt.debug.edc.internal.disassembler.IDisassembler;
import org.eclipse.cdt.debug.edc.internal.disassembler.x86.AddressExpressionEvaluatorX86;
import org.eclipse.cdt.debug.edc.internal.disassembler.x86.DisassemblerX86;
import org.eclipse.cdt.debug.edc.internal.services.dsf.AbstractTargetEnvironment;
import org.eclipse.cdt.debug.edc.internal.services.dsf.ITargetEnvironment;
import org.eclipse.cdt.debug.edc.internal.symbols.TypeUtils;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;

/**
 * {@link ITargetEnvironment} service for x86.
 */
public class TargetEnvironmentX86 extends AbstractTargetEnvironment implements ITargetEnvironment {

	// breakpoint instruction for x86.
	final static byte[] sBreakpointInstruction = { (byte) 0xcc };

	private IDisassembler disassembler = null;

	private IAddressExpressionEvaluator aeEvaluator = null;

	private HashMap<Integer, Integer> basicTypeSizes = null;

	public TargetEnvironmentX86(DsfSession session, ILaunch launch) {
		super(session, new String[] { ITargetEnvironment.class.getName(), TargetEnvironmentX86.class.getName() },
				launch);
	}

	public String getArchitecture() {
		return ARCH_X86;
	}

	public byte[] getBreakpointInstruction(IDMContext context, IAddress address) {
		// arguments are ignored.
		return sBreakpointInstruction;
	}

	public void updateBreakpointProperties(IDMContext context, IAddress address, Map<String, Object> properties) {
	}

	public IDisassembler getDisassembler() {
		if (disassembler == null)
			disassembler = new DisassemblerX86();

		return disassembler;
	}

	public String getOS() {
		return OS_UNKNOWN;
	}

	public String getPCRegisterID() {
		return "EIP";
	}

	public String getProperty(String propertyKey) {
		return null;
	}

	public boolean isLittleEndian(IDMContext context) {
		return true;
	}

	public int getLongestInstructionLength() {
		// it's said to be 17 bytes...
		return 18;
	}

	public IAddressExpressionEvaluator getAddressExpressionEvaluator() {
		if (aeEvaluator == null)
			aeEvaluator = new AddressExpressionEvaluatorX86();

		return aeEvaluator;
	}

	public HashMap<Integer, Integer> getBasicTypeSizes() {
		if (this.basicTypeSizes == null) {
			this.basicTypeSizes = new HashMap<Integer, Integer>();
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_CHAR, 1);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_SHORT, 2);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_INT, 4);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG, 4);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG_LONG, 8);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_FLOAT, 4);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_FLOAT_COMPLEX, 8);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_DOUBLE, 8);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_DOUBLE_COMPLEX, 16);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG_DOUBLE, 8);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_LONG_DOUBLE_COMPLEX, 16);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_BOOL, 1);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_BOOL_C9X, 1);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_WCHAR_T, 2);
			this.basicTypeSizes.put(TypeUtils.BASIC_TYPE_POINTER, 4);
		}

		return this.basicTypeSizes;
	}

	public boolean isCharSigned() {
		return true;
	}

	public int getEnumSize() {
		return 4;
	}

	public int getPointerSize() {
		return 4;
	}
}

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

package org.eclipse.cdt.debug.edc.internal.arm.disassembler;

import java.nio.ByteBuffer;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.AbstractDisassembler;
import org.eclipse.cdt.debug.edc.disassembler.DisassembledInstruction;
import org.eclipse.cdt.debug.edc.internal.arm.TargetEnvironmentARM;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * ARM disassembler
 */
public class DisassemblerARM extends AbstractDisassembler {

	public static interface IDisassemblerOptionsARM extends IDisassemblerOptions {
		public static final String DISASSEMBLER_MODE = "DisassemblerMode"; // value:
		public static final String ENDIAN_MODE = "EndianMode"; // value:
	}

	public DisassemblerARM(ITargetEnvironment env) {
		super(env);
	}
	
	public DisassembledInstruction disassembleOneInstruction(IAddress address, ByteBuffer codeBytes,
				Map<String, Object> options)
			throws CoreException {
		return disassembleOneInstruction(address, codeBytes, options, null);
	}

	public DisassembledInstruction disassembleOneInstruction(IAddress address, ByteBuffer codeBytes,
				Map<String, Object> options, IDisassemblyDMContext dmc)
			throws CoreException {
		TargetEnvironmentARM envARM;
		if (!options.containsKey(IDisassemblerOptionsARM.DISASSEMBLER_MODE) && dmc != null
			&& null != (envARM = (TargetEnvironmentARM)getTargetEnvironment())) {
			boolean useCPSR = options.containsKey(IDisassemblerOptions.ADDRESS_IS_PC);
			boolean thumbMode = envARM.isThumbMode(dmc, address, useCPSR);
			options.put(IDisassemblerOptionsARM.DISASSEMBLER_MODE, thumbMode ? 2 : 1);
		}
		InstructionParserARM parser = new InstructionParserARM(address, codeBytes);
		return parser.disassemble(options);
	}

}

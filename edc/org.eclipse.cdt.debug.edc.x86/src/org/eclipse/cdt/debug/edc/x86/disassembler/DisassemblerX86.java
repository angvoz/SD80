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

package org.eclipse.cdt.debug.edc.x86.disassembler;

import java.nio.ByteBuffer;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.disassembler.AbstractDisassembler;
import org.eclipse.cdt.debug.edc.disassembler.DisassembledInstruction;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * x86 disassembler
 */
public class DisassemblerX86 extends AbstractDisassembler {

	public static interface IDisassemblerOptionsX86 extends IDisassemblerOptions {
		public static final String ADDRESS_MODE = "AddressMode"; // value:
		// Integer
	}

	public DisassemblerX86(ITargetEnvironment env) {
		super(env);
	}

	public DisassembledInstruction disassembleOneInstruction(IAddress address, ByteBuffer codeBytes,
			Map<String, Object> options) throws CoreException {
		InstructionParserX86 parser = new InstructionParserX86(address, codeBytes);
		return parser.disassemble(options);
	}

	public DisassembledInstruction disassembleOneInstruction(IAddress address, ByteBuffer codeBytes,
										Map<String, Object> options, IDisassemblyDMContext dmc)
	  throws CoreException {
		return disassembleOneInstruction(address, codeBytes, options);
	}

}

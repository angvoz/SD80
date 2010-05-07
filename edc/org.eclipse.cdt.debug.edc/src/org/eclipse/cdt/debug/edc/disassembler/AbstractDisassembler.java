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

package org.eclipse.cdt.debug.edc.disassembler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.core.runtime.CoreException;

/**
 * Abstract disassembler that implements the platform neutral methods.
 */
public abstract class AbstractDisassembler implements IDisassembler {

	public List<IDisassembledInstruction> disassembleInstructions(IAddress startAddress, IAddress endAddress,
			ByteBuffer codeBuffer, Map<String, Object> options) throws CoreException {
		IDisassembledInstruction inst = null;
		ArrayList<IDisassembledInstruction> result = new ArrayList<IDisassembledInstruction>();
		IAddress address = startAddress;

		while (codeBuffer.hasRemaining() && address.compareTo(endAddress) < 0) {
			try {
				inst = disassembleOneInstruction(address, codeBuffer, options);
				result.add(inst);

				// next instruction address
				// Note at this point the current position of code buffer should
				// point to the next instruction.
				address = address.add(inst.getSize());

			} catch (CoreException e) {
				throw EDCDebugger.newCoreException("Fail to disassemble instruction at " + address.toHexAddressString()
						+ "\nCause: " + e.getLocalizedMessage(), e);
			}
		}

		return result;
	}
}

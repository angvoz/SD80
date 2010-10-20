/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
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
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Abstract disassembler that implements the platform neutral methods.
 */
public abstract class AbstractDisassembler implements IDisassembler {

	private final ITargetEnvironment targetEnvironment;

	/**
	 * Don't allow constructor without passing ITargetEnvironment
	 * @see org.eclipse.cdt.debug.edc.AbstractDisassembler#disassemblerAbstractDisassembler(ITargetEnvironment)
	 */
	@SuppressWarnings("unused")
	private AbstractDisassembler() { targetEnvironment = null; }

	/**
	 * Since a disassembler is generally created within the implementor of 
	 * {@link ITargetEnvironment#getDisassembler()}, this constructor together
	 * with the private default constructor forces the implementation to pass
	 * it's own "this" pointer for use in later disassembler processing.
	 * @since 2.0
	 */
	protected AbstractDisassembler(ITargetEnvironment env) {
		targetEnvironment = env;
	}

	/**
	 * Returns the ITargetEnvironment used by the disassembler.
	 * @since 2.0
	 */
	protected ITargetEnvironment getTargetEnvironment() {
		return targetEnvironment;
	}

	/**
	 * Translates the raw memory in the buffer into a list of disassembled instructions.
	 *
	 * @param startAddress the start address
	 * @param endAddress the end address
	 * @param codeBuffer the code buffer
	 * @param options the options, often target environment specific, that are used for disassembly
	 * @param dmc the disassembly context
	 * @return the list of disassembled instructions
	 * @throws CoreException the core exception
	 * @since 2.0
	 */
	public List<IDisassembledInstruction> disassembleInstructions(IAddress startAddress, IAddress endAddress,
				ByteBuffer codeBuffer, Map<String, Object> options, IDisassemblyDMContext dmc)
			throws CoreException {
		IDisassembledInstruction inst = null;
		ArrayList<IDisassembledInstruction> result = new ArrayList<IDisassembledInstruction>();
		IAddress address = startAddress;

		while (codeBuffer.hasRemaining() && address.compareTo(endAddress) < 0) {
			try {
				inst = disassembleOneInstruction(address, codeBuffer, options, dmc);
				result.add(inst);

				// next instruction address
				// Note at this point the current position of code buffer should
				// point to the next instruction.
				address = address.add(inst.getSize());
			} catch (CodeBufferUnderflowException e) {
				if (result.size() == 0) {
					if (options.containsKey(IDisassemblerOptions.ADDRESS_IS_PC)) {
						throwAnnotated(e, address);
					}
					throw e;
				}
				break;
			} catch (CoreException e) {
				throwAnnotated(e, address);
			}
		}

		return result;
	}

	private void throwAnnotated(CoreException e, IAddress address) throws CoreException {
		throw EDCDebugger.newCoreException("Fail to disassemble instruction at "
				+ address.toHexAddressString() + "\nCause: " + e.getLocalizedMessage(), e);
	}
}

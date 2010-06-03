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

package org.eclipse.cdt.debug.edc;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Expression evaluator that computes jump-to address for a control-change
 * instruction such as jump and call instruction. To evaluate an address
 * expression, accessing registers and/or memory is required. And DSF services
 * are invoked for those access.<br>
 * <br>
 * As the address expression is usually produced by disassembler, this evaluator
 * implementation should stay in sync with corresponding disassembler
 * implementation.
 */
public interface IAddressExpressionEvaluator {

	/**
	 * Evaluate a expression synchronously.<br>
	 * <br>
	 * This method should be called only when control is at the instruction. As
	 * DSF services will be called, this method should also be called in DSF
	 * dispatch thread.
	 * 
	 * @param context
	 *            Execution DMC.
	 * @param expression
	 *            the address expression from a control-change instruction. This
	 *            expression is usually produced by disassembler.
	 * @param regService
	 *            EDC version of DSF IRegisters service for register access.
	 * @param memService
	 *            EDC version of DSF IMemory service for memory access.
	 * @return address computed.
	 * @throws CoreException
	 *             on any error.
	 */
	IAddress evaluate(IExecutionDMContext context, String expression, IRegisters regService, IMemory memService)
			throws CoreException;
}

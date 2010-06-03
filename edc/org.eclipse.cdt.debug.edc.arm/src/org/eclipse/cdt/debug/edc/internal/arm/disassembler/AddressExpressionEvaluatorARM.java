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

import java.text.MessageFormat;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IAddressExpressionEvaluator;
import org.eclipse.cdt.debug.edc.internal.arm.ARMPlugin;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Address expression evaluator for ARM.<br>
 * <br>
 * Examples expressions for ARM:
 * 
 * <pre>
 *  rn			: address in register n
 *  sp			: address in SP register
 *  lr			: address in LR register
 *  pc			: address in PC register
 * </pre>
 */
public class AddressExpressionEvaluatorARM implements IAddressExpressionEvaluator {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.IAddressExpressionEvaluator#evaluate(org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext, java.lang.String, org.eclipse.cdt.dsf.debug.service.IRegisters, org.eclipse.cdt.dsf.debug.service.IMemory)
	 */
	public IAddress evaluate(IExecutionDMContext context, String expression, IRegisters regService, IMemory memService)
			throws CoreException {
		IAddress address = null;
		if ((expression.startsWith("r") && expression.length() < 4) || expression.equals("sp")
				|| expression.equals("lr") || expression.equals("pc")) {
			String regName = expression.toUpperCase();
			String regVal = ((Registers) regService).getRegisterValue((IEDCExecutionDMC) context, regName);
			address = new Addr32(regVal, 16);
		} else {
			// invalid
			throw new CoreException(new Status(IStatus.ERROR, ARMPlugin.PLUGIN_ID, MessageFormat.format(
					"Jump address expression {0} is invalid.", expression)));
		}
		return address;
	}

}

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

package org.eclipse.cdt.debug.edc.internal.disassembler.arm;

import java.text.MessageFormat;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.IAddressExpressionEvaluator;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Registers;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
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

	public IAddress evaluate(ExecutionDMC context, String expression, Registers regService, Memory memService)
			throws CoreException {
		IAddress address = null;
		if ((expression.startsWith("r") && expression.length() < 4) || expression.equals("sp")
				|| expression.equals("lr") || expression.equals("pc")) {
			String regName = expression.toUpperCase();
			String regVal = regService.getRegisterValue(context, regName);
			address = new Addr32(regVal, 16);
		} else {
			// invalid
			throw new CoreException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, MessageFormat.format(
					"Jump address expression {0} is invalid.", expression)));
		}
		return address;
	}

}

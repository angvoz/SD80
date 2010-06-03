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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.core.runtime.CoreException;

/*
 * Push a double value on the instruction stack
 */
public class PushDouble extends SimpleInstruction {

	private double fValue;
	private boolean isLong;
	
	/**
	 * Constructor for pushing a double value on the stack
	 * 
	 * @param value
	 *            - double value
	 */
	public PushDouble(double value) {
		fValue = value;
	}

	/**
	 * Constructor for pushing a double value on the stack
	 * 
	 * @param value
	 *            - string version of a double
	 * @throws NumberFormatException
	 */
	public PushDouble(String value) throws NumberFormatException {
		if (value.toLowerCase().endsWith("l")) { //$NON-NLS-1$
			isLong = true;
			value = value.substring(0, value.length() - 1);
		}
		fValue = Double.valueOf(value).doubleValue();
	}

	/**
	 * Execute pushing a double value on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		int size;
		if (isLong) {
			size = fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_LONG_DOUBLE);
			pushNewValue(fInterpreter.getTypeEngine().getBasicType(ICPPBasicType.t_double, ICPPBasicType.IS_LONG, size), fValue);
		}
		else {
			size = fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_DOUBLE);
			pushNewValue(fInterpreter.getTypeEngine().getBasicType(ICPPBasicType.t_double, 0, size), fValue);
		}
			
	}

	/**
	 * Show a double value as a string
	 * 
	 * @return string version of a double
	 */
	@Override
	public String toString() {
		return Double.toString(fValue);
	}

}

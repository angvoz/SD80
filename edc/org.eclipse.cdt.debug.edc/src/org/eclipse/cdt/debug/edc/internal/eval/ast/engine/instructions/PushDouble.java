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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import org.eclipse.core.runtime.CoreException;

/*
 * Push a double value on the instruction stack
 */
public class PushDouble extends SimpleInstruction {

	private double fValue;

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
		fValue = Double.valueOf(value).doubleValue();
	}

	/**
	 * Execute pushing a double value on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		pushNewValue(fValue);
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

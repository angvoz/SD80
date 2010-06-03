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

import org.eclipse.core.runtime.CoreException;

/*
 * Push a float on the instruction stack
 */
public class PushFloat extends SimpleInstruction {

	private float fValue;

	/**
	 * Constructor for pushing a float on the stack
	 * 
	 * @param value
	 *            - float value
	 */
	public PushFloat(float value) {
		fValue = value;
	}

	/**
	 * Constructor for pushing a float on the stack
	 * 
	 * @param value
	 *            - string version of a float
	 * @throws NumberFormatException
	 */
	public PushFloat(String value) throws NumberFormatException {
		fValue = Float.valueOf(value).floatValue();
	}

	/**
	 * Execute pushing a float on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		pushNewValue(fInterpreter.getTypeEngine().getFloatTypeOfSize(4), fValue);
	}

	/**
	 * Show a float value as a string
	 * 
	 * @return string version of a float
	 */
	@Override
	public String toString() {
		return Float.toString(fValue);
	}

}

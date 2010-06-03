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
 * Push a boolean on the instruction stack
 */
public class PushBoolean extends SimpleInstruction {
	private boolean fValue;

	/**
	 * Constructor for pushing a boolean on the stack
	 * 
	 * @param value
	 *            - boolean value
	 */
	public PushBoolean(boolean value) {
		fValue = value;
	}

	/**
	 * Execute pushing a boolean on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		pushNewValue(fInterpreter.getTypeEngine().getBooleanType(4), fValue);
	}

	/**
	 * Show a boolean value as a string
	 * 
	 * @return string version of a boolean
	 */
	@Override
	public String toString() {
		return Boolean.toString(fValue);
	}
}

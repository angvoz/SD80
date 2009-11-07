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
 * Push a string on the instruction stack
 */
public class PushString extends SimpleInstruction {

	private String fValue;

	/**
	 * Constructor for pushing a string on the stack
	 * 
	 * @param value
	 *            - string value
	 */
	public PushString(String value) {
		fValue = value;
	}

	/**
	 * Execute pushing a string on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		pushNewValue(fValue);
	}

	/**
	 * Show a string value
	 * 
	 * @return string
	 */
	@Override
	public String toString() {
		return fValue;
	}

}

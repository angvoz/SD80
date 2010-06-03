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
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.core.runtime.CoreException;

/*
 * Push a string on the instruction stack
 */
public class PushString extends SimpleInstruction {

	private String fValue;
	private boolean isWide;
	private IType stringType;

	/**
	 * Constructor for pushing a string on the stack
	 * 
	 * @param value
	 *            - string value in format "..." or L"..."
	 */
	public PushString(String value) {
		if (value.startsWith("L")) { //$NON-NLS-1$
			isWide = true;
			value = value.substring(1);
		}
		
		fValue = value.substring(1, value.length() - 1);
	}

	/**
	 * Execute pushing a string on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		if (stringType == null) {
			int size = fInterpreter.getTypeEngine().getTypeSize(isWide ? TypeUtils.BASIC_TYPE_WCHAR_T : TypeUtils.BASIC_TYPE_CHAR);
			IType charType = fInterpreter.getTypeEngine().getBasicType(ICPPBasicType.t_char, 0, size);
			stringType = fInterpreter.getTypeEngine().getCharArrayType(charType, fValue.length() + 1);
		}
		pushNewValue(stringType, fValue);
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

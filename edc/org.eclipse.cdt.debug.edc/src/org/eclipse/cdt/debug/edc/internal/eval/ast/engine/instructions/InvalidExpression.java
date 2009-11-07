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

/**
 * Invalid expression
 */
public class InvalidExpression extends SimpleInstruction implements IInvalidExpression {

	final String message;

	public InvalidExpression(String message) {
		super();
		this.message = message;
	}

	/**
	 * Resolve an invalid expression
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
		push(this);
		setLastValue(this);
		setValueLocation(""); //$NON-NLS-1$
		setValueType(""); //$NON-NLS-1$
	}

	public String getMessage() {
		return this.message;
	}

}

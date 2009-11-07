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
 * Push this on the instruction stack
 */
public class PushThis extends SimpleInstruction {

	/**
	 * Execute pushing this on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() throws CoreException {
	}

}

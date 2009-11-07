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

public abstract class SimpleInstruction extends Instruction {

	/**
	 * Constructor for a simple instruction
	 */
	protected SimpleInstruction() {
		super();
	}

	/**
	 * Get simple instruction size
	 * 
	 * @return 1
	 */
	@Override
	public int getSize() {
		return 1;
	}

}

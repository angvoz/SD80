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

public abstract class CompoundInstruction extends Instruction {

	private int size;

	/**
	 * Constructor for a compound instruction
	 * 
	 * @param start
	 *            - instruction start
	 */
	protected CompoundInstruction(int start) {
		size = -start;
	}

	/**
	 * Set compound instruction end
	 * 
	 * @param end
	 *            - compound instruction end
	 */
	public void setEnd(int end) {
		size += end;
	}

	/**
	 * Get compound instruction size
	 * 
	 * @return compound instruction size
	 */
	@Override
	public int getSize() {
		return size;
	}

}

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
package org.eclipse.cdt.debug.edc.internal.symbols;

import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;

/**
 * Interface representing a variable value located in a register.
 */
public interface IRegisterVariableLocation extends IVariableLocation {

	/**
	 * Get the name of the register containing the variable value
	 * 
	 * @return the register name if known, null otherwise
	 */
	String getRegisterName();

	/**
	 * Get the id of the register containing the variable value
	 * 
	 * @return the register id if known, -1 otherwise
	 */
	int getRegisterID();
}

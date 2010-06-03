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
package org.eclipse.cdt.debug.edc.symbols;

/**
 * Interface representing a variable value located in memory.
 */
public interface IMemoryVariableLocation extends IVariableLocation {
	/**
	 * Is this address a runtime address or a link address?
	 * @return true if runtime address, false if link address
	 */
	boolean isRuntimeAddress();

}

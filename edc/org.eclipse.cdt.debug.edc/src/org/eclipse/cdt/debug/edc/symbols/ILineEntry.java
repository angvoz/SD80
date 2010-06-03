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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.core.runtime.IPath;

/**
 * Interface that represents a line table entry
 */
public interface ILineEntry extends Comparable<Object> {

	/**
	 * Get the file path that the line entry applies to
	 * 
	 * @return the file path as defined in the symbolics
	 */
	IPath getFilePath();

	/**
	 * Get the line number in the file
	 * 
	 * @return the line number
	 */
	int getLineNumber();

	/**
	 * Get the column number in the line
	 * 
	 * @return
	 */
	int getColumnNumber();

	/**
	 * Get the low link address of the line entry
	 * 
	 * @return the low address
	 */
	IAddress getLowAddress();

	/**
	 * Get the high link address of the line entry
	 * 
	 * @return the high address
	 */
	IAddress getHighAddress();

	/**
	 * Set the high link address of the line entry
	 */
	void setHighAddress(IAddress highAddress);

}

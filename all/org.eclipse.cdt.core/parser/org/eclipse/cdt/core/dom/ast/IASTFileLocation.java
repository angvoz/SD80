/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * Represents a node location that is directly in the source file.
 * 
 * @author Doug Schaefer
 */
public interface IASTFileLocation extends IASTNodeLocation {

	/**
	 * The name of the file.
	 * 
	 * @return the name of the file
	 */
	public String getFileName();
    
    /**
     * Get the starting line number.
     * 
     * @return in representing line number
     */
    public int getStartingLineNumber();
    
    /**
     * Get the ending line number. 
     * 
     * @return int representing line number
     */
    public int getEndingLineNumber();
}

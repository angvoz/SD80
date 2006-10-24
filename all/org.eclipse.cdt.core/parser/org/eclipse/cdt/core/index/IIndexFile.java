/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.core.index;

import org.eclipse.core.runtime.CoreException;

/**
 * Represents a file that has been indexed.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the CDT team.
 * </p>
 * 
 * @since 4.0
 */
public interface IIndexFile {

	/**
	 * Returns the absolute path of the location of the file.
	 * @return the absolute path of the location of the file
	 * @throws CoreException
	 */
	String getLocation() throws CoreException;

	/**
	 * Returns all includes found in this file.
	 * @return an array of all includes found in this file
	 * @throws CoreException
	 */
	IIndexInclude[] getIncludes() throws CoreException;
	
	/**
	 * Returns all macros defined in this file.
	 * @return an array of macros found in this file
	 * @throws CoreException
	 */
	IIndexMacro[] getMacros() throws CoreException;
	
	/**
	 * Last modification of file before it was indexed.
	 * @return the last modification date of the file at the time it was parsed.
	 * @throws CoreException 
	 */
	long getTimestamp() throws CoreException;
}

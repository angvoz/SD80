/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.core.index;

import org.eclipse.core.runtime.CoreException;

/**
 * Interface for an include directive stored in the index.
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
public interface IIndexInclude {
	IIndexInclude[] EMPTY_INCLUDES_ARRAY = new IIndexInclude[0];

	/**
	 * Returns the file that contains this directive.
	 * @return the file performing the include
	 * @throws CoreException
	 */
	IIndexFile getIncludedBy() throws CoreException;

	/**
	 * Returns the IIndexFileLocation of the file that contains this directive.
	 * @return the IIndexFileLocation of the file performing the include
	 * @throws CoreException
	 */
	IIndexFileLocation getIncludedByLocation() throws CoreException;
	
	/**
	 * Returns the IIndexFileLocation of the file that is included by this directive.
	 * @return the IIndexFileLocation of the file that is included by this directive
	 * @throws CoreException
	 */
	IIndexFileLocation getIncludesLocation() throws CoreException;

	/**
	 * Returns the character offset of the name of the include in its source file. The name does
	 * not include the enclosing quotes or angle brackets.
	 * @throws CoreException 
	 */
	int getNameOffset() throws CoreException;

	/**
	 * Returns the length of the name of the include. The name does
	 * not include the enclosing quotes or angle brackets.
	 * @throws CoreException 
	 */
	int getNameLength() throws CoreException;

	/** 
	 * Returns whether this is a system include (an include specified within angle
	 * brackets).
	 * @throws CoreException 
	 */
	boolean isSystemInclude() throws CoreException;
}

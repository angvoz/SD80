/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.core.search;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * Interface used for returning matches from the Search Engine
 *
 */
public interface IMatch {
	
	int getElementType();

	int getVisibility();

	String getName();

	String getParentName();

	IResource getResource();
	
	IPath getLocation();

	IPath getReferenceLocation();
	
	IMatchLocatable getLocatable();
	
	boolean isStatic();
	boolean isConst();
	boolean isVolatile();
}

/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.internal.core.index;

public interface IIndexEntry {
	
	/**
	 * Returns the type for this index entry.
	 * @return IIndex.DECLARATION, IIndex.DEFINITION or IIndex.REFERENCE
	 */
	public int getEntryType();
	/**
	 * Write the entry to the passed in indexer output
	 * @param output
	 */
	public void serialize(IIndexerOutput output);
	
}

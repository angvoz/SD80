/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;
/**
 * IEntryResult is the interface used to represent individual index
 * entries for the purpose of client queries.
 */
public interface IEntryResult {
	/**
	 * Returns the unique file numbers of files that have a
	 * reference to this entry.
	 */
	public int[] getFileReferences();
	/**
	 * Returns the encoded word of this entry
	 */
	public char[] getWord();
}


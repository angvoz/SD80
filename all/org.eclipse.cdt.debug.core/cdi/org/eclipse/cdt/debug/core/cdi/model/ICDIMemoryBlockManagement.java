/*******************************************************************************
 * Copyright (c) 2002, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * The memory manager manages the collection of memory blocks 
 * specified for the debug session.
 * 
 * ICDIMemoryBlockManagement
 * 
 */
public interface ICDIMemoryBlockManagement {

	/**
	 * Returns a memory block specified by given identifier.
	 * @param address 
	 * @param units - number of bytes
	 * @param wordSize - this parameter has been deprecated in 4.0
	 * and will always be passed as the value 1. If the memory
	 * has an addressable size (number of bytes per address)
	 * greather than 1, the CDI client should take care not to
	 * return the value of wordSize we pass in here, but rather
	 * return the actual addressable size for that memory.
	 * @return a memory block with the specified identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock createMemoryBlock(String address, int units, int wordSize)
		throws CDIException;

	/**
	 * Removes the given array of memory blocks from the debug session.
	 * 
	 * @param memoryBlock - the array of memory blocks to be removed
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException;

	/**
	 * Removes all memory blocks from the debug session.
	 * 
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeAllBlocks() throws CDIException;

	/**
	 * Returns an array of all memory blocks set for this debug session.
	 *
	 * @return an array of all memory blocks set for this debug session
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock[] getMemoryBlocks() throws CDIException;

}

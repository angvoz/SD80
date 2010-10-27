/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension;

/**
 * represents a block of memory that has an associated status,
 * to be converted to a TCF message that will properly turn into
 * org.eclipse.tm.internal.tcf.services.remote.MemoryProxy
 * on the requesting side of the TCF channel.
 * 
 * @author kirk.beitz@nokia.com
 * @since 2.0
 * @see MemoryBlockMultiStatusException
 */
public class PartialMemoryBlock {

	private final int address;
	private final int size;
	private final int status;

	/**
	 * block of memory that is often combined with other blocks in
	 * a list held by {@link MemoryBlockMultiStatusException} to
	 * mirror a <code>MemoryProxy.Range</code> as described in
	 * 
	 * @param address
	 * @param size
	 * @param status
	 */
	public PartialMemoryBlock(int address, int size, int status) {
		this.address = address;
		this.size = size;
		this.status = status;
	}

	/**
	 * @return the final address held by this object
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * @return the final size held by this object
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return the final status held by this object
	 */
	public int getStatus() {
		return status;
	}
}

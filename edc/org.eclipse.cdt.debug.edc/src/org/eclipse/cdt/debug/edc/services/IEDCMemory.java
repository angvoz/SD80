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
package org.eclipse.cdt.debug.edc.services;

import java.util.ArrayList;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * Interface to the EDC memory service. Used for synchronous access to
 * target memory.
 */
public interface IEDCMemory extends IMemory, IEDCService {

	/**
	 * Fills a buffer with the contents of a range of memory.
	 *
	 * @param context the executable context
	 * @param address the starting address of the range of memory
	 * @param memBuffer the buffer to be filled with the memory contents
	 * @param count the number of words to read
	 * @param word_size the word_size
	 * @return status of the memory request
	 */
	public IStatus getMemory(IEDCExecutionDMC context, IAddress address,
			final ArrayList<MemoryByte> memBuffer, int count, int word_size);

}
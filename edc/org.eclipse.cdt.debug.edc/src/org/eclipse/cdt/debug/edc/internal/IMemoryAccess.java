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

package org.eclipse.cdt.debug.edc.internal;

import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.model.MemoryByte;

/**
 * @author Administrator
 * @since 2.0
 *
 */
public interface IMemoryAccess {

	/**
	 * Retrieves the memory address values as shown at the Hex Pane Memory
	 * View. Every cell has 4 bytes.
	 * @param contextId string representing the context of for the memory
	 * @param memoryAddress The memory address to get its values.
	 * @param length the amount of memory to retrieve
	 * @return An array of memory bytes starting from the memory address given.
	 * @throws Exception Any exception is propagated to the caller.
	 */
	public MemoryByte[] getMemoryValues(final DsfSession session,
			final IEDCExecutionDMC exe_dmc, final String memoryAddress,
			final int length)
		throws Exception;

	/**
	 * Changes the memory value for the given memory address for the length of the array
	 * @param contextId string representing the context of for the memory
	 * @param memoryAddress The memory address which content will be changed.
	 * @param newMemoryValue The new value of the memory address content.
	 * @return True if the change was successful. False if the value couldn't be changed.
	 * @throws Exception Any exception will be propagated to the caller.
	 */
	public boolean changeMemoryValue(final DsfSession session,
			final IEDCExecutionDMC exe_dmc, final String memoryAddress,
			final byte[] newMemoryValue)
		throws Exception;
}

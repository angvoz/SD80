/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension.transport;

import java.io.IOException;

/**
 * Low level tranport layer.
 * 
 * @author LWang
 * 
 */
public interface ITransportChannel {

	/**
	 * Open the transport channel.
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException;

	/**
	 * Close the transport channel.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;

	/**
	 * Read one byte
	 * 
	 * @return
	 * @throws IOException
	 */
	public int get() throws IOException;

	public byte[] getBytes() throws IOException;

	/**
	 * Check if there is input available without blocking.
	 * 
	 * @return true if there is input available. false otherwise.
	 * @throws IOException
	 */
	public boolean hasInput() throws IOException;

	/**
	 * write one byte.
	 * 
	 * @param b
	 * @throws IOException
	 */
	public void put(int b) throws IOException;

	/**
	 * Write array of byte.
	 * 
	 * @param array
	 * @throws IOException
	 */
	public void put(byte[] array) throws IOException;

	/**
	 * Flush output.
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException;
}

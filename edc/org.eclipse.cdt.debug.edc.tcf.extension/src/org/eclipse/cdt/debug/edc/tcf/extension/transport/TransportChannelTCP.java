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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;

/**
 * @author LWang
 * 
 */
public class TransportChannelTCP implements ITransportChannel {

	private Socket socket;
	private InputStream inp;
	private OutputStream out;
	private boolean closed;
	private String host;
	private int port;

	/**
	 * @param host
	 * @param port
	 */
	public TransportChannelTCP(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * @since 2.0
	 */
	public boolean isOpen() {
		return socket != null && !closed;
	}
	
	public void open() throws IOException {
		socket = new Socket(host, port);
		socket.setTcpNoDelay(true);
		inp = new BufferedInputStream(socket.getInputStream());
		out = new BufferedOutputStream(socket.getOutputStream());
		closed = false;
	}

	public void close() throws IOException {
		closed = true;
		socket.close();
		out.close();
		inp.close();
	}

	public boolean hasInput() throws IOException {
		return inp.available() > 0;
	}

	public int get() throws IOException {
		checkClosed();
		return inp.read();
	}

	private void checkClosed() throws IOException {
		if (closed) {
			String msg = MessageFormat.format("Transport ''{0}'' already closed.", toString());
			throw new IOException(msg);
		}
	}

	public void put(int b) throws IOException {
		assert b >= 0 && b <= 0xff;
		checkClosed();
		out.write(b);
	}

	public void put(byte[] a) throws IOException {
		checkClosed();
		out.write(a);
	}

	public void flush() throws IOException {
		checkClosed();
		out.flush();
	}

	@Override
	public String toString() {
		return "TCP::" + host + ":" + port; // $NON-NLS-1$ $NON-NLS-2$
	}

	public byte[] getBytes() throws IOException {
		checkClosed();
		
		int avail = inp.available();
		if (avail <= 0) avail = 256;	// not implemented or will block 
		
		byte[] result = new byte[avail];
		int ret = inp.read(result);
		if (ret < 0)
			return null;
		
		if (ret < result.length) {
			byte[] shortResult = new byte[ret];
			System.arraycopy(result, 0, shortResult, 0, ret);
			result = shortResult;
		}
		return result;
	}
}

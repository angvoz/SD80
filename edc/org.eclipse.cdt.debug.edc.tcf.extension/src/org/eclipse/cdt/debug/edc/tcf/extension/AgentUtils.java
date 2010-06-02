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

package org.eclipse.cdt.debug.edc.tcf.extension;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;

/**
 * @author LWang
 * 
 */
public class AgentUtils {

	private static byte[] sJsonErrInvalidArgNum = null;

	/**
	 * Find a free TCP port.
	 * 
	 * @return
	 * @throws IOException
	 */
	public static int findFreePort() throws IOException {
		ServerSocket server = new ServerSocket(0);
		int port = server.getLocalPort();
		server.close();
		return port;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> toEnvMap(Object o) {
		Map<String, String> m = new HashMap<String, String>();
		if (o == null)
			return m;
		Collection<String> c = (Collection<String>) o;
		for (String s : c) {
			int i = s.indexOf('=');
			if (i >= 0)
				m.put(s.substring(0, i), s.substring(i + 1));
			else
				m.put(s, "");
		}
		return m;
	}

	@SuppressWarnings("unchecked")
	public static String[] toStringArray(Object o) {
		if (o == null)
			return null;
		Collection<String> c = (Collection<String>) o;
		return c.toArray(new String[c.size()]);
	}

	public static Map<String, Object> makeErrorReport(int code, String msg) {
		Map<String, Object> err = new HashMap<String, Object>();
		err.put(IErrorReport.ERROR_TIME, new Long(System.currentTimeMillis()));
		err.put(IErrorReport.ERROR_CODE, new Integer(code));
		err.put(IErrorReport.ERROR_FORMAT, msg);
		return err;
	}

	public static Map<String, Object> makeErrorReport(String msg) {
		return makeErrorReport(IErrorReport.TCF_ERROR_OTHER, msg);
	}

	public static byte[] jsonErr(String err_msg) throws IOException {
		return JSON.toJSONSequence(new Object[] { AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, err_msg) });
	}

	public static byte[] jsonErrInvalidArgNum() throws IOException {
		if (sJsonErrInvalidArgNum == null)
			sJsonErrInvalidArgNum = JSON.toJSONSequence(new Object[] {
					AgentUtils.makeErrorReport(IErrorReport.TCF_ERROR_OTHER, "Invalid number of arguments"), null });

		return sJsonErrInvalidArgNum;
	}

	/**
	 * Convert a string of hexidecimal characters to a byte array. E.g.
	 * "ff0a0120" => {255, 10, 1, 32}
	 * 
	 * @param str
	 *            - a string with even number of hexadecimal digit characters
	 *            where each pair of digits represents an 8-bit byte.
	 * @return byte array
	 */
	public static byte[] hexStringToByteArray(String str) {
		if (str.length() % 2 != 0)
			throw new IllegalArgumentException("Argument string must have even number of characters.");

		byte[] ret = new byte[str.length() / 2];
		for (int i = 0; i < str.length(); i += 2) {
			int k = Integer.parseInt(str.substring(i, i + 2), 16);
			ret[i / 2] = (byte) k;
		}

		return ret;
	}

	/**
	 * Reverse of {@link AgentUtils#hexStringToByteArray(String)}.
	 * 
	 * @param bytes
	 * @return
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		if (bytes.length == 0)
			return "";

		StringBuffer buf = new StringBuffer();
		for (byte b : bytes) {
			String s = Integer.toHexString(b & 0xff);
			if (s.length() == 1)
				buf.append('0');
			buf.append(s);
		}

		return buf.toString();
	}

	/**
	 * Send invalid argument number error over channel if actual number of args
	 * is different from expected. Return true if they are the same.
	 * 
	 * @param token
	 *            IToken
	 * @param channel
	 *            IChannel
	 * @param actualNumArgs
	 *            int
	 * @param expectedNumArgs
	 *            int
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean checkNumArgs(IToken token, IChannel channel, int actualNumArgs, int expectedNumArgs)
			throws IOException {
		if (actualNumArgs != expectedNumArgs) {
			channel.sendResult(token, AgentUtils.jsonErrInvalidArgNum());
			return false;
		}
		return true;
	}

	/**
	 * Swap 4 byte array.
	 * @param ba - the content is changed after this call.
	 */
	public static void swap4(byte[] ba) {
		if (ba.length != 4)
			throw new IllegalArgumentException("swap4() only supports swap of 4 byte array.");
		
		byte m = ba[0];
		ba[0] = ba[3];
		ba[3] = m;
		m = ba[1];
		ba[1] = ba[2];
		ba[2] = m;
	}
}

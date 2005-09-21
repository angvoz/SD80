/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.util;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;

/**
 * Socket utilities.
 */
public class SocketUtil {

	private static final Random fgRandom= new Random(System.currentTimeMillis());

	/**
	 * Method that looks for an unused local port
	 * 
	 * @param searchFrom lower limit of port range
	 * @param searchTo upper limit of port range
	 */
	public static int findUnusedLocalPort(String host, int searchFrom, int searchTo) {
		for (int i= 0; i < 10; i++) {
			int port= getRandomPort(searchFrom, searchTo);
			try {
				new Socket(host, port);
			} catch (SocketException e) {
				return port;
			} catch (IOException e) {
			}
		}
		return -1;
	}
	
	private static int getRandomPort(int low, int high) {
		return (int)(fgRandom.nextFloat()*(high-low))+low;
	}
}



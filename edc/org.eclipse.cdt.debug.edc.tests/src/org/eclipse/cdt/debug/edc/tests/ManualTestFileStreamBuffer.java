/*
* Copyright (c) 2010 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/

package org.eclipse.cdt.debug.edc.tests;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

import junit.framework.TestCase;

import org.eclipse.cdt.debug.edc.internal.FileStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.IStreamBuffer;
import org.junit.After;
import org.junit.Test;

/**
 * Test the FileStreamBuffer class
 */
public class ManualTestFileStreamBuffer extends TestCase {

	protected File theFile;
	protected RandomAccessFile randomAccessFile;

	@After
	public void tearDown() throws Exception {
		
		if (randomAccessFile != null) {
			randomAccessFile.close();
			theFile.delete();
		}
	};
	
	@Test
	public void testHugeFile() throws Exception {
		theFile = File.createTempFile("huge", "dat");
		theFile.deleteOnExit();
		randomAccessFile = new RandomAccessFile(theFile, "rw");
		long size = 1024 * 1024 * 1024;
		
		System.out.print("Writing temp file... ");
		while (size > 0) {
			try {
				randomAccessFile.setLength(size);
				break;
			} catch (IOException e) {
				size /= 2;
			}
		}
		
		
		traverseFileSlow();
		traverseFileFast();
	}

	/**
	 * @throws IOException 
	 * 
	 */
	private void traverseFileSlow() throws IOException {
		long size = randomAccessFile.length();
		System.out.println(size);
		
		
		System.gc();
		System.runFinalization();

		long baseMemUsage = Runtime.getRuntime().freeMemory();
		long baseTime = System.currentTimeMillis();
		
		IStreamBuffer sb = new FileStreamBuffer(randomAccessFile, ByteOrder.LITTLE_ENDIAN);
		assertEquals(size, sb.capacity());
		
		// read the whole thing slowly
		while (sb.hasRemaining()) {
			sb.getInt();
		}

		long usedTime = System.currentTimeMillis() - baseTime;

		long endMemUsage = Runtime.getRuntime().freeMemory();
		
		// make sure we didn't eat a lot of memory reading that file
		System.out.println("Memory used: " +  (endMemUsage - baseMemUsage));
		
		// account for weird fluctuations in test environment
		assertTrue(baseMemUsage + "/"+endMemUsage, Math.abs(endMemUsage - baseMemUsage) < 4 * 1024 * 1024);
		
		System.out.println("Spent "+ usedTime + " to traverse (slow)");
		
	}
	

	/**
	 * @throws IOException 
	 * 
	 */
	private void traverseFileFast() throws IOException {
		long size = randomAccessFile.length();
		System.out.println(size);

		System.gc();
		System.runFinalization();

		byte[] buffer = new byte[65536];
		
		long baseMemUsage = Runtime.getRuntime().freeMemory();
		long baseTime = System.currentTimeMillis();
		
		IStreamBuffer sb = new FileStreamBuffer(randomAccessFile, ByteOrder.LITTLE_ENDIAN);
		assertEquals(size, sb.capacity());
		
		// read the whole thing quickly
		while (sb.hasRemaining()) {
			long remaining = sb.remaining();
			sb.get(buffer, 0, (int) Math.min(remaining, buffer.length));
		}

		long usedTime = System.currentTimeMillis() - baseTime;

		long endMemUsage = Runtime.getRuntime().freeMemory();
		
		// make sure we didn't eat a lot of memory reading that file
		System.out.println("Memory used: " +  (endMemUsage - baseMemUsage));
		
		// account for weird fluctuations in test environment
		assertTrue(baseMemUsage + "/"+endMemUsage, Math.abs(endMemUsage - baseMemUsage) < 4 * 1024 * 1024);
		
		System.out.println("Spent "+ usedTime + " to traverse (fast)");
		
	}
}

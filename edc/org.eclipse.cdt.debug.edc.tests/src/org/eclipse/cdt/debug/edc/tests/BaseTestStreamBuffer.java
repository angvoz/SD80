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

import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;
import java.util.Random;

import junit.framework.TestCase;

import org.eclipse.cdt.debug.edc.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.StreamBufferBase;

/**
 * Test the MemoryStreamBuffer interface
 */
public abstract class BaseTestStreamBuffer extends TestCase {

	protected abstract IStreamBuffer createBuffer(byte[] content, ByteOrder order) throws Exception;
	
	public void doTestStreamLimits() throws Exception {
		byte[] content = { 1, 2, 3, 4, 5, 6, 7, 8 };
		IStreamBuffer sb = createBuffer(content, ByteOrder.LITTLE_ENDIAN); 
		sb.position(4);
		sb.getInt();
		try {
			sb.get();
			fail();
		} catch (BufferUnderflowException e) {
			assertEquals(8, sb.position());
		}
		// should be allowed
		sb.position(8);
		try {
			sb.position(9);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals(8, sb.position());
		}
		
	}
	public void doTestStreamBE() throws Exception {
		byte[] content = { 1, 2, 3, 4, 5, 6, 7, 8 };
		IStreamBuffer sb = createBuffer(content, ByteOrder.BIG_ENDIAN); 
		assertEquals(1, sb.get());
		assertEquals(2, sb.get());
		assertEquals(2, sb.position());
		assertEquals(6, sb.remaining());
		assertEquals(0x304, sb.getShort());
		assertEquals(0x5060708, sb.getInt());
		assertEquals(0, sb.remaining());
		sb.position(0);
		assertEquals(8, sb.remaining());
		assertEquals(0, sb.position());
		assertEquals(0x01020304, sb.getInt());
		assertEquals(4, sb.position());
		assertEquals(0x05060708, sb.getInt());
		
		sb.position(0);
		assertEquals(0x0102030405060708L, sb.getLong());
		
		sb.position(3);
		assertEquals(0x0405, sb.getChar());
	}
	
	public void doTestStreamLE() throws Exception {
		byte[] content = { 1, 2, 3, 4, 5, 6, 7, 8 };
		IStreamBuffer sb = createBuffer(content, ByteOrder.LITTLE_ENDIAN); 
		assertEquals(1, sb.get());
		assertEquals(2, sb.get());
		assertEquals(2, sb.position());
		assertEquals(0x403, sb.getShort());
		assertEquals(0x8070605, sb.getInt());
		assertEquals(0, sb.remaining());
		sb.position(0);
		assertEquals(8, sb.remaining());
		assertEquals(0, sb.position());
		assertEquals(0x04030201, sb.getInt());
		assertEquals(4, sb.position());
		assertEquals(0x08070605, sb.getInt());
		
		sb.position(3);
		assertEquals(0x0504, sb.getChar());
	}
	
	public void doTestStreamPaging() throws Exception {
		int LIMIT = 0x8000 - 256;
		byte[] big = new byte[LIMIT];
		for (int i = 0; i < LIMIT; i++)
			big[i] = (byte) (i >> 8);
		
		IStreamBuffer sb = createBuffer(big, ByteOrder.BIG_ENDIAN);
		
		assertEquals(0, sb.getInt());
		assertEquals(0, sb.getLong());
		
		int val = (StreamBufferBase.BUFFER_SIZE - 1) >> 8;
		sb.position(StreamBufferBase.BUFFER_SIZE - 1);
		assertEquals(val, sb.get());
		assertEquals(val + 1, sb.get());
		
		sb.position(StreamBufferBase.BUFFER_SIZE - 4);
		assertEquals(val|(val<<8)|(val<<16)|(val<<24), sb.getInt());
		val++;
		assertEquals(val|(val<<8)|(val<<16)|(val<<24), sb.getInt());
		
		sb.position(LIMIT - StreamBufferBase.BUFFER_SIZE);
		while (sb.hasRemaining()) {
			val = ((int) (sb.position() >> 8));
			assertEquals(sb.position()+"", val|(val<<8)|(val<<16)|(val<<24), sb.getInt());
		}
		assertEquals(sb.capacity(), sb.position());
	}
	
	public void doTestSubStream1() throws Exception {
		int LIMIT = 0x8000;
		byte[] big = new byte[LIMIT];
		for (int i = 0; i < LIMIT; i++)
			big[i] = (byte) (i >> 8);
		
		IStreamBuffer sb = createBuffer(big, ByteOrder.LITTLE_ENDIAN);

		sb.position(0x500);
		IStreamBuffer sub = sb.wrapSubsection(0x1234);
		
		assertEquals(0x1234, sub.capacity());
		assertEquals(0, sub.position());
		
		while (sub.hasRemaining()) {
			int val = ((int)((sub.position() + 0x500) >> 8));
			assertEquals(sub.position()+"", val|(val<<8)|(val<<16)|(val<<24), sub.getInt());
		}
		assertEquals(sub.capacity(), sub.position());
		
		try {
			sub.get();
			fail();
		} catch (BufferUnderflowException e) {
		}
		
		// positioning into subsection...
		sub.position(0);
		assertEquals(0x505, sub.getShort());
		
		sub.position(0x1234);
		try {
			sub.position(0x1235);
			fail();
		} catch (IllegalArgumentException e) {
			
		}
		
		// does not affect outer buffer
		assertEquals(0x500, sb.position());
		
		// test sub-buffer of sub-buffer
		
		sub.position(0x3FC);
		IStreamBuffer sub2 = sub.wrapSubsection(0x100);
		assertEquals(0x100, sub2.capacity());
		assertEquals(0x0, sub2.position());
		
		long val = sub2.getLong();
		assertEquals(0x0909090908080808L, val);
	}
	
	public void doTestSubStream2() throws Exception {
		int LIMIT = 1024 * 1024;
		byte[] big = new byte[LIMIT];
		for (int i = 0; i < LIMIT; i++)
			big[i] = (byte) (i >> 8);
		
		IStreamBuffer sb = createBuffer(big, ByteOrder.BIG_ENDIAN);

		long baseTime = System.currentTimeMillis();
		
		Random random = new Random(0x19293845);
		for (int count = 0; count < 1000; count++) {
			// like doTestSubStream1, but a lot of little buffers, and reading the full buffer
			
			long base = random.nextInt((int) sb.capacity()) & ~3;
			sb.position(base);
			
			int size = (int) (sb.capacity() - sb.position());
			if (size > 32768)
				size = 32768;
			
			IStreamBuffer sub = sb.wrapSubsection(size);
			
			assertEquals(size, sub.capacity());
			assertEquals(0, sub.position());
			
			byte[] subContent = new byte[size];
			sub.get(subContent);
			
			assertEquals(size, sub.position());
			
			try {
				sub.get();
				fail();
			} catch (BufferUnderflowException e) {
			}
			
			for (int idx = 0; idx < subContent.length; idx++) {
				int val = ((int)((idx + base) >> 8)) & 0xff;
				int got = subContent[idx] & 0xff;
				if (val != got)
					assertEquals(idx+"", val, got);
			}

		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Sub buffer traverse time: " + (endTime - baseTime));
		
	}
	
}

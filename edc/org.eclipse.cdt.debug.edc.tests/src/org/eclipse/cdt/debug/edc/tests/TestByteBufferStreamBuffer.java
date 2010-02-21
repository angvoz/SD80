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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.cdt.debug.edc.internal.ByteBufferStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.IStreamBuffer;
import org.junit.Test;

/**
 * Test the FileStreamBuffer class
 */
public class TestByteBufferStreamBuffer extends BaseTestStreamBuffer {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.tests.BaseTestStreamBuffer#createBuffer(byte[], java.nio.ByteOrder)
	 */
	@Override
	protected IStreamBuffer createBuffer(byte[] content, ByteOrder order) throws Exception {
		ByteBuffer buffer = ByteBuffer.wrap(content);
		buffer.order(order);
		return new ByteBufferStreamBuffer(buffer);
	}
	
	@Test
	public void testDiskStreamLimits() throws Exception {
		doTestStreamLimits();
	}
	@Test
	public void testDiskStreamBE() throws Exception {
		doTestStreamBE();
	}
	
	@Test
	public void testDiskStreamLE() throws Exception {
		doTestStreamLE();
	}
	
	@Test
	public void testDiskStreamPaging() throws Exception {
		doTestStreamPaging();
	}
	

	@Test
	public void testSubStream1() throws Exception {
		doTestSubStream1();
	}
	
	@Test
	public void testSubStream2() throws Exception {
		doTestSubStream2();
	}
}

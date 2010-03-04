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

package org.eclipse.cdt.debug.edc.internal;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.eclipse.cdt.debug.edc.IStreamBuffer;

/**
 * This implementation of IStreamBuffer works on an existing ByteBuffer.
 */
public class ByteBufferStreamBuffer extends StreamBufferBase {

	private ByteBuffer buffer;
	
	
	/**
	 * Wrap in-memory content.
	 * @param content
	 * @param order
	 */
	public ByteBufferStreamBuffer(ByteBuffer buffer) throws IOException {
		super(buffer.order(), 0, buffer.capacity());
		this.buffer = buffer;
	}
	public ByteBufferStreamBuffer(ByteBuffer buffer, long position, long size) {
		super(buffer.order(), position, size);
		this.buffer = buffer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.StreamBufferBase#fetchPage(byte[], int, int)
	 */
	@Override
	protected void fetchPage(byte[] buffer, long sourceOffset, int count) {
		if (sourceOffset > Integer.MAX_VALUE)
			throw new BufferUnderflowException();
		this.buffer.position((int) sourceOffset);
		this.buffer.get(buffer, 0, count);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.StreamBufferBase#createSubBuffer(long, long)
	 */
	@Override
	protected IStreamBuffer createSubBuffer(long offset, long size) {
		return new ByteBufferStreamBuffer(buffer, offset, size);
	}
	
}

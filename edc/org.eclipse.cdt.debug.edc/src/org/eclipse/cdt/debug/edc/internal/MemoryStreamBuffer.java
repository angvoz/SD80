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

import java.nio.ByteOrder;

/**
 * This implementation of IStreamBuffer works on memory content.
 */
public class MemoryStreamBuffer extends StreamBufferBase {

	private byte[] content;

	
	/**
	 * Wrap in-memory content.
	 * @param content
	 * @param order
	 */
	public MemoryStreamBuffer(byte[] content, ByteOrder order) {
		super(order, 0, content.length);
		this.content = content;
	}
	public MemoryStreamBuffer(byte[] content, ByteOrder order, long position, long size) {
		super(order, position, size);
		this.content = content;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.StreamBufferBase#fetchPage(byte[], int, int)
	 */
	@Override
	protected void fetchPage(byte[] buffer, long sourceOffset, int count) {
		System.arraycopy(content, (int) sourceOffset, buffer, 0, count);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.StreamBufferBase#createSubBuffer(long, long)
	 */
	@Override
	protected IStreamBuffer createSubBuffer(long offset, long size) {
		return new MemoryStreamBuffer(content, order, offset, size);
	}
}

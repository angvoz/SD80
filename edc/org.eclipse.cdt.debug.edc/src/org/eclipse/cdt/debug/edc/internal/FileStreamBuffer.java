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
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;

import org.eclipse.cdt.debug.edc.IStreamBuffer;

/**
 * This implementation of IStreamBuffer works on file content.
 */
public class FileStreamBuffer extends StreamBufferBase {
	private final boolean DEBUG = false;
	private RandomAccessFile file;
	
	
	/**
	 * Wrap in-memory content.
	 * @param content
	 * @param order
	 */
	public FileStreamBuffer(RandomAccessFile file, ByteOrder order) throws IOException {
		super(order, 0, file.length());
		this.file = file;
	}
	public FileStreamBuffer(RandomAccessFile file, ByteOrder order, long position, long size) {
		super(order, position, size);
		this.file = file;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.StreamBufferBase#fetchPage(byte[], int, int)
	 */
	@Override
	protected void fetchPage(byte[] buffer, long sourceOffset, int count) {
		try {
			if (DEBUG) System.out.print("Reading "+ sourceOffset + " x "+ count + "... ");
			file.seek(sourceOffset);
			file.read(buffer, 0, count);
			if (DEBUG) System.out.println("done");
		} catch (IOException e) {
			BufferUnderflowException be = new BufferUnderflowException();
			be.initCause(e);
			throw be;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.StreamBufferBase#createSubBuffer(long, long)
	 */
	@Override
	protected IStreamBuffer createSubBuffer(long offset, long size) {
		return new FileStreamBuffer(file, order, offset, size);
	}
}

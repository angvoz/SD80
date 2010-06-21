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

import java.nio.BufferUnderflowException;
import java.nio.ByteOrder;

import org.eclipse.cdt.debug.edc.IStreamBuffer;

/**
 * 
 */
public abstract class StreamBufferBase implements IStreamBuffer {
	/* must be a power of 2 */
	public static final int BUFFER_SIZE = 4096;
	
	protected ByteOrder order;
	
	// absolute
	private long position;
	// absolute
	private long sourceCapacity;

	private byte[] buffer;
	// absolute source position in buffer[0]
	private long sourceOffset;
	// absolute source position in buffer[buffer.length]
	private long sourceLimit;
	
	// offset from source to position
	private final long baseOffset;
	
	/**
	 * Create a buffer over some source content
	 * @param order native byte order of content
	 * @param baseOffset base offset from source to this buffer
	 * @param capacity total size of the source (from baseOffset)
	 */
	public StreamBufferBase(ByteOrder order, long baseOffset, long capacity) {
		this.order = order;
		this.baseOffset = baseOffset;
		this.position = 0;
		this.sourceCapacity = capacity;
		
		this.buffer = new byte[BUFFER_SIZE];
		this.sourceOffset = 0;
		this.sourceLimit = 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getClass().getSimpleName() +  " pos="+position() + " of "+ capacity() + " base="+ baseOffset; //$NON-NLS-N$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/**
	 * Fetch a page of content from the buffer.
	 * @param buffer the buffer
	 * @param sourceOffset absolute offset in original content
	 * @throws BufferUnderflowException
	 */
	protected abstract void fetchPage(byte[] buffer, long sourceOffset, int count);
	
	protected abstract IStreamBuffer createSubBuffer(long offset, long size);
	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#wrapSubsection(int)
	 */
	public IStreamBuffer wrapSubsection(long size) {
		long availableSize = capacity() - position();
		if (availableSize < size)
			size = availableSize;
		return createSubBuffer(position() + baseOffset, size);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#capacity()
	 */
	public long capacity() {
		return sourceCapacity;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#hasRemaining()
	 */
	public boolean hasRemaining() {
		return position < sourceCapacity;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#remaining()
	 */
	public long remaining() {
		return sourceCapacity - position;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#position()
	 */
	public long position() {
		return position;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#position(int)
	 */
	public IStreamBuffer position(long newPosition) {
		if (newPosition < 0 || newPosition > sourceCapacity)
			throw new IllegalArgumentException(newPosition + " not in 0.."+ sourceCapacity);
		
		this.position = newPosition;
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#get(byte[], int, int)
	 */
	public IStreamBuffer get(byte[] dst, int offset, int length) {
		// read page-by-page if possible
		while (length > 0) {
			if (needFetch())
				refetch();
			
			int left = (int) Math.min(sourceLimit - position, length);
			if (left > 0) {
				System.arraycopy(buffer, (int) (position - sourceOffset), dst, (int) offset, left);
				offset += left;
				position += left;
				length -= left;
			} else {
				break;
			}
		}
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#get(byte[])
	 */
	public IStreamBuffer get(byte[] dst) {
		return get(dst, 0, dst.length);
	}

	/**
	 * Fill memory buffer from source
	 */
	protected void refetch() {
		long newSourceOffset = position - (position & buffer.length - 1);
		if (newSourceOffset < 0)
			throw new BufferUnderflowException();
		if (newSourceOffset >= sourceCapacity)
			throw new BufferUnderflowException();
		
		int toFetch = (int) Math.min(sourceCapacity - newSourceOffset, buffer.length);
		fetchPage(buffer, newSourceOffset + baseOffset, toFetch);
		sourceOffset = newSourceOffset;
		sourceLimit = sourceOffset + toFetch;
	}
	
	protected final boolean needFetch() {
		return (position < sourceOffset || position >= sourceLimit);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#get()
	 */
	public byte get() {
		if (needFetch()) 
			refetch();
		
		if (position < sourceCapacity)
			return buffer[(int)((position++) - sourceOffset)];
		else
			throw new BufferUnderflowException();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#getChar()
	 */
	public char getChar() {
		return (char) getShort();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#getShort()
	 */
	public short getShort() {
		int a = get() & 0xff;
		int b = get() & 0xff;
		if (order == ByteOrder.LITTLE_ENDIAN)
			return (short) (a | (b << 8));
		else
			return (short) (b | (a << 8));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#getInt()
	 */
	public int getInt() {
		int a = getShort() & 0xffff;
		int b = getShort() & 0xffff;
		if (order == ByteOrder.LITTLE_ENDIAN)
			return a | (b << 16);
		else
			return b | (a << 16);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.IStreamBuffer#getLong()
	 */
	public long getLong() {
		long a = getInt();
		long b = getInt();
		if (order == ByteOrder.LITTLE_ENDIAN)
			return a | (b << 32);
		else
			return b | (a << 32);
	}
	
	public ByteOrder getOrder() {
		return order;
	}
	public void setOrder(ByteOrder order) {
		this.order = order;
	}
}

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

package org.eclipse.cdt.debug.edc;

import java.nio.Buffer;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * This is an subset of the ByteBuffer interface used for read-only 
 * scanning of a buffer.  Its implementation is optimized for streaming access
 * (i.e. not random access) and for making cheap sub-buffers.
 */
public interface IStreamBuffer {
	/**
	 * @see ByteBuffer#capacity()
	 */
	public long capacity();

	/**
	 * @see ByteBuffer#hasRemaining()
	 */
	public boolean hasRemaining();

	/**
	 * @see Buffer#remaining()
	 */
	public long remaining();
	
    /**
     * @see Buffer#position()
     */
    public long position();

    /**
     * @see Buffer#position(int)
     */
    public IStreamBuffer position(long newPosition);
 
    /**
     * Skip ahead in the buffer.
     *
     * @param amount the number of bytes to skip ahead
     * @return this buffer
     * @since 2.0
     */
    public IStreamBuffer skip(long amount);

	 /**
	  * @see ByteBuffer#get()
	  * @throws BufferUnderflowException
     */
    public abstract byte get();
    
    /**
     * @see ByteBuffer#get(byte[], int, int)
     * @throws BufferUnderflowException
     */
    public IStreamBuffer get(byte[] dst, int offset, int length);

    /**
     * @see ByteBuffer#get(byte[])
     * @throws BufferUnderflowException
     */
    public IStreamBuffer get(byte[] dst);

    /**
     * @see ByteBuffer#getChar()
     * @throws BufferUnderflowException
     */
    public abstract char getChar();
    
    /**
     * @see ByteBuffer#getShort()
     * @throws BufferUnderflowException
     */
    public abstract short getShort();
    

    /**
     * @see ByteBuffer#getInt()
     * @throws BufferUnderflowException
     */
    public abstract int getInt();

    /**
     * @see ByteBuffer#getLong()
     * @throws BufferUnderflowException
     */
    public abstract long getLong();

    /**
     * Wrap a portion of the buffer.  This is a cheap operation whose
     * returned buffer maintains references to the receiver but has an
     * independent position.
     * @param size the size to wrap, starting from the current {@link #position()}
     */
    IStreamBuffer wrapSubsection(long size);

	
}

/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Nokia - split out interface
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.internal.symbols.elf;

import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Abstraction for the file reading used by the ERandomAccessFile and Elf classes.
 * <p>
 * This provides endian-aware streamed read access to a file (methods ending in 'E').  
 * The {@link #setEndian(boolean)} call must be invoked prior to calling them.
 * <p>
 * This interface permits clients to interleave calls to the big-endian {@link DataInput}
 * methods as well as the mixed-endian methods in this interface.  The endianness
 * may also be changed at will.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 7.0
 */
public interface IRandomReadAccessFile extends DataInput, Closeable {

	/**
	 * Set endianness of file content.
	 * @param le true for little-endian, false for big-endian.
	 */
	void setEndian(boolean le);

	/** 
	 * Set a base offset from which seek() and getFilePointer() measure,
	 * and seek there. 
	 */
	void setFileOffset(long offset) throws IOException;
	
	/** 
	 * Get the basis for seek operations
	 * @see #setFileOffset(long)
	 * @return offset
	 * @throws IOException
	 */
	long getFilePointer() throws IOException;
	
	/**
	 * Seek relative to the pointer set by {@link #setFileOffset(long)}. 
	 * @see RandomAccessFile#seek(long) 
	 */
	void seek(long pos) throws IOException;

	/** 
	 * Read 2 bytes and construct a short according to endianness. 
	 * @see DataInput#readShort() 
	 */
	short readShortE() throws IOException;

	/** 
	 * Read 4 bytes and construct an int according to endianness. 
	 * @see DataInput#readInt() 
	 */
	long readIntE() throws IOException;
	
	/** 
	 * Read 8 bytes and construct a long according to endianness. 
	 * @see DataInput#readLong() 
	 */
	long readLongE() throws IOException;

	/**
	 * Read content and swap the entire range as if it were one large
	 * integer, according to the endianness.
	 * <p>
	 * This assumes the incoming data is big-endian, so only swaps if
	 * {@link #setEndian(boolean)} was called with 'true'.
	 * @see DataInput#readFully(byte[]) 
	 */
	void readFullyE(byte[] bytes) throws IOException;

	/** @see RandomAccessFile#read(byte[], int, int) */
	int read(byte b[], int off, int len) throws IOException;

	/** 
	 * @see RandomAccessFile#read(byte[]) 
	 */
    int read(byte b[]) throws IOException;
	
    /**
     * @see RandomAccessFile#length() 
     */
    long length() throws IOException;

    /** 
     * Get a read-only buffer for the given range
     * @param offset absolute offset (<b>not</b> relative to {@link #setFileOffset(long)}).
     * @param size the size in bytes; may not extend beyond EOF. 
     */
    ByteBuffer createReadByteBuffer(long offset, long size) throws IOException;
}

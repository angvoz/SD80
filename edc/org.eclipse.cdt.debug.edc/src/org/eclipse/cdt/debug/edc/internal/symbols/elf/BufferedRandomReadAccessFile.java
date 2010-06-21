/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.internal.symbols.elf;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.eclipse.cdt.debug.edc.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.FileStreamBuffer;

/**
 * Buffering version of the {@link IRandomReadAccessFile} interface that uses the
 * {@link IStreamBuffer} implementation.
 */
public class BufferedRandomReadAccessFile implements
		IRandomReadAccessFile {

	/** source file */
	private RandomAccessFile file;
	
	/** endian-aware buffer */
	private FileStreamBuffer buffer;
	/** native Java (big endian) buffer */
	private FileStreamBuffer bigEndianBuffer;

	/** current basis pointer */
	private long filePointer;

	/**
	 * @param osString
	 * @throws IOException 
	 */
	public BufferedRandomReadAccessFile(String file, boolean isle) throws IOException {
		this.file = new RandomAccessFile(file, "r"); //$NON-NLS-1$
		// for ELF-aware reading
		this.buffer = new FileStreamBuffer(this.file,  isle ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		// for native DataInput APIs
		this.bigEndianBuffer = new FileStreamBuffer(this.file, ByteOrder.BIG_ENDIAN);
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readFully(byte[])
	 */
	public void readFully(byte[] b) throws IOException {
		try {
			bigEndianBuffer.get(b);
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readFully(byte[], int, int)
	 */
	public void readFully(byte[] b, int off, int len) throws IOException {
		try {
			bigEndianBuffer.get(b, off, len);
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#skipBytes(int)
	 */
	public int skipBytes(int n) throws IOException {
		long cap = bigEndianBuffer.capacity();
		long cur = bigEndianBuffer.position();
		long pos = Math.min(cap, cur + n);
		bigEndianBuffer.position(pos);
		buffer.position(pos);
		return (int) (pos - cur);
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readBoolean()
	 */
	public boolean readBoolean() throws IOException {
		try {
			return bigEndianBuffer.get() != 0;
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readByte()
	 */
	public byte readByte() throws IOException {
		try {
			return bigEndianBuffer.get();
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readUnsignedByte()
	 */
	public int readUnsignedByte() throws IOException {
		try {
			return bigEndianBuffer.get() & 0xff;
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readShort()
	 */
	public short readShort() throws IOException {
		try {
			return bigEndianBuffer.getShort();
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readUnsignedShort()
	 */
	public int readUnsignedShort() throws IOException {
		try {
			return bigEndianBuffer.getShort() & 0xffff;
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readChar()
	 */
	public char readChar() throws IOException {
		try {
			return bigEndianBuffer.getChar();
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readInt()
	 */
	public int readInt() throws IOException {
		try {
			return bigEndianBuffer.getInt();
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readLong()
	 */
	public long readLong() throws IOException {
		try {
			return bigEndianBuffer.getLong();
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readFloat()
	 */
	public float readFloat() throws IOException {
		try {
			return Float.intBitsToFloat(bigEndianBuffer.getInt());
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readDouble()
	 */
	public double readDouble() throws IOException {
		try {
			return Double.longBitsToDouble(bigEndianBuffer.getLong());
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readLine()
	 */
	public String readLine() throws IOException {
		try {
			StringBuilder sb = new StringBuilder();
			try {
				int b;
				while ((b = bigEndianBuffer.get()) != 0) {
					sb.append((char) (b & 0xff));
				}
			} catch (BufferUnderflowException e) {
				if (sb.length() == 0)
					return null;
			}
			return sb.toString();
		} finally {
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.DataInput#readUTF()
	 */
	public String readUTF() throws IOException {
		try {
			int consumed = 0;
			short length = bigEndianBuffer.getShort();
			consumed += 2;
			StringBuilder sb = new StringBuilder();
			while (length-- > 0) {
				// TODO
				int c = (bigEndianBuffer.get()) & 0xff;
				if (c >= 0x80)
					assert false;
				consumed++;
				sb.append(c);
			}
			return sb.toString();
		} finally {		
			buffer.position(bigEndianBuffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() throws IOException {
		file.close();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#setEndian(boolean)
	 */
	public void setEndian(boolean le) {
		buffer.setOrder(le ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		
		// keep bigEndianBuffer the same
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#setFileOffset(long)
	 */
	public void setFileOffset(long offset) throws IOException {
		this.filePointer = offset;
		try {
			buffer.position(offset);
		} catch (IllegalArgumentException e) {
			throw (IOException) (new IOException().initCause(e));
		} finally {
			bigEndianBuffer.position(offset);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#getFilePointer()
	 */
	public long getFilePointer() throws IOException {
		return filePointer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#seek(long)
	 */
	public void seek(long pos) throws IOException {
		try {
			buffer.position(pos + filePointer);
		} catch (IllegalArgumentException e) {
			throw (IOException) (new IOException().initCause(e));
		} finally {
			bigEndianBuffer.position(pos + filePointer);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#readShortE()
	 */
	public short readShortE() throws IOException {
		try {
			 return buffer.getShort();
		} finally {
			bigEndianBuffer.position(buffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#readIntE()
	 */
	public long readIntE() throws IOException {
		try {
			return buffer.getInt();
		} finally {
			bigEndianBuffer.position(buffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#readLongE()
	 */
	public long readLongE() throws IOException {
		try {
			return buffer.getLong();
		} finally {
			bigEndianBuffer.position(buffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#readFullyE(byte[])
	 */
	public void readFullyE(byte[] bytes) throws IOException {
		try {
			buffer.get(bytes);
			if (buffer.getOrder() != ByteOrder.BIG_ENDIAN) {
				for(int i=0; i < (bytes.length / 2); i++)
				{
					byte tmp = bytes[i];
					bytes[i] = bytes[bytes.length - i -1];
					bytes[bytes.length - i -1] = tmp; 
				}
			}
		} finally {
			bigEndianBuffer.position(buffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		try {
			long max = bigEndianBuffer.capacity();
			int toRead = (int) Math.min(max - (off + len), len);
			buffer.get(b, off, toRead);
			return toRead;
		} finally {
			bigEndianBuffer.position(buffer.position());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#length()
	 */
	public long length() throws IOException {
		return buffer.capacity();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.utils.IRandomReadAccessFile#createReadByteBuffer(long, long)
	 */
	public ByteBuffer createReadByteBuffer(long offset, long size)
			throws IOException {
		// we don't use this in EDC, so this is slower than wanted
		// TODO
		assert false;
		try {
			byte[] contents = new byte[(int) size];
			long cur = buffer.position();
			buffer.position(offset);
			buffer.get(contents);
			buffer.position(cur);
			return ByteBuffer.wrap(contents);
		} catch (IllegalArgumentException e) {
			throw (IOException) (new IOException().initCause(e));
		}
	}

}

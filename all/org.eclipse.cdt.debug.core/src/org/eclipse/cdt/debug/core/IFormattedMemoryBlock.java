/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;

/**
 * 
 * A contiguos segment of memory in an execution context represented 
 * as a table of values.
 * 
 * @since Jul 31, 2002
 */
public interface IFormattedMemoryBlock extends IMemoryBlock
{
	public static final int MEMORY_SIZE_BYTE = 1;
	public static final int MEMORY_SIZE_HALF_WORD = 2;
	public static final int MEMORY_SIZE_WORD = 4;
	public static final int MEMORY_SIZE_DOUBLE_WORD = 8;
	public static final int MEMORY_SIZE_FLOAT = 8;
	public static final int MEMORY_SIZE_DOUBLE_FLOAT = 16;

	public static final int MEMORY_FORMAT_HEX = 0;
	public static final int MEMORY_FORMAT_BINARY = 1;
	public static final int MEMORY_FORMAT_OCTAL = 2;
	public static final int MEMORY_FORMAT_SIGNED_DECIMAL = 3;
	public static final int MEMORY_FORMAT_UNSIGNED_DECIMAL = 4;

	public static final int MEMORY_BYTES_PER_ROW_4 = 4;
	public static final int MEMORY_BYTES_PER_ROW_8 = 8;
	public static final int MEMORY_BYTES_PER_ROW_16 = 16;
	public static final int MEMORY_BYTES_PER_ROW_32 = 32;
	public static final int MEMORY_BYTES_PER_ROW_64 = 64;
	public static final int MEMORY_BYTES_PER_ROW_128 = 128;

	/**
	 * Returns the address expression specified to obtain this memory block.
	 * 
	 * @return the address expression
	 */
	public String getAddressExpression();

	/**
	 * Returns the format of the memory words of this block.
	 * 
	 * @return The format of the memory words of this block
	 */
	int getFormat();
	
	/**
	 * Returns the size of each memory word in bytes.
	 * 
	 * @return the size of each memory word in bytes
	 */
	int getWordSize();
	
	/**
	 * Sets the size of memory unit.
	 * 
	 * @param size the size of memory unit
	 */
	void setWordSize( int size );
	
	/**
	 * Returns the number of rows in the output table.
	 * 
	 * @return the number of rows in the output table
	 */
	int getNumberOfRows();
	
	/**
	 * Returns the number of columns in the output table.
	 * 
	 * @return the number of columns in the output table
	 */
	int getNumberOfColumns();

	/**
	 * Returns whether each row should include an ASCII dump.
	 * 
	 * @return whether each row should include an ASCII dump
	 */
	boolean displayASCII();
	
	/**
	 * Returns the array of rows.
	 * 
	 * @return the array of rows
	 */
	IFormattedMemoryBlockRow[] getRows();

	char getPaddingCharacter();

	long nextRowAddress();
	
	long previousRowAddress();
	
	long nextPageAddress();
	
	long previousPageAddress();

	void reformat( int format,
				   int wordSize,
				   int numberOfRows,
				   int numberOfColumns ) throws DebugException;

	void reformat( int format,
				   int wordSize,
				   int numberOfRows,
				   int numberOfColumns,
				   char paddingChar ) throws DebugException;
	void dispose();

	Long[] getChangedAddresses();
	
	boolean isFrozen();
	
	void setFrozen( boolean frozen );
}

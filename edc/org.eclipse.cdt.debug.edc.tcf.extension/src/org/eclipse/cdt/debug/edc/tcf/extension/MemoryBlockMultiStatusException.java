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

package org.eclipse.cdt.debug.edc.tcf.extension;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.eclipse.tm.tcf.services.IMemory.ErrorOffset;

/**
 * This exception class provides the agent with a container that closely mirrors what will end up in a
 * <code>org.eclipse.tm.internal.tcf.services.remote.MemoryProxy.MemoryErrorReport</code>
 * containing a list of <code>MemoryProxy.Range</code> objects on the other side of a TCF channel.
 * <p>
 * To mimic <code>MemoryProxy.Range</code>, this object holds an <code>ArrayList</code>
 * of {@link PartialMemoryBlock} objects.
 * <p>
 * An exception is used because the memory block decorated with multiple status
 * values for internally segmented sections of the block is considered the exceptional
 * case, and may need to be thrown up the chain to get around conventional parameter
 * and return values.
 * 
 * @author kirk.beitz@nokia.com
 * @since 2.0
 */
public class MemoryBlockMultiStatusException extends AgentException {
	private static final long serialVersionUID = -5403275982691807261L;

	/**
	 * the list mirroring the list of 
	 * <code>
	 * org.eclipse.tm.internal.tcf.services.remote.MemoryProxy.Range
	 * </code>
	 * objects on the other side of the TCF channel.
	 */
	private final ArrayList<PartialMemoryBlock> pbList;

	/**
	 * the byte buffer of data representing the memory being decorated
	 * with multiple status values
	 */
	private final ByteBuffer buf;

	/**
	 * version of constructor allowing an already partially filled
	 * buffer to append the <code>PartialMemoryBlock</code> list from
	 * another <code>MemoryBlockMultiStatusException</code>.
	 * <p>
	 * Primary use case is expected to be a low-level memory read that
	 * returned fewer bytes than expected, resulting in only exception
	 * status for the end portion of the block of memory being read.
	 * 
	 * @param msg the original problem to associate with the whole exception
	 * @param block the block of memory to associate with the whole exception
	 * @param m exception from which to extract the additional PartialMemoryBlock list 
	 */
	public MemoryBlockMultiStatusException(String msg,
			ByteBuffer block, MemoryBlockMultiStatusException m) {
		super(msg);
		buf = block;
		pbList = (m != null) ? m.getPartialBLocks() : null;
	}

	/**
	 * version of constructor combining the information from two other
	 * MemoryBlockMultiStatusException exceptions into a single exception.
	 * <p>
	 * primary use case is expected to be a low-level memory read that
	 * fails and splits the read into smaller chunks to determine if a
	 * different status should decorate segments within the full block
	 * 
	 * @param msg the original problem to associate with thewhole exception
	 * @param fullBlock the block of memory to associate with the whole exception
	 * @param m1 the first of two exceptions to combine
	 * @param m2 the second of two exceptions to combine
	 */
	public MemoryBlockMultiStatusException(String msg, ByteBuffer fullBlock,
			MemoryBlockMultiStatusException m1, MemoryBlockMultiStatusException m2) {
		super(msg);
		buf = fullBlock;
		if (m1 != null) {
			pbList = m1.getPartialBLocks();
			if (m2 != null && m2.getPartialBLocks() != null) {
				pbList.addAll(m2.getPartialBLocks());
			}
		} else if (m2 != null) {
			pbList = m2.getPartialBLocks();			
		} else {
			pbList = null;
		}
	}

	/**
	 * seminal version of the constructor, containing a block of memory
	 * that could not be processed, and creating a single status to
	 * associate with the entire passed block.
	 * <p>
	 * primary use cases are expected to be a low-level memory read that
	 * fails in a way in which it is known that the entire block has a
	 * single status, often whereby the block is then to be combined with
	 * other <code>PartialMemoryBlocks</code> to create a
	 * <code>MemoryBlockMultiStatusException</code>
	 * that represents a bigger block containing the block passed to
	 * this constructor.
	 * @param block empty block (often expected to be a slice of a bigger block)
	 * @param addr target memory address location of the block
	 * @param size size of the block being described
	 */
	public MemoryBlockMultiStatusException(ByteBuffer block, int addr, int size) {
		super((String)null);
		buf = block;
		buf.put((byte)0xBE);
		pbList = new ArrayList<PartialMemoryBlock>(1);
		pbList.add(new PartialMemoryBlock(addr, size, ErrorOffset.BYTE_INVALID));
	}

	/**
	 * access this exception's list of descriptors
	 * that mirrors the <code>MemoryProxy.Range</code> created in 
	 * <code>
	 * org.eclipse.tm.internal.tcf.services.remote.MemoryProxy.MemoryErrorReport
	 * </code>
	 * from data returned from the TCF agent for a MemoryService#getMemory() call.
	 * 
	 * @return the list of block descriptors
	 */
	public ArrayList<PartialMemoryBlock> getPartialBLocks() {
		return pbList;
	}

	/**
	 * access this exception's buffer representing the memory data
	 * read (as well as sections that may contain unknown content
	 * for <code>PartialMemoryBlock</code> objects that have status
	 * but no associated memory data).
	 * @return the block of memory data held by this exception
	 */
	public ByteBuffer getBuf() {
		return buf;
	}
}

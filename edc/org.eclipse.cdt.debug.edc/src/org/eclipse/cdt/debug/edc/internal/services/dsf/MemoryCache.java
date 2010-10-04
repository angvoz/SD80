/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.debug.edc.MemoryUtils;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.EDCTrace;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.debug.edc.snapshot.ISnapshotContributor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.utils.Addr32Factory;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.cdt.utils.Addr64Factory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IMemory.DoneGetContext;
import org.eclipse.tm.tcf.services.IMemory.DoneMemory;
import org.eclipse.tm.tcf.services.IMemory.ErrorOffset;
import org.eclipse.tm.tcf.services.IMemory.MemoryContext;
import org.eclipse.tm.tcf.services.IMemory.MemoryError;
import org.eclipse.tm.tcf.util.TCFTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is adapted from 
 * org.eclipse.cdt.dsf.mi.service.MIMemory.MIMemoryCache 
 * 
 */
public class MemoryCache implements ISnapshotContributor {

	// Timeout waiting for TCF agent reply.
	final private int TIMEOUT = 6000; // milliseconds
	private int minimumBlockSize = 0;

	/**
	 * @param minimumBlockSize minimum size of memory block to cache.
	 */
	public MemoryCache(int minimumBlockSize) {
		this.minimumBlockSize = minimumBlockSize;
		
		// create the memory block cache
		memoryBlockList = new SortedMemoryBlockList();
		
		tcfMemoryContexts = new HashMap<String, MemoryContext>();
	}

	public void reset() {
		// clear the memory cache
		memoryBlockList.clear();
	}

    /**
	 *  This function walks the address-sorted memory block list to identify
     *  the 'missing' blocks (i.e. the holes) that need to be fetched on the target.
     * 
     *  The idea is fairly simple but an illustration could perhaps help.
     *  Assume the cache holds a number of cached memory blocks with gaps i.e.
     *  there is un-cached memory areas between blocks A, B and C:
     * 
     *        +---------+      +---------+      +---------+
     *        +    A    +      +    B    +      +    C    +
     *        +---------+      +---------+      +---------+
     *        :         :      :         :      :         :
     *   [a]  :         :  [b] :         :  [c] :         :  [d]
     *        :         :      :         :      :         :
     *   [e---+--]      :  [f--+---------+--]   :         :
     *   [g---+---------+------+---------+------+---------+----]
     *        :         :      :         :      :         :
     *        :   [h]   :      :   [i----+--]   :         :
     * 
     * 
     *  We have the following cases to consider.The requested block [a-i] either:
     * 
     *  [1] Fits entirely before A, in one of the gaps, or after C
     *      with no overlap and no contiguousness (e.g. [a], [b], [c] and [d])
     *      -> Add the requested block to the list of blocks to fetch
     * 
     *  [2] Starts before an existing block but overlaps part of it, possibly
     *      spilling in the gap following the cached block (e.g. [e], [f] and [g])
     *      -> Determine the length of the missing part (< count)
     *      -> Add a request to fill the gap before the existing block
     *      -> Update the requested block for the next iteration:
     *         - Start address to point just after the end of the cached block
     *         - Count reduced by cached block length (possibly becoming negative, e.g. [e])
     *      At this point, the updated requested block starts just beyond the cached block
     *      for the next iteration.
     * 
     *  [3] Starts at or into an existing block and overlaps part of it ([h] and [i])
     *      -> Update the requested block for the next iteration:
     *         - Start address to point just after the end of the cached block
     *         - Count reduced by length to end of cached block (possibly becoming negative, e.g. [h])
     *      At this point, the updated requested block starts just beyond the cached block
     *      for the next iteration.
     * 
     *  We iterate over the cached blocks list until there is no entry left or until
     *  the remaining requested block count is <= 0, meaning the result list contains
     *  only the sub-blocks needed to fill the gap(s), if any.
     * 
     *  (As is often the case, it takes much more typing to explain it than to just do it :-)
     *
     *  What is missing is a parameter that indicates the minimal block size that is worth fetching.
     *  This is target-specific and straight in the realm of the coalescing function... 
     *  
     * @param reqBlockStart The address of the requested block
     * @param count Its length
     * @return A list of the sub-blocks to fetch in order to fill enough gaps in the memory cache
     * to service the request
     */
	private LinkedList<MemoryBlock> getListOfMissingBlocks(IAddress reqBlockStart, int count) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { reqBlockStart.toHexAddressString(), count }); }
		LinkedList<MemoryBlock> list = new LinkedList<MemoryBlock>();

		ListIterator<MemoryBlock> it = memoryBlockList.listIterator();

		// Look for holes in the list of memory blocks
		while (it.hasNext() && count > 0) {
			MemoryBlock cachedBlock = it.next();
			IAddress cachedBlockStart = cachedBlock.fAddress;
			IAddress cachedBlockEnd = cachedBlock.fAddress.add(cachedBlock.fLength);

			// Case where we miss a block before the cached block
			if (reqBlockStart.distanceTo(cachedBlockStart).longValue() >= 0) {
				int length = (int) Math.min(reqBlockStart.distanceTo(cachedBlockStart).longValue(), count);
				// If both blocks start at the same location, no need to create
				// a new cached block
				if (length > 0) {
					IAddress blockAddress;
					if (reqBlockStart instanceof Addr64) {
						IAddressFactory f = new Addr64Factory();
						blockAddress = f.createAddress(reqBlockStart.getValue());
					} else {
						IAddressFactory f = new Addr32Factory();
						blockAddress = f.createAddress(reqBlockStart.getValue());
					}
					MemoryBlock newBlock = new MemoryBlock(blockAddress, length, new MemoryByte[0]);
					list.add(newBlock);
				}
				// Adjust request block start and length for the next iteration
				reqBlockStart = cachedBlockEnd;
				count -= length + cachedBlock.fLength;
			}

			// Case where the requested block starts somewhere in the cached
			// block
			else if (cachedBlockStart.distanceTo(reqBlockStart).longValue() > 0
					&& reqBlockStart.distanceTo(cachedBlockEnd).longValue() >= 0) {
				// Start of the requested block already in cache
				// Adjust request block start and length for the next iteration
				count -= reqBlockStart.distanceTo(cachedBlockEnd).longValue();
				reqBlockStart = cachedBlockEnd;
			}
		}

		// Case where we miss a block at the end of the cache
		if (count > 0) {
			IAddress blockAddress;
			if (reqBlockStart instanceof Addr64) {
				IAddressFactory f = new Addr64Factory();
				blockAddress = f.createAddress(reqBlockStart.getValue());
			} else {
				IAddressFactory f = new Addr32Factory();
				blockAddress = f.createAddress(reqBlockStart.getValue());
			}
			MemoryBlock newBlock = new MemoryBlock(blockAddress, count, new MemoryByte[0]);
			list.add(newBlock);
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(list); }
		return list;
	}

	/**
	 * This function walks the address-sorted memory block list to get the
	 * cached memory bytes (possibly from multiple contiguous blocks). This
	 * function is called *after* the missing blocks have been read from the
	 * back end i.e. the requested memory is all cached.
	 * 
	 * Again, this is fairly simple. As we loop over the address-ordered list,
	 * There are really only 2 cases:
	 * 
	 * [1] The requested block fits entirely in the cached block ([a] or [b])
	 * [2] The requested block starts in a cached block and ends in the
	 * following (contiguous) one ([c]) in which case it is treated as 2
	 * contiguous requests ([c'] and [c"])
	 * 
	 * +--------------+--------------+ + A + B + +--------------+--------------+
	 * : [a----] : [b-----] : : : : : [c-----+------] : : [c'---]+[c"---] :
	 * 
	 * @param reqBlockStart
	 *            The address of the requested block
	 * @param count
	 *            Its length
	 * @return The cached memory content
	 */
	private MemoryByte[] getMemoryBlockFromCache(IAddress reqBlockStart, int count) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { reqBlockStart.toHexAddressString(), count }); }

		MemoryByte[] resultBlock = new MemoryByte[count];

		IAddress reqBlockEnd = reqBlockStart.add(count);
		ListIterator<MemoryBlock> iter = memoryBlockList.listIterator();

		while (iter.hasNext()) {
			MemoryBlock cachedBlock = iter.next();
			IAddress cachedBlockStart = cachedBlock.fAddress;
			IAddress cachedBlockEnd = cachedBlock.fAddress.add(cachedBlock.fLength);

			// Case where the cached block overlaps completely the requested
			// memory block
			if (cachedBlockStart.distanceTo(reqBlockStart).longValue() >= 0
					&& reqBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0) {
				int pos = (int) cachedBlockStart.distanceTo(reqBlockStart).longValue();
				System.arraycopy(cachedBlock.fBlock, pos, resultBlock, 0, count);
			}

			// Case where the beginning of the cached block is within the
			// requested memory block
			else if (reqBlockStart.distanceTo(cachedBlockStart).longValue() >= 0
					&& cachedBlockStart.distanceTo(reqBlockEnd).longValue() > 0) {
				int pos = (int) reqBlockStart.distanceTo(cachedBlockStart).longValue();
				int length = (int) Math.min(cachedBlock.fLength, count - pos);
				System.arraycopy(cachedBlock.fBlock, 0, resultBlock, pos, length);
			}

			// Case where the end of the cached block is within the requested
			// memory block
			else if (cachedBlockStart.distanceTo(reqBlockStart).longValue() >= 0
					&& reqBlockStart.distanceTo(cachedBlockEnd).longValue() > 0) {
				int pos = (int) cachedBlockStart.distanceTo(reqBlockStart).longValue();
				int length = (int) Math.min(cachedBlock.fLength - pos, count);
				System.arraycopy(cachedBlock.fBlock, pos, resultBlock, 0, length);
			}
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(resultBlock); }
		return resultBlock;
	}

	/**
	 * This function walks the address-sorted memory block list and updates the
	 * content with the actual memory just read from the target.
	 * 
	 * @param modBlockStart
	 * @param count
	 * @param modBlock
	 */
	private void updateMemoryCache(IAddress modBlockStart, int count, MemoryByte[] modBlock) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { modBlockStart.toHexAddressString(), count }); }
		IAddress modBlockEnd = modBlockStart.add(count);
		ListIterator<MemoryBlock> iter = memoryBlockList.listIterator();

		while (iter.hasNext()) {
			MemoryBlock cachedBlock = iter.next();
			IAddress cachedBlockStart = cachedBlock.fAddress;
			IAddress cachedBlockEnd = cachedBlock.fAddress.add(cachedBlock.fLength);

			// For now, we only bother to update bytes already cached.
			// Note: In a better implementation (v1.1), we would augment
			// the cache with the missing memory blocks since we went
			// through the pains of reading them in the first place.
			// (this is left as an exercise to the reader :-)

			// Case where the modified block is completely included in the
			// cached block
			if (cachedBlockStart.distanceTo(modBlockStart).longValue() >= 0
					&& modBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0) {
				int pos = (int) cachedBlockStart.distanceTo(modBlockStart).longValue();
				System.arraycopy(modBlock, 0, cachedBlock.fBlock, pos, count);
			}

			// Case where the beginning of the modified block is within the
			// cached block
			else if (cachedBlockStart.distanceTo(modBlockStart).longValue() >= 0
					&& modBlockStart.distanceTo(cachedBlockEnd).longValue() > 0) {
				int pos = (int) cachedBlockStart.distanceTo(modBlockStart).longValue();
				int length = (int) cachedBlockStart.distanceTo(modBlockEnd).longValue();
				System.arraycopy(modBlock, 0, cachedBlock.fBlock, pos, length);
			}

			// Case where the end of the modified block is within the cached
			// block
			else if (cachedBlockStart.distanceTo(modBlockEnd).longValue() > 0
					&& modBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0) {
				int pos = (int) modBlockStart.distanceTo(cachedBlockStart).longValue();
				int length = (int) cachedBlockStart.distanceTo(modBlockEnd).longValue();
				System.arraycopy(modBlock, pos, cachedBlock.fBlock, 0, length);
			}
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
		return;
	}

	/**
	 * This function iterates through missing blocks (blocks not currently
	 * cached, but wanted) and reads from the target and creates new cached
	 * blocks.
	 * 
	 * @param tcfMemoryService
	 * @param context
	 * @param address
	 * @param word_size
	 * @param count
	 * @param drm
	 */
	public void getMemory(final IMemory tcfMemoryService, final IMemoryDMContext context, final IAddress address,
			final int word_size, final int count, final DataRequestMonitor<MemoryByte[]> drm) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { context, address.toHexAddressString(), word_size, count }); }

		// determine number of read requests to issue
		final LinkedList<MemoryBlock> missingBlocks = getListOfMissingBlocks(address, count);
		final int numberOfRequests = missingBlocks.size();

		if (numberOfRequests > 0 && tcfMemoryService == null) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, "Fail to read memory.")); //$NON-NLS-1$
			drm.done();
			return;
		}
		// System.out.printf("MemoryCache.getMemory address=%x count=%d numberOfRequests=%d\n",
		// address.getValue(), count, numberOfRequests);
		for (int i = 0; i < numberOfRequests; i++) {
			MemoryBlock block = missingBlocks.get(i);
			IAddress blockAddress = block.fAddress;
			int blockLength = (int) block.fLength;
			if (blockLength < minimumBlockSize)
				blockLength = minimumBlockSize;
			
			MemoryByte[] result;
			try {
				result = readBlock(tcfMemoryService, context, blockAddress, word_size, blockLength);
				MemoryBlock newBlock = new MemoryBlock(blockAddress, blockLength, result);
				memoryBlockList.add(newBlock);
			} catch (IOException e) {
				drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
						"Fail to read memory.", e.getCause())); //$NON-NLS-1$
				drm.done();
				return;
			}
		}
		drm.setData(getMemoryBlockFromCache(address, count));
		drm.done();
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	private MemoryContext getTCFMemoryContext(final IMemory tcfMemoryService, final String contextID) throws IOException {

		MemoryContext ret = tcfMemoryContexts.get(contextID);
		if (ret != null)
			return ret;
		
		final TCFTask<MemoryContext> tcfTask = new TCFTask<MemoryContext>(TIMEOUT) {

			public void run() {
				tcfMemoryService.getContext(contextID, new DoneGetContext() {

					public void doneGetContext(IToken token, Exception error, MemoryContext context) {
						if (error == null) {
							done(context);
						} else {
							error(error);
						}
					}
				});
			}
		};

		try {
			ret = tcfTask.getIO();
		} catch (IOException e) {
			throw e;
		}

		if (ret != null)
			tcfMemoryContexts.put(contextID, ret);
		
		return ret;
	}

	/**
	 * This function does the actual reading from the target.
	 * 
	 * @param tcfMemoryService
	 * @param context
	 * @param address
	 * @param word_size
	 * @param count
	 * @return
	 * @throws IOException
	 */
	private MemoryByte[] readBlock(final IMemory tcfMemoryService, final IMemoryDMContext context,
			final IAddress address, final int word_size, final int count) throws IOException {

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { context, address.toHexAddressString(), word_size, count }); }

		final MemoryContext tcfMC = getTCFMemoryContext(tcfMemoryService, ((IEDCDMContext)context).getID());
		
		MemoryByte[] result = null;

		final TCFTask<MemoryByte[]> tcfTask = new TCFTask<MemoryByte[]>(TIMEOUT) {

			public void run() {
				Number tcfAddress = address.getValue();
				final byte[] buffer = new byte[word_size * count];
				tcfMC.get(tcfAddress, word_size, buffer, 0, count * word_size, 0, new DoneMemory() {

					public void doneMemory(IToken token, MemoryError error) {
						if (error == null || !(error instanceof IMemory.ErrorOffset)) {
							MemoryByte[] res = new MemoryByte[buffer.length];
							for (int i = 0; i < buffer.length; i++) {
								res[i] = new MemoryByte(buffer[i]);
							}
							done(res);
						} else if (error instanceof IMemory.ErrorOffset) {
							IMemory.ErrorOffset errorOffset = (ErrorOffset) error;
							MemoryByte[] res = new MemoryByte[buffer.length];
							
							// TODO: figure actual endianness (MemoryByte.BIG_ENDIAN) flag; 
							// we leave out the flag here which defaults to little-endian
							for (int i = 0; i < buffer.length; i++) {
								byte flags = MemoryByte.ENDIANESS_KNOWN | MemoryByte.READABLE | MemoryByte.WRITABLE;
								
								int st = errorOffset.getStatus(i);
								if ((st & IMemory.ErrorOffset.BYTE_CANNOT_READ) != 0)
									flags &= ~MemoryByte.READABLE;
								if ((st & IMemory.ErrorOffset.BYTE_CANNOT_WRITE) != 0)
									flags &= ~MemoryByte.WRITABLE;
								if ((st & IMemory.ErrorOffset.BYTE_INVALID) != 0)
									flags &= ~(MemoryByte.READABLE + MemoryByte.WRITABLE);
								if ((st & IMemory.ErrorOffset.BYTE_UNKNOWN) != 0)
									flags = 0;
								
								res[i] = new MemoryByte(buffer[i], flags);
							}
							done(res);
						} else {
							error(error);
						}
					}
				});
			}
		};

		try {
			result = tcfTask.getIO();
		} catch (IOException e) {
			throw e;
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
		return result;
	}

	/**
	 * This function writes a block of memory and then re-reads and updates any
	 * cached blocks.
	 * 
	 * @param tcfMemoryService
	 * @param context
	 * @param address
	 * @param offset
	 * @param word_size
	 * @param count
	 * @param buffer
	 * @param rm
	 */
	public void setMemory(IMemory tcfMemoryService, IMemoryDMContext context, final IAddress address,
			final long offset, final int word_size, final int count, byte[] buffer, final RequestMonitor rm) {

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { context, address.toHexAddressString(), offset, word_size, count }); }

		try {
			writeBlock(tcfMemoryService, context, address, offset, word_size, count, buffer);
			if (blockIsCached(address.add(offset), word_size * count)) {
				MemoryByte[] update = readBlock(tcfMemoryService, context, address.add(offset), word_size, count);
				updateMemoryCache(address.add(offset), update.length, update);
			}
		} catch (IOException e) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Error Writing Memory", e)); //$NON-NLS-1$
		}
		rm.done();

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	/**
	 * This function figures out if a block of memory is already cached.
	 * 
	 * @param modBlockStart
	 * @param count
	 * @return
	 */
	private boolean blockIsCached(IAddress modBlockStart, int count) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { modBlockStart.toHexAddressString(), count }); }
		boolean cacheFound = false;

		IAddress modBlockEnd = modBlockStart.add(count);
		ListIterator<MemoryBlock> iter = memoryBlockList.listIterator();

		while (iter.hasNext()) {
			MemoryBlock cachedBlock = iter.next();
			IAddress cachedBlockStart = cachedBlock.fAddress;
			IAddress cachedBlockEnd = cachedBlock.fAddress.add(cachedBlock.fLength);

			// For now, we only bother to update bytes already cached.
			// Note: In a better implementation (v1.1), we would augment
			// the cache with the missing memory blocks since we went
			// through the pains of reading them in the first place.
			// (this is left as an exercise to the reader :-)

			// Case where the modified block is completely included in the
			// cached block
			if (cachedBlockStart.distanceTo(modBlockStart).longValue() >= 0
					&& modBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0) {
				cacheFound = true;
			}

			// Case where the beginning of the modified block is within the
			// cached block
			else if (cachedBlockStart.distanceTo(modBlockStart).longValue() >= 0
					&& modBlockStart.distanceTo(cachedBlockEnd).longValue() > 0) {
				cacheFound = true;
			}

			// Case where the end of the modified block is within the cached
			// block
			else if (cachedBlockStart.distanceTo(modBlockEnd).longValue() > 0
					&& modBlockEnd.distanceTo(cachedBlockEnd).longValue() >= 0) {
				cacheFound = true;
			}
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(cacheFound); }
		return cacheFound;
	}

	/**
	 * This function writes a memory block to the target.
	 * 
	 * @param tcfMemoryService
	 * @param context
	 * @param address
	 * @param offset
	 * @param word_size
	 * @param count
	 * @param buffer
	 * @throws IOException
	 */
	private void writeBlock(final IMemory tcfMemoryService, final IMemoryDMContext context, final IAddress address,
			final long offset, final int word_size, final int count, final byte[] buffer) throws IOException {

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { context, address.toHexAddressString(), offset, word_size, count }); }

		final TCFTask<MemoryByte[]> tcfTask = new TCFTask<MemoryByte[]>(TIMEOUT) {

			public void run() {
				final TCFTask<MemoryByte[]> task = this;
				String memoryContextID = ((IEDCDMContext) context).getID();
				tcfMemoryService.getContext(memoryContextID, new DoneGetContext() {

					public void doneGetContext(IToken token, Exception error, MemoryContext context) {
						if (error == null) {
							Number tcfAddress = address.add(offset).getValue();
							context.set(tcfAddress, word_size, buffer, 0, count * word_size, 0, new DoneMemory() {

								public void doneMemory(IToken token, MemoryError error) {
									if (error == null) {
										task.done(null);
									} else {
										task.error(error);
									}
								}
							});
						} else {
							task.error(error);
						}
					}
				});
			}
		};

		try {
			tcfTask.getIO();
		} catch (IOException e) {
			throw e;
		}

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
	}

	private final Map<String, MemoryContext>	tcfMemoryContexts;
	private final SortedMemoryBlockList memoryBlockList;

	private class MemoryBlock {
		public MemoryBlock(IAddress fAddress, long fLength, MemoryByte[] fBlock) {
			super();
			this.fAddress = fAddress;
			this.fLength = fLength;
			this.fBlock = fBlock;
		}

		public IAddress fAddress;
		public long fLength;
		public MemoryByte[] fBlock;
	}

	@SuppressWarnings("serial")
	private class SortedMemoryBlockList extends LinkedList<MemoryBlock> {
		public SortedMemoryBlockList() {
			super();
		}

		// Insert the block in the sorted linked list and merge contiguous
		// blocks if necessary
		@Override
		public boolean add(MemoryBlock block) {
			if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { block.fAddress.toHexAddressString(), block.fLength }); }

			// If the list is empty, just store the block
			if (isEmpty()) {
				addFirst(block);
				return true;
			}

			// Insert the block at the correct location and then
			// merge the blocks if possible
			ListIterator<MemoryBlock> it = listIterator();
			while (it.hasNext()) {
				int index = it.nextIndex();
				MemoryBlock item = it.next();
				if (block.fAddress.compareTo(item.fAddress) < 0) {
					add(index, block);
					compact(index);
					return true;
				}
			}

			// Put at the end of the list and merge if necessary
			addLast(block);
			compact(size() - 1);

			if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
			return true;
		}

		// Merge this block with its contiguous neighbors (if any)
		// Note: Merge is not performed if resulting block size would exceed
		// MAXINT
		private void compact(int index) {
			if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { index }); }

			MemoryBlock newBlock = get(index);

			// Case where the block is to be merged with the previous block
			if (index > 0) {
				MemoryBlock prevBlock = get(index - 1);
				IAddress endOfPreviousBlock = prevBlock.fAddress.add(prevBlock.fLength);
				if (endOfPreviousBlock.distanceTo(newBlock.fAddress).longValue() == 0) {
					long newLength = prevBlock.fLength + newBlock.fLength;
					if (newLength <= Integer.MAX_VALUE) {
						MemoryByte[] block = new MemoryByte[(int) newLength];
						System.arraycopy(prevBlock.fBlock, 0, block, 0, (int) prevBlock.fLength);
						System.arraycopy(newBlock.fBlock, 0, block, (int) prevBlock.fLength, (int) newBlock.fLength);
						newBlock = new MemoryBlock(prevBlock.fAddress, newLength, block);
						remove(index);
						index -= 1;
						set(index, newBlock);
					}
				}
			}

			// Case where the block is to be merged with the following block
			int lastIndex = size() - 1;
			if (index < lastIndex) {
				MemoryBlock nextBlock = get(index + 1);
				IAddress endOfNewBlock = newBlock.fAddress.add(newBlock.fLength);
				if (endOfNewBlock.distanceTo(nextBlock.fAddress).longValue() == 0) {
					long newLength = newBlock.fLength + nextBlock.fLength;
					if (newLength <= Integer.MAX_VALUE) {
						MemoryByte[] block = new MemoryByte[(int) newLength];
						System.arraycopy(newBlock.fBlock, 0, block, 0, (int) newBlock.fLength);
						System.arraycopy(nextBlock.fBlock, 0, block, (int) newBlock.fLength, (int) nextBlock.fLength);
						newBlock = new MemoryBlock(newBlock.fAddress, newLength, block);
						set(index, newBlock);
						remove(index + 1);
					}
				}
			}

			if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
		}
	}

	/**
	 * Refreshes cache blocks when memory has been changed through an event.
	 * 
	 * @param tcfMemoryService
	 * @param context
	 * @param address
	 * @param offset
	 * @param word_size
	 * @param count
	 * @param rm
	 * @return true = cache has been modified
	 */
	public boolean refreshMemory(IMemory tcfMemoryService, IMemoryDMContext context, IAddress address, int offset,
			int word_size, int count, RequestMonitor rm) {
		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceEntry(new Object[] { context, address.toHexAddressString(), offset, count }); }

		boolean modified = false;
		// Check if we already cache part of this memory area (which means it
		// is used by a memory service client that will have to be updated)
		LinkedList<MemoryBlock> list = getListOfMissingBlocks(address, count);
		int sizeToRead = 0;
		for (MemoryBlock block : list) {
			sizeToRead += block.fLength;
		}

		// If none of the requested memory is in cache, just get out
		if (sizeToRead == count) {
			rm.done();
			return false;
		}

		try {
			MemoryByte[] newBlock = readBlock(tcfMemoryService, context, address, word_size, count);
			MemoryByte[] oldBlock = getMemoryBlockFromCache(address, count);
			boolean blocksDiffer = false;
			for (int i = 0; i < oldBlock.length; i++) {
				if (oldBlock[i].getValue() != newBlock[i].getValue()) {
					blocksDiffer = true;
					break;
				}
			}
			if (blocksDiffer) {
				updateMemoryCache(address, count, newBlock);
				modified = true;
			}
		} catch (IOException e) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Error Writing Memory", e)); //$NON-NLS-1$
		}
		rm.done();

		if (EDCTrace.MEMORY_TRACE_ON) { EDCTrace.traceExit(); }
		return modified;
	}

	private static final String MEMORY_CACHE = "memory_cache";
	private static final String MEMORY_BLOCK = "memory_block";
	private static final String ATTR_ADDRESS = "address";
	private static final String ATTR_LENGTH = "length";
	private static final String ATTR_VALUE = "value";

	public void loadSnapshot(Element element) throws Exception {
		reset();
		NodeList blockElements = element.getElementsByTagName(MEMORY_BLOCK);

		int numBlocks = blockElements.getLength();
		for (int i = 0; i < numBlocks; i++) {
			Element blockElement = (Element) blockElements.item(i);
			String blockAddress = blockElement.getAttribute(ATTR_ADDRESS);
			String blockLength = blockElement.getAttribute(ATTR_LENGTH);
			String blockValue = blockElement.getAttribute(ATTR_VALUE);
			MemoryByte[] blockBytes = MemoryUtils.convertHexStringToMemoryBytes(blockValue, blockValue.length() / 2, 2);
			MemoryBlock newBlock = new MemoryBlock(new Addr64(blockAddress), Long.parseLong(blockLength), blockBytes);
			memoryBlockList.add(newBlock);
		}
	}

	public Element takeShapshot(IAlbum album, Document document, IProgressMonitor monitor) {
		Element memoryCacheElement = document.createElement(MEMORY_CACHE);
		ListIterator<MemoryBlock> iter = memoryBlockList.listIterator();

		while (iter.hasNext()) {
			MemoryBlock block = iter.next();
			Element blockElement = document.createElement(MEMORY_BLOCK);
			blockElement.setAttribute(ATTR_ADDRESS, block.fAddress.toHexAddressString());
			blockElement.setAttribute(ATTR_LENGTH, Long.toString(block.fLength));
			blockElement.setAttribute(ATTR_VALUE, MemoryUtils.convertMemoryBytesToHexString(block.fBlock));
			memoryCacheElement.appendChild(blockElement);
		}
		return memoryCacheElement;
	}
}

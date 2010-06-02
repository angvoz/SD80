package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.cdt.debug.edc.symbols.IRangeList;

/**
 * This is a range of non-contiguous addresses 
 */
public class RangeList implements IRangeList {
	/** consecutive start, end entries */
	private List<Long> ranges = new ArrayList<Long>(); 
	private long low = Long.MAX_VALUE, high = Long.MIN_VALUE;
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[0x" + Long.toHexString(getLowAddress()) + " to 0x" + Long.toHexString(getHighAddress()) + ")";
	}
	
	public void addRange(long start, long end) {
		if (!ranges.isEmpty()) {
			if (ranges.get(ranges.size() - 1) == start) {
				ranges.set(ranges.size() - 1, end);
				if (end > high)
					high = end;
				return;
			}
		}
		ranges.add(start);
		ranges.add(end);
		
		// track these dynamically since the list is not guaranteed to be ordered
		if (start < low)
			low = start;
		if (end > high)
			high = end;
	}
	
	public long getLowAddress() {
		if (ranges.isEmpty())
			return 0;
		else 
			return low;
	}

	public long getHighAddress() {
		if (ranges.isEmpty())
			return 0;
		else 
			return high;
	}
	
	/** Get an iterator over the ranges.  Fetch two entries at a time,
	 * which describe a [start, end) range. */
	public Iterator<Entry> iterator() {
		return new Iterator<Entry>() {
			int index = 0;

			public boolean hasNext() {
				return index < ranges.size();
			}

			public Entry next() {
				if (index >= ranges.size())
					throw new NoSuchElementException();
				index += 2;
				return new IRangeList.Entry(ranges.get(index - 2), ranges.get(index - 1));
			}

			public void remove() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

	/**
	 * Fixup a range list by adding a new low range
	 * @param addr
	 */
	public void addLowRange(long addr) {
		if (low > addr) {
			low = addr;
			if (!ranges.isEmpty()) {
				ranges.set(0, low);
			}
		}
	}

	/**
	 * Fixup a range list by adding a new high range
	 * @param addr
	 */
	public void addHighRange(long addr) {
		if (high < addr) {
			high = addr;
			if (!ranges.isEmpty()) {
				ranges.set(ranges.size() - 1, addr);
			}
		}
		
	}
	
	/**
	 * Tell if an address is in a range.
	 * @param addr
	 */
	public boolean isInRange(long addr) {
		for (Entry entry : this) {
			if (entry.low >= addr && addr < entry.high)
				return true;
		}
		return false;
	}
}
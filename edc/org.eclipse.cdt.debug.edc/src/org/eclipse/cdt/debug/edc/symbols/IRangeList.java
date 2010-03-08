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

package org.eclipse.cdt.debug.edc.symbols;

import java.util.Iterator;

/**
 * This describes a range of non-contiguous addresses. 
 * 
 * Note: this uses long instead of IAddress for efficiency.
 */
public interface IRangeList extends Iterable<IRangeList.Entry> {

	static class Entry implements Comparable<Entry> {
		public Entry(long low, long high) {
			this.low = low;
			this.high = high;
		}

		final public long low, high;
		
		@Override
		public String toString() {
			return "[" + Long.toHexString(low) + "-" + Long.toHexString(high) + ")";
		}
		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Entry o) {
			if (low < o.low)
				return -1;
			if (high >= o.high)
				return 1;
			return 0;
		}
	}
	
	/** Get absolute low address for the range */
	long getLowAddress();
	/** Get absolute high address for the range */
	long getHighAddress();
	
	/** Iterate over every portion of the range */
	Iterator<IRangeList.Entry> iterator();
	
	/**
	 * Tell if an address is in the range.
	 * @param addr
	 */
	boolean isInRange(long addr);
}

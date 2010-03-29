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
			if (low > o.low && high >= o.high)
				return 1;
			if (low == o.low && high > o.high)
				return 1;
			return 0;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (high ^ (high >>> 32));
			result = prime * result + (int) (low ^ (low >>> 32));
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Entry other = (Entry) obj;
			if (high != other.high)
				return false;
			if (low != other.low)
				return false;
			return true;
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

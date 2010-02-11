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
package org.eclipse.cdt.debug.edc.internal.symbols.files;

/**
 * Information about the range of content for a section.  Used as a key in {@link SectionMapper}.
 */
public class SectionInfo {

	public long fileOffset;
	public long sectionSize;

	public SectionInfo(long fileOffset, long sectionSize) {
		this.fileOffset = fileOffset;
		this.sectionSize = sectionSize;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "section from " + Long.toHexString(fileOffset) + " - " + Long.toHexString(fileOffset + sectionSize); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (fileOffset ^ (fileOffset >>> 32));
		result = prime * result + (int) (sectionSize ^ (sectionSize >>> 32));
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
		SectionInfo other = (SectionInfo) obj;
		if (fileOffset != other.fileOffset)
			return false;
		if (sectionSize != other.sectionSize)
			return false;
		return true;
	}
	
	
}

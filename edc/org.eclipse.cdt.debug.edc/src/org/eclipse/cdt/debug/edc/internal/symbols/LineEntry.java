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
package org.eclipse.cdt.debug.edc.internal.symbols;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.symbols.ILineEntry;
import org.eclipse.core.runtime.IPath;

public class LineEntry implements ILineEntry {

	protected IPath filePath;
	protected int lineNumber;
	protected int columnNumber;
	protected IAddress lowAddress;
	protected IAddress highAddress;

	public LineEntry(IPath filePath, int lineNumber, int columnNumber, IAddress lowAddress, IAddress highAddress) {
		this.filePath = filePath;
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.lowAddress = lowAddress;
		this.highAddress = highAddress;
	}

	public IPath getFilePath() {
		return filePath;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getColumnNumber() {
		return columnNumber;
	}

	public IAddress getLowAddress() {
		return lowAddress;
	}

	public IAddress getHighAddress() {
		return highAddress;
	}

	public void setHighAddress(IAddress highAddress) {
		this.highAddress = highAddress;
	}

	public int compareTo(Object o) {
		if (o instanceof ILineEntry) {
			// some entries have low==high
			int diff = lowAddress.compareTo(((ILineEntry) o).getLowAddress());
			if (diff != 0)
				return diff;
			if (highAddress != null && ((ILineEntry) o).getHighAddress() != null)
				return highAddress.compareTo(((ILineEntry) o).getHighAddress());
			return 0;
		} else if (o instanceof IAddress) {
			return lowAddress.compareTo(o);
		}

		return 0;
	}

	@Override
	public String toString() {
		return "LineEntry [lowAddress="
				+ (lowAddress != null ? lowAddress.toHexAddressString() : "null")
				+ ", highAddress="
				+ (highAddress != null ? highAddress.toHexAddressString() : "null")
				+ ((filePath != null) ? ", path=" + filePath.toOSString() + ", " : ", ")
				+ "line=" + lineNumber + ", column=" + columnNumber
				+ "]" ; //$NON-NLS-1$
	}
}

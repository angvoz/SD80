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
		StringBuilder builder = new StringBuilder();
		builder.append("LineEntry ["); //$NON-NLS-1$
		builder.append("lowAddress="); //$NON-NLS-1$
		builder.append(lowAddress.toHexAddressString());
		builder.append(", highAddress="); //$NON-NLS-1$
		builder.append(highAddress.toHexAddressString());
		builder.append(", "); //$NON-NLS-1$
		if (filePath != null) {
			builder.append("path="); //$NON-NLS-1$
			builder.append(filePath.toOSString());
			builder.append(", "); //$NON-NLS-1$
		}
		builder.append("line="); //$NON-NLS-1$
		builder.append(lineNumber);
		builder.append(", "); //$NON-NLS-1$
		builder.append("column="); //$NON-NLS-1$
		builder.append(columnNumber);
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}

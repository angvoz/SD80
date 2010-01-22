/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.util.Arrays;

public class LocationEntry {

	public final long highPC;
	public final long lowPC;
	public final byte[] bytes;

	public LocationEntry(long lowPC, long highPC, byte[] bytes) {
		this.lowPC = lowPC;
		this.highPC = highPC;
		this.bytes = bytes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LocationEntry ["); //$NON-NLS-1$
		if (bytes != null) {
			builder.append("bytes="); //$NON-NLS-1$
			builder.append(Arrays.toString(bytes));
			builder.append(", "); //$NON-NLS-1$
		}
		builder.append("highPC="); //$NON-NLS-1$
		builder.append(highPC);
		builder.append(", lowPC="); //$NON-NLS-1$
		builder.append(lowPC);
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}
}

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
import org.eclipse.cdt.debug.edc.symbols.ISymbol;

public class Symbol implements ISymbol {

	protected String name;
	protected IAddress address;
	protected long size;

	public Symbol(String name, IAddress address, long size) {
		this.name = name;
		this.address = address;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public IAddress getAddress() {
		return address;
	}

	public long getSize() {
		return size;
	}

	public int compareTo(Object o) {
		if (o instanceof Symbol) {
			return address.compareTo(((Symbol) o).address);
		} else if (o instanceof IAddress) {
			return address.compareTo(o);
		}
		return 0;
	}
	
	@Override
	public String toString() {
		return "name=" + name + //$NON-NLS-1$
				", address=0x" + Long.toHexString(address.getValue().longValue()) + //$NON-NLS-1$
				", size=" + size; //$NON-NLS-1$
	}

}

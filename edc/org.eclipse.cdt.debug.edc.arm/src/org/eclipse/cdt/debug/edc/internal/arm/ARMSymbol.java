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
package org.eclipse.cdt.debug.edc.internal.arm;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.arm.IARMSymbol;
import org.eclipse.cdt.utils.Addr32;

public class ARMSymbol implements IARMSymbol {

	protected String name;
	protected IAddress address;
	protected long size;
	protected boolean isThumb;

	public ARMSymbol(String name, IAddress address, long size) {
		this.name = name;
		this.size = size;
		isThumb = address.getValue().testBit(0);
		this.address = new Addr32(address.getValue().clearBit(0).longValue());
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
		if (o instanceof ARMSymbol) {
			return address.compareTo(((ARMSymbol) o).address);
		} else if (o instanceof IAddress) {
			return address.compareTo(o);
		}
		return 0;
	}
	
	public boolean isThumbAddress() {
		return isThumb;
	}

	@Override
	public String toString() {
		return "name=" + name + //$NON-NLS-1$
				", address=0x" + Long.toHexString(address.getValue().longValue()) + //$NON-NLS-1$
				", size=" + size + ", thumb=" + isThumb; //$NON-NLS-1$
	}

}

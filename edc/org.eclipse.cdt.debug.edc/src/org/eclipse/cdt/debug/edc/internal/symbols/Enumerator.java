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

import org.eclipse.cdt.debug.edc.symbols.IEnumerator;

public class Enumerator implements IEnumerator {

	protected final String name;
	protected final Long value;

	public Enumerator(String name, long value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public long getValue() {
		return value;
	}

	@Override
	public String toString() {
		return "name = " + name + ", value = " + value; //$NON-NLS-1$ //$NON-NLS-2$
	}

}

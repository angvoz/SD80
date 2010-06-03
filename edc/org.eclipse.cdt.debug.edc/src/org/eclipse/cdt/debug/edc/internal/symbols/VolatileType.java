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

import java.util.Map;

import org.eclipse.cdt.debug.edc.symbols.IScope;

/**
 * Pseudo-type that represents the volatile qualifier
 */
public class VolatileType extends Type implements IQualifierType {

	public VolatileType(IScope scope, Map<Object, Object> properties) {
		super("volatile", scope, 0, properties); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Type#getByteSize()
	 */
	@Override
	public int getByteSize() {
		return updateByteSizeFromSubType();
	}
}

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
 * Pseudo-type that represents the const qualifier
 */
public class ConstType extends Type implements IConstType {

	public ConstType(IScope scope, Map<Object, Object> properties) {
		super("const", scope, 0, properties); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.Type#getByteSize()
	 */
	@Override
	public int getByteSize() {
		return updateByteSizeFromSubType();
	}

}

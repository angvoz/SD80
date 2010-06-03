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

public class PointerType extends MayBeQualifiedType implements IPointerType {

	public PointerType(String name, IScope scope, int byteSize, Map<Object, Object> properties) {
		super(name, scope, byteSize, properties);
	}

	// create an internal pointer for expression evaluation
	public PointerType() {
		super("", null, 0, null); //$NON-NLS-1$
	}

}

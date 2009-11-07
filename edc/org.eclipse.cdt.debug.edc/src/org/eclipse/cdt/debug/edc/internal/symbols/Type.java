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
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.util.Map;

public class Type implements IType {

	protected String name;
	protected final IScope scope;
	protected int byteSize;
	protected IType type; // subtype, if any
	protected Map<Object, Object> properties;

	public Type(String name, IScope scope, int byteSize, Map<Object, Object> properties) {
		this.name = name;
		this.scope = scope;
		this.byteSize = byteSize;
		this.properties = properties;
	}

	public String getName() {
		return name;
	}

	public IScope getScope() {
		return scope;
	}

	public int getByteSize() {
		return byteSize;
	}

	public Map<Object, Object> getProperties() {
		return properties;
	}

	public IType getType() {
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}
}

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
import org.eclipse.cdt.debug.edc.symbols.IType;


public class Type implements IType {

	/** This property key maps to an {@link IForwardTypeReference} object */
	public static final String TYPE_REFERENCE = "type_reference"; //$NON-NLS-1$
	
	protected String name;
	protected IScope scope;
	protected int byteSize;
	protected IType type; // subtype, if any, maybe IForwardTypeReference
	protected Map<Object, Object> properties;	// may be null

	public Type(String name, IScope scope, int byteSize, Map<Object, Object> properties) {
		this.name = name;
		this.scope = scope;
		this.byteSize = byteSize;
		this.properties = properties;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IType#dispose()
	 */
	public void dispose() {
		properties = null;
		scope = null;
		type = null;
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
		if (type == null && properties != null) {
			type = (IType) properties.get(TYPE_REFERENCE);
		}
		if (type instanceof IForwardTypeReference) {
			type = ((IForwardTypeReference) type).getReferencedType();
		}
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	public void setScope(IScope scope) {
		this.scope = scope;
	}
	
	protected int updateByteSizeFromSubType() {
		if (byteSize == 0) {
			IType theType = getType();
			if (theType != null)
				byteSize = theType.getByteSize();
		}
		return byteSize;
	}

}

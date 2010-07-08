/*******************************************************************************

 * Copyright (c) 2010 Nokia and others.
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
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;

public class TemplateParamType implements ITemplateParam {

	final String name;
	IType type;
	
	public TemplateParamType(String name, IType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		if (name == null || name.length() == 0) {
			getType();
			return TypeUtils.getFullTypeName(type);
		} else {
			return this.name;
		}
	}

	public IType getType() {
		IType nextType = this.type;
		while (nextType != null) {
			if (nextType instanceof IForwardTypeReference)
				nextType = ((IForwardTypeReference) type).getReferencedType();
			nextType = nextType.getType();
		}
		
		return this.type;
	}

	public IScope getScope() {
		return null;
	}

	public int getByteSize() {
		return 0;
	}

	public Map<Object, Object> getProperties() {
		return null;
	}

	public void setType(IType type) {
	}

	public void dispose() {
	}

}

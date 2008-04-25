/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.lrparser.c99.bindings;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICArrayType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;

@SuppressWarnings("restriction")
public class C99ArrayType implements ICArrayType, ITypeContainer {

	private boolean isConst;
	private boolean isRestrict;
	private boolean isStatic;
	private boolean isVolatile;
	private boolean isVariableLength;

	private IType type;
	
	
	public C99ArrayType() {
	}
	
	public C99ArrayType(IType type) {
		this.type = type;
	}
	
	public boolean isConst() {
		return isConst;
	}

	public void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	public boolean isRestrict() {
		return isRestrict;
	}

	public void setRestrict(boolean isRestrict) {
		this.isRestrict = isRestrict;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	public boolean isVolatile() {
		return isVolatile;
	}

	public void setVolatile(boolean isVolatile) {
		this.isVolatile = isVolatile;
	}

	public boolean isVariableLength() {
		return isVariableLength;
	}

	public void setVariableLength(boolean isVariableLength) {
		this.isVariableLength = isVariableLength;
	}

	public IASTExpression getArraySizeExpression() throws DOMException {
		return null;
	}

	public IType getType() {
		return type;
	}
	
	public void setType(IType type) {
		this.type = type;
	}

	public boolean isSameType(IType t) {
		if(t == this)
			return true;
		if(t instanceof ITypedef)
			return t.isSameType(this);
		if(t instanceof ICArrayType) {
			ICArrayType at = (ICArrayType)t;
			try {
				if(at.isConst() == isConst &&
				   at.isRestrict() == isRestrict &&
				   at.isStatic() == isStatic &&
				   at.isVolatile() == isVolatile &&
				   at.isVariableLength() == isVariableLength) {
					return at.isSameType(type);
				}
			} catch(DOMException _) { }
		}
		return false;
	}

	
	@Override
	public C99ArrayType clone() {
		C99ArrayType clone = null;
		try {
			clone = (C99ArrayType) super.clone();
			clone.type = (IType) type.clone();
		} catch (CloneNotSupportedException e) {
			assert false; // not going to happen
		}
		return clone;
	}

}

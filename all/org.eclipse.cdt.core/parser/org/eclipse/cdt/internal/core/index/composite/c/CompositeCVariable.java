/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.c;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

class CompositeCVariable extends CompositeCBinding implements IVariable {

	public CompositeCVariable(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		super(cf, rbinding);
	}

	public IType getType() throws DOMException {
		IType rtype = ((IVariable)rbinding).getType();
		return cf.getCompositeType((IIndexType)rtype);
	}

	public boolean isAuto() throws DOMException {
		return ((IVariable)rbinding).isAuto();
	}

	public boolean isExtern() throws DOMException {
		return ((IVariable)rbinding).isExtern();
	}

	public boolean isRegister() throws DOMException {
		return ((IVariable)rbinding).isRegister();
	}

	public boolean isStatic() throws DOMException {
		return ((IVariable)rbinding).isStatic();
	}

	public IValue getInitialValue() {
		return ((IVariable)rbinding).getInitialValue();
	}
}

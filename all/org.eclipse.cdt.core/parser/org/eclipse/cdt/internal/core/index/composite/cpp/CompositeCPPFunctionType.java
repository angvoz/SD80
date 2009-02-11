/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Andrew Ferguson (Symbian) - Initial implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.internal.core.index.composite.CompositeFunctionType;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPFunctionType extends CompositeFunctionType implements ICPPFunctionType {

	public CompositeCPPFunctionType(ICPPFunctionType rtype,	ICompositesFactory cf) {
		super(rtype, cf);
	}

	@Deprecated
	public IPointerType getThisType() {
		return null;
	}

	public boolean isConst() {
		return ((ICPPFunctionType) type).isConst();
	}

	public boolean isVolatile() {
		return ((ICPPFunctionType) type).isVolatile();
	}
	
	@Override
	public Object clone() {
		fail(); return null;
	}
}

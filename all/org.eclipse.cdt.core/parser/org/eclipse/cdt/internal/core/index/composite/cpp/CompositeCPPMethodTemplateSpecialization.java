/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPMethodTemplateSpecialization
	extends CompositeCPPFunctionTemplateSpecialization
	implements ICPPMethod {

	public CompositeCPPMethodTemplateSpecialization(ICompositesFactory cf,
			ICPPFunction ft) {
		super(cf, ft);
	}
	
	public boolean isDestructor() {
		return ((ICPPMethod)rbinding).isDestructor();
	}

	public boolean isImplicit() {
		return ((ICPPMethod)rbinding).isImplicit();
	}

	public boolean isVirtual() throws DOMException {
		return ((ICPPMethod)rbinding).isVirtual();
	}

	public ICPPClassType getClassOwner() throws DOMException {
		IIndexFragmentBinding rowner = (IIndexFragmentBinding) ((ICPPMethod)rbinding).getClassOwner();
		return (ICPPClassType) cf.getCompositeBinding(rowner);
	}

	public int getVisibility() throws DOMException {
		return ((ICPPMethod)rbinding).getVisibility();
	}

	public boolean isPureVirtual() throws DOMException {
		return ((ICPPMethod)rbinding).isPureVirtual();
	}
}

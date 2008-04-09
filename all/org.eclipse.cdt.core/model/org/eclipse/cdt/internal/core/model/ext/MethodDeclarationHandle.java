/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.core.model.FunctionDeclaration;
import org.eclipse.cdt.internal.core.model.MethodDeclaration;

public class MethodDeclarationHandle extends CElementHandle implements IMethodDeclaration {
	private String[] fParameterTypes;
	private ASTAccessVisibility fVisibility;
	private boolean fIsStatic;
	private boolean fIsConstructor;
	private boolean fIsDestructor;
	
	public MethodDeclarationHandle(ICElement parent, ICPPMethod method) throws DOMException {
		this(parent, ICElement.C_METHOD_DECLARATION, method);
	}
	
	protected MethodDeclarationHandle(ICElement parent, int type, ICPPMethod method) throws DOMException {
		super(parent, type, method.getName());
		fParameterTypes= extractParameterTypes(method);
		fVisibility= getVisibility(method);
		try {
			fIsStatic= method.isStatic();
			fIsConstructor= method instanceof ICPPConstructor;
			if (!fIsConstructor) 
				fIsDestructor= method.isDestructor();
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IMethodDeclaration) {
			return MethodDeclaration.equals(this, (IMethodDeclaration) obj);
		}
		return false;
	}

	public int getNumberOfParameters() {
		return fParameterTypes.length;
	}

	public String[] getParameterTypes() {
		return fParameterTypes;
	}

	public String getSignature() throws CModelException {
		return FunctionDeclaration.getSignature(this);
	}

	public boolean isStatic() throws CModelException {
		return fIsStatic;
	}

	public ASTAccessVisibility getVisibility() throws CModelException {
		return fVisibility;
	}
	
	public boolean isConstructor() throws CModelException {
		return fIsConstructor;
	}

	public boolean isDestructor() throws CModelException {
		return fIsDestructor;
	}

}

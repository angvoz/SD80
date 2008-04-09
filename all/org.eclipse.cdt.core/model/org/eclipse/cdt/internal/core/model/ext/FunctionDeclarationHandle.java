/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.model.ext;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.internal.core.model.FunctionDeclaration;

public class FunctionDeclarationHandle extends CElementHandle implements org.eclipse.cdt.core.model.IFunctionDeclaration {

	private String[] fParameterTypes;
	private boolean fIsStatic;

	public FunctionDeclarationHandle(ICElement parent, IFunction func) throws DOMException {
		this(parent, ICElement.C_FUNCTION_DECLARATION, func);
	}

	protected FunctionDeclarationHandle(ICElement parent, int type, IFunction func) throws DOMException {
		super(parent, type, func.getName());
		fParameterTypes= extractParameterTypes(func);
		try {
			fIsStatic= func.isStatic();
		} catch (DOMException e) {
			CCorePlugin.log(e);
			fIsStatic= false;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IFunctionDeclaration) {
			return FunctionDeclaration.equals(this, (IFunctionDeclaration) obj);
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

}

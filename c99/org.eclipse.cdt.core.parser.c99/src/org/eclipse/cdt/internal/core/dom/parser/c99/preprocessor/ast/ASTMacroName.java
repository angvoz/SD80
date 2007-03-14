/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.ast;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

// TODO: will need to call back into the LocationResolver for resolving bindings
public class ASTMacroName extends ASTNode implements IASTName {

	private final String name;
	
	public ASTMacroName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	public IBinding getBinding() {
		// TODO Auto-generated method stub
		return null;
	}

	public IASTCompletionContext getCompletionContext() {
		return null;
	}

	public ILinkage getLinkage() {
		return Linkage.NO_LINKAGE;
	}

	public IBinding resolveBinding() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setBinding(IBinding binding) {
		// TODO Auto-generated method stub
		
	}

	public boolean isDeclaration() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDefinition() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isReference() {
		// TODO Auto-generated method stub
		return false;
	}

	public char[] toCharArray() {
		// TODO Auto-generated method stub
		return null;
	}

}

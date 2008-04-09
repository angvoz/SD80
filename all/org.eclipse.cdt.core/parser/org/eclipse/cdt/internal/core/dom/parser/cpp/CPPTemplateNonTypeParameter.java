/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * @author aniefer
 */
public class CPPTemplateNonTypeParameter extends CPPTemplateParameter implements
		ICPPTemplateNonTypeParameter {

	private IType type = null;
	
	/**
	 * @param name
	 */
	public CPPTemplateNonTypeParameter(IASTName name) {
		super(name);
	}

	public IASTExpression getDefault() {
		IASTName name = getPrimaryDeclaration();
		IASTDeclarator dtor = (IASTDeclarator) name.getParent();
		IASTInitializer initializer = dtor.getInitializer();
		if( initializer instanceof IASTInitializerExpression )
			return ((IASTInitializerExpression) initializer).getExpression();

		return null;
	}

	public IType getType() {
		if( type == null ){
			IASTName name = getPrimaryDeclaration();
		    IASTDeclarator dtor = (IASTDeclarator) name.getParent();
		    type = CPPVisitor.createType( dtor );
		}
		return type;
	}

	public boolean isStatic() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isExtern() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isAuto() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRegister() throws DOMException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addDefinition(IASTNode node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDeclaration(IASTNode node) {
		// TODO Auto-generated method stub
		
	}

}

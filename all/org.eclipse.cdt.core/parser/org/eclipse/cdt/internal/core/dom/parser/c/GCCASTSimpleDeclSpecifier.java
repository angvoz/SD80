/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Emanuel Graf IFS - Bugfix for #198257
 *******************************************************************************/

/*
 * Created on May 18, 2005
 */
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.gnu.c.IGCCASTSimpleDeclSpecifier;

/**
 * @author aniefer
 *
 */
public class GCCASTSimpleDeclSpecifier extends CASTSimpleDeclSpecifier implements IGCCASTSimpleDeclSpecifier {

	private IASTExpression typeOfExpression;

	public GCCASTSimpleDeclSpecifier() {
	}

	public GCCASTSimpleDeclSpecifier(IASTExpression typeofExpression) {
		setTypeofExpression(typeofExpression);
	}

	public void setTypeofExpression(IASTExpression typeofExpression) {
		this.typeOfExpression = typeofExpression;
		if (typeofExpression != null) {
			typeofExpression.setParent(this);
			typeofExpression.setPropertyInParent(TYPEOF_EXPRESSION);
		}
	}

	public IASTExpression getTypeofExpression() {
		return typeOfExpression;
	}

    @Override
	public boolean accept( ASTVisitor action ){
        if( action.shouldVisitDeclSpecifiers ){
		    switch( action.visit( this ) ){
		    case ASTVisitor.PROCESS_ABORT : return false;
		    case ASTVisitor.PROCESS_SKIP  : return true;
		    default : break;
	        }
		}
		if( typeOfExpression != null )
			if( !typeOfExpression.accept( action ) ) return false;
		
		if( action.shouldVisitDeclSpecifiers ){
			switch( action.leave( this ) ){
			case ASTVisitor.PROCESS_ABORT : return false;
			case ASTVisitor.PROCESS_SKIP  : return true;
			default : break;
			}
		}

		return true;
    }
}

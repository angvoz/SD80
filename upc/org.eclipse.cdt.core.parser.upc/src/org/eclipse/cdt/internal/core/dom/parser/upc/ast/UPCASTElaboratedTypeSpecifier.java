/*******************************************************************************
 *  Copyright (c) 2006, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.upc.ast;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.upc.ast.IUPCASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTElaboratedTypeSpecifier;

@SuppressWarnings("restriction")
public class UPCASTElaboratedTypeSpecifier extends CASTElaboratedTypeSpecifier implements IUPCASTElaboratedTypeSpecifier {

	private int referenceType;
	private int sharedQualifier;
	private IASTExpression blockSizeExpression;
	
	
	public UPCASTElaboratedTypeSpecifier() {
	}

	public UPCASTElaboratedTypeSpecifier(int kind, IASTName name) {
		super(kind, name);
	}
	
	public UPCASTElaboratedTypeSpecifier(int kind, IASTName name, IASTExpression blockSizeExpression) {
		super(kind, name);
		setBlockSizeExpression(blockSizeExpression);
	}

	@Override
	public UPCASTElaboratedTypeSpecifier copy() {
		IASTName name = getName();
		UPCASTElaboratedTypeSpecifier copy = new UPCASTElaboratedTypeSpecifier(getKind(), name == null ? null : name.copy());
		copy.referenceType = referenceType;
		copy.sharedQualifier = sharedQualifier;
		copy.setBlockSizeExpression(blockSizeExpression == null ? null : blockSizeExpression.copy());
		copy.setOffsetAndLength(this);
		return copy;
	}
	
	public IASTExpression getBlockSizeExpression() {
		return blockSizeExpression;
	}

	public int getReferenceType() {
		return referenceType;
	}

	public int getSharedQualifier() {
		return sharedQualifier;
	}

	public void setBlockSizeExpression(IASTExpression expr) {
		this.blockSizeExpression = expr;
		if(expr != null) {
			expr.setParent(this);
			expr.setPropertyInParent(BLOCK_SIZE_EXPRESSION);
		}
	}

	public void setReferenceType(int referenceType) {
		this.referenceType = referenceType;
	}

	public void setSharedQualifier(int shared) {
		this.sharedQualifier = shared;
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
        if( getName() != null ) if( !getName().accept( action ) ) return false;
        if( blockSizeExpression != null) if( !blockSizeExpression.accept( action ) ) return false;    
        
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

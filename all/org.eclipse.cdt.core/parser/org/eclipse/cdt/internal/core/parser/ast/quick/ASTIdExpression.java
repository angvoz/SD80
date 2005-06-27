/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.parser.ast.quick;

import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTIdExpression extends ASTExpression implements IASTExpression {

	private final char[] idExpression;


	/**
	 * @param kind
	 * @param idExpression
	 */
	public ASTIdExpression(Kind kind, char[] idExpression) {
		super(kind);
		this.idExpression = idExpression;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getIdExpression()
	 */
	public String getIdExpression() {
		return String.valueOf(idExpression);
	}
	
	public char[] getIdExpressionCharArray(){
		return idExpression;
	}
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}

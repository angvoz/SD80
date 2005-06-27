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
public class ASTUnaryExpression extends ASTExpression implements IASTExpression {

	private final IASTExpression lhs;

	/**
	 * @param kind
	 * @param lhs
	 */
	public ASTUnaryExpression(Kind kind, IASTExpression lhs) {
		super(kind);
		this.lhs = lhs;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLHSExpression()
	 */
	public IASTExpression getLHSExpression() {
		return lhs;
	}
	
	public String toString(){
		return ASTUtil.getExpressionString( this );
	}
}

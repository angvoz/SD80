/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.cdt.internal.core.parser.ast.expression;


/**
 * @author jcamelon
 *
 */
public class ASTLiteralExpression extends ASTExpression {

	private final String literal;

	/**
	 * @param kind
	 * @param literal
	 */
	public ASTLiteralExpression(Kind kind, String literal) {
		super( kind );
		this.literal =literal;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTExpression#getLiteralString()
	 */
	public String getLiteralString() {
		return literal;
	}
}

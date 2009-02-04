/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTTypeIdExpression extends IASTExpression {

	/**
	 * <code>op_sizeof</code> sizeof( typeId ) expression
	 */
	public static final int op_sizeof = 0;

	/**
	 * For c++, only.
	 */
	public static final int op_typeid = 1;

	/**
	 * For gnu-parsers, only.
	 * <code>op_alignOf</code> is used for __alignOf( typeId ) type expressions.
	 */
	public static final int op_alignof = 2;

	/**
	 * For gnu-parsers, only.
	 * <code>op_typeof</code> is used for typeof( typeId ) type expressions.
	 */
	public static final int op_typeof = 3;

	/**
	 * @deprecated constants should be declared here, to avoid using the same constant in different
	 * interfaces.
	 */
	@Deprecated
	public static final int op_last = op_alignof;

	/**
	 * Get the operator for the expression.
	 * 
	 * @return int
	 */
	public int getOperator();

	/**
	 * Set the operator for the expression.
	 * @param value int
	 */
	public void setOperator(int value);

	/**
	 * <code>TYPEID</code> represents the relationship between an <code>IASTTypeIdExpression</code> and
	 * it's nested <code>IASTTypeId</code>.
	 */
	public static final ASTNodeProperty TYPE_ID = new ASTNodeProperty("IASTTypeIdExpression.TYPE_ID - IASTTypeId for IASTTypeIdExpression"); //$NON-NLS-1$

	/**
	 * Set the type Id.
	 * @param typeId
	 */
	public void setTypeId(IASTTypeId typeId);

	/**
	 * Get the type Id.
	 */
	public IASTTypeId getTypeId();

	/**
	 * @since 5.1
	 */
	public IASTTypeIdExpression copy();
}

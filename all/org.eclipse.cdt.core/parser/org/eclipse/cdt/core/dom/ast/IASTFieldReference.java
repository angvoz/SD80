/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents expressions that access a field reference. e.g. a.b =>
 * a is the expression, b is the field name. e.g. a()->def => a() is the
 * expression, def is the field name.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTFieldReference extends IASTExpression, IASTNameOwner {

	/**
	 * <code>FIELD_OWNER</code> represents the relationship between a
	 * <code>IASTFieldReference</code> and its <code>IASTExpression</code>
	 * field owner.
	 */
	public static final ASTNodeProperty FIELD_OWNER = new ASTNodeProperty(
			"IASTFieldReference.FIELD_OWNER - IASTFieldReference's Owner"); //$NON-NLS-1$

	/**
	 * <code>FIELD_NAME</code> represents the relationship between a
	 * <code>IASTFieldReference</code> and its <code>IASTName</code> field
	 * name.
	 */
	public static final ASTNodeProperty FIELD_NAME = new ASTNodeProperty(
			"IASTFieldReference.FIELD_NAME - IASTName for IASTFieldReference"); //$NON-NLS-1$

	/**
	 * This returns an expression for the object containing the field.
	 * 
	 * @return the field owner
	 */
	public IASTExpression getFieldOwner();

	/**
	 * Set the expression for the object containing the field.
	 * 
	 * @param expression
	 */
	public void setFieldOwner(IASTExpression expression);

	/**
	 * This returns the name of the field being dereferenced.
	 * 
	 * @return the name of the field (<code>IASTName</code>)
	 */
	public IASTName getFieldName();

	/**
	 * Set the name of the field.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setFieldName(IASTName name);

	/**
	 * This returns true of this is the arrow operator and not the dot operator.
	 * 
	 * @return is this a pointer dereference
	 */
	public boolean isPointerDereference();

	/**
	 * Set whether or not this is a pointer dereference (default == no).
	 * 
	 * @param value
	 *            boolean
	 */
	public void setIsPointerDereference(boolean value);
	
	/**
	 * @since 5.1
	 */
	public IASTFieldReference copy();

}

/*******************************************************************************
 *  Copyright (c) 2004, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;

/**
 * C++ adds a few things to function declarators.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface ICPPASTFunctionDeclarator extends IASTStandardFunctionDeclarator {

	/**
	 * Used as return value for {@link #getExceptionSpecification()}.
	 * @since 5.1
	 */
	public static final IASTTypeId[] NO_EXCEPTION_SPECIFICATION = {};

	/**
	 * <code>EXCEPTION_TYPEID</code> represents the type IDs throws in the
	 * exception specification.
	 */
	public static final ASTNodeProperty EXCEPTION_TYPEID = new ASTNodeProperty(
			"ICPPASTFunctionDeclarator.EXCEPTION_TYPEID - TypeId throws in the exception specification"); //$NON-NLS-1$

	/**
	 * Is this a const method?
	 */
	public boolean isConst();

	/**
	 * Set the method to be const or not.
	 */
	public void setConst(boolean value);

	/**
	 * Is this a volatile method?
	 */
	public boolean isVolatile();

	/**
	 * Set the method to be volatile or not.
	 */
	public void setVolatile(boolean value);

	/**
	 * Is the method pure virtual?
	 */
	public boolean isPureVirtual();

	/**
	 * Set this method to be pure virtual.
	 */
	public void setPureVirtual(boolean isPureVirtual);

	/**
	 * Returns an array of type-ids representing the exception specification. The return value
	 * {@link #NO_EXCEPTION_SPECIFICATION} indicates that no exceptions are specified, whereas
	 * {@link IASTTypeId#EMPTY_TYPEID_ARRAY} is used for an empty exception specification.
	 */
	public IASTTypeId[] getExceptionSpecification();

	/**
	 * Add an exception specification type Id.
	 */
	public void addExceptionSpecificationTypeId(IASTTypeId typeId);

	/**
	 * Configures the declarator with an empty exception specification (as opposed to having none). 
	 * 
	 * @since 5.1
	 */
	public void setEmptyExceptionSpecification();
	
	/**
	 * Get function scope this node represents. Returns <code>null</code>, if this declarator does not
	 * declare a function-prototype or function-definition.
	 */
	public ICPPFunctionScope getFunctionScope();


	@Deprecated
	public static final ASTNodeProperty CONSTRUCTOR_CHAIN_MEMBER = new ASTNodeProperty(
			"ICPPASTFunctionDeclarator.CONSTRUCTOR_CHAIN_MEMBER - Role of a Constructor Chain Initializer"); //$NON-NLS-1$
	
	/**
	 * @deprecated  use {@link ICPPASTFunctionDefinition#getMemberInitializers}, instead.
	 */
	@Deprecated
	public ICPPASTConstructorChainInitializer[] getConstructorChain();

	@Deprecated
	public void addConstructorToChain(ICPPASTConstructorChainInitializer initializer);
	
	/**
	 * @since 5.1
	 */
	public ICPPASTFunctionDeclarator copy();
}

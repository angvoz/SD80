/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM Rational Software) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This interface represents enumerations in C and C++.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTEnumerationSpecifier extends IASTDeclSpecifier, IASTNameOwner {

	/**
	 * This interface represents an enumerator member of an enum specifier.
	 * 
	 * @author jcamelon
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	public interface IASTEnumerator extends IASTNode, IASTNameOwner {
		/**
		 * Empty array (constant).
		 */
		public static final IASTEnumerator[] EMPTY_ENUMERATOR_ARRAY = new IASTEnumerator[0];

		/**
		 * <code>ENUMERATOR_NAME</code> describes the relationship between
		 * <code>IASTEnumerator</code> and <code>IASTName</code>.
		 */
		public static final ASTNodeProperty ENUMERATOR_NAME = new ASTNodeProperty(
				"IASTEnumerator.ENUMERATOR_NAME - IASTName for IASTEnumerator"); //$NON-NLS-1$

		/**
		 * Set the enumerator's name.
		 * 
		 * @param name
		 */
		public void setName(IASTName name);

		/**
		 * Get the enumerator's name.
		 * 
		 * @return <code>IASTName</code>
		 */
		public IASTName getName();

		/**
		 * <code>ENUMERATOR_VALUE</code> describes the relationship between
		 * <code>IASTEnumerator</code> and <code>IASTExpression</code>.
		 */
		public static final ASTNodeProperty ENUMERATOR_VALUE = new ASTNodeProperty(
				"IASTEnumerator.ENUMERATOR_VALUE - IASTExpression (value) for IASTEnumerator"); //$NON-NLS-1$

		/**
		 * Set enumerator value.
		 * 
		 * @param expression
		 */
		public void setValue(IASTExpression expression);

		/**
		 * Get enumerator value.
		 * 
		 * @return <code>IASTExpression</code> value
		 */
		public IASTExpression getValue();
		
		/**
		 * @since 5.1
		 */
		public IASTEnumerator copy();

	}

	/**
	 * <code>ENUMERATOR</code> describes the relationship between
	 * <code>IASTEnumerationSpecifier</code> and the nested
	 * <code>IASTEnumerator</code>s.
	 */
	public static final ASTNodeProperty ENUMERATOR = new ASTNodeProperty(
			"IASTEnumerationSpecifier.ENUMERATOR - nested IASTEnumerator for IASTEnumerationSpecifier"); //$NON-NLS-1$

	/**
	 * Add an enumerator.
	 * 
	 * @param enumerator
	 *            <code>IASTEnumerator</code>
	 */
	public void addEnumerator(IASTEnumerator enumerator);

	/**
	 * Get enumerators.
	 * 
	 * @return <code>IASTEnumerator []</code> array
	 */
	public IASTEnumerator[] getEnumerators();

	/**
	 * <code>ENUMERATION_NAME</code> describes the relationship between
	 * <code>IASTEnumerationSpecifier</code> and its <code>IASTName</code>.
	 */
	public static final ASTNodeProperty ENUMERATION_NAME = new ASTNodeProperty(
			"IASTEnumerationSpecifier.ENUMERATION_NAME - IASTName for IASTEnumerationSpecifier"); //$NON-NLS-1$

	/**
	 * Set the enum's name.
	 * 
	 * @param name
	 */
	public void setName(IASTName name);

	/**
	 * Get the enum's name.
	 */
	public IASTName getName();

	/**
	 * @since 5.1
	 */
	public IASTEnumerationSpecifier copy();
}

/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (IBM) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

/**
 * This is the base interface that represents a declaration specifier sequence.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IASTDeclSpecifier extends IASTNode {

	/**
	 * <code>sc_unspecified</code> undefined storage class
	 */
	public static final int sc_unspecified = 0;

	/**
	 * <code>sc_typedef</code> typedef
	 */
	public static final int sc_typedef = 1;

	/**
	 * <code>sc_extern</code>extern
	 */

	public static final int sc_extern = 2;

	/**
	 * <code>sc_static</code>static
	 */

	public static final int sc_static = 3;

	/**
	 * <code>sc_auto</code>auto
	 */

	public static final int sc_auto = 4;

	/**
	 * <code>sc_register</code>register
	 */
	public static final int sc_register = 5;

	/**
	 * <code>sc_last</code> for sub-interfaces to continue on
	 */
	public static final int sc_last = sc_register;

	/**
	 * Set the storage class.
	 * 
	 * @param storageClass
	 *            int
	 */
	public void setStorageClass(int storageClass);

	/**
	 * Get the storage class.
	 * 
	 * @return int
	 */
	public int getStorageClass();

	// Type qualifier
	/**
	 * Is const modifier used?
	 * 
	 * @return boolean
	 */
	public boolean isConst();

	/**
	 * Set const modifier used.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setConst(boolean value);

	/**
	 * Is volatile modifier used?
	 * 
	 * @return boolean
	 */
	public boolean isVolatile();

	/**
	 * Set volatile modifier used.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setVolatile(boolean value);

	// Function specifier
	/**
	 * Is inline modifier used?
	 * 
	 * @return boolean
	 */
	public boolean isInline();

	/**
	 * Set inline modifier used.
	 * 
	 * @param value
	 *            boolean
	 */
	public void setInline(boolean value);
	
	/**
	 * @since 5.1
	 */
	public IASTDeclSpecifier copy();

}

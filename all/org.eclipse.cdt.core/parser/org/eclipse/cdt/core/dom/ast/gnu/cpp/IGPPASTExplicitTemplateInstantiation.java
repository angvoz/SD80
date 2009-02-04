/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;

/**
 * G++ allows for instantiations to be qualified w/modifiers for scoping.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGPPASTExplicitTemplateInstantiation extends
		ICPPASTExplicitTemplateInstantiation {

	/**
	 * <code>ti_static</code> implies 'static' keyword is used.
	 */
	public static final int ti_static = 1;

	/**
	 * <code>ti_inline</code> implies 'inline' keyword is used.
	 */
	public static final int ti_inline = 2;

	/**
	 * <code>ti_extern</code> implies 'extern' keyword is used.
	 */
	public static final int ti_extern = 3;

	/**
	 * Get the modifier.
	 * 
	 * @return int
	 */
	public int getModifier();

	/**
	 * Set the modifier value.
	 * 
	 * @param value
	 *            (int)
	 */
	public void setModifier(int value);
	
	/**
	 * @since 5.1
	 */
	public IGPPASTExplicitTemplateInstantiation copy();
}

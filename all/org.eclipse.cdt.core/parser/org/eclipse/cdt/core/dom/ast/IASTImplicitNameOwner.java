/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;


/**
 * An implicit name generated on demand.
 * @since 5.1
 */
public interface IASTImplicitNameOwner extends IASTNode {

	public static final ASTNodeProperty IMPLICIT_NAME = 
		new ASTNodeProperty("ICPPASTImplicitNameOwner.IMPLICIT_NAME"); //$NON-NLS-1$
	
	
	public IASTImplicitName[] getImplicitNames();
	
}

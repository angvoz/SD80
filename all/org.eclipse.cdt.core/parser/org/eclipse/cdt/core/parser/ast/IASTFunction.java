/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.ast;

import java.util.Iterator;

/**
 * @author jcamelon
 *
 */
public interface IASTFunction extends IASTCodeScope, IASTOffsetableNamedElement, IASTTemplatedDeclaration, IASTDeclaration, IASTQualifiedNameElement {
	
	public boolean isInline(); 
	public boolean isFriend();
	public boolean isStatic();

	public String getName();

	public IASTAbstractDeclaration getReturnType();   
	public Iterator getParameters();
	public IASTExceptionSpecification getExceptionSpec();
	/**
	 * @param b
	 */
	public void setHasFunctionBody(boolean b);
	public boolean hasFunctionBody();
	
	public boolean previouslyDeclared();

	public int getNameEndOffset(); // necessary for operator new, etc. which are > 1 token
}

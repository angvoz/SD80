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
package org.eclipse.cdt.internal.core.parser.ast.full;

import java.util.Iterator;

import org.eclipse.cdt.internal.core.parser.pst.IContainerSymbol;
import org.eclipse.cdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTCompilationUnit implements IASTFCompilationUnit {

	public ASTCompilationUnit( IContainerSymbol symbol )
	{
		this.symbol = symbol;
		symbol.setASTNode( this );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IScope#getDeclarations()
	 */
	public Iterator getDeclarations() {
		return new ScopeIterator( symbol.getContainedSymbols() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.ISymbolTableExtension#getSymbol()
	 */
	public IContainerSymbol getContainerSymbol() {
		return symbol;
	}

	private final IContainerSymbol symbol;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.IPSTSymbolExtension#getSymbol()
	 */
	public ISymbol getSymbol() {
		return symbol;
	}
}

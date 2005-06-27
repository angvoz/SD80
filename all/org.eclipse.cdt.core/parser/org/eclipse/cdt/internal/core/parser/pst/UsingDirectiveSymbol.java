/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.pst;


public class UsingDirectiveSymbol extends ExtensibleSymbol implements IUsingDirectiveSymbol{

	public UsingDirectiveSymbol( ParserSymbolTable table, IContainerSymbol ns ){
		super( table );
		namespace = ns;
	}
	
	public IContainerSymbol getNamespace(){
		return namespace;
	}
	
	private final IContainerSymbol namespace;
}

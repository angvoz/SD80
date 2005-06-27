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

/**
 * @author aniefer
 */
public class ParserSymbolTableError extends Error {
	/**
	 * Constructor for ParserSymbolTableError.
	 */
	public ParserSymbolTableError() {
		super();
	}

	/**
	 * Constructor for ParserSymbolTableError.
	 * @param int r: reason
	 */
	public ParserSymbolTableError( int r ) {
		reason = r;
	}

	public static final int r_InternalError			  = -1;
	public static final int r_OperationNotSupported   =  0;
	public int reason = -1;
	
}

/*******************************************************************************
 * Copyright (c) 2001 Rational Software Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     Rational Software - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser;

public class ScannerException extends Exception {

	/**
	 * Constructor for ScannerException.
	 */
	public ScannerException() {
		super();
	}

	/**
	 * Constructor for ScannerException.
	 * @param s
	 */
	public ScannerException(String s) {
		super(s);
	}

}

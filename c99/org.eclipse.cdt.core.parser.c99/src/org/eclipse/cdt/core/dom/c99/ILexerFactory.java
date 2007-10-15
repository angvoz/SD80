/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.c99;

import org.eclipse.cdt.core.parser.CodeReader;

/**
 * Used by the preprocessor to create new ILexer objects
 * so that it can tokenize #included source files.
 *
 */
public interface ILexerFactory<TKN> {
	
	/**
	 * Create a lexer for the given CodeReader.
	 */
	ILexer<TKN> createLexer(CodeReader reader);
}

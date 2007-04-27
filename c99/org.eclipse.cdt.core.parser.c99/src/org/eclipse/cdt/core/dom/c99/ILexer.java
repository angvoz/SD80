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

import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.TokenList;

/**
 * A lexer returns a list of tokens that can be passed to the preprocessor.
 * 
 * @author Mike Kucera
 */
public interface ILexer {
	
	public static final int OPTION_GENERATE_COMMENT_TOKENS = 1;
	
	/**
	 * Get a list of tokens.
	 */
	TokenList lex(int options);
}

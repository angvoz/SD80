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

package org.eclipse.cdt.core.dom.parser.c99;

import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;

/**
 * Provides token mappings for keywords that are defined in the C99 standard.
 * 
 * @author Mike Kucera
 */
public class C99KeywordMap extends LPGKeywordMap {
	

	public C99KeywordMap() {
		putKeyword(C99Parsersym.TK_auto);
		putKeyword(C99Parsersym.TK_break);
		putKeyword(C99Parsersym.TK_case);
		putKeyword(C99Parsersym.TK_char);
		putKeyword(C99Parsersym.TK_const);
		putKeyword(C99Parsersym.TK_continue);
		putKeyword(C99Parsersym.TK_default);
		putKeyword(C99Parsersym.TK_do);
		putKeyword(C99Parsersym.TK_double);
		putKeyword(C99Parsersym.TK_else);
		putKeyword(C99Parsersym.TK_enum);
		putKeyword(C99Parsersym.TK_extern);
		putKeyword(C99Parsersym.TK_float);
		putKeyword(C99Parsersym.TK_for);
		putKeyword(C99Parsersym.TK_goto);
		putKeyword(C99Parsersym.TK_if);
		putKeyword(C99Parsersym.TK_inline);
		putKeyword(C99Parsersym.TK_int);
		putKeyword(C99Parsersym.TK_long);
		putKeyword(C99Parsersym.TK_register);
		putKeyword(C99Parsersym.TK_restrict);
		putKeyword(C99Parsersym.TK_return);
		putKeyword(C99Parsersym.TK_short);
		putKeyword(C99Parsersym.TK_signed);
		putKeyword(C99Parsersym.TK_sizeof);
		putKeyword(C99Parsersym.TK_static);
		putKeyword(C99Parsersym.TK_struct);
		putKeyword(C99Parsersym.TK_switch);
		putKeyword(C99Parsersym.TK_typedef);
		putKeyword(C99Parsersym.TK_union);
		putKeyword(C99Parsersym.TK_unsigned);
		putKeyword(C99Parsersym.TK_void);
		putKeyword(C99Parsersym.TK_volatile);
		putKeyword(C99Parsersym.TK_while);
		putKeyword(C99Parsersym.TK__Bool);
		putKeyword(C99Parsersym.TK__Complex);
		putKeyword(C99Parsersym.TK__Imaginary);
		
		addBuiltinType(C99Parsersym.TK_char);
		addBuiltinType(C99Parsersym.TK_double);
		addBuiltinType(C99Parsersym.TK_float);
		addBuiltinType(C99Parsersym.TK_int);
		addBuiltinType(C99Parsersym.TK_long);
		addBuiltinType(C99Parsersym.TK_short);
		addBuiltinType(C99Parsersym.TK_signed);
		addBuiltinType(C99Parsersym.TK_unsigned);
		addBuiltinType(C99Parsersym.TK_void);
		addBuiltinType(C99Parsersym.TK__Bool);
		addBuiltinType(C99Parsersym.TK__Complex);
		addBuiltinType(C99Parsersym.TK__Imaginary);
	}

	
	protected String[] getOrderedTerminalSymbols() {
		return C99Parsersym.orderedTerminalSymbols;
	}

}

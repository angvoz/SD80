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

package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor;

import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;

/**
 * Provides token mappings for keywords that are defined in the C99 standard.
 * 
 * @author Mike Kucera
 */
public class C99BaseKeywordMap extends KeywordMap {

	public C99BaseKeywordMap() {
		addKeyword("auto",       C99Parsersym.TK_auto);
		addKeyword("break",      C99Parsersym.TK_break);
		addKeyword("case",       C99Parsersym.TK_case);
		addKeyword("char",       C99Parsersym.TK_char);
		addKeyword("const",      C99Parsersym.TK_const);
		addKeyword("continue",   C99Parsersym.TK_continue);
		addKeyword("default",    C99Parsersym.TK_default);
		addKeyword("do",         C99Parsersym.TK_do);
		addKeyword("double",     C99Parsersym.TK_double);
		addKeyword("else",       C99Parsersym.TK_else);
		addKeyword("enum",       C99Parsersym.TK_enum);
		addKeyword("extern",     C99Parsersym.TK_extern);
		addKeyword("float",      C99Parsersym.TK_float);
		addKeyword("for",        C99Parsersym.TK_for);
		addKeyword("goto",       C99Parsersym.TK_goto);
		addKeyword("if",         C99Parsersym.TK_if);
		addKeyword("inline",     C99Parsersym.TK_inline);
		addKeyword("int",        C99Parsersym.TK_int);
		addKeyword("long",       C99Parsersym.TK_long);
		addKeyword("register",   C99Parsersym.TK_register);
		addKeyword("restrict",   C99Parsersym.TK_restrict);
		addKeyword("return",     C99Parsersym.TK_return);
		addKeyword("short",      C99Parsersym.TK_short);
		addKeyword("signed",     C99Parsersym.TK_signed);
		addKeyword("sizeof",     C99Parsersym.TK_sizeof);
		addKeyword("static",     C99Parsersym.TK_static);
		addKeyword("struct",     C99Parsersym.TK_struct);
		addKeyword("switch",     C99Parsersym.TK_switch);
		addKeyword("typedef",    C99Parsersym.TK_typedef);
		addKeyword("union",      C99Parsersym.TK_union);
		addKeyword("unsigned",   C99Parsersym.TK_unsigned);
		addKeyword("void",       C99Parsersym.TK_void);
		addKeyword("volatile",   C99Parsersym.TK_volatile);
		addKeyword("while",      C99Parsersym.TK_while);
		addKeyword("_Bool",      C99Parsersym.TK__Bool);
		addKeyword("_Complex",   C99Parsersym.TK__Complex);
		addKeyword("_Imaginary", C99Parsersym.TK__Imaginary);
	}
}

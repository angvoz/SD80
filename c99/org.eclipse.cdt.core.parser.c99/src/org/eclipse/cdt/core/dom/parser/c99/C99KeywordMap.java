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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.c99.IKeywordMap;
import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;

/**
 * Provides token mappings for keywords that are defined in the C99 standard.
 * 
 * @author Mike Kucera
 */
public class C99KeywordMap implements IKeywordMap {

	
	private Map keywords = new HashMap();
	private Set builtinTypes = new HashSet();
	private Set preprocessorKeywords = new HashSet();
	
	public static final String
		AUTO = "auto", //$NON-NLS-1$
		BREAK = "break", //$NON-NLS-1$
		CASE = "case", //$NON-NLS-1$
		CHAR = "char", //$NON-NLS-1$
		CONST = "const", //$NON-NLS-1$
		CONTINUE = "continue", //$NON-NLS-1$
		DEFAULT = "default", //$NON-NLS-1$
		DO = "do", //$NON-NLS-1$
		DOUBLE = "double", //$NON-NLS-1$
		ELSE = "else", //$NON-NLS-1$
		ENUM = "enum", //$NON-NLS-1$
		EXTERN = "extern", //$NON-NLS-1$
		FLOAT = "float", //$NON-NLS-1$
		FOR = "for", //$NON-NLS-1$
		GOTO = "goto", //$NON-NLS-1$
		IF = "if", //$NON-NLS-1$
		INLINE = "inline", //$NON-NLS-1$
		INT = "int", //$NON-NLS-1$
		LONG = "long", //$NON-NLS-1$
		REGISTER = "register", //$NON-NLS-1$
		RESTRICT = "restrict", //$NON-NLS-1$
		RETURN = "return", //$NON-NLS-1$
		SHORT = "short", //$NON-NLS-1$
		SIGNED = "signed", //$NON-NLS-1$
		SIZEOF = "sizeof", //$NON-NLS-1$
		STATIC = "static", //$NON-NLS-1$
		STRUCT = "struct", //$NON-NLS-1$
		SWITCH = "switch", //$NON-NLS-1$
		TYPEDEF = "typedef", //$NON-NLS-1$
		UNION = "union", //$NON-NLS-1$
		UNSIGNED = "unsigned", //$NON-NLS-1$
		VOID = "void", //$NON-NLS-1$
		VOLATILE = "volatile", //$NON-NLS-1$
		WHILE = "while", //$NON-NLS-1$
		_BOOL = "_Bool", //$NON-NLS-1$
		_COMPLEX = "_Complex", //$NON-NLS-1$
		_IMAGINARY = "_Imaginary"; //$NON-NLS-1$
	
	
	public static final String 
		POUND_DEFINE = "#define", //$NON-NLS-1$
		POUND_UNDEF = "#undef", //$NON-NLS-1$
		POUND_IF = "#if", //$NON-NLS-1$
		POUND_IFDEF = "#ifdef", //$NON-NLS-1$
		POUND_IFNDEF = "#ifndef", //$NON-NLS-1$
		POUND_ELSE = "#else", //$NON-NLS-1$
		POUND_ENDIF = "#endif", //$NON-NLS-1$
		POUND_INCLUDE = "#include", //$NON-NLS-1$
		POUND_LINE = "#line", //$NON-NLS-1$
		POUND_ERROR = "#error", //$NON-NLS-1$
		POUND_PRAGMA = "#pragma", //$NON-NLS-1$
		POUND_ELIF = "#elif", //$NON-NLS-1$
		POUND_BLANK = "#", //$NON-NLS-1$
	    POUND_INCLUDE_NEXT = "include_next"; //$NON-NLS-1$
		
	
	
	public C99KeywordMap() {
		addKeywords();
		addBuiltinTypes();
		addPreprocessorKeywords();
	}
	
	
	public Integer getKeywordKind(String identifier) {
		return (Integer) keywords.get(identifier);
	}
	
	
	private void addKeywords() {
		putKeyword(AUTO,       C99Parsersym.TK_auto);
		putKeyword(BREAK,      C99Parsersym.TK_break);
		putKeyword(CASE,       C99Parsersym.TK_case);
		putKeyword(CHAR,       C99Parsersym.TK_char);
		putKeyword(CONST,      C99Parsersym.TK_const);
		putKeyword(CONTINUE,   C99Parsersym.TK_continue);
		putKeyword(DEFAULT,    C99Parsersym.TK_default);
		putKeyword(DO,         C99Parsersym.TK_do);
		putKeyword(DOUBLE,     C99Parsersym.TK_double);
		putKeyword(ELSE,       C99Parsersym.TK_else);
		putKeyword(ENUM,       C99Parsersym.TK_enum);
		putKeyword(EXTERN,     C99Parsersym.TK_extern);
		putKeyword(FLOAT,      C99Parsersym.TK_float);
		putKeyword(FOR,        C99Parsersym.TK_for);
		putKeyword(GOTO,       C99Parsersym.TK_goto);
		putKeyword(IF,         C99Parsersym.TK_if);
		putKeyword(INLINE,     C99Parsersym.TK_inline);
		putKeyword(INT,        C99Parsersym.TK_int);
		putKeyword(LONG,       C99Parsersym.TK_long);
		putKeyword(REGISTER,   C99Parsersym.TK_register);
		putKeyword(RESTRICT,   C99Parsersym.TK_restrict);
		putKeyword(RETURN,     C99Parsersym.TK_return);
		putKeyword(SHORT,      C99Parsersym.TK_short);
		putKeyword(SIGNED,     C99Parsersym.TK_signed);
		putKeyword(SIZEOF,     C99Parsersym.TK_sizeof);
		putKeyword(STATIC,     C99Parsersym.TK_static);
		putKeyword(STRUCT,     C99Parsersym.TK_struct);
		putKeyword(SWITCH,     C99Parsersym.TK_switch);
		putKeyword(TYPEDEF,    C99Parsersym.TK_typedef);
		putKeyword(UNION,      C99Parsersym.TK_union);
		putKeyword(UNSIGNED,   C99Parsersym.TK_unsigned);
		putKeyword(VOID,       C99Parsersym.TK_void);
		putKeyword(VOLATILE,   C99Parsersym.TK_volatile);
		putKeyword(WHILE,      C99Parsersym.TK_while);
		putKeyword(_BOOL,      C99Parsersym.TK__Bool);
		putKeyword(_COMPLEX,   C99Parsersym.TK__Complex);
		putKeyword(_IMAGINARY, C99Parsersym.TK__Imaginary);
	}
	
	
	private void addBuiltinTypes() {
		addBuiltinType(CHAR);
		addBuiltinType(DOUBLE);
		addBuiltinType(FLOAT);
		addBuiltinType(INT);
		addBuiltinType(LONG);
		addBuiltinType(SHORT);
		addBuiltinType(SIGNED);
		addBuiltinType(UNSIGNED);
		addBuiltinType(VOID);
		addBuiltinType(_BOOL);
		addBuiltinType(_COMPLEX);
		addBuiltinType(_IMAGINARY);
	}
	
	private void addPreprocessorKeywords() {
		addPreprocessorKeyword(POUND_DEFINE);
		addPreprocessorKeyword(POUND_UNDEF);
		addPreprocessorKeyword(POUND_IF);
		addPreprocessorKeyword(POUND_IFDEF);
		addPreprocessorKeyword(POUND_IFNDEF);
		addPreprocessorKeyword(POUND_ELSE);
		addPreprocessorKeyword(POUND_ENDIF);
		addPreprocessorKeyword(POUND_INCLUDE);
		addPreprocessorKeyword(POUND_LINE);
		addPreprocessorKeyword(POUND_ERROR);
		addPreprocessorKeyword(POUND_PRAGMA);
		addPreprocessorKeyword(POUND_ELIF);
		addPreprocessorKeyword(POUND_BLANK);
		addPreprocessorKeyword(POUND_INCLUDE_NEXT);
	}
	
	protected void putKeyword(String keyword, int kind) {
		keywords.put(keyword, new Integer(kind));
	}
	
	protected void addBuiltinType(String type) {
		builtinTypes.add(type);
	}
	
	protected void addPreprocessorKeyword(String keyword) {
		preprocessorKeywords.add(keyword);
	}
	
	public String[] getKeywords() {
		return (String[]) keywords.keySet().toArray(new String[0]);
	}
	
	public String[] getBuiltinTypes() {
		return (String[]) builtinTypes.toArray(new String[0]);
	}
	
	public String[] getPreprocessorKeywords() {
		return (String[]) preprocessorKeywords.toArray(new String[0]);
	}
}

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

package org.eclipse.cdt.internal.core.dom.parser.c99;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains lexer actions for recognizing keyworks and
 * creating tokens.
 * 
 * @author Mike Kucera
 */
public class C99LexerAction {
	
	// maps C99 keywords to their token kind
	private Map keywordMap = new HashMap();
	
	
	private C99Lexer lexer = null;
	
	C99LexerAction(C99Lexer lexer) {
		this.lexer = lexer;

		addKeyword("auto",       C99Lexer.TK_auto);
		addKeyword("break",      C99Lexer.TK_break);
		addKeyword("case",       C99Lexer.TK_case);
		addKeyword("char",       C99Lexer.TK_char);
		addKeyword("const",      C99Lexer.TK_const);
		addKeyword("continue",   C99Lexer.TK_continue);
		addKeyword("default",    C99Lexer.TK_default);
		addKeyword("do",         C99Lexer.TK_do);
		addKeyword("double",     C99Lexer.TK_double);
		addKeyword("else",       C99Lexer.TK_else);
		addKeyword("enum",       C99Lexer.TK_enum);
		addKeyword("extern",     C99Lexer.TK_extern);
		addKeyword("float",      C99Lexer.TK_float);
		addKeyword("for",        C99Lexer.TK_for);
		addKeyword("goto",       C99Lexer.TK_goto);
		addKeyword("if",         C99Lexer.TK_if);
		addKeyword("inline",     C99Lexer.TK_inline);
		addKeyword("int",        C99Lexer.TK_int);
		addKeyword("long",       C99Lexer.TK_long);
		addKeyword("register",   C99Lexer.TK_register);
		addKeyword("restrict",   C99Lexer.TK_restrict);
		addKeyword("return",     C99Lexer.TK_return);
		addKeyword("short",      C99Lexer.TK_short);
		addKeyword("signed",     C99Lexer.TK_signed);
		addKeyword("sizeof",     C99Lexer.TK_sizeof);
		addKeyword("static",     C99Lexer.TK_static);
		addKeyword("struct",     C99Lexer.TK_struct);
		addKeyword("switch",     C99Lexer.TK_switch);
		addKeyword("typedef",    C99Lexer.TK_typedef);
		addKeyword("union",      C99Lexer.TK_union);
		addKeyword("unsigned",   C99Lexer.TK_unsigned);
		addKeyword("void",       C99Lexer.TK_void);
		addKeyword("volatile",   C99Lexer.TK_volatile);
		addKeyword("while",      C99Lexer.TK_while);
		addKeyword("_Bool",      C99Lexer.TK__Bool);
		addKeyword("_Complex",   C99Lexer.TK__Complex);
		addKeyword("_Imaginary", C99Lexer.TK__Imaginary);
	}
	 
	
	/**
	 * Allows subclasses to add new keywords to the base set.
	 */
	protected void addKeyword(String keyword, int token) {
		keywordMap.put(keyword, new Integer(token));
	}
	
	 
	/**
	 * This method is required by LPG, it maps individual characters
	 * to their character kind, the character kinds are what is recognized
	 * by the scanner.
	 */
	public int getKind(int i) {
		int streamLength = lexer.getStreamLength();
		char c = (i >= streamLength ? '\uffff' : lexer.getCharValue(i));

		switch(c) {
			case 'a': return C99Lexer.Char_a;  case 'A': return C99Lexer.Char_A;
			case 'b': return C99Lexer.Char_b;  case 'B': return C99Lexer.Char_B;
			case 'c': return C99Lexer.Char_c;  case 'C': return C99Lexer.Char_C;
			case 'd': return C99Lexer.Char_d;  case 'D': return C99Lexer.Char_D;
			case 'e': return C99Lexer.Char_e;  case 'E': return C99Lexer.Char_E;
			case 'f': return C99Lexer.Char_f;  case 'F': return C99Lexer.Char_F;
			case 'g': return C99Lexer.Char_g;  case 'G': return C99Lexer.Char_G;
			case 'h': return C99Lexer.Char_h;  case 'H': return C99Lexer.Char_H;
			case 'i': return C99Lexer.Char_i;  case 'I': return C99Lexer.Char_I;
			case 'j': return C99Lexer.Char_j;  case 'J': return C99Lexer.Char_J;
			case 'k': return C99Lexer.Char_k;  case 'K': return C99Lexer.Char_K;
			case 'l': return C99Lexer.Char_l;  case 'L': return C99Lexer.Char_L;
			case 'm': return C99Lexer.Char_m;  case 'M': return C99Lexer.Char_M;
			case 'n': return C99Lexer.Char_n;  case 'N': return C99Lexer.Char_N;
			case 'o': return C99Lexer.Char_o;  case 'O': return C99Lexer.Char_O;
			case 'p': return C99Lexer.Char_p;  case 'P': return C99Lexer.Char_P;
			case 'q': return C99Lexer.Char_q;  case 'Q': return C99Lexer.Char_Q;
			case 'r': return C99Lexer.Char_r;  case 'R': return C99Lexer.Char_R;
			case 's': return C99Lexer.Char_s;  case 'S': return C99Lexer.Char_S;
			case 't': return C99Lexer.Char_t;  case 'T': return C99Lexer.Char_T;
			case 'u': return C99Lexer.Char_u;  case 'U': return C99Lexer.Char_U;
			case 'v': return C99Lexer.Char_v;  case 'V': return C99Lexer.Char_V;
			case 'w': return C99Lexer.Char_w;  case 'W': return C99Lexer.Char_W;
			case 'x': return C99Lexer.Char_x;  case 'X': return C99Lexer.Char_X;
			case 'y': return C99Lexer.Char_y;  case 'Y': return C99Lexer.Char_Y;
			case 'z': return C99Lexer.Char_z;  case 'Z': return C99Lexer.Char_Z;
			case '0': return C99Lexer.Char_0;
			case '1': return C99Lexer.Char_1;
			case '2': return C99Lexer.Char_2;
			case '3': return C99Lexer.Char_3;
			case '4': return C99Lexer.Char_4;
			case '5': return C99Lexer.Char_5;
			case '6': return C99Lexer.Char_6;
			case '7': return C99Lexer.Char_7;
			case '8': return C99Lexer.Char_8;
			case '9': return C99Lexer.Char_9;
			case '_': return C99Lexer.Char__;
			case '\\': return C99Lexer.Char_BackSlash;
			case '.': return C99Lexer.Char_Dot;
			case '<': return C99Lexer.Char_LessThan;
			case '>': return C99Lexer.Char_GreaterThan;
			case '+': return C99Lexer.Char_Plus;
			case '-': return C99Lexer.Char_Minus;
			case '/': return C99Lexer.Char_Slash;
			case '*': return C99Lexer.Char_Star;
			case '(': return C99Lexer.Char_LeftParen;
			case ')': return C99Lexer.Char_RightParen;
			case '=': return C99Lexer.Char_Equal;
			case '[': return C99Lexer.Char_LeftBracket;
			case ']': return C99Lexer.Char_RightBracket;
			case '{': return C99Lexer.Char_LeftBrace;
			case '}': return C99Lexer.Char_RightBrace;
			case '&': return C99Lexer.Char_Ampersand;
			case '~': return C99Lexer.Char_Tilde;
			case '!': return C99Lexer.Char_Bang;
			case '%': return C99Lexer.Char_Percent;
			case '^': return C99Lexer.Char_Caret;
			case '|': return C99Lexer.Char_Bar;
			case '?': return C99Lexer.Char_Question;
			case ':': return C99Lexer.Char_Colon;
			case ';': return C99Lexer.Char_SemiColon;
			case ',': return C99Lexer.Char_Comma;
			case '#': return C99Lexer.Char_Hash;
			case '\'': return C99Lexer.Char_SingleQuote;
			case '"': return C99Lexer.Char_DoubleQuote;
			case ' ': return C99Lexer.Char_Space;
			case 0x09: return C99Lexer.Char_HT;
			case 0x0A: return C99Lexer.Char_LF;
			case 0x0C: return C99Lexer.Char_FF;
			case 0x0D: return C99Lexer.Char_CR;
			case '\uffff': return C99Lexer.Char_EOF;
			default : return C99Lexer.Char_Unused;
		}
	}
	
	/**
	 * Checks if the identifier is in the keywordMap and creates the corresponding
	 * keyword token if it is, creates an identifier token otherwise.
	 */
	protected void makeKeywordOrIdentifierToken() {
		int startOffset = lexer.getLeftSpan(), endOffset = lexer.getRightSpan();
		char[] inputChars = lexer.getInputChars();
		
		StringBuffer sb = new StringBuffer(endOffset - startOffset + 1);
		for(int i = startOffset; i <= endOffset; i++) {
			sb.append(inputChars[i]);	
		}
		
		Integer keywordKind = (Integer) keywordMap.get(sb.toString());
		int token = keywordKind == null ? C99Lexer.TK_identifier : keywordKind.intValue();
		//System.out.println("Token: " + C99Parsersym.orderedTerminalSymbols[token]);
		lexer.makeToken(startOffset, endOffset, token);
	}
	
	/**
	 * Create a token.
	 */
	protected void makeToken(int token) {
		int startOffset = lexer.getLeftSpan(), endOffset = lexer.getRightSpan();
		//System.out.println("Token: " + C99Parsersym.orderedTerminalSymbols[token]);
		lexer.makeToken(startOffset, endOffset, token);
	}
	
	
	
	
}

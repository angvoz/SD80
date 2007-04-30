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

import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.C99Token;

import lpg.lpgjavaruntime.IToken;

/**
 * Maps characters in the input stream to 'token kinds' that
 * are recognized by the LPG lex parser, recognizes
 * trigraph sequences. 
 * 
 * @author Mike Kucera
 */
public class C99LexerKind {
	
	
	/**
	 * Returns the character kind at the given stream index.
	 * 
	 * Detects trigraph sequences and replaces them with
	 * their corresponding character kind. Trigraph sequences
	 * allow C code to be written in environments where the
	 * full ASCII character set is not available.
	 * 
	 * Trigraph     Equivalent
     * ========     ==========
     *   ??=            #
     *   ??/            \
     *   ??'            ^
     *   ??(            [
     *   ??)            ]
     *   ??!            |
     *   ??<            {
     *   ??>            }
     *   ??-            ~
     *    
	 * @param i index into the character stream
	 */
	public static int getKind(C99Lexer lexer, final int i) {
		int streamLength = lexer.getStreamLength();
		if(i >= streamLength)
			return C99Lexer.Char_EOF;
		
		char c = lexer.getCharValue(i);
			
		// detect trigraph sequences
		if(c == '?' && i+2 < streamLength && lexer.getCharValue(i+1) == '?') {
				
			int kind;
			switch(lexer.getCharValue(i+2)) {
				case '=' : kind = C99Lexer.Char_Hash;         break;
				case '(' : kind = C99Lexer.Char_LeftBracket;  break;
				case ')' : kind = C99Lexer.Char_RightBracket; break;
				case '/' : kind = C99Lexer.Char_BackSlash;    break;
				case '\'': kind = C99Lexer.Char_Caret;        break;
				case '<' : kind = C99Lexer.Char_LeftBrace;    break;
				case '>' : kind = C99Lexer.Char_RightBrace;   break;
				case '!' : kind = C99Lexer.Char_Bar;          break;
				case '-' : kind = C99Lexer.Char_Tilde;        break;
				default  : return getKind(c);
			}
			
			lexer.setStreamIndex(i+2); // advance the stream past the trigraph sequence
			return kind;
		}

		return getKind(c);
	}
	
	
	
	/**
	 * Creates a token, takes trigraph sequences into account.
	 */
	public static IToken makeToken(C99Lexer lexer, final int kind) {
		int startOffset = lexer.getLeftSpan();
		int endOffset   = lexer.getRightSpan();
		char[] input = lexer.getInputChars();
		
		if(startOffset == endOffset && input[startOffset] == '?' && kind != C99Parsersym.TK_Question) {
			// The token starts with a '?' but its not a question token, then it must be a trigraph.
			endOffset += 2; // make sure the toString() method of the token returns the entire trigraph sequence
		}
		
		C99Token token = new C99Token(startOffset, endOffset, kind);
		token.setRepresentation(input, startOffset, endOffset);
		return token;
	}
	
	
	/**
	 * This method is required by LPG, it maps individual characters
	 * to their character kind, the character kinds are recognized
	 * by the scanner.
	 */
	private static int getKind(char c) {
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
}

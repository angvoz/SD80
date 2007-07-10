/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c99;

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.core.dom.parser.c99.IToken;
import org.eclipse.cdt.core.dom.parser.c99.PPToken;


public class C99PPTokenComparator implements IPPTokenComparator {
		
	public boolean compare(PPToken pptoken, IToken token) {
		if(token == null)
			return false;
		
		switch(token.getKind()) {
			case C99Parsersym.TK_Hash              : return pptoken == PPToken.HASH;
			case C99Parsersym.TK_HashHash          : return pptoken == PPToken.HASHHASH;
			case C99Parsersym.TK_LeftParen         : return pptoken == PPToken.LPAREN;
			case C99Parsersym.TK_NewLine           : return pptoken == PPToken.NEWLINE;
			case C99Parsersym.TK_Comma             : return pptoken == PPToken.COMMA;
			case C99Parsersym.TK_RightParen        : return pptoken == PPToken.RPAREN;
			case C99Parsersym.TK_DotDotDot         : return pptoken == PPToken.DOTDOTDOT;
			case C99Parsersym.TK_EOF_TOKEN         : return pptoken == PPToken.EOF;
			case C99Parsersym.TK_stringlit         : return pptoken == PPToken.STRINGLIT;
			case C99Parsersym.TK_integer           : return pptoken == PPToken.INTEGER;
			case C99Parsersym.TK_LT                : return pptoken == PPToken.LEFT_ANGLE_BRACKET;
			case C99Parsersym.TK_GT                : return pptoken == PPToken.RIGHT_ANGLE_BRACKET;
			case C99Parsersym.TK_SingleLineComment : return pptoken == PPToken.SINGLE_LINE_COMMENT;
			case C99Parsersym.TK_MultiLineComment  : return pptoken == PPToken.MULTI_LINE_COMMENT;
			// an identifier might be a preprocessing directive like #if or #include
			case C99Parsersym.TK_identifier : 
				PPToken result = PPToken.getDirective(token.toString());
				return pptoken == ((result == null) ? PPToken.IDENT : result);
		}
		return false;
	}
	

	public int getKind(int tokenToMake) {
		switch(tokenToMake) {
			case KIND_IDENTIFIER        : return C99Parsersym.TK_identifier;
			case KIND_EOF               : return C99Parsersym.TK_EOF_TOKEN;
			case KIND_COMPLETION        : return C99Parsersym.TK_Completion;
			case KIND_END_OF_COMPLETION : return C99Parsersym.TK_EndOfCompletion;
			case KIND_INTEGER           : return C99Parsersym.TK_integer;
			case KIND_STRINGLIT         : return C99Parsersym.TK_stringlit;
			case KIND_INVALID           : return C99Parsersym.TK_Invalid;
			default                     : return C99Parsersym.TK_Invalid;
		}
	}


	public String[] getLPGOrderedTerminalSymbols() {
		return C99Parsersym.orderedTerminalSymbols;
	}
	
	
}

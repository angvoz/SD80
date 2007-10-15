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

import static org.eclipse.cdt.core.dom.parser.c99.PPToken.*;
import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;
import org.eclipse.cdt.core.dom.parser.c99.PPToken;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.SynthesizedToken;
import org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor.Token;


public class C99PPTokenComparator implements IPPTokenComparator<IToken> {
		
	private static final int PLACEMARKER_VALUE = Integer.MAX_VALUE;
	
	
	public PPToken getKind(IToken token) {
		if(token == null)
			return null;
		
		switch(token.getKind()) {
			case C99Parsersym.TK_Hash              : return HASH;
			case C99Parsersym.TK_HashHash          : return HASHHASH;
			case C99Parsersym.TK_LeftParen         : return LPAREN;
			case C99Parsersym.TK_NewLine           : return NEWLINE;
			case C99Parsersym.TK_Comma             : return COMMA;
			case C99Parsersym.TK_RightParen        : return RPAREN;
			case C99Parsersym.TK_DotDotDot         : return DOTDOTDOT;
			case C99Parsersym.TK_EOF_TOKEN         : return EOF;
			case C99Parsersym.TK_stringlit         : return STRINGLIT;
			case C99Parsersym.TK_integer           : return INTEGER;
			case C99Parsersym.TK_SingleLineComment : return SINGLE_LINE_COMMENT;
			case C99Parsersym.TK_MultiLineComment  : return MULTI_LINE_COMMENT;
			case C99Parsersym.TK_identifier        : return IDENT;
			case C99Parsersym.TK_charconst         : return CHARCONST;
			
			case C99Parsersym.TK_And        : return AND;
			case C99Parsersym.TK_Star       : return STAR;
			case C99Parsersym.TK_Plus       : return PLUS;
			case C99Parsersym.TK_Minus      : return MINUS;
			case C99Parsersym.TK_Tilde      : return TILDE;
			case C99Parsersym.TK_Bang       : return BANG;
			case C99Parsersym.TK_Slash      : return SLASH;
			case C99Parsersym.TK_Percent    : return PERCENT;
			case C99Parsersym.TK_RightShift : return RIGHTSHIFT;
			case C99Parsersym.TK_LeftShift  : return LEFTSHIFT;
			case C99Parsersym.TK_LT         : return LT;
			case C99Parsersym.TK_GT         : return GT;
			case C99Parsersym.TK_LE         : return LE;
			case C99Parsersym.TK_GE         : return GE;
			case C99Parsersym.TK_EQ         : return EQ;
			case C99Parsersym.TK_NE         : return NE;
			case C99Parsersym.TK_Caret      : return CARET;
			case C99Parsersym.TK_Or         : return OR;
			case C99Parsersym.TK_AndAnd     : return ANDAND;
			case C99Parsersym.TK_OrOr       : return OROR;
			case C99Parsersym.TK_Question   : return QUESTION;
			case C99Parsersym.TK_Colon      : return COLON;
			
			// TODO: will removing this case cause the switch to compile into a tableswitch bytecode?
			// tableswitch is faster than lookupswitch
			case PLACEMARKER_VALUE : return PLACEMARKER;
		}
		return null;
	}
	

	public IToken createToken(int tokenToMake, int startOffset, int endOffset, String image) {
		int kind;
		switch(tokenToMake) {
			case KIND_IDENTIFIER        : kind = C99Parsersym.TK_identifier; break;
			case KIND_COMPLETION        : kind = C99Parsersym.TK_Completion; break;
			case KIND_END_OF_COMPLETION : kind = C99Parsersym.TK_EndOfCompletion; break;
			case KIND_INTEGER           : kind = C99Parsersym.TK_integer; break;
			case KIND_STRINGLIT         : kind = C99Parsersym.TK_stringlit; break;
			case KIND_INVALID           : kind = C99Parsersym.TK_Invalid; break;
			case KIND_PLACEMARKER       : kind = PLACEMARKER_VALUE; break;
			default                     : kind = C99Parsersym.TK_Invalid; break;
		}
		
		return new SynthesizedToken(startOffset, endOffset, kind, image);
	}

	public IToken cloneToken(IToken token) {
		if(token instanceof Token) {
			return (IToken)((Token)token).clone();
		}
		throw new RuntimeException("don't know what kind of token that is"); //$NON-NLS-1$
	}


	public int getEndOffset(IToken token) {
		return token.getEndOffset();
	}

	public int getStartOffset(IToken token) {
		return token.getStartOffset();
	}

	public void setEndOffset(IToken token, int offset) {
		token.setEndOffset(offset);
	}

	public void setStartOffset(IToken token, int offset) {
		token.setStartOffset(offset);
	}
}

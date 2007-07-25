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
package org.eclipse.cdt.core.dom.parser.c99;

import java.util.HashMap;
import java.util.Map;


/**
 * Enumeration of preprocessor token kinds.
 * 
 * @author Mike Kucera
 */
public final class PPToken {
	
	public static final PPToken 
		HASH                = new PPToken(),
		STRINGLIT           = new PPToken(),
		INTEGER             = new PPToken(),
		LEFT_ANGLE_BRACKET  = new PPToken(),
		RIGHT_ANGLE_BRACKET = new PPToken(),
		HASHHASH            = new PPToken(),
		LPAREN              = new PPToken(),
		NEWLINE             = new PPToken(),
		IDENT               = new PPToken(),
		COMMA               = new PPToken(),
		RPAREN              = new PPToken(),
		DOTDOTDOT           = new PPToken(),
		SINGLE_LINE_COMMENT = new PPToken(),
		MULTI_LINE_COMMENT  = new PPToken(),
		EOF                 = new PPToken(),
		
		IF                  = new PPToken("if"),
		IFDEF               = new PPToken("ifdef"),
		IFNDEF              = new PPToken("ifndef"),
		ELIF                = new PPToken("elif"),
		ELSE                = new PPToken("else"),
		ENDIF               = new PPToken("endif"),
		DEFINE              = new PPToken("define"),
		UNDEF               = new PPToken("undef"),
		INCLUDE             = new PPToken("include"),
		INCLUDE_NEXT        = new PPToken("include_next"),
		PRAGMA              = new PPToken("pragma"),
		ERROR               = new PPToken("error"),
		WARNING             = new PPToken("warning");
	
	
	
	private String keyword = null;
	
	private PPToken() {}
	private PPToken(String keyword) { this.keyword = keyword; }
	
	
	private static final Map directiveMap = new HashMap();
	private static final String[] directives;
	
	static {
		directiveMap.put(IF.keyword,           IF);
		directiveMap.put(IFDEF.keyword,        IFDEF);
		directiveMap.put(IFNDEF.keyword,       IFNDEF);
		directiveMap.put(ELIF.keyword,         ELIF);
		directiveMap.put(ELSE.keyword,         ELSE);
		directiveMap.put(ENDIF.keyword,        ENDIF);
		directiveMap.put(DEFINE.keyword,       DEFINE);
		directiveMap.put(UNDEF.keyword,        UNDEF);
		directiveMap.put(INCLUDE.keyword,      INCLUDE);
		directiveMap.put(INCLUDE_NEXT.keyword, INCLUDE_NEXT);
		directiveMap.put(PRAGMA.keyword,       PRAGMA);
		directiveMap.put(ERROR.keyword,        ERROR);
		directiveMap.put(WARNING.keyword,      WARNING);
		
		
		directives = new String[directiveMap.size()];
		directiveMap.keySet().toArray(directives);
	}
	
	
	public static PPToken getDirective(String ident) {
		return (PPToken)directiveMap.get(ident);
	}
	
	public static String[] getPreprocessorKeywords() {
		return directives;
	}
}

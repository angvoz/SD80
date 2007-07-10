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
		
		IF                  = new PPToken(),
		IFDEF               = new PPToken(),
		IFNDEF              = new PPToken(),
		ELIF                = new PPToken(),
		ELSE                = new PPToken(),
		ENDIF               = new PPToken(),
		DEFINE              = new PPToken(),
		UNDEF               = new PPToken(),
		INCLUDE             = new PPToken(),
		INCLUDE_NEXT        = new PPToken(),
		PRAGMA              = new PPToken(),
		ERROR               = new PPToken(),
		WARNING             = new PPToken();
	
	
	private PPToken() {}
	
	
	private static final Map directives = new HashMap();
	static {
		directives.put("if",           PPToken.IF);
		directives.put("ifdef",        PPToken.IFDEF);
		directives.put("ifndef",       PPToken.IFNDEF);
		directives.put("elif",         PPToken.ELIF);
		directives.put("else",         PPToken.ELSE);
		directives.put("endif",        PPToken.ENDIF);
		directives.put("define",       PPToken.DEFINE);
		directives.put("undef",        PPToken.UNDEF);
		directives.put("include",      PPToken.INCLUDE);
		directives.put("include_next", PPToken.INCLUDE_NEXT);
		directives.put("pragma",       PPToken.PRAGMA);
		directives.put("error",        PPToken.ERROR);
		directives.put("warning",      PPToken.WARNING);
	}
	
	
	public static PPToken getDirective(String ident) {
		return (PPToken)directives.get(ident);
	}
}

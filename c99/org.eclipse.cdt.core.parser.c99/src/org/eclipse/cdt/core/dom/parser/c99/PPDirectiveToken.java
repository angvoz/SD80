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
 * Directives that are recognized by the preprocessor.
 * An identifier token is checked if its a directive token
 * when it occurs after a # at the beginning of a line.
 * 
 * @author Mike Kucera
 */
public enum PPDirectiveToken {

	IF("if"), //$NON-NLS-1$
	IFDEF("ifdef"), //$NON-NLS-1$
	IFNDEF("ifndef"), //$NON-NLS-1$
	ELIF("elif"),//$NON-NLS-1$
	ELSE("else"),//$NON-NLS-1$
	ENDIF("endif"),//$NON-NLS-1$
	DEFINE("define"),//$NON-NLS-1$
	UNDEF("undef"),//$NON-NLS-1$
	INCLUDE("include"),//$NON-NLS-1$
	INCLUDE_NEXT("include_next"),//$NON-NLS-1$
	PRAGMA("pragma"),//$NON-NLS-1$
	ERROR("error"),//$NON-NLS-1$
	WARNING("warning");//$NON-NLS-1$

	private String keyword = null;
	private PPDirectiveToken(String keyword) { this.keyword = keyword; }
	
	
	private static final Map<String,PPDirectiveToken> directiveMap = new HashMap<String,PPDirectiveToken>();
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
	
	
	public static PPDirectiveToken getTokenKind(String ident) {
		return directiveMap.get(ident);
	}
	
	public static String[] getPreprocessorKeywords() {
		return directives;
	}
}

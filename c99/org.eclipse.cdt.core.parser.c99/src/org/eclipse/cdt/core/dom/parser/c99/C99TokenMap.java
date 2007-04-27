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

import lpg.lpgjavaruntime.IToken;

import org.eclipse.cdt.internal.core.dom.parser.c99.C99Parsersym;


/**
 * Maps token kinds from new parsers back to the token kinds defined in C99Parsersym.
 * If this is not done then C99ParserAction will not behave properly.
 * 
 * @author Mike Kucera
 */
class C99TokenMap {

	// LPG token kinds start at 0
	public static int INVALID_KIND = -1;
	
	private int[] kindMap = null; 
	private Map symbolMap = new HashMap();
	
	
	
	public C99TokenMap(String[] toSymbols) {
		if(toSymbols == C99Parsersym.orderedTerminalSymbols)
			return;
		
		kindMap = new int[toSymbols.length];
		
		for(int i = 0; i < C99Parsersym.orderedTerminalSymbols.length; i++) {
			symbolMap.put(C99Parsersym.orderedTerminalSymbols[i], new Integer(i));
		}
		
		for(int i = 0; i < toSymbols.length; i++) {
			Integer kind = (Integer)symbolMap.get(toSymbols[i]);
			kindMap[i] = kind == null ? INVALID_KIND : kind.intValue();
		}
	}
	
	
	/**
	 * Maps the given token kind back to the same token kind defined in C99Parsersym.
	 */
	public int asC99Kind(int kind) {
		if(kindMap == null)
			return kind;
		
		return kind >= kindMap.length ? INVALID_KIND : kindMap[kind];
	}
	
	
	/**
	 * Maps the given token kind back to the same token kind defined in C99Parsersym.
	 */
	public int asC99Kind(IToken token) {
		return asC99Kind(token.getKind());
	}
}

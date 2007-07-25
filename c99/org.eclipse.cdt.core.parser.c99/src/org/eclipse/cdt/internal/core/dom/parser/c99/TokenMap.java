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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.parser.c99.ITokenMap;


/**
 * Maps token kinds from a sub-parser back to the corresponding
 * token kinds in a base parser.
 * 
 * @author Mike Kucera
 */
public class TokenMap implements ITokenMap {

	// LPG token kinds start at 0
	// the kind is not part of the base language parser
	public static int INVALID_KIND = -1;
	
	private int[] kindMap = null; 
	
	
	/**
	 * @param toSymbols An array of symbols where the index is the token kind and the
	 * element data is a string representing the token kind. It is expected
	 * to pass the orderedTerminalSymbols field from an LPG generated symbol
	 * file, for example C99Parsersym.orderedTerminalSymbols.
	 */
	public TokenMap(String[] toSymbols, String[] fromSymbols) {
		// If this map is not being used with an extension then it becomes an "identity map".
		if(toSymbols == fromSymbols)
			return;
		
		kindMap = new int[fromSymbols.length];
		Map toMap = new HashMap();
		
		for(int i = 0; i < toSymbols.length; i++) {
			toMap.put(toSymbols[i], new Integer(i));
		}
		
		for(int i = 0; i < fromSymbols.length; i++) {
			Integer kind = (Integer)toMap.get(fromSymbols[i]);
			kindMap[i] = kind == null ? INVALID_KIND : kind.intValue();
		}
	}
	
	
	/**
	 * Maps a token kind back to the corresponding kind define in the base C99 parser.
	 */
	public int mapKind(int kind) {
		if(kindMap == null)
			return kind;
		
		if(kind < 0 || kind >= kindMap.length)
			return INVALID_KIND;
		
		return kindMap[kind];
	}
}
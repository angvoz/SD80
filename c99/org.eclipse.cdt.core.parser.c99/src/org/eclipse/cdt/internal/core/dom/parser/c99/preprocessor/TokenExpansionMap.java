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
package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lpg.lpgjavaruntime.IToken;

/**
 * Maps a token to the macro invocation that produced the token.
 * Used to properly log the end of a macro invocation.
 * 
 * @author Mike Kucera
 *
 */
public class TokenExpansionMap {

	private Map tokenExpansionMap = new HashMap(); // HashMap<IToken, List<Macro>>
	
	public void putMacro(IToken token, IToken macroName, Macro macro) {
		LinkedList existingMacros = (LinkedList) tokenExpansionMap.get(macroName);
		if(existingMacros == null) {
			LinkedList macros = new LinkedList();
			macros.add(macro);
			tokenExpansionMap.put(token, macros);
		}
		else {
			existingMacros.addFirst(macro);
			tokenExpansionMap.put(token, existingMacros);
		}
	}
	

	public Macro[] getMacros(IToken token) {
		List macros = (List) tokenExpansionMap.get(token);
		if(macros == null || macros.isEmpty()) {
			return null;
		}
		return (Macro[]) macros.toArray(new Macro[0]);
	}
}

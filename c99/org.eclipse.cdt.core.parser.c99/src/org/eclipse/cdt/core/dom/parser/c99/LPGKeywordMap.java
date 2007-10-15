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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.dom.c99.IKeywordMap;

public abstract class LPGKeywordMap implements IKeywordMap {

	private Map<String, Integer> keywords = new HashMap<String, Integer>();
	private Set<String> builtinTypes = new HashSet<String>();
	
	
	protected abstract String[] getOrderedTerminalSymbols();

	
	public Integer getKeywordKind(String identifier) {
		return keywords.get(identifier);
	}
	
	protected void putKeyword(int kind) {
		keywords.put(getOrderedTerminalSymbols()[kind], kind);
	}
	
	protected void addBuiltinType(int kind) {
		builtinTypes.add(getOrderedTerminalSymbols()[kind]);
	}
	
	public String[] getKeywords() {
		return (String[]) keywords.keySet().toArray(new String[0]);
	}
	
	public String[] getBuiltinTypes() {
		return (String[]) builtinTypes.toArray(new String[0]);
	}
	
	public String[] getPreprocessorKeywords() {
		return PPDirectiveToken.getPreprocessorKeywords();
	}
	
	

}

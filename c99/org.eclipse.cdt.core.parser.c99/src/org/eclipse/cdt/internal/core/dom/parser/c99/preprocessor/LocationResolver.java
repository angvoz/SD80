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
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.core.parser.scanner2.FunctionStyleMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap;
import org.eclipse.cdt.internal.core.parser.scanner2.ObjectStyleMacro;


/**
 * Simple subclass of LocationMap that acts as an adapter, making it easier to 
 * adapt C99 preprocessor internals to the DOM preprocessor internals.
 * 
 * LocationResolver is responsible for:
 * - Mapping AST node offsets to source file locations (i.e. IASTNode.getFileLocation())
 * - Generating and returning AST nodes for preprocessor directives.
 * - Reslolving locations of macro expansions.
 * 
 *
 * @author Mike Kucera
 */
public class LocationResolver extends LocationMap implements IPreprocessorLog {

	
	// keep track of these wierd IMacroDefinion objects, 
	// why this isn't encapsulated in LocationMap I have no idea
	private Map macroDefinitions = new HashMap();
	
	
	/**
	 * Informs the LocationReslolver that a macro has been defined.
	 * Works for both object and function style macros.
	 */
	public void defineMacro(Macro macro) {
		int startOffset   = macro.getDirectiveStartOffset();
		int endOffset     = macro.getDirectiveEndOffset();
		int nameOffset    = macro.getNameStartOffset();
		int nameEndOffset = macro.getNameEndOffset();
		char[] macroName  = macro.getName().toString().toCharArray();
		char[] expansion  = macro.getReplacementSequenceAsString().toCharArray();
		
		IMacroDefinition macroDef;
		
		if(macro.isObjectLike()) {
			ObjectStyleMacro osm = new ObjectStyleMacro(macroName, expansion);
			macroDef = super.defineObjectStyleMacro(osm, startOffset, nameOffset, nameEndOffset, endOffset);
		}
		else {
			char[][] argList = getParamsAsChars(macro);
			FunctionStyleMacro fsm = new FunctionStyleMacro(macroName, expansion, argList);
			macroDef = super.defineFunctionStyleMacro(fsm, startOffset, nameOffset, nameEndOffset, endOffset);
		}
		
		macroDefinitions.put(macro.getName().toString(), macroDef);
	}
	
	
	private static char[][] getParamsAsChars(Macro macro) {
		List paramNames = macro.getParamNames();
		if(paramNames == null || paramNames.isEmpty())
			return new char[][] {null};
		
		char[][] argList = new char[paramNames.size()][];
		for(int i = 0; i < argList.length; i++) {
			argList[i] = ((String)paramNames.get(i)).toCharArray();
		}
		return argList;
	}
	
	
	public void undefineMacro(int directiveStartOffset, int directiveEndOffset, String macroName, int nameOffset) {
		super.encounterPoundUndef(directiveStartOffset, directiveEndOffset, macroName.toCharArray(), nameOffset, getMacroDefinition(macroName));
	}
	
	
	// TODO: what if macroDef is null?
	// TODO what if the macro isn't object style?
	public void startMacroExpansion(Macro macro, int nameStartOffset, int endOffset) {
		IMacroDefinition macroDef = getMacroDefinition(macro);
		if(macro.isObjectLike())
			super.startObjectStyleMacroExpansion(macroDef, nameStartOffset, endOffset);
		else
			super.startFunctionStyleExpansion(macroDef, getParamsAsChars(macro), nameStartOffset, endOffset);
	}
	
	
	public void endMacroExpansion(Macro macro, int offset) {
		IMacroDefinition macroDef = getMacroDefinition(macro);
		if(macro.isObjectLike())
			super.endObjectStyleMacroExpansion(macroDef, offset);
		else
			super.endFunctionStyleExpansion(macroDef, offset);
		
	}

	
	private IMacroDefinition getMacroDefinition(Macro macro) {
		return getMacroDefinition(macro.getName().toString());
	}
	
	private IMacroDefinition getMacroDefinition(String macroName) {
		return (IMacroDefinition) macroDefinitions.get(macroName);
	}
}

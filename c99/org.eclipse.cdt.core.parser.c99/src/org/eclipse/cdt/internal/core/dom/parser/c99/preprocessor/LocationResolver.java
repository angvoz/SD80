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
 * Most of the methods in IPreprocessorLog are defined in LocationMap. 
 *  
 * @author Mike Kucera
 */
public class LocationResolver extends LocationMap implements IPreprocessorLog {

	
	// keep track of these wierd IMacroDefinion objects, 
	// why this isn't encapsulated in LocationMap I have no idea
	private Map macroDefinitions = new HashMap();
	private boolean ignoreMacroExpansions;
	
	public void setIgnoreMacroExpansions(boolean ignore) {
		this.ignoreMacroExpansions = ignore;
	}
	
	/**
	 * Informs the LocationReslolver that a macro has been defined.
	 * Works for both object and function style macros.
	 */
	public void defineMacro(Macro macro) {
		int startOffset   = macro.getDirectiveStartOffset();
		int endOffset     = macro.getDirectiveEndOffset();
		int nameOffset    = macro.getNameStartOffset();
		int nameEndOffset = macro.getNameEndOffset() + 1;
		char[] macroName  = macro.getName().toCharArray();
		char[] expansion  = macro.getExpansion().toCharArray();
		
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
	

	/**
	 * Registers macros that come from IScannerInfo and the index.
	 * These will pop up in the content assist popup.
	 */
	public void registerBuiltinMacro(Macro macro) {
		if(macro == null)
			return; 
		
		char[] macroName  = macro.getName().toCharArray();
		char[] expansion  = macro.getExpansion().toCharArray();
		
		IMacroDefinition macroDef;
		if(macro.isObjectLike()) {
			ObjectStyleMacro osm = new ObjectStyleMacro(macroName, expansion);
			macroDef = super.registerBuiltinObjectStyleMacro(osm);
		}
		else {
			char[][] argList = getParamsAsChars(macro);
			FunctionStyleMacro fsm = new FunctionStyleMacro(macroName, expansion, argList);
			macroDef = super.registerBuiltinFunctionStyleMacro(fsm);
		}
		
		macroDefinitions.put(macro.getName().toString(), macroDef);
	}
	
	
	/**
	 * Returns the macro's parameters as a char[][]
	 */
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
	public void startMacroExpansion(Macro macro, int nameStartOffset, int endOffset, char[][] actualArgs) {
		if(ignoreMacroExpansions)
			return;
		
		IMacroDefinition macroDef = getMacroDefinition(macro);
		if(macro.isObjectLike()) {
			//System.out.println("C99 startObjectStyleMacroExpansion(" + nameStartOffset + "," + endOffset + ")");
			super.startObjectStyleMacroExpansion(macroDef, nameStartOffset, endOffset);
		}
		else {
			//System.out.println("C99 startFunctionStyleExpansion(" + nameStartOffset + "," + endOffset + ")");
			super.startFunctionStyleExpansion(macroDef, getParamsAsChars(macro), nameStartOffset, endOffset, actualArgs);
		}
	}
	
	
	public void endMacroExpansion(Macro macro, int offset) {
		if(ignoreMacroExpansions)
			return;
		
		IMacroDefinition macroDef = getMacroDefinition(macro);
		if(macro.isObjectLike()) {
			//System.out.println("C99 endObjectStyleMacroExpansion(" + offset + ")");
			super.endObjectStyleMacroExpansion(macroDef, offset);
		}
		else {
			//System.out.println("C99 endFunctionStyleExpansion(" + offset + ")");
			super.endFunctionStyleExpansion(macroDef, offset);
		}
		
	}

	
	private IMacroDefinition getMacroDefinition(Macro macro) {
		return getMacroDefinition(macro.getName());
	}
	
	private IMacroDefinition getMacroDefinition(String macroName) {
		return (IMacroDefinition) macroDefinitions.get(macroName);
	}


	
}

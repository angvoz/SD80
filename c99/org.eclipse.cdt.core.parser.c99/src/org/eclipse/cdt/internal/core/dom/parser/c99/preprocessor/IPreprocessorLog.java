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

import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IMacro;

/**
 * A slightly modified version of IScannerPreprocessorLog
 * 
 * @author Mike Kucera
 */
public interface IPreprocessorLog {

	public void setIgnoreMacroExpansions(boolean ignore);
	
    public void startTranslationUnit(CodeReader reader);

    public void endTranslationUnit(int offset);

    
    /**
     * Call when an inactive include is encountered.
     */
    public void startInclusion(CodeReader reader, int offset, int endOffset, int nameOffset, int nameEndoffset, char[] name, boolean systemInclude);

    public void endInclusion(CodeReader reader, int offset);

    
    /**
     * Call when an include thats in inactive code is encountered, the include is not followed.
     */
	public void encounterPoundInclude(int startOffset, int nameOffset, int nameEndOffset, int endOffset, char[] name, boolean systemInclude, boolean active);
	
	
	
    public void startMacroExpansion(Macro macro, int nameStartOffset, int endOffset, char[][] actualArguments);
	
	public void endMacroExpansion(Macro macro, int offset);
    
    public void defineMacro(Macro macro);

    public void encounterPoundIf(int startOffset, int endOffset, boolean taken, char[] condition);

    public void encounterPoundIfdef(int startOffset, int endOffset, boolean taken, char[] condition);

    public void encounterPoundIfndef(int startOffset, int endOffset, boolean taken, char[] condition);

    public void encounterPoundElse(int startOffset, int endOffset, boolean taken);

    public void encounterPoundElif(int startOffset, int endOffset, boolean taken, char[] condition);

    public void encounterPoundEndIf(int startOffset, int endOffset);

    public void encounterPoundPragma(int startOffset, int endOffset, char[] msg);

    public void encounterPoundError(int startOffset, int endOffset, char[] msg);

    public void encounterPoundWarning(int startOffset, int endOffset, char[] msg);
    
    public void undefineMacro(int directiveStartOffset, int directiveEndOffset, String macroName, int nameOffset);

	public void encounterProblem(IASTProblem problem);

	public void registerBuiltinMacro(Macro macro);
	
	public void registerIndexMacro(IMacro macro);
}

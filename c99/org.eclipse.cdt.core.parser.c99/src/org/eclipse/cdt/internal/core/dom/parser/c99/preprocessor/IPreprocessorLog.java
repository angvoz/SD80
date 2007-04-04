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

/**
 * A slightly modified version of IScannerPreprocessorLog
 * 
 * @author Mike Kucera
 */
public interface IPreprocessorLog {

    public void startTranslationUnit(CodeReader reader);

    public void endTranslationUnit(int offset);

    public void startInclusion(CodeReader reader, int offset, int endOffset, int nameOffset, int nameEndoffset, char[] name, boolean systemInclude);

    public void endInclusion(CodeReader reader, int offset);

    public void startMacroExpansion(Macro macro, int nameStartOffset, int endOffset);
	
	public void endMacroExpansion(Macro macro, int offset);
    
    public void defineMacro(Macro macro);

    public void encounterPoundIf(int startOffset, int endOffset, boolean taken);

    public void encounterPoundIfdef(int startOffset, int endOffset, boolean taken);

    public void encounterPoundIfndef(int startOffset, int endOffset, boolean taken);

    public void encounterPoundElse(int startOffset, int endOffset, boolean taken);

    public void encounterPoundElif(int startOffset, int endOffset, boolean taken);

    public void encounterPoundEndIf(int startOffset, int endOffset);

    public void encounterPoundPragma(int startOffset, int endOffset);

    public void encounterPoundError(int startOffset, int endOffset);

    public void encounterPoundWarning(int startOffset, int endOffset);
    
    public void undefineMacro(int directiveStartOffset, int directiveEndOffset, String macroName, int nameOffset);

	public void encounterPoundInclude(int startOffset, int nameOffset, int nameEndOffset, int endOffset, char[] name, boolean systemInclude, boolean active);

	public void encounterProblem(IASTProblem problem);

	public void registerBuiltinMacro(Macro macro);
}

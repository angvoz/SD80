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

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.c99.IParseResult;

/**
 * Groups the results of a parse into one handy object.
 *
 * In the future it would be nice to extend this object
 * with diagnostic information about parse errors.
 */
public class C99ParseResult implements IParseResult {
	private boolean encounteredError;
	private IASTTranslationUnit translationUnit;
	private IASTCompletionNode completionNode;
	
	
	public C99ParseResult() {}
	
	public C99ParseResult(IASTTranslationUnit tu, IASTCompletionNode compNode, boolean encounteredError) {
		this.translationUnit = tu;
		this.completionNode = compNode;
		this.encounteredError = encounteredError;
	}

	public boolean encounteredError() {
		return encounteredError;
	}
	void setEncounteredError(boolean encounteredError) {
		this.encounteredError = encounteredError;
	}
	
	public IASTTranslationUnit getTranslationUnit() {
		return translationUnit;
	}
	void setTranslationUnit(IASTTranslationUnit translationUnit) {
		this.translationUnit = translationUnit;
	}
	
	public IASTCompletionNode getCompletionNode() {
		return completionNode;
	}
	void setCompletionNode(IASTCompletionNode completionNode) {
		this.completionNode = completionNode;
	}
}

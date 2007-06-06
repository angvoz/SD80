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
package org.eclipse.cdt.core.dom.c99;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;


/**
 * The result of a parse.
 * 
 * If the parse was successful the result will contain a translation unit.
 * 
 * If the parse failed then the get methods will return null 
 * and encounteredError() will return true.
 * 
 * If it is the result of a completion parse then the completion
 * node will be available as well as the translation unit.
 * 
 * @author Mike Kucera
 */
public interface IParseResult {

	
	/**
	 * Returns true if any kind of syntax error is encountered,
	 * even if the parser was able to recover from the error
	 * and still generate an AST.
	 */
	public boolean encounteredError();

	public IASTTranslationUnit getTranslationUnit();

	public IASTCompletionNode getCompletionNode();

}

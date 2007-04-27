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
package org.eclipse.cdt.internal.core.dom.parser.c99;

import java.util.List;
import java.util.Vector;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * An AST node that represents the location of content assist
 * in the source file.
 * 
 * This node may contain the prefix text of an identifer up to the point. If
 * there is no prefix, the completion occurred at the point where a new token
 * would have begun.
 * 
 * Contains a list of name nodes, each name represents an identifier
 * at the point where content assist was invoked. There is usually
 * a single name node, however if an ambiguity is detected then that section
 * of the source may be parsed more than once (for example, as an expression then as a declaration).
 * This results in an ambiguity node in the tree and one name node for each of the ways it was parsed.
 * 
 * The full AST may be accessed via getTranslationUnit() or by following
 * the parent pointers of the name nodes.
 * 
 * @author Mike Kucera
 */
public class ASTCompletionNode implements IASTCompletionNode {

	private final List names = new Vector();
	private final String prefix;
	
	private IASTTranslationUnit tu = null;
	
	
	/**
	 * Creates a completion node.
	 */
	public ASTCompletionNode(String prefix) {
		this.prefix = prefix;
	}

	
	public void addName(IASTName name) {
		names.add(name);
	}

	
	/**
	 * Returns the length of the prefix.
	 */
	public int getLength() {
		return prefix == null ? 0 : prefix.length();
	}

	
	public IASTName[] getNames() {
		return (IASTName[]) names.toArray(new IASTName[0]);
	}

	
	/**
	 * If the point of completion was at the end of a potential identifier, this
	 * string contains the text of that identifier.
	 */
	public String getPrefix() {
		return prefix;
	}

	public IASTTranslationUnit getTranslationUnit() {
		return tu;
	}


	public void setTranslationUnit(IASTTranslationUnit tu) {
		this.tu = tu;
	}
	
}

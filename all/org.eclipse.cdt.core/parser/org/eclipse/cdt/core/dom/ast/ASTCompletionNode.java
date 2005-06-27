/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.IToken;

/**
 * This class represents the node that would occur at the point of a context
 * completion.
 * 
 * This node may contain the prefix text of an identifer up to the point. If
 * there is no prefix, the completion occurred at the point where a new token
 * would have begun.
 * 
 * The node points to the parent node where this node, if replaced by a proper
 * node, would reside in the tree.
 * 
 * @author Doug Schaefer
 */
public class ASTCompletionNode {

	private IToken completionToken;

	private List names = new ArrayList();
	
	private IASTTranslationUnit translationUnit;

	// used for debug
	public int count;
	
	/**
	 * Only constructor.
	 * 
	 * @param completionToken the completion token
	 * @param translationUnit the translation unit for this completion
	 */
	public ASTCompletionNode(IToken completionToken, IASTTranslationUnit translationUnit) {
		this.completionToken = completionToken;
		this.translationUnit = translationUnit;
	}

	/**
	 * Add a name to node.
	 * 
	 * @param name
	 */
	public void addName(IASTName name) {
		names.add(name);
	}

	/**
	 * If the point of completion was at the end of a potential identifier, this
	 * string contains the text of that identifier.
	 * 
	 * @return the prefix text up to the point of completion
	 */
	public String getPrefix() {
		return completionToken.getImage();
	}

	/**
	 * Get the length of the completion point.
	 * 
	 * @return length of completion token
	 */
	public int getLength() {
		return completionToken.getLength();
	}

	/**
	 * Get a list of names that fit in this context.
	 * 
	 * @return array of IASTName's
	 */
	public IASTName[] getNames() {
		return (IASTName[]) names.toArray(new IASTName[names.size()]);
	}

	/**
	 * Get the translation unit for this completion
	 * 
	 * @return the translation unit
	 */
	public IASTTranslationUnit getTranslationUnit() {
		return translationUnit;
	}
}

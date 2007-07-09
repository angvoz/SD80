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

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.parser.c99.IToken;



/**
 * A linked-list of tokens.
 *
 */
public class TokenList {

	private final LinkedList list = new LinkedList();
	
	
	public TokenList() {
	}
	
	public TokenList(IToken token) {
		add(token);
	}
	
	public void addFirst(IToken token) {
		list.addFirst(token);
	}
	
	public void add(IToken token) {
		list.add(token);
	}
	
	public IToken removeFirst() {
		return (IToken) list.removeFirst();
	}
	
	public IToken removeLast() {
		return (IToken) list.removeLast();
	}
	
	
	public IToken first() {
		return (IToken) list.getFirst();
	}
	
	public IToken last() {
		return (IToken) list.getLast();
	}
	
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	/**
	 */
	public int size() {
		return list.size();
	}
	
	
	public Iterator iterator() {
		return list.iterator();
	}
	
	
	/**
	 * Creates a copy of this TokenList, 
	 * the IToken objects themselves are not copied.
	 */
	public TokenList shallowCopy() {
		TokenList newList = new TokenList();
		for(Iterator iter = iterator(); iter.hasNext();) {
			newList.add((IToken)iter.next());
		}
		return newList;
	}
	
	
	public String toString() {
		if(isEmpty())
			return ""; //$NON-NLS-1$
		
		StringBuffer sb = new StringBuffer();
		
		Iterator iter = iterator();
		IToken prevToken = (IToken) iter.next();
		sb.append(prevToken.toString());
		
		while(iter.hasNext()) {
			IToken token = (IToken) iter.next();
			addSpaceBetween(sb, prevToken, token);
			sb.append(token.toString());
			prevToken = token;
		}
		return sb.toString();
	}
	
	/**
	 * Adds the number of characters of whitespace between the two tokens.
	 */
	private static void addSpaceBetween(StringBuffer sb, IToken t1, IToken t2) {
		int numSpaces = t2.getStartOffset() - (t1.getEndOffset() + 1);
		for(int i = 0; i < numSpaces; i++) {
			sb.append(' ');
		}
	}
	
	
	/**
	 * Used for testing.
	 */
	public int[] kindArray() {
		int[] kinds = new int[list.size()];
		for(int i = 0; i < kinds.length; i++ ) {
			kinds[i] = ((IToken)list.get(i)).getKind();
		}
		return kinds;
	}
	
	
}

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

import lpg.lpgjavaruntime.IToken;

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
	
	public String toString() {
		return list.toString();
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

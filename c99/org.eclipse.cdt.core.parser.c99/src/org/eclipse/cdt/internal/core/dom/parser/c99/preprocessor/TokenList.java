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


/**
 * A linked-list of tokens.
 *
 * TODO: this class is redundant in Java 5, get rid of it and replace with List<IToken>
 */
public class TokenList<TKN> implements Iterable<TKN> {

	private final LinkedList<TKN> list = new LinkedList<TKN>();
	
	
	public TokenList() {
	}
	
	public TokenList(TKN token) {
		add(token);
	}
	
	public void addFirst(TKN token) {
		list.addFirst(token);
	}
	
	public void add(TKN token) {
		list.add(token);
	}
	
	public TKN removeFirst() {
		return list.removeFirst();
	}
	
	public TKN removeLast() {
		return list.removeLast();
	}
	
	
	public TKN first() {
		return list.getFirst();
	}
	
	public TKN last() {
		return list.getLast();
	}
	
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	/**
	 */
	public int size() {
		return list.size();
	}
	
	
	public Iterator<TKN> iterator() {
		return list.iterator();
	}
	
	public String toString() {
		return list.toString();
	}
	
	/**
	 * Creates a copy of this TokenList, 
	 * the IToken objects themselves are not copied.
	 */
	public TokenList<TKN> shallowCopy() {
		TokenList<TKN> newList = new TokenList<TKN>();
		for(TKN t : this) {
			newList.add(t);
		}
		return newList;
	}
}

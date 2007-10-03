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

package org.eclipse.cdt.core.dom.parser.c99;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Stack used to store AST nodes while the AST is built during the parse.
 * 
 * Some grammar rules have arbitrary length lists on the right side.
 * For example the rule for compound statements (where block_item_list is any 
 * number of statements or declarations):
 * 
 * compound-statement ::= '{' <openscope> block_item_list '}'
 * 
 * There is a problem when trying to build the AST node for the compound statment...
 * you don't know how many block_items are contained in the compound statment, so 
 * you don't know how many times to pop the AST stack.
 * 
 * One inelegant solution is to count the block-items as they are parsed. This
 * is inelegant because nested compound-statments are allowed so you would
 * have to maintain several counts at the same time.
 * 
 * This class represents a ASTStack that is implemented as a stack of "AST Scopes".
 * There is a special grammar rule <openscope> that creates a new AST Scope. 
 * So, in order to consume all the block_items, all that has to be done is 
 * iterate over the topmost scope and then close it when done.
 * 
 * 
 * @author Mike Kucera
 */
public class ASTStack<T> {
	// TODO rename to ScopedStack, or MarkedStack, but not ASTStack
	
	// Stores the AST nodes as the AST is being built
	private LinkedList<T> topScope = new LinkedList<T>();
	
	// A stack of stacks, used to implement scoping of the astStack
	private final LinkedList<LinkedList<T>> astScopeStack = new LinkedList<LinkedList<T>>();
	
	
	
	/**
	 * Saves the current scope and pushes a new one.
	 * Usually called by the parser actions.
	 */
	public void openASTScope() {
		astScopeStack.add(topScope); 
		topScope = new LinkedList<T>();
	}
	
	
	/**
	 * Usually called by a reduction rule in this class.
	 */
	public List<T> closeASTScope() {
		List<T> temp = topScope;
		topScope = astScopeStack.removeLast();
		return temp;
	}
	
	public void push(T o) {
		topScope.add(o);
	}
	
	public T pop() {
		return topScope.removeLast();
	}
	
	public T peek() {
		return topScope.getLast();
	}
	
	public Object[] topScopeArray(Object[] type) {
		return topScope.toArray(type);
	}
	
	public List<T> topScope() {
		return topScope;
	}
	
	/**
	 * Returns an iterator that will iterate over the topmost scope
	 * starting at the bottom.
	 */
	public Iterator<T> topScopeIterator() { 
		return topScope.iterator();
	}

	
	public boolean isEmpty() {
		return topScope.isEmpty() && astScopeStack.isEmpty();
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(List<T> scope : astScopeStack)
			appendScopeContents(sb, scope);
		appendScopeContents(sb, topScope);
		return sb.toString();
	}


	private void appendScopeContents(StringBuffer sb, List<T> scope) {
		sb.append('[');
		boolean first = true;
		for(T t : scope) {
			if(first) 
				first = false;
			else
				sb.append(',');
			sb.append(t);
		}
		sb.append(']');
	}
	
	
}

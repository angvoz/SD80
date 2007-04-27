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

import lpg.lpgjavaruntime.IToken;

/**
 * Represents a list of tokens that compromise an argument to a macro invocation.
 * 
 * When computing a macro invocation the arguments to the
 * macro are usually recursively processed. However, when a
 * parameter is an operand to the ## operator then the raw (unprocessed)
 * argument is used. This class allows access to both the processed
 * and unprocessed versions of the argument.
 *   
 * @author Mike Kucera
 */
class MacroArgument {
	
	private final TokenList rawTokens;
	private final IProcessCallback processCallback;
	
	private TokenList processedTokens = null;
	
	/**
	 * If the argument needs to be recursively processed then
	 * this class will call back into the preprocessor to
	 * do the processing. The preprocessor must supply a callback
	 * so this can be done.
	 */
	public interface IProcessCallback {
		TokenList process(TokenList tokens);
	}
	
	
	/**
	 * Precondition: tokens != null
	 */
	public MacroArgument(TokenList tokens, IProcessCallback processor) {
		if(tokens == null)
			throw new IllegalArgumentException(Messages.getString("MacroArgument.0")); //$NON-NLS-1$
		this.rawTokens = tokens;
		this.processCallback = processor;
	}
	
	
	/**
	 * Returns the tokens that make up the macro argument without
	 * recursivley processing them.
	 */
	public TokenList getRawTokens() {
		return rawTokens;
	}
	
	
	/**
	 * Calls back into the preprocessor to recursively process this argument.
	 * 
	 * If this method is called then getRawTokens() will probably start
	 * returning an empty list.
	 */
	public synchronized TokenList getProcessedTokens() {
		if(processCallback == null)
			return rawTokens;
		
		if(processedTokens == null)
			processedTokens = processCallback.process(cloneTokenList(rawTokens));
		
		return processedTokens; 
	}
	
	
	public boolean isEmpty() {
		return rawTokens.isEmpty();
	}
	
	private static TokenList cloneTokenList(TokenList orig) {
		TokenList clone = new TokenList();
		for(Iterator iter = orig.iterator(); iter.hasNext(); ) {
			IToken token = (IToken) iter.next();
			clone.add(new C99Token(token)); // TODO: remove dependancy on C99Token
		}
		return clone;
	}
	
	
	public String toString() {
		return rawTokens.toString();
	}
}

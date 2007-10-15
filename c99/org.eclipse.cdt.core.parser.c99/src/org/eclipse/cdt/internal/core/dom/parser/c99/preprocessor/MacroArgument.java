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

import org.eclipse.cdt.core.dom.c99.IPPTokenComparator;


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
class MacroArgument<TKN> {
	
	private final TokenList<TKN> rawTokens;
	private final IProcessCallback<TKN> processCallback;
	private final ObjectTagger<TKN,String> disabledTokens;
	
	private TokenList<TKN> processedTokens = null;
	
	
	/**
	 * If the argument needs to be recursively processed then
	 * this class will call back into the preprocessor to
	 * do the processing. The preprocessor must supply a callback
	 * so this can be done.
	 */
	public interface IProcessCallback<TKN> {
		TokenList<TKN> process(TokenList<TKN> tokens);
	}
	
	
	/**
	 * Precondition: tokens != null
	 */
	public MacroArgument(TokenList<TKN> tokens, IProcessCallback<TKN> processor, ObjectTagger<TKN,String> disabledTokens) {
		if(tokens == null)
			throw new IllegalArgumentException(Messages.getString("MacroArgument.0")); //$NON-NLS-1$
		this.rawTokens = tokens;
		this.processCallback = processor;
		this.disabledTokens = disabledTokens;
	}
	
	
	/**
	 * Returns the tokens that make up the macro argument without
	 * recursively processing them.
	 */
	public TokenList<TKN> getRawTokens() {
		// return a copy because the tokens may be needed more than once
		return rawTokens.shallowCopy();
	}
	
	
	/**
	 * Calls back into the preprocessor to recursively process this argument.
	 * 
	 * If this method is called then getRawTokens() will probably start
	 * returning an empty list.
	 */
	public synchronized TokenList<TKN> getProcessedTokens(IPPTokenComparator<TKN> comparator) {
		if(processCallback == null)
			return rawTokens;
		
		if(processedTokens == null)
			processedTokens = processCallback.process(cloneTokenList(rawTokens, comparator));
		
		// return a copy because the tokens may be needed more than once
		return processedTokens.shallowCopy(); 
	}
	
	
	public boolean isEmpty() {
		return rawTokens.isEmpty();
	}
	
	// TODO: now that TokenList.shallowCopy() exists can this method be removed?
	private TokenList<TKN> cloneTokenList(TokenList<TKN> orig, IPPTokenComparator<TKN> comparator) {
		TokenList<TKN> clone = new TokenList<TKN>();
		for(TKN t : orig) {
			TKN newToken = comparator.cloneToken(t);
			disabledTokens.shareTags(t, newToken);
			clone.add(newToken);
		}
		return clone;
	}
	
	
//	public String toString() {
//		return C99Preprocessor.tokensToString(comparator, rawTokens);
//	}
}

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

import java.util.List;

import lpg.lpgjavaruntime.IToken;

/**
 * Provides an interface to the token stream that
 * can be used by the parser semantic actions (C99ParseAction).
 * 
 * Allows the semantic actions to directly inspect the token 
 * stream. Used to calculate AST node offsets and for 
 * other purposes.
 * 
 * @author Mike Kucera
 *
 */
public interface IParserActionTokenProvider {
	public IToken getRightIToken();
	public IToken getLeftIToken();
	public IToken getEOFToken();
	public int getRuleTokenCount();
	public List<IToken> getRuleTokens();
	public List<IToken> getCommentTokens();
}

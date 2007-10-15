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


/**
 * A token that is created by the preprocessor, as opposed to being
 * created by the lexer.
 * 
 * The preprocessor needs to create tokens when expanding macros
 * and doing token pasting.
 *  
 * @author Mike Kucera
 */
public class SynthesizedToken extends Token {

	
	public SynthesizedToken(int startOffset, int endOffset, int kind, String source) {
		this(startOffset, endOffset, kind, source.toCharArray());
	}
	
	public SynthesizedToken(int startOffset, int endOffset, int kind, char[] source) {
		super(startOffset, endOffset, kind, source);
		super.setSourceIndex(0, source.length - 1); // important, this is the big difference
	}
	
	public SynthesizedToken clone() {
		return new SynthesizedToken(getStartOffset(), getEndOffset(), getKind(), getSource());
	}
}

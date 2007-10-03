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

import lpg.lpgjavaruntime.PrsStream;

import org.eclipse.cdt.core.dom.parser.c99.IToken;


/**
 * Our implementation of LPG's IToken interface.
 * 
 * @author Mike Kucera
 */
public class Token implements IToken {

	public static final Token DUMMY_TOKEN = new SynthesizedToken(0, 0, 0, "<dummy>"); //$NON-NLS-1$

	// Used to set an attribute on the token in order to relay more information
	// than just the token's kind. Used by the preprocessor.
	private int preprocessorAttribute = ATTR_NO_ATTRIBUTE;

	private char[] source;
	
	private int kind;
	
	// the start offset of the token in terms of the offsets calculated by the preprocessor
	private int startOffset;
	private int endOffset;
	// the actual index into the source array where the characters that make up this token start
	
	private int sourceStartIndex;
	private int sourceEndIndex;

	private int tokenIndex;
	private int adjunctIndex;
	
	private String cachedString = null;
	
	public Token(int sourceStartIndex, int sourceEndIndex, int kind, char[] source) {
		this.source = source;
		this.kind = kind;
		
		this.startOffset = sourceStartIndex;
		this.sourceStartIndex  = sourceStartIndex;
		
		this.endOffset = sourceEndIndex;
		this.sourceEndIndex = sourceEndIndex;
	}
	
	
	public void setSourceIndex(int startIndex, int endIndex) {
		this.sourceStartIndex = startIndex;
		this.sourceEndIndex = endIndex;
	}
	
	protected char[] getSource() {
		return source;
	}

	protected int sourceLength() {
		return sourceEndIndex - sourceStartIndex + 1;
	}
	
	/**
	 * Two SourceToken objects are considered equals if
	 * st1.toString().equals(st2.toString()).
	 * 
	 * Two tokens with different kind may be equal,
	 * it is strictly a text compare.
	 */
	public boolean equals(Object o) {
		if(!(o instanceof Token))
			return false;
		
		Token t = (Token)o;
		
		int length = sourceLength();
		// also makes sure we don't get an ArrayIndexOutOfBoundsException
		if(length != t.sourceLength())
			return false;
		
		for(int i = 0; i < length; i++) {
			if(source[sourceStartIndex+i] != t.source[t.sourceStartIndex+i]) {
				return false;
			}
		}
		return true;
	}

	
	
	public int hashCode() {
		// TODO: for better performance, a better hashing function should be used
		int hash = 0;
		for(int i = sourceStartIndex; i <= sourceEndIndex; i++) {
			char c = source[i];
			hash = (hash * 31) ^ c;
		}
		return hash;
	}
	

	
	public String toString() {
		if(cachedString == null)
			cachedString = new String(source, sourceStartIndex, sourceLength());
		return cachedString;
	}
	
	public Token clone() {
		Token t = new Token(startOffset, endOffset, kind, source);
		t.setSourceIndex(sourceStartIndex, sourceEndIndex);
		t.setPreprocessorAttribute(preprocessorAttribute);
		return t;
	}
	
	
	// Covariant return types would be nice, too bad we are stuck in 1.4 land
	public lpg.lpgjavaruntime.IToken[] getFollowingAdjuncts() {
		return null;
	}

	public lpg.lpgjavaruntime.IToken[] getPrecedingAdjuncts() {
		return null;
	}

	public int getPreprocessorAttribute() {
		return preprocessorAttribute;
	}

	public void setPreprocessorAttribute(int preprocessorAttribute) {
		this.preprocessorAttribute = preprocessorAttribute;
	}


	public int getAdjunctIndex() {
		return adjunctIndex;
	}


	public int getColumn() {
		return 0;
	}


	public int getEndColumn() {
		return 0;
	}


	public int getEndLine() {
		return 0;
	}


	public int getEndOffset() {
		return endOffset;
	}


	public int getKind() {
		return kind;
	}


	public int getLine() {
		return 0;
	}


	public PrsStream getPrsStream() {
		return null;
	}


	public int getStartOffset() {
		return startOffset;
	}


	public int getTokenIndex() {
		return tokenIndex;
	}


	public String getValue(char[] arg0) {
		return toString();
	}


	public void setAdjunctIndex(int adjunctIndex) {
		this.adjunctIndex = adjunctIndex;
	}


	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}


	public void setKind(int kind) {
		this.kind = kind;
	}


	public void setStartOffset(int startOffset) {
		this.startOffset = startOffset;
	}


	public void setTokenIndex(int tokenIndex) {
		this.tokenIndex = tokenIndex;
	}

}

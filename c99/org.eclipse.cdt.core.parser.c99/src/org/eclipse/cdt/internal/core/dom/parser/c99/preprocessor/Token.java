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

import org.eclipse.cdt.core.dom.parser.c99.IToken;

import lpg.lpgjavaruntime.AbstractToken;


/**
 * This Token class is more flexible than the Token class provided by LPG.
 * The main difference is that it is possible to specify the String 
 * representation of the token. This is very useful when 
 * new tokens are created during preprocessing that don't directly map 
 * to the underlying source code.
 * 
 * @author Mike Kucera
 */
public class Token extends AbstractToken implements IToken {

	public static final Token DUMMY_TOKEN = new Token(0, 0, 0, "<dummy>"); //$NON-NLS-1$
	
	private String representation;
	
	private char[] source;
	private int sourceStartOffset;
	private int sourceEndOffset;
	
	// Used to set an attribute on the token in order to relay more information
	// than just the token's kind. Used by the preprocessor.
	private int preprocessorAttribute = ATTR_NO_ATTRIBUTE;
	
	// class invariant: representation == null ^ source == null
	
	
	public Token(int startOffset, int endOffset, int kind, String representation) {
		super(null, startOffset, endOffset, kind);
		this.representation = representation;
		this.source = null;
	}
	
	public Token(int startOffset, int endOffset, int kind, char[] source) {
		super(null, startOffset, endOffset, kind);
		this.source = source;
		this.sourceStartOffset = startOffset;
		this.sourceEndOffset = endOffset;
		this.representation = null;
	}

	public Token(IToken t) {
		super(null, t.getStartOffset(), t.getEndOffset(), t.getKind());
		if(t instanceof Token) {
			Token c99t = (Token) t;
			if(c99t.representation != null)
				setRepresentation(c99t.representation);
			else if(c99t.source != null)
				setRepresentation(c99t.source, c99t.sourceStartOffset, c99t.sourceEndOffset);
			preprocessorAttribute = c99t.getPreprocessorAttribute();
		}
		else {
			this.representation = t.toString();
			this.source = null;
		}
	}
	
	
	public void setRepresentation(String representation) {
		this.representation = representation;
		this.source = null;
	}
	
	
	public void setRepresentation(char[] source, int startOffset, int endOffset) {
		this.source = source;
		this.sourceStartOffset = startOffset;
		this.sourceEndOffset = endOffset;
		this.representation = null;
	}
	
	
	public String toString() {
		if(source != null) {
			return new String(source, sourceStartOffset, sourceEndOffset - sourceStartOffset + 1);
		}
		else if(representation != null){
			return representation;
		}
		return ""; //$NON-NLS-1$
	}
	
	public Object clone() {
		return new Token(this);
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

}

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

import lpg.lpgjavaruntime.AbstractToken;
import lpg.lpgjavaruntime.IToken;

/**
 * A C99Token is more flexible than the Token class provided by LPG.
 * The main difference is that it is possible to specify the String 
 * representation of the token. This is very useful when 
 * new tokens are created during preprocessing that don't directly map 
 * to the underlying source code.
 * 
 * @author Mike Kucera
 */
public class C99Token extends AbstractToken implements IToken {

	public static final C99Token DUMMY_TOKEN = new C99Token(0, 0, 0, "<dummy>");
	
	
	// Class invariant: representation == null | source == null;
	
	private String representation;
	
	
	private char[] source;
	private int sourceStartOffset;
	private int sourceEndOffset;
	
	
	public C99Token(int startOffset, int endOffset, int kind, String representation) {
		this(startOffset, endOffset, kind);
		setRepresentation(representation);
	}
	
	public C99Token(int startOffset, int endOffset, int kind) {
		super(null, startOffset, endOffset, kind);
	}

	public C99Token(IToken t) {
		this(t.getStartOffset(), t.getEndOffset(), t.getKind());
		if(t instanceof C99Token) {
			C99Token c99t = (C99Token) t;
			if(c99t.representation != null)
				setRepresentation(c99t.representation);
			else if(c99t.source != null)
				setRepresentation(c99t.source, c99t.sourceStartOffset, c99t.sourceEndOffset);
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
		else {
			return representation;
		}
	}
	
	public Object clone() {
		return new C99Token(this);
	}
	
	
	public IToken[] getFollowingAdjuncts() {
		// TODO Auto-generated method stub
		return null;
	}

	public IToken[] getPrecedingAdjuncts() {
		// TODO Auto-generated method stub
		return null;
	}

}

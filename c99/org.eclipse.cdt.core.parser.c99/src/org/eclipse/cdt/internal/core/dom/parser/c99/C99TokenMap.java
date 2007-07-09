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
package org.eclipse.cdt.internal.core.dom.parser.c99;

import org.eclipse.cdt.core.dom.parser.c99.TokenMap;

public class C99TokenMap extends TokenMap {

	public C99TokenMap() {
		super(C99Parsersym.orderedTerminalSymbols);
	}
	
	public int getCompletionTokenKind() {
		return C99Parsersym.TK_Completion;
	}

	public int getEOFTokenKind() {
		return C99Parsersym.TK_EOF_TOKEN;
	}

	public int getEndOfCompletionTokenKind() {
		return C99Parsersym.TK_EndOfCompletion;
	}

	public int getIntegerTokenKind() {
		return C99Parsersym.TK_integer;
	}

	public int getInvalidTokenKind() {
		return C99Parsersym.TK_Invalid;
	}

	public int getStringLitTokenKind() {
		return C99Parsersym.TK_stringlit;
	}

	public int getIdentifierTokenKind() {
		return C99Parsersym.TK_identifier;
	}

}

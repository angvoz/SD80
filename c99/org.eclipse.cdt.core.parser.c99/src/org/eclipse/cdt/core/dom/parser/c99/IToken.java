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

/**
 * An extension to the LPG IToken interface that adds an attribute field,
 * this field allows to preprocessor to tag a token with a bit of
 * useful information.
 * 
 * @author Mike Kucera
 *
 */
public interface IToken extends lpg.lpgjavaruntime.IToken {

	public static final int ATTR_NO_ATTRIBUTE = 0;
	
	// The token is a place marker, created when an empty parameter is passed to a macro.
	public static final int ATTR_PLACE_MARKER = 1;
	
	// The token is a macro name that should not be expanded, used to avoid macro recursion.
	public static final int ATTR_DISABLED_MACRO_NAME = 2;
	
	// The identifier token is a parameter, makes the macro invocation code a bit cleaner.
	public static final int ATTR_PARAMETER = 3;
	
	
	public int getPreprocessorAttribute();
	
	public void setPreprocessorAttribute(int attr);
	
}

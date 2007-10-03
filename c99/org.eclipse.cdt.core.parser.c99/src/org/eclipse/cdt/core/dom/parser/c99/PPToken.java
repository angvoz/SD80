/*******************************************************************************
* Copyright (c) 2006, 2007 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/
package org.eclipse.cdt.core.dom.parser.c99;



/**
 * Enumeration of preprocessor token kinds.
 * Does not include directive tokens.
 * 
 * @see PPDirectiveToken
 * 
 * @author Mike Kucera
 */
public enum PPToken {
	
	HASH,     
	STRINGLIT,         
	INTEGER,           
	LEFT_ANGLE_BRACKET,
	RIGHT_ANGLE_BRACKET, 
	HASHHASH,            
	LPAREN,             
	NEWLINE,            
	IDENT,             
	COMMA,              
	RPAREN,             
	DOTDOTDOT,          
	SINGLE_LINE_COMMENT, 
	MULTI_LINE_COMMENT, 
	EOF;

}

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

import lpg.lpgjavaruntime.IToken;


/**
 * Maps tokens defined in parser extensions back to the token kinds
 * defined in the C99 parser.
 * 
 * When LPG is used to generate a parser extension it will
 * generate all-new token kinds. In order for the semantic actions to be able
 * to interpret these token kinds correctly they will be mapped back
 * to the token kinds defined in C99Parsersym. 
 * 
 * @author Mike Kucera
 */
public interface ITokenMap {

	/**
	 * Maps the given token kind back to the same token kind defined in C99Parsersym.
	 */
	int asC99Kind(int kind);

	/**
	 * Maps the given token kind back to the same token kind defined in C99Parsersym.
	 */
	int asC99Kind(IToken token);

}

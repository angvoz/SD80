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
 * A keyword map provides the mechanism to add new keywords. 
 * Anything that is parsed as an identifier is then checked against
 * a keyword map in case the identifier is actually a keyword.
 * 
 * Maps keywords to token kinds.
 * 
 * @author Mike Kucera
 */
public interface IKeywordMap {
	
	/**
	 * Returns the token kind for the given string, 
	 * returns null if the string is not a keyword.
	 */
	public Integer getKeywordKind(String identifier);
}

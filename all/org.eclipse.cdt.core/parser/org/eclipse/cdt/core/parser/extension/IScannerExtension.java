/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.extension;

import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author jcamelon
 */
public interface IScannerExtension extends Cloneable {
	
	public void setScanner( IScanner scanner );
	public Object clone( );

	public String initializeMacroValue( String original );

	/**
	 * 
	 */
	public void setupBuiltInMacros(ParserLanguage language);
}

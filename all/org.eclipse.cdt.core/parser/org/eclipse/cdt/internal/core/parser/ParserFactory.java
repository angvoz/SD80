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
package org.eclipse.cdt.internal.core.parser;

import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.full.FullParseASTFactory;
import org.eclipse.cdt.internal.core.parser.ast.quick.QuickParseASTFactory;

/**
 * @author jcamelon
 *
 */
public class ParserFactory {

	public static IASTFactory createASTFactory( boolean quickParse )
	{
		if( quickParse )
			return new QuickParseASTFactory(); 
		else
			return new FullParseASTFactory(); 
	}
}

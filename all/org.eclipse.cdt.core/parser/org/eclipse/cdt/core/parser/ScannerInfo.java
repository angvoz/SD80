/*******************************************************************************
 * Copyright (c) 2002, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Rational Software - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Collections;
import java.util.Map;

/**
 * Implementation of the {@link IScannerInfo} interface. Allows to configure the preprocessor.
 */
public class ScannerInfo implements IScannerInfo
{
	private Map<String, String> definedSymbols = Collections.emptyMap(); 
	private String [] includePaths = {}; 
	
	public ScannerInfo()
	{
	}
	
	public ScannerInfo(Map<String, String> macroDefinitions, String[] includeSearchPath)
	{
		if (macroDefinitions != null) {
			definedSymbols = macroDefinitions;
		}
		if (includeSearchPath != null) {
			includePaths = includeSearchPath;
		}
	}
		
	public ScannerInfo(Map<String, String> macroDefinitions) {
		this(macroDefinitions, null);
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IScannerInfo#getDefinedSymbols()
     */
    public Map<String, String> getDefinedSymbols()
    {
        return definedSymbols;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IScannerInfo#getIncludePaths()
     */
    public String[] getIncludePaths()
    {
        return includePaths;
    }
}

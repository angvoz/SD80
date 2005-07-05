/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * @author jcamelon
 *
 */
public class ExtendedScannerInfo extends ScannerInfo implements IExtendedScannerInfo {

	private static final String [] EMPTY_STRING_ARRAY = new String[ 0 ];
	private String [] m, i, localIncludePaths;
	
	public ExtendedScannerInfo()
	{
	}
	
	public ExtendedScannerInfo( Map d, String [] incs )
	{
		super(d,incs);
	}
	
	public ExtendedScannerInfo( Map d, String [] incs, String [] macros, String [] includes )
	{
		super(d,incs);
		m = macros;
		i = includes;
	}
	
	public ExtendedScannerInfo( IScannerInfo info )
	{
		super(info.getDefinedSymbols(), info.getIncludePaths());
		if (info instanceof IExtendedScannerInfo) {
			IExtendedScannerInfo einfo = (IExtendedScannerInfo)info;
			m = einfo.getMacroFiles();
			i = einfo.getIncludeFiles();
			localIncludePaths = einfo.getLocalIncludePath();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getMacroFiles()
	 */
	public String[] getMacroFiles() {
		if( m == null ) return EMPTY_STRING_ARRAY;
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getIncludeFiles()
	 */
	public String[] getIncludeFiles() {
		if( i == null ) return EMPTY_STRING_ARRAY;
		return i;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getLocalIncludePath()
	 */
	public String[] getLocalIncludePath() {
		if ( localIncludePaths == null ) return EMPTY_STRING_ARRAY;
		return localIncludePaths;
	}
}

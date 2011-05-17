/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.cdt.core.IErrorParser2;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;


public class GCCBuildCommandPatternHighlighter extends RegexErrorParser implements IErrorParser2{
	public GCCBuildCommandPatternHighlighter() {
		{
			String pat = GCCBuildCommandParser.PATTERN_COMPILE_UNQUOTED_FILE.pattern();
			String fileExpr = "$"+GCCBuildCommandParser.PATTERN_UNQUOTED_FILE_GROUP; //$NON-NLS-1$
			String descExpr = "$0"; //$NON-NLS-1$
			addPattern(new RegexErrorPattern(pat, fileExpr, null, descExpr, null, IMarkerGenerator.SEVERITY_WARNING, true));
		}
		{
			String pat = GCCBuildCommandParser.PATTERN_COMPILE_QUOTED_FILE.pattern();
			String fileExpr = "$"+GCCBuildCommandParser.PATTERN_QUOTED_FILE_GROUP; //$NON-NLS-1$
			String descExpr = "$0"; //$NON-NLS-1$
			addPattern(new RegexErrorPattern(pat, fileExpr, null, descExpr, null, IMarkerGenerator.SEVERITY_WARNING, true));
		}
	}
	
	private GCCBuildCommandPatternHighlighter(String id, String name) {
		super(id, name);
	}


	@Override
	public Object clone() throws CloneNotSupportedException {
		GCCBuildCommandPatternHighlighter that = new GCCBuildCommandPatternHighlighter(getId(), getName());
		for (RegexErrorPattern pattern : getPatterns()) {
			that.addPattern((RegexErrorPattern)pattern.clone());
		}
		return that;
	}

	public int getProcessLineBehaviour() {
		return KEEP_LONGLINES;
	}

}

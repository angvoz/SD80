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
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;


public class GCCBuildCommandPatternHighlighter extends RegexErrorParser implements IErrorParser2{
	// ID of the parser taken from the extension point
	private static final String GCC_BUILD_COMMAND_PARSER_EXT = "org.eclipse.cdt.make.core.build.command.parser.gcc"; //$NON-NLS-1$

	public GCCBuildCommandPatternHighlighter() {
		init(GCC_BUILD_COMMAND_PARSER_EXT);
	}

	private GCCBuildCommandPatternHighlighter(String id, String name) {
		super(id, name);
	}
	
	protected void init(String buildCommandParserId) {
		GCCBuildCommandParser gccBuildCommandParser = (GCCBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(buildCommandParserId);
		{
			String pat = gccBuildCommandParser.getPatternCompileUnquotedFile();
			String fileExpr = "$"+gccBuildCommandParser.getGroupForPatternUnquotedFile(); //$NON-NLS-1$
			String descExpr = "$0"; //$NON-NLS-1$
			addPattern(new RegexErrorPattern(pat, fileExpr, null, descExpr, null, IMarkerGenerator.SEVERITY_WARNING, true));
		}
		{
			String pat = gccBuildCommandParser.getPatternCompileQuotedFile();
			String fileExpr = "$"+gccBuildCommandParser.getGroupForPatternQuotedFile(); //$NON-NLS-1$
			String descExpr = "$0"; //$NON-NLS-1$
			addPattern(new RegexErrorPattern(pat, fileExpr, null, descExpr, null, IMarkerGenerator.SEVERITY_WARNING, true));
		}
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

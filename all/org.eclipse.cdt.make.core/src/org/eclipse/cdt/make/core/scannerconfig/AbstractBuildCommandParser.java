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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser2;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.errorparsers.RegexErrorParser;
import org.eclipse.cdt.core.errorparsers.RegexErrorPattern;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;

public abstract class AbstractBuildCommandParser extends AbstractLanguageSettingsOutputScanner implements
		ILanguageSettingsBuildOutputScanner {

	private static final Pattern PATTERN_OPTIONS = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$
	private static final int PATTERN_OPTION_GROUP = 0;

	@Override
	protected String parseResourceName(String line) {
		if (line==null) {
			return null;
		}
		
		String sourceFileName = null;
		String patternCompileUnquotedFile = getPatternCompileUnquotedFile();
		Matcher fileMatcher = Pattern.compile(patternCompileUnquotedFile).matcher(line);
	
		if (fileMatcher.matches()) {
			sourceFileName = fileMatcher.group(getGroupForPatternUnquotedFile());
		} else {
			String patternCompileQuotedFile = getPatternCompileQuotedFile();
			fileMatcher = Pattern.compile(patternCompileQuotedFile).matcher(line);
			if (fileMatcher.matches()) {
				sourceFileName = fileMatcher.group(getGroupForPatternQuotedFile());
			}
		}
		return sourceFileName;
	}

	@Override
	protected List<String> parseOptions(String line) {
		if (line==null) {
			return null;
		}
		
		List<String> options = new ArrayList<String>();
		Matcher optionMatcher = PATTERN_OPTIONS.matcher(line);
		while (optionMatcher.find()) {
			String option = optionMatcher.group(PATTERN_OPTION_GROUP);
			if (option!=null) {
				options.add(option);
			}
		}
		return options;
	}

	@Override
	public boolean processLine(String line, ErrorParserManager epm) {
		return super.processLine(line, epm);
	}

	@SuppressWarnings("nls")
	private String getGccCommandPattern() {
		String parameter = getCustomParameter();
		return "(" + parameter + ")";
	}

	@SuppressWarnings("nls")
	private String getPatternCompileUnquotedFile() {
		String patternFileName = "([^'\"\\s]*\\." + getPatternFileExtensions() + ")";
		return "\\s*\"?" + getGccCommandPattern() + "\"?.*\\s" + patternFileName + "(\\s.*)?[\r\n]*";
	}

	@SuppressWarnings("nls")
	private String getPatternCompileQuotedFile() {
		String patternFileName = "(.*\\." + getPatternFileExtensions() + ")";
		return "\\s*\"?" + getGccCommandPattern() + "\"?.*\\s" + "(['\"])" + patternFileName + "\\"
				+ (countGroups(getGccCommandPattern()) + 1) + "(\\s.*)?[\r\n]*";
	}

	private int getGroupForPatternUnquotedFile() {
		return countGroups(getGccCommandPattern()) + 1;
	}

	private int getGroupForPatternQuotedFile() {
		return countGroups(getGccCommandPattern()) + 2;
	}

	protected static abstract class AbstractBuildCommandPatternHighlighter extends RegexErrorParser implements IErrorParser2 {
		public AbstractBuildCommandPatternHighlighter(String pluginExtension) {
			init(pluginExtension);
		}

		protected void init(String buildCommandParserId) {
			AbstractBuildCommandParser gccBuildCommandParser = (AbstractBuildCommandParser) LanguageSettingsManager.getExtensionProviderCopy(buildCommandParserId);
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

		public int getProcessLineBehaviour() {
			return KEEP_LONGLINES;
		}
	}


}

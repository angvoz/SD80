/*******************************************************************************
 * Copyright (c) 2009, 2009 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.core.scannerconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

public class GCCBuildCommandParser extends AbstractBuildCommandParser {
	// TODO better algorithm to figure out the file
	private static final Pattern PATTERN_FILE = Pattern.compile("gcc.*\\s(\\S*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))(\\s.*)?");
	private static final Pattern PATTERN_OPTIONS = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s]+)))?");
	private static final Pattern PATTERN_QUOTED = Pattern.compile("(.*)(['\"])(.*)\\2(.*)");

	private final OptionParser[] optionParsers = new OptionParser[] {
			new IncludePathOptionParser("-I\\s*([\"'])(.*)\\1", "$2"),
			new IncludePathOptionParser("-I\\s*([^\\s\"']*)", "$1"),
			new IncludeFileOptionParser("-include\\s*([\"'])(.*)\\1", "$2"),
			new IncludeFileOptionParser("-include\\s*([^\\s\"']*)", "$1"),
			new MacroOptionParser("-D\\s*([\"'])([^=]*)(=(.*))?\\1", "$2", "$4"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=([\"'])(.*?)\\2", "$1", "$3"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)(=([^\\s\"']*))?", "$1", "$3"),
			new MacroFileOptionParser("-macros\\s*([\"'])(.*)\\1", "$2"),
			new MacroFileOptionParser("-macros\\s*([^\\s\"']*)", "$1"),
			new LibraryPathOptionParser("-L\\s*([\"'])(.*)\\1", "$2"),
			new LibraryPathOptionParser("-L\\s*([^\\s\"']*)", "$1"),
			new LibraryFileOptionParser("-l\\s*([^\\s\"']*)", "lib$1.a"),
	};
	
	
	private abstract class OptionParser {
		protected final Pattern pattern;
		protected final String patternStr;
		
		public OptionParser(String pattern) {
			this.patternStr = pattern;
			this.pattern = Pattern.compile(pattern);
		}
		
		public abstract ICLanguageSettingEntry createEntry(Matcher matcher);
		
		/**
		 * TODO: explain
		 */
		private String extractOption(String input) {
			String option = input.replaceFirst("("+patternStr+").*", "$1");
			return option;
		}
		
		protected ICLanguageSettingEntry parse(String input) {
			String option = extractOption(input);
			Matcher matcher = pattern.matcher(option);
			if (matcher.matches()) {
				return createEntry(matcher);
			}
			return null;
		}
		
		protected String parseStr(Matcher matcher, String str) {
			if (str!=null)
				return matcher.replaceAll(str);
			return null;
		}
	}
	
	private class IncludePathOptionParser extends OptionParser {
		private String nameExpression;
		
		public IncludePathOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher) {
			String name = parseStr(matcher, nameExpression);
			return new CIncludePathEntry(name, ICSettingEntry.READONLY);
		}
		
	}
	
	private class IncludeFileOptionParser extends OptionParser {
		private String nameExpression;
		
		public IncludeFileOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher) {
			String name = parseStr(matcher, nameExpression);
			return new CIncludeFileEntry(name, ICSettingEntry.READONLY);
		}
		
	}
	
	private class MacroOptionParser extends OptionParser {
		private String nameExpression;
		private String valueExpression;
		
		public MacroOptionParser(String pattern, String nameExpression, String valueExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
			this.valueExpression = valueExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher) {
			String name = parseStr(matcher, nameExpression);
			String value = parseStr(matcher, valueExpression);
			return new CMacroEntry(name, value, ICSettingEntry.READONLY);
		}
		
	}
	
	private class MacroFileOptionParser extends OptionParser {
		private String nameExpression;
		
		public MacroFileOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher) {
			String name = parseStr(matcher, nameExpression);
			return new CMacroFileEntry(name, ICSettingEntry.READONLY);
		}
		
	}
	
	private class LibraryPathOptionParser extends OptionParser {
		private String nameExpression;
		
		public LibraryPathOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher) {
			String name = parseStr(matcher, nameExpression);
			return new CLibraryPathEntry(name, ICSettingEntry.READONLY);
		}
		
	}
	
	private class LibraryFileOptionParser extends OptionParser {
		private String nameExpression;
		
		public LibraryFileOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher) {
			String name = parseStr(matcher, nameExpression);
			return new CLibraryFileEntry(name, ICSettingEntry.READONLY);
		}
		
	}
	
	@Override
	public boolean processLine(String line) {
		Matcher fileMatcher = PATTERN_FILE.matcher(line);
		if (!fileMatcher.matches()) {
			return false;
		}
		String fileName = fileMatcher.group(1);
		
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		Matcher optionMatcher = PATTERN_OPTIONS.matcher(line);
		while (optionMatcher.find()) {
			String option = optionMatcher.group(0);
			
			for (OptionParser optionParser : optionParsers) {
				ICLanguageSettingEntry entry = optionParser.parse(option);
				if (entry!=null) {
					entries.add(entry);
					break;
				}
			}
		}
		if (entries.size()>0) {
			setSettingEntries(entries, fileName);
			return true;
		}
		return false;
	}

}

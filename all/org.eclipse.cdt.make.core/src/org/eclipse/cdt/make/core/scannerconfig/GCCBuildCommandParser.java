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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.URIUtil;

public class GCCBuildCommandParser extends AbstractBuildCommandParser {
	// TODO better algorithm to figure out the file
	private static final Pattern PATTERN_FILE = Pattern.compile("((gcc)|(g\\+\\+)).*\\s([^'\"\\s]*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))(\\s.*)?[\r\n]*");
	private static final int PATTERN_FILE_GROUP = 4;
	private static final Pattern PATTERN_FILE_QUOTED = Pattern.compile("((gcc)|(g\\+\\+)).*\\s(['\"])(.*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))\\4(\\s.*)?[\r\n]*");
	private static final int PATTERN_FILE_QUOTED_GROUP = 5;
	private static final Pattern PATTERN_OPTIONS = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s]+)))?");

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
	private ErrorParserManager errorParserManager = null;


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
			if (errorParserManager!=null) {
				IPath path = new Path(name);
				URI uri = null;
				if (!path.isAbsolute()) {
					URI cwd = errorParserManager.getWorkingDirectoryURI();
					uri = URIUtil.append(cwd, path.toString());
				} else {
					uri = errorParserManager.toURI(path);
				}

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();
				IContainer[] folders = root.findContainersForLocationURI(uri);
				if (folders.length>0) {
					IFolder folder = (IFolder)folders[0];
					return new CIncludePathEntry(folder, ICSettingEntry.READONLY);
				}
			}
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
			if (errorParserManager!=null) {
				IFile file = errorParserManager.findFileName(name);
				if (file!=null) {
					return new CIncludeFileEntry(file, ICSettingEntry.READONLY);
				}
			}

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
	public boolean processLine(String line, ErrorParserManager epm) {
		errorParserManager = epm;
		String fileName = null;
		Matcher fileMatcher = PATTERN_FILE.matcher(line);

		if (fileMatcher.matches()) {
			fileName = fileMatcher.group(PATTERN_FILE_GROUP);
		} else {
			fileMatcher = PATTERN_FILE_QUOTED.matcher(line);
			if (fileMatcher.matches()) {
				fileName = fileMatcher.group(PATTERN_FILE_QUOTED_GROUP);
			}
		}

		IResource file = null;
		if (fileName!=null) {
			// TODO: move to AbstractBuildCommandParser
			if (epm!=null) {
				file = epm.findFileName(fileName);
			} else {
				IProject project = getProject();
				file = project.findMember(fileName);
			}
		}

		if (file!=null) {
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
				setSettingEntries(entries, file);
			} else {
				setSettingEntries(null, file);
			}
		}
		return false;
	}

}

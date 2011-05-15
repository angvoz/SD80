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

import java.io.File;
import java.io.IOException;
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
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;

public class GCCBuildCommandParser extends AbstractBuildCommandParser implements ILanguageSettingsEditableProvider {
	// TODO better algorithm to figure out the file
	// TODO test cases for boost bjam syntax with white spaces and quotes
	//    "g++"  -ftemplate-depth-128 -O0 -fno-inline -Wall -g -mthreads  -DBOOST_ALL_NO_LIB=1 -DBOOST_PYTHON_SOURCE -DBOOST_PYTHON_STATIC_LIB  -I"." -I"c:\Python25\Include" -c -o "bin.v2\libs\python\build\gcc-mingw-3.4.5\debug\link-static\threading-multi\numeric.o" "libs\python\src\numeric.cpp"
	private static final Pattern PATTERN_FILE = Pattern.compile("\\s*\"?((gcc)|(g\\+\\+))\"?.*\\s([^'\"\\s]*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))(\\s.*)?[\r\n]*");
	private static final int PATTERN_FILE_GROUP = 4;
	private static final Pattern PATTERN_FILE_QUOTED = Pattern.compile("\\s*\"?((gcc)|(g\\+\\+))\"?.*\\s(['\"])(.*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))\\4(\\s.*)?[\r\n]*");
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
	private IResource sourceFile = null;
	private String parsedSourceFileName = null;
	
	private abstract class OptionParser {
		protected final Pattern pattern;
		protected final String patternStr;

		public OptionParser(String pattern) {
			this.patternStr = pattern;
			this.pattern = Pattern.compile(pattern);
		}

		public abstract ICLanguageSettingEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager);

		/**
		 * TODO: explain
		 */
		private String extractOption(String input) {
			String option = input.replaceFirst("("+patternStr+").*", "$1");
			return option;
		}

		protected ICLanguageSettingEntry parse(String input, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String option = extractOption(input);
			Matcher matcher = pattern.matcher(option);
			if (matcher.matches()) {
				return createEntry(matcher, sourceFile, parsedSourceFileName, errorParserManager);
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
		public ICLanguageSettingEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			IPath path = new Path(name);
			
			if (!GCCBuildCommandParser.this.isExpandRelativePaths()) {
				return new CIncludePathEntry(name, 0);
			}
			
			URI uri = null;
			URI cwd = null;
			if (!path.isAbsolute()) {
				if (sourceFile!=null && parsedSourceFileName!=null) {
					IPath parsedSrcPath = new Path(parsedSourceFileName);
					if (!parsedSrcPath.isAbsolute()) {
						// try to figure CWD from file currently compiling (if found in workspace)
						// consider "gcc -I./relative/to/build/dir -c relative/src/file.cpp"
						IPath absPath = sourceFile.getLocation();
						int absSegmentsCount = absPath.segmentCount();
						int relSegmentsCount = parsedSrcPath.segmentCount();
						if (absSegmentsCount>=relSegmentsCount) {
							IPath ending = absPath.removeFirstSegments(absSegmentsCount-relSegmentsCount);
							ending = ending.setDevice(parsedSrcPath.getDevice());
							if (ending.equals(parsedSrcPath)) {
								IPath cwdPath = absPath.removeLastSegments(relSegmentsCount);
								// FIXME errorParserManager.toURI(cwdPath);
								// FIXME why errorParserManager is null?
								cwd = org.eclipse.core.filesystem.URIUtil.toURI(cwdPath);
							}
						}
					}
				}

				if (cwd==null && errorParserManager!=null) {
					// backing to ErrorParserManager if CWD not found
					cwd = errorParserManager.getWorkingDirectoryURI();
				}
				if (cwd!=null) {
					uri = URIUtil.append(cwd, path.toString());
				}
			} else {
				// FIXME errorParserManager.toURI(path);
				// FIXME why errorParserManager is null?
				uri = org.eclipse.core.filesystem.URIUtil.toURI(path);
			}
			if (uri!=null) {

				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IWorkspaceRoot root = workspace.getRoot();
				IContainer[] folders = root.findContainersForLocationURI(uri);
				if (folders.length>0) {
					IContainer container = folders[0];
					if ((container instanceof IProject || container instanceof IFolder) && container.isAccessible()) {
						return new CIncludePathEntry(container.getFullPath(), ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
					} else {
						return new CIncludePathEntry(container.getLocation(), 0);
					}
				}

				File file = new java.io.File(uri);
//				if (file.exists()) {
					IPath includePath;
					try {
						includePath = new Path(file.getCanonicalPath());
						// FIXME
						includePath = includePath.setDevice(null);
						
						return new CIncludePathEntry(includePath, 0);
					} catch (IOException e) {
						MakeCorePlugin.log(e);
					}
//				}
			}
			return new CIncludePathEntry(name, 0);
		}

	}

	private class IncludeFileOptionParser extends OptionParser {
		private String nameExpression;

		public IncludeFileOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			if (errorParserManager!=null) {
				IFile file = errorParserManager.findFileName(name);
				if (file!=null) {
					return new CIncludeFileEntry(file, 0);
				}
			}

			return new CIncludeFileEntry(name, 0);
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
		public ICLanguageSettingEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			String value = parseStr(matcher, valueExpression);
			return new CMacroEntry(name, value, 0);
		}

	}

	private class MacroFileOptionParser extends OptionParser {
		private String nameExpression;

		public MacroFileOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			return new CMacroFileEntry(name, 0);
		}

	}

	private class LibraryPathOptionParser extends OptionParser {
		private String nameExpression;

		public LibraryPathOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			return new CLibraryPathEntry(name, 0);
		}

	}

	private class LibraryFileOptionParser extends OptionParser {
		private String nameExpression;

		public LibraryFileOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public ICLanguageSettingEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			return new CLibraryFileEntry(name, 0);
		}

	}

	@Override
	public boolean processLine(String line, ErrorParserManager epm) {
		errorParserManager = epm;
		sourceFile = null;
		parsedSourceFileName = null;
//		Matcher fileMatcher = PATTERN_FILE.matcher(line);
//		Matcher fileMatcher = PATTERN_FILE.matcher(line);
//		Pattern PATTERN = Pattern.compile("\\s*\"?((gcc)|(g\\+\\+))\"?.*\\s      ([^'\"\\s]*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))(\\s.*)?[\r\n]*");
//		Pattern PATTERN = Pattern.compile("\\s*\"?((gcc)|(g\\+\\+))\"?.*\\s['\"]?([^'\"\\s]*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))['\"]?((\\s.*)|())[\r\n]*");
//		Pattern PATTERN = Pattern.compile("\\s*\"?((gcc)|(g\\+\\+))\"?.*\\s['\"]?([^'\"\\s]*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))['\"]?((\\s.*)|())[\r\n]*");
		Pattern PATTERN = Pattern.compile("\\s*\"?((gcc)|(g\\+\\+))\"?.*\\s([^'\"\\s]*\\.((c)|(cc)|(cpp)|(cxx)|(C)|(CC)|(CPP)|(CXX)))((\\s.*)|())[\r\n]*");
		Matcher fileMatcher = PATTERN.matcher(line);

		if (fileMatcher.matches()) {
			parsedSourceFileName = fileMatcher.group(PATTERN_FILE_GROUP);
		} else {
			fileMatcher = PATTERN_FILE_QUOTED.matcher(line);
			if (fileMatcher.matches()) {
				parsedSourceFileName = fileMatcher.group(PATTERN_FILE_QUOTED_GROUP);
			}
		}

		if (parsedSourceFileName!=null) {
			// TODO: move to AbstractBuildCommandParser
			if (epm!=null) {
				sourceFile = epm.findFileName(parsedSourceFileName);
			} else {
				IProject project = getProject();
				sourceFile = project.findMember(parsedSourceFileName);
			}
		}

		if (sourceFile!=null) {
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			Matcher optionMatcher = PATTERN_OPTIONS.matcher(line);
			while (optionMatcher.find()) {
				String option = optionMatcher.group(0);

				for (OptionParser optionParser : optionParsers) {
					ICLanguageSettingEntry entry = optionParser.parse(option, sourceFile, parsedSourceFileName, errorParserManager);
					if (entry!=null && !entries.contains(entry)) {
						entries.add(entry);
						break;
					}
				}
			}
			if (entries.size()>0) {
				setSettingEntries(entries, sourceFile);
			} else {
				setSettingEntries(null, sourceFile);
			}
			IStatus status = new Status(IStatus.INFO, MakeCorePlugin.PLUGIN_ID, getClass().getSimpleName()+" collected "+entries.size()+" entries for "+sourceFile);
			MakeCorePlugin.log(status);
		} else {
			if (parsedSourceFileName!=null) {
				IStatus status = new Status(IStatus.INFO, MakeCorePlugin.PLUGIN_ID, getClass().getSimpleName()+" not found resource "+parsedSourceFileName);
				MakeCorePlugin.log(status);
			}
			
		}
		return false;
	}
	
	@Override
	public GCCBuildCommandParser cloneShallow() throws CloneNotSupportedException {
		return (GCCBuildCommandParser) super.cloneShallow();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public GCCBuildCommandParser clone() throws CloneNotSupportedException {
		return (GCCBuildCommandParser) super.clone();
	}

}

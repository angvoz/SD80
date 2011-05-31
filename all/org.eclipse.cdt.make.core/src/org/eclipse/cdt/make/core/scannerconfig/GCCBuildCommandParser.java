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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class GCCBuildCommandParser extends AbstractBuildCommandParser implements ILanguageSettingsEditableProvider {
	private static final String PATTERN_GCC_COMMAND = "((gcc)|(g\\+\\+)|(c\\+\\+))"; //$NON-NLS-1$
	private static final Pattern PATTERN_OPTIONS = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$
	private static final int PATTERN_OPTION_GROUP = 0;

	@SuppressWarnings("nls")
	private static int countGroups(String str) {
		return str.replaceAll("[^\\(]","").length();
	}
	
	@SuppressWarnings("nls")
	private String getPatternFileExtensions() {
		IContentTypeManager manager = Platform.getContentTypeManager();
		
		Set<String> fileExts = new HashSet<String>();
		
		IContentType contentTypeCpp = manager.getContentType("org.eclipse.cdt.core.cxxSource");
		fileExts.addAll(Arrays.asList(contentTypeCpp.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));
		
		IContentType contentTypeC = manager.getContentType("org.eclipse.cdt.core.cSource");
		fileExts.addAll(Arrays.asList(contentTypeC.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));
		
		String pattern = "(";
		for (String ext : fileExts) {
			if (pattern.length()!=1)
				pattern += "|";
			pattern += "(" + Pattern.quote(ext) + ")";
			ext = ext.toUpperCase();
			if (!fileExts.contains(ext)) {
				pattern += "|(" + Pattern.quote(ext) + ")";
			}
		}
		pattern += ")";
		
		return pattern;
	}

	@SuppressWarnings("nls")
	/*package*/ String getPatternCompileUnquotedFile() {
		String patternFileName = "([^'\"\\s]*\\." + getPatternFileExtensions() + ")";
		return "\\s*\"?" + PATTERN_GCC_COMMAND + "\"?.*\\s" + patternFileName + "(\\s.*)?[\r\n]*";
	}
	/*package*/ static final int PATTERN_UNQUOTED_FILE_GROUP = countGroups(PATTERN_GCC_COMMAND)+1;

	@SuppressWarnings("nls")
	/*package*/ String getPatternCompileQuotedFile() {
		String patternFileName = "(.*\\." + getPatternFileExtensions() + ")";
		return "\\s*\"?" +PATTERN_GCC_COMMAND + "\"?.*\\s"+"(['\"])" + patternFileName + "\\"+(countGroups(PATTERN_GCC_COMMAND)+1)+"(\\s.*)?[\r\n]*";
	}
	/*package*/ static final int PATTERN_QUOTED_FILE_GROUP = countGroups(PATTERN_GCC_COMMAND)+2;
	

	@SuppressWarnings("nls")
	private final OptionParser[] optionParsers = new OptionParser[] {
			new IncludePathOptionParser("-I\\s*([\"'])(.*)\\1", "$2"),
			new IncludePathOptionParser("-I\\s*([^\\s\"']*)", "$1"),
			new IncludeFileOptionParser("-include\\s*([\"'])(.*)\\1", "$2"),
			new IncludeFileOptionParser("-include\\s*([^\\s\"']*)", "$1"),
			new MacroOptionParser("-D\\s*([\"'])([^=]*)(=(.*))?\\1", "$2", "$4"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=(\\\\([\"']))(.*?)\\2", "$1", "$3$4$3"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=([\"'])(.*?)\\2", "$1", "$3"),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)(=([^\\s\"']*))?", "$1", "$3"),
			new MacroOptionParser("-U\\s*([^\\s=\"']*)", "$1", ICSettingEntry.UNDEFINED),
			new MacroFileOptionParser("-macros\\s*([\"'])(.*)\\1", "$2"),
			new MacroFileOptionParser("-macros\\s*([^\\s\"']*)", "$1"),
			new LibraryPathOptionParser("-L\\s*([\"'])(.*)\\1", "$2"),
			new LibraryPathOptionParser("-L\\s*([^\\s\"']*)", "$1"),
			new LibraryFileOptionParser("-l\\s*([^\\s\"']*)", "lib$1.a"),
	};
	private ErrorParserManager errorParserManager = null;
	private IResource sourceFile = null;
	private String parsedSourceFileName = null;
	
	private URI buildDirURI;
	private IPath mappedRootPath = new Path("/");

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
		public CIncludePathEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			
			if (isExpandRelativePaths()) {
				URI uri = getURI(new Path(name));
				if (uri!=null) {
					IPath path = getFullWorkspacePathForFolder(uri);
					if (path!=null) {
						return new CIncludePathEntry(path, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
					}
					
					path = getCanonicalFilesystemLocation(uri);
					if (path!=null)
						return new CIncludePathEntry(path, 0);
				}
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
		public CIncludeFileEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			
			if (isExpandRelativePaths()) {
				URI uri = getURI(new Path(name));
				if (uri!=null) {
					IPath path = getFullWorkspacePathForFile(uri);
					if (path!=null) {
						return new CIncludeFileEntry(path, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
					}
					
					path = getCanonicalFilesystemLocation(uri);
					if (path!=null)
						return new CIncludeFileEntry(path, 0);
				}
			}
			
			return new CIncludeFileEntry(name, 0);
		}

	}

	private class MacroOptionParser extends OptionParser {
		private String nameExpression;
		private String valueExpression;
		private int flag = 0;

		public MacroOptionParser(String pattern, String nameExpression, String valueExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
			this.valueExpression = valueExpression;
		}
		
		public MacroOptionParser(String pattern, String nameExpression, int flag) {
			super(pattern);
			this.nameExpression = nameExpression;
			this.valueExpression = null;
			this.flag = flag;
		}
		
		@Override
		public CMacroEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			String value = null;
			if ((flag&ICSettingEntry.UNDEFINED) != ICSettingEntry.UNDEFINED)
				value = parseStr(matcher, valueExpression);
			return new CMacroEntry(name, value, flag);
		}

	}

	private class MacroFileOptionParser extends OptionParser {
		private String nameExpression;

		public MacroFileOptionParser(String pattern, String nameExpression) {
			super(pattern);
			this.nameExpression = nameExpression;
		}
		@Override
		public CMacroFileEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);

			if (isExpandRelativePaths()) {
				URI uri = getURI(new Path(name));
				if (uri!=null) {
					IPath path = getFullWorkspacePathForFile(uri);
					if (path!=null) {
						return new CMacroFileEntry(path, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
					}
					
					path = getCanonicalFilesystemLocation(uri);
					if (path!=null)
						return new CMacroFileEntry(path, 0);
				}
			}
			
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
		public CLibraryPathEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			
			if (isExpandRelativePaths()) {
				URI uri = getURI(new Path(name));
				if (uri!=null) {
					IPath path = getFullWorkspacePathForFolder(uri);
					if (path!=null) {
						return new CLibraryPathEntry(path, ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED);
					}
					
					path = getCanonicalFilesystemLocation(uri);
					if (path!=null)
						return new CLibraryPathEntry(path, 0);
				}
			}
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
		public CLibraryFileEntry createEntry(Matcher matcher, IResource sourceFile, String parsedSourceFileName, ErrorParserManager errorParserManager) {
			String name = parseStr(matcher, nameExpression);
			return new CLibraryFileEntry(name, 0);
		}

	}

	@Override
	public boolean processLine(String line, ErrorParserManager epm) {
		errorParserManager = epm;
		sourceFile = null;
		parsedSourceFileName = null;
		String patternCompileUnquotedFile = getPatternCompileUnquotedFile();
		Matcher fileMatcher = Pattern.compile(patternCompileUnquotedFile).matcher(line);

		if (fileMatcher.matches()) {
			parsedSourceFileName = fileMatcher.group(PATTERN_UNQUOTED_FILE_GROUP);
		} else {
			String patternCompileQuotedFile = getPatternCompileQuotedFile();
			fileMatcher = Pattern.compile(patternCompileQuotedFile).matcher(line);
			if (fileMatcher.matches()) {
				parsedSourceFileName = fileMatcher.group(PATTERN_QUOTED_FILE_GROUP);
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
			buildDirURI = null;
			mappedRootPath = new Path("/");
			if (sourceFile!=null && parsedSourceFileName!=null && errorParserManager!=null) {
				IPath parsedSrcPath = new Path(parsedSourceFileName);
				if (parsedSrcPath.isAbsolute())
					mappedRootPath = getMappedRoot(sourceFile, parsedSrcPath);

				IPath cwdPath = errorParserManager.getWorkingDirectory();
				if (cwdPath!=null) {
					buildDirURI = org.eclipse.core.filesystem.URIUtil.toURI(mappedRootPath.append(cwdPath));
				}
			}
			
			if (buildDirURI==null) {
				if (errorParserManager!=null) {
					// backing to ErrorParserManager if CWD not found
					buildDirURI = errorParserManager.getWorkingDirectoryURI();
				} else if (sourceFile!=null) {
					// FIXME - take build dir from configuration
					buildDirURI = sourceFile.getProject().getLocationURI();
				}
			}

			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			Matcher optionMatcher = PATTERN_OPTIONS.matcher(line);
			while (optionMatcher.find()) {
				String option = optionMatcher.group(PATTERN_OPTION_GROUP).trim();

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
	
	private IPath getMappedRoot(IResource sourceFile, IPath parsedSrcPath) {
		IPath mappedRootPath = new Path("/");
		// try to figure CWD from file currently compiling (if found in workspace)
		// consider "gcc -I./relative/to/build/dir -c relative/src/file.cpp"
		IPath absPath = sourceFile.getLocation();
		int absSegmentsCount = absPath.segmentCount();
		int relSegmentsCount = parsedSrcPath.segmentCount();
		if (absSegmentsCount>=relSegmentsCount) {
			IPath ending = absPath.removeFirstSegments(absSegmentsCount-relSegmentsCount);
			ending = ending.setDevice(parsedSrcPath.getDevice()).makeAbsolute();
			if (ending.equals(parsedSrcPath.makeAbsolute())) {
				mappedRootPath = absPath.removeLastSegments(relSegmentsCount);
			}
		}
		return mappedRootPath;
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

	private URI getURI(IPath path) {
		URI uri = null;
		if (!path.isAbsolute()) {
			if (buildDirURI!=null) {
				uri = URIUtil.append(buildDirURI, path.toString());
			}
		} else {
			path = mappedRootPath.append(path);
			uri = org.eclipse.core.filesystem.URIUtil.toURI(path);
		}
		return uri;
	}

	private IPath getFullWorkspacePathForFolder(URI uri) {
		IPath path = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IContainer[] folders = root.findContainersForLocationURI(uri);
		if (folders.length>0) {
			IContainer container = folders[0];
			if ((container instanceof IProject || container instanceof IFolder)) { // treat IWorkspaceRoot as non-workspace path
				path = container.getFullPath();
			}
		}
		return path;
	}

	private IPath getFullWorkspacePathForFile(URI uri) {
		IPath path = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IFile[] files = root.findFilesForLocationURI(uri);
		if (files.length>0) {
			IFile file = files[0];
			path = file.getFullPath();
		}
		return path;
	}
	
	private IPath getCanonicalFilesystemLocation(URI uri) {
		IPath path = null;
		try {
			File file = new java.io.File(uri);
			path = new Path(file.getCanonicalPath());
		} catch (IOException e) {
			MakeCorePlugin.log(e);
		}
		return path;
	}
}

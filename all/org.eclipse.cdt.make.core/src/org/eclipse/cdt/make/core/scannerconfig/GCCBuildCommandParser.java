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
import java.net.URISyntaxException;
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
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.EFS;
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
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

public class GCCBuildCommandParser extends AbstractBuildCommandParser implements ILanguageSettingsEditableProvider {
	private static final Pattern PATTERN_OPTIONS = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$
	private static final int PATTERN_OPTION_GROUP = 0;

	private ErrorParserManager errorParserManager = null;
	private IResource sourceFile = null;
	private String parsedSourceFileName = null;
	
	/*
	 * Where source tree starts if mapped. This kind of mapping applied automatically
	 * in cases when the absolute path to the source file on the remote system is
	 * simulated inside a project in the workspace.
	 */
	private URI mappedRootURI = null;
	private URI buildDirURI;

	@SuppressWarnings("nls")
	private final AbstractOptionParser[] optionParsers = new AbstractOptionParser[] {
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

	private abstract class AbstractOptionParser {
		protected final Pattern pattern;
		protected final String patternStr;
		private String nameExpression;
		private String valueExpression;

		public AbstractOptionParser(String pattern, String nameExpression, String valueExpression) {
			this.patternStr = pattern;
			this.nameExpression = nameExpression;
			this.valueExpression = valueExpression;

			this.pattern = Pattern.compile(pattern);
		}

		public AbstractOptionParser(String pattern, String nameExpression) {
			this(pattern, nameExpression, null);
		}
		
		@SuppressWarnings("nls")
		public int getKind() {
			return createEntry("dummy", "dummy", 0).getKind();
		}
		
		public abstract ICLanguageSettingEntry createEntry(String name, String value, int flag);

		/**
		 * TODO: explain
		 */
		@SuppressWarnings("nls")
		private String extractOption(String input) {
			String option = input.replaceFirst("("+patternStr+").*", "$1");
			return option;
		}

		protected String parseStr(Matcher matcher, String str) {
			if (str!=null)
				return matcher.replaceAll(str);
			return null;
		}
		
	}

	private class IncludePathOptionParser extends AbstractOptionParser {
		public IncludePathOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}
		@Override
		public CIncludePathEntry createEntry(String name, String value, int flag) {
			return new CIncludePathEntry(value, flag);
		}
		
	}

	private class IncludeFileOptionParser extends AbstractOptionParser {
		public IncludeFileOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}
		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return new CIncludeFileEntry(value, flag);
		}
	}

	private class MacroOptionParser extends AbstractOptionParser {
		private int undefFlag = 0;

		public MacroOptionParser(String pattern, String nameExpression, String valueExpression) {
			super(pattern, nameExpression, valueExpression);
		}
		public MacroOptionParser(String pattern, String nameExpression, int flag) {
			super(pattern, nameExpression);
			this.undefFlag = flag;
		}
		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return new CMacroEntry(name, value, flag | undefFlag);
		}
	}

	private class MacroFileOptionParser extends AbstractOptionParser {
		public MacroFileOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}
		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return new CMacroFileEntry(value, flag);
		}
	}

	private class LibraryPathOptionParser extends AbstractOptionParser {
		public LibraryPathOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}
		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return new CLibraryPathEntry(value, flag);
		}
	}

	private class LibraryFileOptionParser extends AbstractOptionParser {
		public LibraryFileOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}
		@Override
		public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
			return new CLibraryFileEntry(value, flag);
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
			parsedSourceFileName = fileMatcher.group(getGroupForPatternUnquotedFile());
		} else {
			String patternCompileQuotedFile = getPatternCompileQuotedFile();
			fileMatcher = Pattern.compile(patternCompileQuotedFile).matcher(line);
			if (fileMatcher.matches()) {
				parsedSourceFileName = fileMatcher.group(getGroupForPatternQuotedFile());
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
			mappedRootURI = null;
			mappedRootURI = EFSExtensionManager.getDefault().createNewURIFromPath(sourceFile.getLocationURI(), "/");
			if (sourceFile!=null && parsedSourceFileName!=null && errorParserManager!=null) {
				URI cwdURI = null;
				IPath parsedSrcPath = new Path(parsedSourceFileName);
				if (parsedSrcPath.isAbsolute()) {
					mappedRootURI = getMappedRoot(sourceFile, parsedSrcPath);
				} else {
					cwdURI = findBaseLocationURI(sourceFile.getLocationURI(), parsedSourceFileName);
				}
				if (cwdURI==null) {
					cwdURI = errorParserManager.getWorkingDirectoryURI();
				}

				
				String cwdPath = EFSExtensionManager.getDefault().getPathFromURI(cwdURI);
				if (cwdPath!=null && mappedRootURI!=null) {
					buildDirURI = EFSExtensionManager.getDefault().append(mappedRootURI, cwdPath);
				} else {
					buildDirURI = cwdURI;
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

				for (AbstractOptionParser optionParser : optionParsers) {
					ICLanguageSettingEntry entry = null;
					
					String opt = optionParser.extractOption(option);
					Matcher matcher = optionParser.pattern.matcher(opt);
					if (matcher.matches()) {
						String name = optionParser.parseStr(matcher, optionParser.nameExpression);
						String value = null;
						int flag = 0;

						int kind = optionParser.getKind();
						switch (kind) {
						case ICSettingEntry.MACRO:
							value = optionParser.parseStr(matcher, optionParser.valueExpression);
							break;
						case ICSettingEntry.INCLUDE_PATH:
						case ICSettingEntry.INCLUDE_FILE:
						case ICSettingEntry.MACRO_FILE:
						case ICSettingEntry.LIBRARY_PATH:
							if (isExpandRelativePaths()) {
								URI uri = getURI(name);
								if (uri!=null) {
									IPath path = getFullWorkspacePath(uri, kind);
									if (path!=null) {
										name = path.toString();
										flag = ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED;
									} else {
										path = getFilesystemLocation(uri);
										if (path!=null) {
											name = path.toString();
										}
									}
								}
							}
							value = name;
							break;
						case ICSettingEntry.LIBRARY_FILE:
							value = name;
							break;
						} 

						entry = optionParser.createEntry(name, value, flag);
					}

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
	
	private URI findBaseLocationURI(URI fileURI, String relativeFileName) {
		URI cwdURI = null;
		String path = fileURI.getPath();
		
		String[] segments = relativeFileName.split("[/\\\\]"); //$NON-NLS-1$
		
		// start removing segments from the end of the path
		for (int i=segments.length-1;i>=0;i--) {
			String lastSegment = segments[i];
			if (lastSegment.length()>0 && !lastSegment.equals(".")) { //$NON-NLS-1$
				if (lastSegment.equals("..")) { //$NON-NLS-1$
					// navigating ".." in the other direction is ambiguous, bailing out
					return null;
				} else {
					if (path.endsWith("/"+lastSegment)) { //$NON-NLS-1$
						int pos = path.lastIndexOf(lastSegment);
						path = path.substring(0, pos);
						continue;
					} else {
						// ouch, relativeFileName does not match fileURI, bailing out
						return null;
					}
				}
			}
		}
		
		try {
			cwdURI = new URI(fileURI.getScheme(), fileURI.getUserInfo(), fileURI.getHost(), fileURI.getPort(), path, fileURI.getQuery(), fileURI.getFragment());
		} catch (URISyntaxException e) {
			// It should be valid URI here or something is wrong
			MakeCorePlugin.log(e);
		}
		
		return cwdURI;
	}

	private URI getMappedRoot(IResource sourceFile, IPath parsedSrcPath) {
		URI fileURI = sourceFile.getLocationURI();
		IPath mappedRootPath = new Path("/");
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
		
		URI mappedRootURI = EFSExtensionManager.getDefault().createNewURIFromPath(fileURI, mappedRootPath.toString());
		return mappedRootURI;
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

	private URI getURI(String name) {
		URI uri = null;
		
		Path path = new Path(name);
		URI baseURI = path.isAbsolute() ? mappedRootURI :  buildDirURI;
		
		if (buildDirURI.getScheme().equals(EFS.SCHEME_FILE)) {
			IPath baseLocation = org.eclipse.core.filesystem.URIUtil.toPath(baseURI);
			// careful not to use 'path' here but 'name' as we want to properly navigate symlinks
			uri = resolvePathFromBaseLocation(name, baseLocation);
		} else {
			// use canonicalized path here, in particular replace all '\' with '/' for Windows paths
			uri = EFSExtensionManager.getDefault().append(baseURI, path.toString());
			
		}

		if (uri==null) {
			// if everything fails
			uri = org.eclipse.core.filesystem.URIUtil.toURI(name);
		}
		return uri;
	}

	/**
	 * The manipulations here are done to resolve "../" navigation for symbolic links
	 * where "link/.." cannot be collapsed as it must follow the real filesystem path.
	 * {@link java.io.File#getCanonicalPath()} deals with that correctly
	 * but {@link Path} or {@link URI} try to normalize the path which would be incorrect here.
	 */
	private URI resolvePathFromBaseLocation(String name, IPath baseLocation) {
		String pathName = name;
		if (baseLocation!=null && !baseLocation.isEmpty()) {
			String device = new Path(pathName).getDevice();
			if (device!=null && device.length()>0) {
				pathName = pathName.substring(device.length());
			}
			pathName = pathName.replace(File.separatorChar, '/');
			
			baseLocation = baseLocation.addTrailingSeparator();
			if (pathName.startsWith("/")) {
				pathName = pathName.substring(1);
			}
			pathName = baseLocation.toString()+pathName;
		}
		
		try {
			File file = new File(pathName);
			file = file.getCanonicalFile();
			return file.toURI();
		} catch (IOException e) {
			// if error just leave it as is
		}
		
		URI uri = org.eclipse.core.filesystem.URIUtil.toURI(pathName);
		return uri;
	}

	private IPath getFullWorkspacePath(URI uri, int kind) {
		IPath path = null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		switch (kind) {
		case ICSettingEntry.INCLUDE_PATH:
		case ICSettingEntry.LIBRARY_PATH:
			IContainer[] folders = root.findContainersForLocationURI(uri);
			if (folders.length>0) {
				IContainer container = folders[0];
				if ((container instanceof IProject || container instanceof IFolder)) { // treat IWorkspaceRoot as non-workspace path
					path = container.getFullPath();
				}
			}
			break;
		case ICSettingEntry.INCLUDE_FILE:
		case ICSettingEntry.MACRO_FILE:
			IFile[] files = root.findFilesForLocationURI(uri);
			if (files.length>0) {
				IFile file = files[0];
				path = file.getFullPath();
			}
			break;
		}

		return path;
	}

	private IPath getFilesystemLocation(URI uri) {
		// EFSExtensionManager mapping
		String pathStr = EFSExtensionManager.getDefault().getMappedPath(uri);
		uri = org.eclipse.core.filesystem.URIUtil.toURI(pathStr);
		
		try {
			File file = new java.io.File(uri);
			String canonicalPathStr = file.getCanonicalPath();
			return new Path(canonicalPathStr);
		} catch (Exception e) {
			MakeCorePlugin.log(e);
		}
		return null;
	}
	
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
	protected String getGccCommandPattern() {
		String parameter = getCustomParameter();
		return "(" + parameter + ")";
	}
	
	@SuppressWarnings("nls")
	/*package*/ String getPatternCompileUnquotedFile() {
		String patternFileName = "([^'\"\\s]*\\." + getPatternFileExtensions() + ")";
		return "\\s*\"?" + getGccCommandPattern() + "\"?.*\\s" + patternFileName + "(\\s.*)?[\r\n]*";
	}

	@SuppressWarnings("nls")
	/*package*/ String getPatternCompileQuotedFile() {
		String patternFileName = "(.*\\." + getPatternFileExtensions() + ")";
		return "\\s*\"?" +getGccCommandPattern() + "\"?.*\\s"+"(['\"])" + patternFileName + "\\"+(countGroups(getGccCommandPattern())+1)+"(\\s.*)?[\r\n]*";
	}

	/*package*/ int getGroupForPatternUnquotedFile() {
		return countGroups(getGccCommandPattern())+1;
	}

	/*package*/ int getGroupForPatternQuotedFile() {
		return countGroups(getGccCommandPattern())+2;
	}
	
}

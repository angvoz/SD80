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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.internal.core.XmlUtil;
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.w3c.dom.Element;

public abstract class AbstractLanguageSettingsOutputScanner extends LanguageSettingsSerializable implements
		ILanguageSettingsOutputScanner {

	protected static abstract class AbstractOptionParser {
			protected final Pattern pattern;
			protected final String patternStr;
			protected String nameExpression;
			protected String valueExpression;
	
			public AbstractOptionParser(String pattern, String nameExpression, String valueExpression) {
				this.patternStr = pattern;
				this.nameExpression = nameExpression;
				this.valueExpression = valueExpression;
	
				this.pattern = Pattern.compile(pattern);
			}
	
			public AbstractOptionParser(String pattern, String nameExpression) {
				this(pattern, nameExpression, null);
			}
	
			public int getKind() {
				@SuppressWarnings("nls")
				int entry = createEntry("dummy", "dummy", 0).getKind();
				return entry;
			}
	
			public abstract ICLanguageSettingEntry createEntry(String name, String value, int flag);
	
			/**
			 * TODO: explain
			 */
			protected String extractOption(String input) {
				@SuppressWarnings("nls")
				String option = input.replaceFirst("(" + patternStr + ").*", "$1");
				return option;
			}
	
			protected String parseStr(Matcher matcher, String str) {
				if (str != null)
					return matcher.replaceAll(str);
				return null;
			}
	
		}

	protected static class IncludePathOptionParser extends AbstractOptionParser {
		private int extraFlag = 0;

		public IncludePathOptionParser(String pattern, String nameExpression) {
			super(pattern, nameExpression);
		}
		
		public IncludePathOptionParser(String pattern, String nameExpression, int flag) {
			super(pattern, nameExpression);
			extraFlag = flag;
		}
		
		@Override
		public CIncludePathEntry createEntry(String name, String value, int flag) {
			return new CIncludePathEntry(value, flag | extraFlag);
		}

	}

	protected static class IncludeFileOptionParser extends AbstractOptionParser {
			public IncludeFileOptionParser(String pattern, String nameExpression) {
				super(pattern, nameExpression);
			}
	
			@Override
			public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
				return new CIncludeFileEntry(value, flag);
			}
		}

	protected static class MacroOptionParser extends AbstractOptionParser {
			private int extraFlag = 0;
	
			public MacroOptionParser(String pattern, String nameExpression, String valueExpression) {
				super(pattern, nameExpression, valueExpression);
			}
	
			public MacroOptionParser(String pattern, String nameExpression, String valueExpression, int flag) {
				super(pattern, nameExpression, valueExpression);
				this.extraFlag = flag;
			}
	
			public MacroOptionParser(String pattern, String nameExpression, int flag) {
				super(pattern, nameExpression);
				this.extraFlag = flag;
			}
			
			@Override
			public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
				return new CMacroEntry(name, value, flag | extraFlag);
			}
		}

	protected static class MacroFileOptionParser extends AbstractOptionParser {
			public MacroFileOptionParser(String pattern, String nameExpression) {
				super(pattern, nameExpression);
			}
	
			@Override
			public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
				return new CMacroFileEntry(value, flag);
			}
		}

	protected static class LibraryPathOptionParser extends AbstractOptionParser {
			public LibraryPathOptionParser(String pattern, String nameExpression) {
				super(pattern, nameExpression);
			}
	
			@Override
			public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
				return new CLibraryPathEntry(value, flag);
			}
		}

	protected static class LibraryFileOptionParser extends AbstractOptionParser {
			public LibraryFileOptionParser(String pattern, String nameExpression) {
				super(pattern, nameExpression);
			}
	
			@Override
			public ICLanguageSettingEntry createEntry(String name, String value, int flag) {
				return new CLibraryFileEntry(value, flag);
			}
		}

	protected static final String ATTR_EXPAND_RELATIVE_PATHS = "expand-relative-paths"; //$NON-NLS-1$

	protected ICConfigurationDescription currentCfgDescription = null;
	protected IProject currentProject;

	protected ErrorParserManager errorParserManager = null;

	protected IResource resource = null;

	protected String parsedResourceName = null;

	protected boolean expandRelativePaths = true;
	
	protected boolean isForProject = false;

	public boolean isResolvePaths() {
		return expandRelativePaths;
	}

	public void setExpandRelativePaths(boolean expandRelativePaths) {
		this.expandRelativePaths = expandRelativePaths;
	}


	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		currentCfgDescription = cfgDescription;
		currentProject = cfgDescription != null ? cfgDescription.getProjectDescription().getProject() : null;
	}

	protected ICConfigurationDescription getConfigurationDescription() {
		return currentCfgDescription;
	}

	protected IProject getProject() {
		return currentProject;
	}

	public boolean processLine(String line) {
		return processLine(line, null);
	}

	public void shutdown() {
	}

	protected void setSettingEntries(List<ICLanguageSettingEntry> entries, IResource rc) {
		IProject project = getProject();
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (rc!=null) {
			ICLanguageSetting ls = cfgDescription.getLanguageSettingForFile(rc.getProjectRelativePath(), true);
			String languageId = ls.getLanguageId();
			setSettingEntries(cfgDescription, rc, languageId, entries);
		}
	}

	@Override
	public Element serialize(Element parentElement) {
		Element elementProvider = super.serialize(parentElement);
		elementProvider.setAttribute(ATTR_EXPAND_RELATIVE_PATHS, Boolean.toString(expandRelativePaths));
		return elementProvider;
	}
	
	@Override
	public void load(Element providerNode) {
		super.load(providerNode);
		
		String expandRelativePathsValue = XmlUtil.determineAttributeValue(providerNode, ATTR_EXPAND_RELATIVE_PATHS);
		if (expandRelativePathsValue!=null)
			expandRelativePaths = Boolean.parseBoolean(expandRelativePathsValue);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (expandRelativePaths ? 1231 : 1237);
		return result;
	}

	public boolean processLine(String line, ErrorParserManager epm) {
		errorParserManager = epm;
		// FIXME
		if (isForProject) {
//			resource = currentProject;
			resource = null;
		} else {
			parsedResourceName = parseResourceName(line);
			resource = findResource(parsedResourceName);
		}
		

	
//		if (resource != null) {
			URI buildDirURI = null;
			URI cwdURI = null;
	
			/*
			 * Where source tree starts if mapped. This kind of mapping applied automatically in cases when
			 * the absolute path to the source file on the remote system is simulated inside a project in the
			 * workspace.
			 */
			URI mappedRootURI = null;
	
			if (resource!=null && isResolvePaths()) {
				IPath parsedSrcPath = new Path(parsedResourceName);
				if (parsedSrcPath.isAbsolute()) {
					mappedRootURI = getMappedRoot(resource, parsedSrcPath);
				} else {
					mappedRootURI = EFSExtensionManager.getDefault().createNewURIFromPath(
							resource.getLocationURI(), "/"); //$NON-NLS-1$
				}
	
				if (!parsedSrcPath.isAbsolute()) {
					cwdURI = findBaseLocationURI(resource.getLocationURI(), parsedResourceName);
				}
				if (cwdURI == null && errorParserManager != null) {
					cwdURI = errorParserManager.getWorkingDirectoryURI();
				}
	
				String cwdPath = cwdURI != null ? EFSExtensionManager.getDefault().getPathFromURI(cwdURI)
						: null;
				if (cwdPath != null && mappedRootURI != null) {
					buildDirURI = EFSExtensionManager.getDefault().append(mappedRootURI, cwdPath);
				} else {
					buildDirURI = cwdURI;
				}
	
				if (buildDirURI == null) {
					// FIXME - take build dir from configuration
					buildDirURI = resource.getProject().getLocationURI();
				}
			}
	
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
			
			List<String> options = parseOptions(line);
			if (options!=null) {
				for (String option : options) {
					for (AbstractOptionParser optionParser : getOptionParsers()) {
						ICLanguageSettingEntry entry = null;
		
						String opt = optionParser.extractOption(option);
						Matcher matcher = optionParser.pattern.matcher(opt);
						if (matcher.matches()) {
							String parsedName = optionParser.parseStr(matcher, optionParser.nameExpression);
							String name = parsedName;
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
								if (isResolvePaths()) {
									URI baseURI = new Path(name).isAbsolute() ? mappedRootURI : buildDirURI;
									URI uri = getURI(name, baseURI);
									if (uri != null) {
										IPath path = getFullWorkspacePath(uri, kind);
										if (path != null) {
											name = path.toString();
											flag = ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED;
										} else {
											path = getFilesystemLocation(uri);
											if (path != null) {
												name = path.toString();
											}
											if (path == null || !new File(name).exists()) {
												IResource resource = findPathInWorkspace(parsedName);
												if (resource != null) {
													path = resource.getFullPath();
													name = path.toString();
													flag = ICSettingEntry.VALUE_WORKSPACE_PATH
															| ICSettingEntry.RESOLVED;
												}
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
		
						if (entry != null && !entries.contains(entry)) {
							entries.add(entry);
							break;
						}
					}
				}
				if (entries.size() > 0) {
					setSettingEntries(entries, resource);
				} else {
					setSettingEntries(null, resource);
				}
				IStatus status = new Status(IStatus.INFO, MakeCorePlugin.PLUGIN_ID, getClass().getSimpleName()
						+ " collected " + entries.size() + " entries for " + resource);
				MakeCorePlugin.log(status);
			}
		// FIXME
//		} else {
//			if (parsedResourceName != null) {
//				IStatus status = new Status(IStatus.INFO, MakeCorePlugin.PLUGIN_ID, getClass()
//						.getSimpleName() + " not found resource " + parsedResourceName);
//				MakeCorePlugin.log(status);
//			}
//	
//		}
		return false;
	}

	/**
	 * TODO
	 */
	protected abstract List<String> parseOptions(String line);

	/**
	 * TODO
	 */
	protected abstract AbstractOptionParser[] getOptionParsers();

	/**
	 * TODO
	 */
	protected abstract String parseResourceName(String line);

	private IResource findPathInWorkspace(String parsedName) {
		IPath path = new Path(parsedName);
		// FIXME
		if (path.equals(new Path(".")) || path.equals(new Path(".."))) {
			return null;
		}
	
		IProject project = getProject();
		if (project==null) {
			return null;
		}

		// prefer the current project
		List<IResource> result = findPathInFolder(path, project);
		int size = result.size();
		if (size==1) { // found the one
			return result.get(0);
		} else if (size>1) { // ambiguous
			return null;
		}
	
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		// then prefer referenced projects
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		Map<String,String> refs = cfgDescription.getReferenceInfo();
		Set<String> referencedProjectsNames = new LinkedHashSet<String>(refs.keySet());
		for (String prjName : referencedProjectsNames) {
			IProject prj = root.getProject(prjName);
			if (prj.isOpen()) {
				result.addAll(findPathInFolder(path, prj));
			}
		}
		size = result.size();
		if (size==1) { // found the one
			return result.get(0);
		} else if (size>1) { // ambiguous
			return null;
		}
	
		// then check all other projects in workspace
		IProject[] projects = root.getProjects();
		for (IProject prj : projects) {
			if (!prj.equals(project) && !referencedProjectsNames.contains(prj.getName()) && prj.isOpen()) {
				result.addAll(findPathInFolder(path, prj));
			}
			
		}
		size = result.size();
		if (size==1) { // found the one
			return result.get(0);
		}
		
		// not found or ambiguous
		return null;
	}

	private List<IResource> findPathInFolder(IPath path, IContainer folder) {
		List<IResource> paths = new ArrayList<IResource>();
		IResource resource = folder.findMember(path);
		if (resource != null) {
			paths.add(resource);
		}
	
		try {
			for (IResource res : folder.members()) {
				if (res instanceof IContainer) {
					paths.addAll(findPathInFolder(path, (IContainer) res));
				}
			}
		} catch (CoreException e) {
			// ignore
		}
	
		return paths;
	}

	private IResource findResource(String parsedResourceName) {
		IResource sourceFile = null;
		if (parsedResourceName != null) {
			if (errorParserManager != null) {
				sourceFile = errorParserManager.findFileName(parsedResourceName);
			} else {
				IProject project = getProject();
				sourceFile = project.findMember(parsedResourceName);
			}
		}
		return sourceFile;
	}

	
	
//	private String parseSourceFileName(String line) {
//		String sourceFileName = null;
//		String patternCompileUnquotedFile = getPatternCompileUnquotedFile();
//		Matcher fileMatcher = Pattern.compile(patternCompileUnquotedFile).matcher(line);
//	
//		if (fileMatcher.matches()) {
//			sourceFileName = fileMatcher.group(getGroupForPatternUnquotedFile());
//		} else {
//			String patternCompileQuotedFile = getPatternCompileQuotedFile();
//			fileMatcher = Pattern.compile(patternCompileQuotedFile).matcher(line);
//			if (fileMatcher.matches()) {
//				sourceFileName = fileMatcher.group(getGroupForPatternQuotedFile());
//			}
//		}
//		return sourceFileName;
//	}

	private URI findBaseLocationURI(URI fileURI, String relativeFileName) {
		URI cwdURI = null;
		String path = fileURI.getPath();
	
		String[] segments = relativeFileName.split("[/\\\\]"); //$NON-NLS-1$
	
		// start removing segments from the end of the path
		for (int i = segments.length - 1; i >= 0; i--) {
			String lastSegment = segments[i];
			if (lastSegment.length() > 0 && !lastSegment.equals(".")) { //$NON-NLS-1$
				if (lastSegment.equals("..")) { //$NON-NLS-1$
					// navigating ".." in the other direction is ambiguous, bailing out
					return null;
				} else {
					if (path.endsWith("/" + lastSegment)) { //$NON-NLS-1$
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
			cwdURI = new URI(fileURI.getScheme(), fileURI.getUserInfo(), fileURI.getHost(),
					fileURI.getPort(), path, fileURI.getQuery(), fileURI.getFragment());
		} catch (URISyntaxException e) {
			// It should be valid URI here or something is wrong
			MakeCorePlugin.log(e);
		}
	
		return cwdURI;
	}

	private URI getMappedRoot(IResource sourceFile, IPath parsedSrcPath) {
		URI fileURI = sourceFile.getLocationURI();
		IPath mappedRootPath = new Path("/"); //$NON-NLS-1$
		IPath absPath = sourceFile.getLocation();
		int absSegmentsCount = absPath.segmentCount();
		int relSegmentsCount = parsedSrcPath.segmentCount();
		if (absSegmentsCount >= relSegmentsCount) {
			IPath ending = absPath.removeFirstSegments(absSegmentsCount - relSegmentsCount);
			ending = ending.setDevice(parsedSrcPath.getDevice()).makeAbsolute();
			if (ending.equals(parsedSrcPath.makeAbsolute())) {
				mappedRootPath = absPath.removeLastSegments(relSegmentsCount);
			}
		}
	
		URI mappedRootURI = EFSExtensionManager.getDefault().createNewURIFromPath(fileURI,
				mappedRootPath.toString());
		return mappedRootURI;
	}

	private URI getURI(String name, URI baseURI) {
		URI uri = null;
	
		if (baseURI==null) {
			uri = resolvePathFromBaseLocation(name, new Path("/"));
		} else if (baseURI.getScheme().equals(EFS.SCHEME_FILE)) {
			// location on the local filesystem
			IPath baseLocation = org.eclipse.core.filesystem.URIUtil.toPath(baseURI);
			// careful not to use 'path' here but 'name' as we want to properly navigate symlinks
			uri = resolvePathFromBaseLocation(name, baseLocation);
		} else {
			// use canonicalized path here, in particular replace all '\' with '/' for Windows paths
			Path path = new Path(name);
			uri = EFSExtensionManager.getDefault().append(baseURI, path.toString());
		}
	
		if (uri == null) {
			// if everything fails
			uri = org.eclipse.core.filesystem.URIUtil.toURI(name);
		}
		return uri;
	}

	/**
	 * The manipulations here are done to resolve "../" navigation for symbolic links where "link/.." cannot
	 * be collapsed as it must follow the real filesystem path. {@link java.io.File#getCanonicalPath()} deals
	 * with that correctly but {@link Path} or {@link URI} try to normalize the path which would be incorrect
	 * here.
	 */
	private URI resolvePathFromBaseLocation(String name, IPath baseLocation) {
		String pathName = name;
		if (baseLocation != null && !baseLocation.isEmpty()) {
			String device = new Path(pathName).getDevice();
			if (device != null && device.length() > 0) {
				pathName = pathName.substring(device.length());
			}
			pathName = pathName.replace(File.separatorChar, '/');
	
			baseLocation = baseLocation.addTrailingSeparator();
			if (pathName.startsWith("/")) { //$NON-NLS-1$
				pathName = pathName.substring(1);
			}
			pathName = baseLocation.toString() + pathName;
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
			if (folders.length > 0) {
				IContainer container = folders[0];
				if ((container instanceof IProject || container instanceof IFolder)) { // treat IWorkspaceRoot
																						// as non-workspace
																						// path
					path = container.getFullPath();
				}
			}
			break;
		case ICSettingEntry.INCLUDE_FILE:
		case ICSettingEntry.MACRO_FILE:
			IFile[] files = root.findFilesForLocationURI(uri);
			if (files.length > 0) {
				IFile file = files[0];
				for (IFile f : files) {
					if (f.getProject().equals(getProject())) {
						file = f;
						break;
					}
				}
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractLanguageSettingsOutputScanner other = (AbstractLanguageSettingsOutputScanner) obj;
		if (expandRelativePaths != other.expandRelativePaths)
			return false;
		return true;
	}

	@SuppressWarnings("nls")
	private String expressionLogicalOr(Set<String> fileExts) {
		String pattern = "(";
		for (String ext : fileExts) {
			if (pattern.length() != 1)
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

	protected String getPatternFileExtensions() {
		IContentTypeManager manager = Platform.getContentTypeManager();
	
		Set<String> fileExts = new HashSet<String>();
	
		IContentType contentTypeCpp = manager.getContentType("org.eclipse.cdt.core.cxxSource"); //$NON-NLS-1$
		fileExts.addAll(Arrays.asList(contentTypeCpp.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));
	
		IContentType contentTypeC = manager.getContentType("org.eclipse.cdt.core.cSource"); //$NON-NLS-1$
		fileExts.addAll(Arrays.asList(contentTypeC.getFileSpecs(IContentType.FILE_EXTENSION_SPEC)));
	
		String pattern = expressionLogicalOr(fileExts);
	
		return pattern;
	}

	protected static int countGroups(String str) {
		@SuppressWarnings("nls")
		int count = str.replaceAll("[^\\(]", "").length();
		return count;
	}

}

/**********************************************************************
 * Created on 25-Mar-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICModelStatus;
import org.eclipse.cdt.core.model.ICModelStatusConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IContainerEntry;
import org.eclipse.cdt.core.model.IElementChangedListener;
import org.eclipse.cdt.core.model.IIncludeEntry;
import org.eclipse.cdt.core.model.ILibraryEntry;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.core.model.IProjectEntry;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.PathEntryContainerInitializer;
import org.eclipse.cdt.core.resources.IPathEntryStore;
import org.eclipse.cdt.core.resources.IPathEntryStoreListener;
import org.eclipse.cdt.core.resources.PathEntryStoreChangedEvent;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * @author alain
 *  
 */
public class PathEntryManager implements IPathEntryStoreListener, IElementChangedListener {

	static String CONTAINER_INITIALIZER_EXTPOINT_ID = "PathEntryContainerInitializer"; //$NON-NLS-1$
	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	static final String[] NO_PREREQUISITES = new String[0];
	/**
	 * pathentry containers pool
	 * accessing the Container is done synch with the class
	 */
	private static HashMap Containers = new HashMap(5);

	static final IPathEntry[] NO_PATHENTRIES = new IPathEntry[0];

	// Synchronized the access of the cache entries.
	private Map resolvedMap = new Hashtable();

	// Accessing the map is synch with the class
	private Map storeMap = new HashMap();

	private static PathEntryManager pathEntryManager;
		private PathEntryManager() {
	}

	/**
	 * Return the singleton.
	 */
	public static synchronized PathEntryManager getDefault() {
		if (pathEntryManager == null) {
			pathEntryManager = new PathEntryManager();
			CoreModel.getDefault().addElementChangedListener(pathEntryManager);
		}
		return pathEntryManager;
	}

	public IPathEntry[] getResolvedPathEntries(ICProject cproject) throws CModelException {
		//boolean markers = cproject.getProject().getWorkspace().isTreeLocked();
		//return getResolvedPathEntries(cproject, !markers);
		return getResolvedPathEntries(cproject, false);
	}
	
	public IPathEntry[] getResolvedPathEntries(ICProject cproject, boolean generateMarkers) throws CModelException {
		IPathEntry[] entries = (IPathEntry[])resolvedMap.get(cproject);
		if (entries == null) {
			IPath projectPath = cproject.getPath();
			entries = getRawPathEntries(cproject);
			ArrayList list = new ArrayList();
			for (int i = 0; i < entries.length; i++) {
				IPathEntry entry = entries[i];
				// Expand the containers.
				if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry centry = (IContainerEntry) entry;
					IPathEntryContainer container = getPathEntryContainer(centry, cproject);
					if (container != null) {
						IPathEntry[] containerEntries = container.getPathEntries();
						if (containerEntries != null) {
							for (int j = 0; j < containerEntries.length; j++) {
								IPathEntry newEntry = cloneEntry(projectPath, containerEntries[j]);
								list.add(newEntry);
							}
						}
					}
				} else {
					IPathEntry clone = cloneEntry(projectPath, entry);
					IPathEntry e = getExpandedPathEntry(clone, cproject);
					if (e != null) {
						list.add(e);
					}
				}
			}
			entries = new IPathEntry[list.size()];
			list.toArray(entries);
			if (generateMarkers) {
				IProject project = cproject.getProject();
				flushPathEntryProblemMarkers(project);
				ICModelStatus status = validatePathEntry(cproject, entries);
				if (!status.isOK()) {
					createPathEntryProblemMarker(project, status);
				}
				for (int j = 0; j < entries.length; j++) {
					status = validatePathEntry(cproject, entries[j], true, false);
					if (!status.isOK()) {
						createPathEntryProblemMarker(project, status);
					}
				}
			}
			resolvedMap.put(cproject, entries);
		}
		return entries;
	}

	private IPathEntry getExpandedPathEntry(IPathEntry entry, ICProject cproject) throws CModelException {
		switch(entry.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE: {
				IIncludeEntry includeEntry = (IIncludeEntry)entry;
				IPath refPath = includeEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					IPath includePath = includeEntry.getIncludePath();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							IProject project = (IProject)res;
							if (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project)) {
								ICProject refCProject = CoreModel.getDefault().create(project);
								if (refCProject != null) {
									IPathEntry[] entries = getResolvedPathEntries(refCProject);
									for (int i = 0; i < entries.length; i++) {
										if (entries[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
											IIncludeEntry refEntry = (IIncludeEntry)entries[i];
											if (refEntry.getIncludePath().equals(includePath)) {
												IPath newBasePath = refEntry.getBasePath();
												return CoreModel.newIncludeEntry(includeEntry.getPath(),
														newBasePath, includePath);
											}
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_INCLUDE) {
									IIncludeEntry refEntry = (IIncludeEntry)entries[i];
									if (refEntry.getIncludePath().equals(includePath)) {
										IPath newBasePath = refEntry.getBasePath();
										return CoreModel.newIncludeEntry(includeEntry.getPath(), newBasePath, includePath);											
									}
								}
							}
						}
					}
				}
				break;
			}

			case IPathEntry.CDT_MACRO: {
				IMacroEntry macroEntry = (IMacroEntry)entry;
				IPath refPath = macroEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					String name = macroEntry.getMacroName();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							IProject project = (IProject)res;
							if (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project)) {
								ICProject refCProject = CoreModel.getDefault().create(project);
								if (refCProject != null) {
									IPathEntry[] entries = getResolvedPathEntries(refCProject);
									for (int i = 0; i < entries.length; i++) {
										if (entries[i].getEntryKind() == IPathEntry.CDT_MACRO) {
											IMacroEntry refEntry = (IMacroEntry)entries[i];
											if (refEntry.getMacroName().equals(name)) {
												String value = refEntry.getMacroValue();
												return CoreModel.newMacroEntry(macroEntry.getPath(), name, value);											
											}
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_MACRO) {
									IMacroEntry refEntry = (IMacroEntry)entries[i];
									if (refEntry.getMacroName().equals(name)) {
										String value = refEntry.getMacroValue();
										return CoreModel.newMacroEntry(macroEntry.getPath(), name, value);											
									}
								}
							}
						}
					}
				}
				break;
			}

			case IPathEntry.CDT_LIBRARY: {
				ILibraryEntry libEntry = (ILibraryEntry)entry;
				IPath refPath = libEntry.getBaseReference();
				if (refPath != null && !refPath.isEmpty()) {
					IPath libraryPath = libEntry.getLibraryPath();
					if (refPath.isAbsolute()) {
						IResource res = cproject.getCModel().getWorkspace().getRoot().findMember(refPath);
						if (res != null && res.getType() == IResource.PROJECT) {
							IProject project = (IProject)res;
							if (CoreModel.hasCNature(project) || CoreModel.hasCCNature(project)) {
								ICProject refCProject = CoreModel.getDefault().create(project);
								if (refCProject != null) {
									IPathEntry[] entries = getResolvedPathEntries(refCProject);
									for (int i = 0; i < entries.length; i++) {
										if (entries[i].getEntryKind() == IPathEntry.CDT_LIBRARY) {
											ILibraryEntry refEntry = (ILibraryEntry)entries[i];
											if (refEntry.getPath().equals(libraryPath)) {
												return CoreModel.newLibraryEntry(entry.getPath(), refEntry.getBasePath(),
														refEntry.getLibraryPath(), refEntry.getSourceAttachmentPath(),
														refEntry.getSourceAttachmentRootPath(),
														refEntry.getSourceAttachmentPrefixMapping(), false);											
											}
										}
									}
								}
							}
						}
					} else { // Container ref
						IPathEntryContainer container = getPathEntryContainer(refPath, cproject);
						if (container != null) {
							IPathEntry[] entries = container.getPathEntries();
							for (int i = 0; i < entries.length; i++) {
								if (entries[i].getEntryKind() == IPathEntry.CDT_LIBRARY) {
									ILibraryEntry refEntry = (ILibraryEntry)entries[i];
									if (refEntry.getPath().equals(libraryPath)) {
										return CoreModel.newLibraryEntry(entry.getPath(), refEntry.getBasePath(),
												refEntry.getLibraryPath(), refEntry.getSourceAttachmentPath(),
												refEntry.getSourceAttachmentRootPath(),
												refEntry.getSourceAttachmentPrefixMapping(), false);											
									}
								}
							}
						}
					}
				}
				break;
			}

		}
		return entry;
	}

	public void setRawPathEntries(ICProject cproject, IPathEntry[] newEntries, IProgressMonitor monitor) throws CModelException {
		try {
			IPathEntry[] oldResolvedEntries = (IPathEntry[]) resolvedMap.get(cproject);
			SetPathEntriesOperation op = new SetPathEntriesOperation(cproject, oldResolvedEntries, newEntries);
			CModelManager.getDefault().runOperation(op, monitor);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public IPathEntry[] getRawPathEntries(ICProject cproject) throws CModelException {
		IProject project = cproject.getProject();
		// Check if the Project is accesible.
		if (!(CoreModel.hasCNature(project) || CoreModel.hasCCNature(project))) {
			throw new CModelException(new CModelStatus(ICModelStatusConstants.ELEMENT_DOES_NOT_EXIST));
		}
		IPathEntry[] pathEntries;
		try {
			IPathEntryStore store = getPathEntryStore(project, true);
			pathEntries = store.getRawPathEntries();
		} catch (CoreException e) {
			throw new CModelException(e);
		}

		// Checks/hacks for backward compatibility ..
		// if no output is specified we return the project
		// if no source is specified we return the project
		boolean foundSource = false;
		boolean foundOutput = false;
		for (int i = 0; i < pathEntries.length; i++) {
			IPathEntry rawEntry = pathEntries[i];
			if (rawEntry.getEntryKind() == IPathEntry.CDT_SOURCE) {
				foundSource = true;
			}
			if (rawEntry.getEntryKind() == IPathEntry.CDT_OUTPUT) {
				foundOutput = true;
			}
		}

		if (!foundSource) {
			IPathEntry[] newEntries = new IPathEntry[pathEntries.length + 1];
			System.arraycopy(pathEntries, 0, newEntries, 0, pathEntries.length);
			newEntries[pathEntries.length] = CoreModel.newSourceEntry(cproject.getPath());
			pathEntries = newEntries;
		}
		if (!foundOutput) {
			IPathEntry[] newEntries = new IPathEntry[pathEntries.length + 1];
			System.arraycopy(pathEntries, 0, newEntries, 0, pathEntries.length);
			newEntries[pathEntries.length] = CoreModel.newOutputEntry(cproject.getPath());
			pathEntries = newEntries;
		}
		return pathEntries;
	}

	public void setPathEntryContainer(ICProject[] affectedProjects, IPathEntryContainer newContainer, IProgressMonitor monitor)
			throws CModelException {
		if (monitor != null && monitor.isCanceled()) {
			return;
		}
		IPath containerPath = (newContainer == null) ? new Path("") : newContainer.getPath(); //$NON-NLS-1$
		final int projectLength = affectedProjects.length;
		final ICProject[] modifiedProjects = new ICProject[projectLength];
		System.arraycopy(affectedProjects, 0, modifiedProjects, 0, projectLength);
		final IPathEntry[][] oldResolvedEntries = new IPathEntry[projectLength][];
		// filter out unmodified project containers
		int remaining = 0;
		for (int i = 0; i < projectLength; i++) {
			if (monitor != null && monitor.isCanceled()) {
				return;
			}
			ICProject affectedProject = affectedProjects[i];
			boolean found = false;
			IPathEntry[] rawPath = getRawPathEntries(affectedProject);
			for (int j = 0, cpLength = rawPath.length; j < cpLength; j++) {
				IPathEntry entry = rawPath[j];
				if (entry.getEntryKind() == IPathEntry.CDT_CONTAINER) {
					IContainerEntry cont = (IContainerEntry) entry;
					if (cont.getPath().equals(containerPath)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				// filter out this project - does not reference the container path
				modifiedProjects[i] = null;
				// Still add it to the cache
				containerPut(affectedProject, containerPath, newContainer);
				continue;
			}
			IPathEntryContainer oldContainer = containerGet(affectedProject, containerPath);
			if (oldContainer != null && newContainer != null && oldContainer.equals(newContainer)) {
				modifiedProjects[i] = null; // filter out this project -
				// container did not change
				continue;
			}
			remaining++;
			oldResolvedEntries[i] = (IPathEntry[]) resolvedMap.remove(affectedProject);
			containerPut(affectedProject, containerPath, newContainer);
		}
		// Nothing change.
		if (remaining == 0) {
			return;
		}
		// trigger model refresh
		try {
			//final boolean canChangeResources = !ResourcesPlugin.getWorkspace().isTreeLocked();
			CoreModel.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor progressMonitor) throws CoreException {

					boolean shouldFire = false;
					CModelManager mgr = CModelManager.getDefault();
					for (int i = 0; i < projectLength; i++) {
						if (progressMonitor != null && progressMonitor.isCanceled()) {
							return;
						}
						ICProject affectedProject = modifiedProjects[i];
						if (affectedProject == null) {
							continue; // was filtered out
						}
						// Only fire deltas if we had previous cache
						if (oldResolvedEntries[i] != null) {
							IPathEntry[] newEntries = getResolvedPathEntries(affectedProject);
							ICElementDelta[] deltas = generatePathEntryDeltas(affectedProject, oldResolvedEntries[i], newEntries);
							if (deltas.length > 0) {
								shouldFire = true;
								for (int j = 0; j < deltas.length; j++) {
									mgr.registerCModelDelta(deltas[j]);
								}
							}
						}
					}
					if (shouldFire) {
						mgr.fire(ElementChangedEvent.POST_CHANGE);
					}
				}
			}, monitor);
		} catch (CoreException e ) {
			//
		}
	}

	public IPathEntryContainer getPathEntryContainer(IContainerEntry entry, ICProject cproject) throws CModelException {
		return getPathEntryContainer(entry.getPath(), cproject);
	}

	public IPathEntryContainer getPathEntryContainer(final IPath containerPath, final ICProject project) throws CModelException {
		// Try the cache.
		IPathEntryContainer container = containerGet(project, containerPath);
		if (container == null) {
			final PathEntryContainerInitializer initializer = getPathEntryContainerInitializer(containerPath.segment(0));
			if (initializer != null) {
				containerPut(project, containerPath, container);
				boolean ok = false;
				try {
					// wrap initializer call with Safe runnable in case
					// initializer would be
					// causing some grief
					Platform.run(new ISafeRunnable() {

						public void handleException(Throwable exception) {
							IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, IStatus.ERROR,
									"Exception occurred in container initializer: "+initializer, exception); //$NON-NLS-1$
							CCorePlugin.log(status);
						}

						public void run() throws Exception {
							initializer.initialize(containerPath, project);
						}
					});
					// retrieve value (if initialization was successful)
					container = containerGet(project, containerPath);
					ok = true;
				} finally {
					if (!ok) {
						containerPut(project, containerPath, null); // flush
						// cache
					}
				}
			}
		}
		return container;
	}

	/**
	 * Helper method finding the container initializer registered for a given container ID or <code>null</code> if none was found
	 * while iterating over the contributions to extension point to the extension point
	 * "org.eclipse.cdt.core.PathEntryContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to identify the registered container initializer.
	 * <p>
	 * 
	 * @param containerID -
	 *            a containerID identifying a registered initializer
	 * @return PathEntryContainerInitializer - the registered container initializer or <code>null</code> if none was found.
	 */
	public PathEntryContainerInitializer getPathEntryContainerInitializer(String containerID) {
		Plugin core = CCorePlugin.getDefault();
		if (core == null) {
			return null;
		}
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, CONTAINER_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					String initializerID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (initializerID != null && initializerID.equals(containerID)) {
						try {
							Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof PathEntryContainerInitializer) {
								return (PathEntryContainerInitializer) execExt;
							}
						} catch (CoreException e) {
							// executable extension could not be created:
							// ignore this initializer if
							//e.printStackTrace();
						}
					}
				}
			}
		}
		return null;
	}

	private synchronized IPathEntryContainer containerGet(ICProject cproject, IPath containerPath) {
		Map projectContainers = (Map) Containers.get(cproject);
		if (projectContainers == null) {
			projectContainers = new HashMap();
			Containers.put(cproject, projectContainers);
		}
		IPathEntryContainer container = (IPathEntryContainer) projectContainers.get(containerPath);
		return container;
	}

	private synchronized void containerPut(ICProject cproject, IPath containerPath, IPathEntryContainer container) {
		Map projectContainers = (Map) Containers.get(cproject);
		if (projectContainers == null) {
			projectContainers = new HashMap();
			Containers.put(cproject, projectContainers);
		}
		projectContainers.put(containerPath, container);
	}

	private synchronized void containerRemove(ICProject cproject) {
		Containers.remove(cproject);
	}

	public String[] projectPrerequisites(IPathEntry[] entries) throws CModelException {
		if (entries != null) {
			ArrayList prerequisites = new ArrayList();
			for (int i = 0, length = entries.length; i < length; i++) {
				if (entries[i].getEntryKind() == IPathEntry.CDT_PROJECT) {
					IProjectEntry entry = (IProjectEntry) entries[i];
					prerequisites.add(entry.getPath().lastSegment());
				}
			}
			int size = prerequisites.size();
			if (size != 0) {
				String[] result = new String[size];
				prerequisites.toArray(result);
				return result;
			}
		}
		return NO_PREREQUISITES;
	}

	public void saveRawPathEntries(ICProject cproject, IPathEntry[] entries) throws CModelException {
		// sanity
		if (entries == null) {
			entries = NO_PATHENTRIES;
		}

		ArrayList list = new ArrayList(entries.length);
		IPath projectPath = cproject.getPath();
		for (int i = 0; i < entries.length; i++) {
			IPathEntry entry;
			
			int kind = entries[i].getEntryKind();
			
			// translate the project prefix.
			IPath resourcePath = entries[i].getPath();
			if (resourcePath == null) {
				resourcePath = Path.EMPTY;
			}
			
			// Do not do this for container, the path is the ID.
			if (kind != IPathEntry.CDT_CONTAINER) {
				// translate to project relative from absolute (unless a device path)
				if (resourcePath.isAbsolute()) {
					if (projectPath != null && projectPath.isPrefixOf(resourcePath)) {
						if (resourcePath.segment(0).equals(projectPath.segment(0))) {
							resourcePath = resourcePath.removeFirstSegments(1);
							resourcePath = resourcePath.makeRelative();
						} else {
							resourcePath = resourcePath.makeAbsolute();
						}
					}
				}
			}
			
			// Specifics to the entries
			switch(kind) {
				case IPathEntry.CDT_INCLUDE: {
					IIncludeEntry include = (IIncludeEntry)entries[i];
					entry =  CoreModel.newIncludeEntry(resourcePath, include.getBasePath(), include.getIncludePath(),
							include.isSystemInclude(), include.getExclusionPatterns(), include.isExported());
					break;
				}
				case IPathEntry.CDT_LIBRARY: {
					ILibraryEntry library = (ILibraryEntry)entries[i];
					IPath sourcePath = library.getSourceAttachmentPath();
					if (sourcePath != null) {
						// translate to project relative from absolute 
						if (projectPath != null && projectPath.isPrefixOf(sourcePath)) {
							if (sourcePath.segment(0).equals(projectPath.segment(0))) {
								sourcePath = sourcePath.removeFirstSegments(1);
								sourcePath = sourcePath.makeRelative();
							}
						}
					}
					entry = CoreModel.newLibraryEntry(resourcePath, library.getBasePath(),
							library.getLibraryPath(), sourcePath, library.getSourceAttachmentRootPath(),
							library.getSourceAttachmentPrefixMapping(), library.isExported());
					break;
				}
				case IPathEntry.CDT_MACRO: {
					IMacroEntry macro = (IMacroEntry)entries[i];
					entry = CoreModel.newMacroEntry(resourcePath, macro.getMacroName(), macro.getMacroValue(),
							macro.getExclusionPatterns(), macro.isExported());
					break;
				}
				case IPathEntry.CDT_OUTPUT: {
					IOutputEntry out = (IOutputEntry)entries[i];
					entry = CoreModel.newOutputEntry(resourcePath, out.getExclusionPatterns());
					break;
				}
				case IPathEntry.CDT_PROJECT: {
					IProjectEntry projEntry = (IProjectEntry)entries[i];
					entry = CoreModel.newProjectEntry(projEntry.getPath(), projEntry.isExported());
					break;
				}
				case IPathEntry.CDT_SOURCE: {
					ISourceEntry source = (ISourceEntry)entries[i];
					entry = CoreModel.newSourceEntry(resourcePath, source.getExclusionPatterns());
					break;
				}
				case IPathEntry.CDT_CONTAINER:
					entry = CoreModel.newContainerEntry(entries[i].getPath(), entries[i].isExported());
				break;
				default:
					entry = entries[i];
			}
			list.add(entry);
		}
		try {
			IPathEntry[] newRawEntries = new IPathEntry[list.size()];
			list.toArray(newRawEntries);
			IProject project = cproject.getProject();
			IPathEntryStore store = getPathEntryStore(project, true);
			store.setRawPathEntries(newRawEntries);
		} catch (CoreException e) {
			throw new CModelException(e);
		}
	}

	public ICElementDelta[] generatePathEntryDeltas(ICProject cproject, IPathEntry[] oldEntries, IPathEntry[] newEntries) {
		ArrayList list = new ArrayList();

		// if nothing was known before do not generate any deltas.
		if (oldEntries == null) {
			return new ICElementDelta[0];
		}
		// Sanity checks
		if (newEntries == null) {
			newEntries = NO_PATHENTRIES;
		}

		// Check the removed entries.
		for (int i = 0; i < oldEntries.length; i++) {
			boolean found = false;
			for (int j = 0; j < newEntries.length; j++) {
				if (oldEntries[i].equals(newEntries[j])) {
					found = true;
					break;
				}
			}
			// Was it deleted.
			if (!found) {
				ICElementDelta delta = makePathEntryDelta(cproject, oldEntries[i], true);
				if (delta != null) {
					list.add(delta);
				}
			}
		}

		// Check the new entries.
		for (int i = 0; i < newEntries.length; i++) {
			boolean found = false;
			for (int j = 0; j < oldEntries.length; j++) {
				if (newEntries[i].equals(oldEntries[j])) {
					found = true;
					break;
				}
			}
			// is it new?
			if (!found) {
				ICElementDelta delta = makePathEntryDelta(cproject, newEntries[i], false);
				if (delta != null) {
					list.add(delta);
				}
			}
		}

		// Check for reorder
		if (list.size() == 0 && oldEntries.length == newEntries.length) {
			for (int i = 0; i < newEntries.length; i++) {
				if (!newEntries[i].equals(oldEntries[i])) {
					ICElementDelta delta = makePathEntryDelta(cproject, null, false);
					if (delta != null) {
						list.add(delta);
					}
				}
			}
		}
		ICElementDelta[] deltas = new ICElementDelta[list.size()];
		list.toArray(deltas);
		return deltas;
	}

	/**
	 * return a delta, with the specified change flag.
	 */
	protected ICElementDelta makePathEntryDelta(ICProject cproject, IPathEntry entry, boolean removed) {
		ICElement celement = null;
		int flag = ICElementDelta.F_PATHENTRY_REORDER;
		if (entry == null) {
			celement = cproject;
			flag = ICElementDelta.F_PATHENTRY_REORDER;
		} else {
			int kind = entry.getEntryKind();
			switch (kind) {
				case IPathEntry.CDT_SOURCE: {
					ISourceEntry source = (ISourceEntry) entry;
					IPath path = source.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_SOURCE : ICElementDelta.F_ADDED_PATHENTRY_SOURCE;
					break;
				}
				case IPathEntry.CDT_LIBRARY: {
					celement = cproject;
					flag = (removed) ? ICElementDelta.F_REMOVED_PATHENTRY_LIBRARY : ICElementDelta.F_ADDED_PATHENTRY_LIBRARY;
					break;
				}
				case IPathEntry.CDT_PROJECT: {
					//IProjectEntry pentry = (IProjectEntry) entry;
					celement = cproject;
					flag = ICElementDelta.F_CHANGED_PATHENTRY_PROJECT;
					break;
				}
				case IPathEntry.CDT_INCLUDE: {
					IIncludeEntry include = (IIncludeEntry) entry;
					IPath path = include.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = ICElementDelta.F_CHANGED_PATHENTRY_INCLUDE;
					break;
				}
				case IPathEntry.CDT_MACRO: {
					IMacroEntry macro = (IMacroEntry) entry;
					IPath path = macro.getPath();
					celement = CoreModel.getDefault().create(path);
					flag = ICElementDelta.F_CHANGED_PATHENTRY_MACRO;
					break;
				}
				case IPathEntry.CDT_CONTAINER: {
					//IContainerEntry container = (IContainerEntry) entry;
					//celement = cproject;
					//SHOULD NOT BE HERE Container are resolved.
					break;
				}
			}
		}
		if (celement == null) {
			celement = cproject;
		}
		CElementDelta delta = new CElementDelta(cproject.getCModel());
		delta.changed(celement, flag);
		return delta;
	}

	static String[] getRegisteredContainerIDs() {
		Plugin core = CCorePlugin.getDefault();
		if (core == null) {
			return null;
		}
		ArrayList containerIDList = new ArrayList(5);
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, CONTAINER_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
				for (int j = 0; j < configElements.length; j++) {
					String idAttribute = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (idAttribute != null)
						containerIDList.add(idAttribute);
				}
			}
		}
		String[] containerIDs = new String[containerIDList.size()];
		containerIDList.toArray(containerIDs);
		return containerIDs;
	}

	public void setPathEntryStore(IProject project, IPathEntryStore newStore) {
		IPathEntryStore oldStore = null;
		synchronized(this) {
			oldStore = (IPathEntryStore)storeMap.remove(project);
			if (newStore != null) {
				storeMap.put(project, newStore);
			}
		}
		if (oldStore != null) {
			// remove are self before closing
			oldStore.removePathEntryStoreListener(this);
			oldStore.close();
		}
	}

	private synchronized IPathEntryStore getPathEntryStore(IProject project, boolean create) throws CoreException {
		IPathEntryStore store = (IPathEntryStore)storeMap.get(project);
		if (store == null && create == true) {
			store = CCorePlugin.getDefault().getPathEntryStore(project);
			storeMap.put(project, store);
			store.addPathEntryStoreListener(this);
		}
		return store;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.resources.IPathEntryStoreListener#pathEntryStoreChanged(org.eclipse.cdt.core.resources.PathEntryChangedEvent)
	 */
	public void pathEntryStoreChanged(PathEntryStoreChangedEvent event) {
		IProject project = event.getProject();
		
		// sanity
		if (project == null) {
			return;
		}

		CModelManager manager = CModelManager.getDefault();
		ICProject cproject = manager.create(project);
		if (event.hasClosed()) {
			setPathEntryStore(project, null);
			containerRemove(cproject);
		}
		if (project.isAccessible()) {
			try {
				// Clear the old cache entries.
				IPathEntry[] oldResolvedEntries = (IPathEntry[])resolvedMap.remove(cproject);
				IPathEntry[] newResolvedEntries = getResolvedPathEntries(cproject);
				ICElementDelta[] deltas = generatePathEntryDeltas(cproject, oldResolvedEntries, newResolvedEntries);
				if (deltas.length > 0) {
					cproject.close();
					for (int i = 0; i < deltas.length; i++) {
						manager.registerCModelDelta(deltas[i]);
					}
					manager.fire(ElementChangedEvent.POST_CHANGE);
				}
			} catch (CModelException e) {
				CCorePlugin.log(e);
			}
		} else {
			resolvedMap.remove(cproject);
			containerRemove(cproject);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IElementChangedListener#elementChanged(org.eclipse.cdt.core.model.ElementChangedEvent)
	 */
	public void elementChanged(ElementChangedEvent event) {
		try {
			processDelta(event.getDelta());
		} catch(CModelException e) {
		}
	}

	protected void processDelta(ICElementDelta delta) throws CModelException {
		int kind= delta.getKind();
		int flags= delta.getFlags();
		ICElement element= delta.getElement();

		//System.out.println("Processing " + element);

		// handle closing and removing of projects
		if (((flags & ICElementDelta.F_CLOSED) != 0) || (kind == ICElementDelta.REMOVED)) {
			if (element.getElementType() == ICElement.C_PROJECT) {
				ICProject cproject = (ICProject)element;
				IProject project = cproject.getProject();
				IPathEntryStore store = null;
				try {
					store = getPathEntryStore(project, false);
					if (store != null) {
						store.close();
					}
				} catch (CoreException e) {
					throw new CModelException(e);
				} finally {
					if (store == null) {
						resolvedMap.remove(cproject);
						containerRemove(cproject);
					}
				}
			}
		}
		ICElementDelta[] affectedChildren= delta.getAffectedChildren();
		for (int i= 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
	}

	protected IPathEntry cloneEntry(IPath rpath, IPathEntry entry) {

		// get the path
		IPath entryPath = entry.getPath();
		if (entryPath == null) {
			entryPath = Path.EMPTY;
		}
		IPath resourcePath = (entryPath.isAbsolute()) ? entryPath : rpath.append(entryPath);

		switch(entry.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE: {
				IIncludeEntry include = (IIncludeEntry)entry;
				return CoreModel.newIncludeEntry(resourcePath, include.getBasePath(), include.getIncludePath(),
						include.isSystemInclude(), include.getExclusionPatterns(), include.isExported());
			}
			case IPathEntry.CDT_LIBRARY: {
				ILibraryEntry library = (ILibraryEntry)entry;
				return CoreModel.newLibraryEntry(resourcePath, library.getBasePath(), library.getLibraryPath(),
						library.getSourceAttachmentPath(), library.getSourceAttachmentRootPath(),
						library.getSourceAttachmentPrefixMapping(), library.isExported());
			}
			case IPathEntry.CDT_MACRO: {
				IMacroEntry macro = (IMacroEntry)entry;
				return CoreModel.newMacroEntry(resourcePath, macro.getMacroName(), macro.getMacroValue(),
						macro.getExclusionPatterns(), macro.isExported());
			}
			case IPathEntry.CDT_OUTPUT: {
				IOutputEntry out = (IOutputEntry)entry;
				return CoreModel.newOutputEntry(resourcePath, out.getExclusionPatterns());
			}
			case IPathEntry.CDT_PROJECT: {
				IProjectEntry projEntry = (IProjectEntry)entry;
				return CoreModel.newProjectEntry(projEntry.getPath(), projEntry.isExported());
			}
			case IPathEntry.CDT_SOURCE: {
				ISourceEntry source = (ISourceEntry)entry;
				return CoreModel.newSourceEntry(resourcePath, source.getExclusionPatterns());
			}
			case IPathEntry.CDT_CONTAINER:
				return CoreModel.newContainerEntry(entry.getPath(), entry.isExported());
		}
		return entry;
	}

	public ICModelStatus validatePathEntry(ICProject cProject, IPathEntry[] entries) {
		
		// Check duplication.
		for (int i = 0; i < entries.length; i++) {
			IPathEntry entry = entries[i];
			if (entry == null) {
				continue;
			}
			for (int j = 0; j < entries.length; j++) {
				IPathEntry otherEntry = entries[j];
				if (otherEntry == null) {
					continue;
				}
				if (entry != otherEntry && otherEntry.equals(entry)) {
					StringBuffer errMesg = new StringBuffer(CCorePlugin.getResourceString("CoreModel.PathEntry.DuplicateEntry")); //$NON-NLS-1$
					errMesg.append(':').append(entry.toString());
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString());
				}
			}
		}

		// allow nesting source entries in each other as long as the outer entry excludes the inner one
		for (int i = 0; i < entries.length; i++) {
			IPathEntry entry = entries[i];
			if (entry == null) {
				continue;
			}
			IPath entryPath = entry.getPath();
			int kind = entry.getEntryKind();
			if (kind == IPathEntry.CDT_SOURCE){
				for (int j = 0; j < entries.length; j++){
					IPathEntry otherEntry = entries[j];
					if (otherEntry == null) {
						continue;
					}
					int otherKind = otherEntry.getEntryKind();
					IPath otherPath = otherEntry.getPath();
					if (entry != otherEntry && (otherKind == IPathEntry.CDT_SOURCE)) {
						char[][] exclusionPatterns = ((ISourceEntry)otherEntry).fullExclusionPatternChars();
						if (otherPath.isPrefixOf(entryPath) && !otherPath.equals(entryPath)
								&& !CoreModelUtil.isExcluded(entryPath.append("*"), exclusionPatterns)) { //$NON-NLS-1$
									
							String exclusionPattern = entryPath.removeFirstSegments(otherPath.segmentCount()).segment(0);
							if (CoreModelUtil.isExcluded(entryPath, exclusionPatterns)) {
								StringBuffer errMesg = new StringBuffer(CCorePlugin.getResourceString("CoreModel.PathEntry.NestedEntry")); //$NON-NLS-1$
								errMesg.append(':').append(entry.toString());
								return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString());
							} else {
								if (otherKind == IPathEntry.CDT_SOURCE) {
									exclusionPattern += '/';
									StringBuffer errMesg = new StringBuffer(CCorePlugin.getResourceString("CoreModel.PathEntry.NestedEntry")); //$NON-NLS-1$
									errMesg.append(':').append(entry.toString());
									return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString());
								} else {
									StringBuffer errMesg = new StringBuffer(CCorePlugin.getResourceString("CoreModel.PathEntry.NestedEntry")); //$NON-NLS-1$
									errMesg.append(':').append(entry.toString());
									return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, errMesg.toString()); //$NON-NLS-1$
								}
							}
						}
					}
				}
			}
		}

		return CModelStatus.VERIFIED_OK;
	}

	public ICModelStatus validatePathEntry(ICProject cProject, IPathEntry entry, boolean checkSourceAttachment, boolean recurseInContainers){
		IProject project = cProject.getProject();
		StringBuffer sb = new StringBuffer();
		sb.append(CCorePlugin.getResourceString("CoreModel.PathEntry.InvalidPathEntry")); //$NON-NLS-1$
		sb.append(':').append(entry.toString());
		String entryMesg = sb.toString();
		switch(entry.getEntryKind()) {
			case IPathEntry.CDT_INCLUDE: {
				IIncludeEntry include = (IIncludeEntry)entry;
				IPath path = include.getPath();
				if (!isValidWorkspacePath(project, path)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
				}
				IPath includePath = include.getFullIncludePath();
				if (!isValidExternalPath(includePath)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$					
				}
				break;
			}
			case IPathEntry.CDT_LIBRARY: {
				ILibraryEntry library = (ILibraryEntry)entry;
				IPath path = library.getPath();
				if (!isValidWorkspacePath(project, path)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
				}
				if (checkSourceAttachment) {
					IPath sourceAttach = library.getSourceAttachmentPath();
					if (sourceAttach != null) {
						if(!sourceAttach.isAbsolute()) {
							if (!isValidWorkspacePath(project, sourceAttach)) {
								return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg);
							}
						} else if (!isValidExternalPath(sourceAttach)) {
							return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg);						
						}
					}
				}
				IPath libraryPath = library.getFullLibraryPath();
				if (!isValidExternalPath(libraryPath)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
				}
				break;
			}
			case IPathEntry.CDT_MACRO: {
				IMacroEntry macro = (IMacroEntry)entry;
				IPath path = macro.getPath();
				if (!isValidWorkspacePath(project, path)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
				}
				break;
			}
			case IPathEntry.CDT_OUTPUT: {
				IOutputEntry out = (IOutputEntry)entry;
				IPath path = out.getPath();
				if (!isValidWorkspacePath(project, path)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
				}
				break;
			}
			case IPathEntry.CDT_PROJECT: {
				IProjectEntry projEntry = (IProjectEntry)entry;
				IPath path = projEntry.getPath();
				if (path != null && path.isAbsolute() && !path.isEmpty()) {
					IProject reqProject = project.getWorkspace().getRoot().getProject(path.segment(0));
					if (!reqProject.exists() || !(CoreModel.hasCCNature(reqProject) || CoreModel.hasCCNature(reqProject))) {
						return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
					}
					if (!reqProject.isOpen()){
						return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
					}
				} else {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
				}				
				break;
			}
			case IPathEntry.CDT_SOURCE: {
				ISourceEntry source = (ISourceEntry)entry;
				IPath path = source.getPath();
				if (!isValidWorkspacePath(project, path)) {
					return new CModelStatus(ICModelStatusConstants.INVALID_PATHENTRY, entryMesg); //$NON-NLS-1$
				}
				break;
			}
			case IPathEntry.CDT_CONTAINER:
				if (recurseInContainers) {
					try {
						IPathEntryContainer cont = getPathEntryContainer((IContainerEntry)entry, cProject);
						IPathEntry[] contEntries = cont.getPathEntries();
						for (int i = 0; i < contEntries.length; i++) {
							ICModelStatus status = validatePathEntry(cProject, contEntries[i], checkSourceAttachment, false);
							if (!status.isOK()) {
								return status;
							}
						}
					} catch (CModelException e) {
						return new CModelStatus(e);
					}
				}
				break;
		}
		return CModelStatus.VERIFIED_OK;
	}

	private boolean isValidWorkspacePath(IProject project, IPath path) {
		if (path == null) {
			return false;
		}
		IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
		// We accept empy path as the project
		IResource res = null;
		if (path.isAbsolute()) {
			res = workspaceRoot.findMember(path);
		} else {
			res = project.findMember(path);
		}
		return (res != null && res.isAccessible());
	}

	private boolean isValidExternalPath(IPath path) {
		if (path != null) {
			File file = path.toFile();
			if (file != null) {
				return file.exists();
			}
		}
		return false;
	}

	/**
	 * Record a new marker denoting a classpath problem
	 */
	void createPathEntryProblemMarker(IProject project, ICModelStatus status) {
			
		IMarker marker = null;
		int severity;
		switch (status.getCode()) {	
			case  ICModelStatusConstants.INVALID_PATHENTRY :
				severity = IMarker.SEVERITY_WARNING;
				break;
	
			case  ICModelStatusConstants.INVALID_PATH:
				severity = IMarker.SEVERITY_WARNING;
				break;
	
			default:
				severity = IMarker.SEVERITY_ERROR;
				break;
		}
		
		try {
			marker = project.createMarker(ICModelMarker.PATHENTRY_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] { 
					IMarker.MESSAGE, 
					IMarker.SEVERITY, 
					IMarker.LOCATION, 
					ICModelMarker.PATHENTRY_FILE_FORMAT,
				},
				new Object[] {
					status.getString(),
					new Integer(severity), 
					"pathentry",//$NON-NLS-1$
					"false",//$NON-NLS-1$
				}
			);
		} catch (CoreException e) {
			// could not create marker: cannot do much
			e.printStackTrace();
		}
	}

	/**
	 * Remove all markers denoting classpath problems
	 */
	protected void flushPathEntryProblemMarkers(IProject project) {
		IWorkspace workspace = project.getWorkspace();

		try {
			if (project.isAccessible()) {
				IMarker[] markers = project.findMarkers(ICModelMarker.PATHENTRY_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				if (markers != null) {
					workspace.deleteMarkers(markers);
				}
			}
		} catch (CoreException e) {
			// could not flush markers: not much we can do
			e.printStackTrace();
		}
	}



}

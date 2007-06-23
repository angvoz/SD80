/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IArchiveContainer;
import org.eclipse.cdt.core.model.IBinaryContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.model.ILibraryReference;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/** 
 * Info for ICProject.
 */

class CProjectInfo extends OpenableInfo {

	BinaryContainer vBin;
	ArchiveContainer vLib;
	ILibraryReference[] libReferences;
	IIncludeReference[] incReferences;
	ISourceRoot[] sourceRoots;
	IOutputEntry[] outputEntries;

	Object[] nonCResources = null;

	/**
	 */
	public CProjectInfo(CElement element) {
		super(element);
		vBin = null;
		vLib = null;
	}

	synchronized public IBinaryContainer getBinaryContainer() {
		if (vBin == null) {
			vBin = new BinaryContainer((CProject)getElement());
		}
		return vBin;
	}

	synchronized public IArchiveContainer getArchiveContainer() {
		if (vLib == null) {
			vLib = new ArchiveContainer((CProject)getElement());
		}
		return vLib;
	}

	/**
	 * @return
	 */
	public Object[] getNonCResources(IResource res) {
		if (nonCResources != null)
			return nonCResources;

		List notChildren = new ArrayList();
		try {
			if (res instanceof IContainer) {
				ICProject cproject = getElement().getCProject();
				ISourceRoot[] sourceRoots = cproject.getSourceRoots();
				IResource[] resources = ((IContainer)res).members();
				
				for (int i = 0; i < resources.length; ++i) {
					IResource child = resources[i];
					
					// Check if under source root
					boolean found = false;
					for (int j = 0; j < sourceRoots.length; ++j)
						if (sourceRoots[j].isOnSourceEntry(child)) {
							found = true; 
							break;
						}
					
					if (found) {
						switch (child.getType()) {
						case IResource.FILE:
							// Must be a translation unit or binary
							if (CoreModel.isValidTranslationUnitName(cproject.getProject(), child.getName())
									|| CModelManager.getDefault().createBinaryFile((IFile)child) != null)
								continue;
							break;
						case IResource.FOLDER:
							// All folders are good
							continue;
						}
					} else if (cproject.isOnOutputEntry(child)) {
						switch (child.getType()) {
						case IResource.FILE:
							if (CModelManager.getDefault().createBinaryFile((IFile)child) != null)
								continue;
							break;
						case IResource.FOLDER:
							// All folders are good here too
							continue;
						}
					}

					// It's a non C resource
					notChildren.add(child);
				}
			}			
		} catch (CModelException e) {
			// this can't be good.
		} catch (CoreException e) {
			// this neither
		}
	
		setNonCResources(notChildren.toArray());	
		return nonCResources;
	}

	/**
	 * @param container
	 * @return
	 */
	public void setNonCResources(Object[] resources) {
		nonCResources = resources;
	}

	/*
	 * Reset the source roots and other caches
	 */
	public void resetCaches() {
		if (libReferences != null) {
			for (int i = 0; i < libReferences.length; i++) {
				try {
					((CElement)libReferences[i]).close();
				} catch (CModelException e) {
					//
				}
			}
		}
		if (incReferences != null) {
			for (int i = 0; i < incReferences.length; i++) {
				try {
					((CElement)incReferences[i]).close();
				} catch (CModelException e) {
					//
				}
			}
		}
		sourceRoots = null;
		outputEntries = null;
		setNonCResources(null);
	}

}

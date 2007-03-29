/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.util;


import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.ILocationProvider;


/**
 * An EditorInput for an external (non-workspace) file.
 */
public class ExternalEditorInput implements ITranslationUnitEditorInput {
           
	private IStorage externalFile;
	private IResource markerResource;
	private ITranslationUnit unit;
	private IPath location;

	/*
	*/
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ExternalEditorInput))
			return false;
		ExternalEditorInput other = (ExternalEditorInput)obj;
		return externalFile.equals(other.externalFile);
	}

	/*
	* @see IEditorInput#exists()
	*/
	public boolean exists() {
		// External file ca not be deleted
		return true;
	}

	/*
	* @see IAdaptable#getAdapter(Class)
	*/
	public Object getAdapter(Class adapter) {
		if (ILocationProvider.class.equals(adapter)) {
			// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=180003
			// see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=179982
			if (location != null) {
				return this;
			}
			return null;
		}
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/*
	* @see IEditorInput#getImageDescriptor()
	*/
	public ImageDescriptor getImageDescriptor() {
		IEditorRegistry registry= PlatformUI.getWorkbench().getEditorRegistry();
		return registry.getImageDescriptor(externalFile.getFullPath().getFileExtension());
	}

	/*
	* @see IEditorInput#getName()
	*/
	public String getName() {
		return externalFile.getName();
	}

	/*
	* @see IEditorInput#getPersistable()
	*/
	public IPersistableElement getPersistable() {
		return null;
	}

	/*
	* see IStorageEditorInput#getStorage()
	*/
	public IStorage getStorage() {
		return externalFile;
	}

	/*
	* @see IEditorInput#getToolTipText()
	*/
	public String getToolTipText() {
		return externalFile.getFullPath().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.editor.ITranslationUnitEditorInput#getTranslationUnit()
	 */
	public ITranslationUnit getTranslationUnit() {
		return unit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.ILocationProvider#getPath(java.lang.Object)
	 */
	public IPath getPath(Object element) {
		return location ;
	}

	public ExternalEditorInput(ITranslationUnit unit, IStorage exFile) {
		this(exFile, exFile.getFullPath());
		this.unit = unit;
		markerResource= unit.getCProject().getProject();
	}

	public ExternalEditorInput(IStorage exFile) {
		this(exFile, exFile.getFullPath());
	}

	public ExternalEditorInput(IStorage exFile, IPath location) {
		externalFile = exFile;
		this.location = location;
	}
	
	/**
	 * This constructor accepts the storage for the editor
	 * and a reference to a resource which holds the markers for the external file.
	 */
	public ExternalEditorInput(IStorage exFile, IResource markerResource)  {
		this(exFile, exFile.getFullPath());
		this.markerResource = markerResource ;
	}

	/**
	 * Return the resource where markers for this external editor input are stored
	 */
	public IResource getMarkerResource() {
		return markerResource;
	}
	
	

}

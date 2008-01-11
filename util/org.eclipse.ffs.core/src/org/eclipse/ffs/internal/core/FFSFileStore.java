/**********************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     Wind River Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.ffs.internal.core;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Doug Schaefer
 *
 */
public class FFSFileStore extends FileStore {

	private final IFileStore target;
	private final FFSFileStore parent;
	private final FFSProject project;

	public FFSFileStore(FFSProject project, FFSFileStore parent, IFileStore target) {
		this.project = project;
		this.parent = parent;
		this.target = target;
	}
	
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		// TODO exclusions, dude...
		String[] children = target.childNames(options, monitor);
		URI[] additionalChildren = project.getAdditionalChildren(this);
		if (additionalChildren.length > 0) {
			String[] moreChildren = new String[children.length + additionalChildren.length];
			System.arraycopy(children, 0, moreChildren, 0, children.length);
			for (int i = 0, j = children.length; i < additionalChildren.length; ++i) {
				moreChildren[j] = EFS.getStore(additionalChildren[i]).getName();
			}
			children = moreChildren;
		}
		return children;
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		return target.fetchInfo(options, monitor);
	}

	public IFileStore getChild(String name) {
		if (project.isChildExcluded(this, name))
			// TODO this should probably return something?
			return null;
		
		URI childURI = project.getChild(this, name);
		if (childURI != null)
			try {
				return new FFSFileStore(project, this, EFS.getStore(childURI));
			} catch (CoreException e) {
			}
		
		return new FFSFileStore(project, this, target.getChild(name));
	}

	public String getName() {
		return target.getName();
	}

	public IPath getPath() {
		if (parent == null)
			// The root does not have a path
			return null;
		if (parent.getParent() == null)
			// Parent is root, start at this node
			return new Path(getName());
		else
			// Normal path creation
			return parent.getPath().append(getName());
	}
	
	public IFileStore getParent() {
		return parent;
	}

	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		return target.mkdir(options, monitor);
	}

	@Override
	public void delete(int options, IProgressMonitor monitor)
			throws CoreException {
		// TODO Auto-generated method stub
		super.delete(options, monitor);
	}
	
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		return target.openInputStream(options, monitor);
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor)	throws CoreException {
		return target.openOutputStream(options, monitor);
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		target.putInfo(info, options, monitor);
	}

	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		return target.toLocalFile(options, monitor);
	}

	public URI toURI() {
		URI rootURI = project.getURI();
		
		// Build path
		IPath path = getPath();
		String pathstr = path == null ? null : getPath().toString();
		
		try {
			URI uri = new URI(FFSFileSystem.SCHEME, rootURI.getAuthority(), rootURI.getPath(), pathstr, rootURI.getScheme());
			return uri;
		} catch (URISyntaxException e) {
			return null;
		}
		
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IFileStore.class || adapter == FFSFileStore.class)
			return this;
		
		return null;
	}
	
	public void addChild(IFileStore child) {
		project.addChild(this, child);
	}
	
	public void excludeChild(String childName) {
		project.excludeChildren(this, Pattern.compile(childName));
	}
	
	public void excludeChildren(String pattern) {
		project.excludeChildren(this, Pattern.compile(pattern));
	}
	
}

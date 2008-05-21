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
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.internal.filesystem.Messages;
import org.eclipse.core.internal.filesystem.Policy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;

/**
 * @author Doug Schaefer
 *
 */
public class FFSFileStore extends FileStore {

	private final IFileStore target;
	private final FFSFileStore parent;
	private final FFSProject project;
	private String name;
	private boolean autoAddChildren = true;

	public boolean isAutoAddChildren() {
		return autoAddChildren;
	}

	public void setAutoAddChildren(boolean autoAddChildren) {
		this.autoAddChildren = autoAddChildren;
	}

	public FFSFileStore(FFSProject project, FFSFileStore parent, IFileStore target) {
		this.project = project;
		this.parent = parent;
		this.target = target;
		this.name = target.getName();
	}

	public FFSFileStore(FFSProject project, FFSFileStore parent, String name) {
		this.project = project;
		this.parent = parent;
		this.target = null;
		this.name = name;
	}

	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		String[] children = new String[0];
		if (target != null && autoAddChildren)
			children = target.childNames(options, monitor);
		URI[] additionalChildren = project.getAdditionalChildren(this);
		if (additionalChildren.length > 0) {
			String[] moreChildren = new String[children.length + additionalChildren.length];
			System.arraycopy(children, 0, moreChildren, 0, children.length);
			for (int i = 0, j = children.length; i < additionalChildren.length; ++i) {
				moreChildren[j++] = EFS.getStore(additionalChildren[i]).getName();
			}
			children = moreChildren;
		}
		return children;
	}

	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		if (target == null)
		{
			FileInfo info = new FileInfo();
			info.setDirectory(true);
			info.setName(name);
			info.setExists(true);
			return info;
		}
		else
			return target.fetchInfo(options, monitor);
	}

	public IFileStore getChild(String name) {
		if (project.isChildExcluded(this, name))
			// TODO this should probably return something?
			return null;
		
		URI childURI = project.getChild(this, name);
		
		if (childURI != null)
			try {
				String scheme = childURI.getScheme();
				if (scheme != null && scheme.equals(FFSFileSystem.SCHEME))
				{
					return new FFSFileStore(project, this, name);
				}
				else
					return new FFSFileStore(project, this, EFS.getStore(childURI));
			} catch (CoreException e) {
			}
			
		if (target == null)
			return new FFSFileStore(project, this, name);
		else
		{
			IFileStore childStore = target.getChild(name);
			return new FFSFileStore(project, this, childStore);
		}
	}

	public String getName() {
		return name;
	}

	public IPath getPath() {
		if (parent == null || parent.getParent() == null)
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
		if (target != null)
			return target.mkdir(options, monitor);
		else
			return this;
	}

	@Override
	public void delete(int options, IProgressMonitor monitor)
			throws CoreException {
		target.delete(options, monitor);
	}
	
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		if (target != null)
		return target.openInputStream(options, monitor);
		else
			return null;
	}

	public OutputStream openOutputStream(int options, IProgressMonitor monitor)	throws CoreException {
		if (target != null)
			return target.openOutputStream(options, monitor);
			else
				return super.openOutputStream(options, monitor);
	}

	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		if (target != null)
		target.putInfo(info, options, monitor);
	}

	public File toLocalFile(int options, IProgressMonitor monitor) throws CoreException {
		if (target != null)
		return target.toLocalFile(options, monitor);
		else
			return null;
	}

	public URI toURI() {
		
		if (target != null)
			return getTargetURI();
		
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

	public void move(IFileStore destination, int options, IProgressMonitor monitor) throws CoreException {
			FFSFileStore destParent = (FFSFileStore) ((FFSFileStore)destination).getParent();
			FFSFileStore srcParent = (FFSFileStore) ((FFSFileStore)this).getParent();
			if (destParent != null && srcParent != null)
			{
				destParent.addChild(this);
				srcParent.removeChild(this);
			}
		}

	@Override
	public void copy(IFileStore destination, int options,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		System.out.println("Copy: " + this + " dest: " + destination);

		super.copy(destination, options, monitor);
	}

	@Override
	protected void copyDirectory(IFileInfo sourceInfo, IFileStore destination,
			int options, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		super.copyDirectory(sourceInfo, destination, options, monitor);
	}

	@Override
	protected void copyFile(IFileInfo sourceInfo, IFileStore destination,
			int options, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		super.copyFile(sourceInfo, destination, options, monitor);
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IFileStore.class || adapter == FFSFileStore.class)
			return this;
		
		return null;
	}
	
	public URI getTargetURI()
	{
		if (target != null)
			return target.toURI();
		else
			return toURI();
	}
	
	public void addChild(FFSFileStore child) {
		project.addChild(this, child);
	}

	private void removeChild(FFSFileStore child) {
		project.removeChild(this, child.getName());
	}

	public void excludeChild(String childName) {
		project.excludeChildren(this, Pattern.compile(childName));
	}
	
	public void excludeChildren(String pattern) {
		project.excludeChildren(this, Pattern.compile(pattern));
	}
	
}

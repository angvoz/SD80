/**********************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.ffs.internal.core;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.IFileTree;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ffs.core.Activator;

/**
 * @author Doug Schaefer
 *
 * This is the flexible file system. It allows you to add and exclude
 * entries from from a given directory.
 * 
 * The URI's for this system are as follows:
 * 
 *    ecproj:///<ecproj file location>?<logical path>#<ecproj file schema>
 * 
 * For example:
 * 
 *    ecproj:///c:/Eclipse/workspace/s?.project
 * 
 */
public class FFSFileSystem extends FileSystem {

	private Map<URI, FFSEcprojFile> ecprojFiles = new HashMap<URI, FFSEcprojFile>();

	private synchronized FFSEcprojFile getEcprojFile(FFSFileSystem fileSystem, URI uri) throws CoreException {
		uri.normalize();
		FFSEcprojFile ecprojFile = ecprojFiles.get(uri);
		if (ecprojFile == null) {
			ecprojFile = new FFSEcprojFile(fileSystem, uri);
			ecprojFiles.put(uri, ecprojFile);
		}
		return ecprojFile;
	}
	
	public IFileStore getStore(URI uri) {
		try {
			String ecprojScheme = uri.getFragment();
			if (ecprojScheme == null)
				ecprojScheme = EFS.SCHEME_FILE;

			URI ecprojURI = new URI(ecprojScheme, uri.getAuthority(), uri.getPath(), null, null);
			FFSEcprojFile ecprojFile = getEcprojFile(this, ecprojURI);
			
			IFileStore root = ecprojFile.getRoot();
			String pathStr = uri.getQuery();
			if (pathStr == null)
				return root;
			IPath path = new Path(pathStr);
			if (path.segmentCount() == 0)
				return root;
			return root.getChild(path);
		} catch (URISyntaxException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "uri", e));
		} catch (CoreException e) {
			Activator.log(e);
		}

		return EFS.getNullFileSystem().getStore(uri);
	}

	public int attributes() {
		// TODO is this right?
		return EFS.getLocalFileSystem().attributes();
	}

	public boolean canDelete() {
		return EFS.getLocalFileSystem().canDelete();
	}

	public boolean canWrite() {
		return EFS.getLocalFileSystem().canWrite();
	}

	public IFileTree fetchFileTree(IFileStore root, IProgressMonitor monitor) {
		try {
			// TODO obviously
			return EFS.getNullFileSystem().fetchFileTree(root, monitor);
		} catch (CoreException e) {
			Activator.log(e);
			return null;
		}
	}

	public IFileStore fromLocalFile(File file) {
		return EFS.getLocalFileSystem().fromLocalFile(file);
	}

	public IFileStore getStore(IPath path) {
		return EFS.getLocalFileSystem().getStore(path);
	}

	public boolean isCaseSensitive() {
		return EFS.getLocalFileSystem().isCaseSensitive();
	}

}

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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * @author Doug Schaefer
 *
 * Contents of the ecproj file.
 */
public class FFSProject {

	private final URI uri;
	private final FFSFileStore root;
	private final Map<IPath, Map<String, URI>> childAdds = new HashMap<IPath, Map<String, URI>>();
	private final Map<IPath, List<Pattern>> childExcludes = new HashMap<IPath, List<Pattern>>();
	
	public FFSProject(URI uri) throws CoreException {
		this.uri = uri;
		this.root = new FFSFileStore(this, null, EFS.getStore(uri));
		
//		IFileStore test = EFS.getLocalFileSystem().getStore(new Path("C:\\Eclipse\\ffs\\testdir"));
//		addChild((FFSFileStore)getRoot(), test);
	}
	
	public FFSFileStore getRoot() {
		return root;
	}
	
	public URI getURI() {
		return uri;
	}
	
	public void addChild(FFSFileStore node, IFileStore child) {
		IPath path = node.getPath();
		Map<String, URI> adds = childAdds.get(path);
		if (adds == null) {
			adds = new HashMap<String, URI>();
			childAdds.put(path, adds);
		}
		adds.put(child.getName(), child.toURI());
	}
	
	public void excludeChildren(FFSFileStore node, Pattern pattern) {
		IPath path = node.getPath();
		List<Pattern> excludes = childExcludes.get(path);
		if (excludes == null) {
			excludes = new ArrayList<Pattern>();
			childExcludes.put(path, excludes);
		}
		excludes.add(pattern);
	}
	
	public URI getChild(FFSFileStore node, String childName) {
		Map<String, URI> adds = childAdds.get(node.getPath());
		if (adds == null)
			return null;
		else
			return adds.get(childName);
	}
	
	public URI[] getAdditionalChildren(FFSFileStore node) {
		Map<String, URI> adds = childAdds.get(node.getPath());
		if (adds == null)
			return new URI[0];
		else
			return adds.values().toArray(new URI[adds.size()]);
	}
	
	public boolean isChildExcluded(FFSFileStore node, IFileStore child) {
		return isChildExcluded(node, child.getName());
	}
	
	public boolean isChildExcluded(FFSFileStore node, String childName) {
		List<Pattern> excludes = childExcludes.get(node.getPath());
		if (excludes == null)
			return false;
		for (Iterator<Pattern> i = excludes.iterator(); i.hasNext();) {
			Pattern pattern = i.next();
			if (pattern.matcher(childName).matches())
				return true;
		}
		return false;
	}
}

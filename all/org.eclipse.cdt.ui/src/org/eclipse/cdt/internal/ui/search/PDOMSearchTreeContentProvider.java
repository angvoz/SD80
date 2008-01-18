/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Ed Swartz (Nokia)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMSearchTreeContentProvider implements ITreeContentProvider, IPDOMSearchContentProvider {

	private TreeViewer viewer;
	private PDOMSearchResult result;
	private Map tree = new HashMap();

	public Object[] getChildren(Object parentElement) {
		Set children = (Set)tree.get(parentElement);
		if (children == null)
			return new Object[0];
		return children.toArray();
	}

	public Object getParent(Object element) {
		Iterator p = tree.keySet().iterator();
		while (p.hasNext()) {
			Object parent = p.next();
			Set children = (Set)tree.get(parent);
			if (children.contains(element))
				return parent;
		}
		return null;
	}

 	public boolean hasChildren(Object element) {
 		return tree.get(element) != null;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
		result = (PDOMSearchResult)newInput;
		tree.clear();
		if (result != null) {
			Object[] elements = result.getElements();
			for (int i = 0; i < elements.length; ++i) {
				insertSearchElement((PDOMSearchElement)elements[i]);
			}
		}
	}

	private void insertChild(Object parent, Object child) {
		Set children = (Set)tree.get(parent);
		if (children == null) {
			children = new HashSet();
			tree.put(parent, children);
		}
		children.add(child);
	}
	
	private void insertSearchElement(PDOMSearchElement element) {
		IIndexFileLocation location= element.getLocation();
		IFile[] files;
		if(location.getFullPath()!=null) {
			files= new IFile[] {ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(location.getFullPath()))};
		} else {
			IPath path= IndexLocationFactory.getAbsolutePath(element.getLocation());
			files= ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
		}
		boolean handled= false;
		if (files.length > 0) {
			for (int j = 0; j < files.length; ++j) {
				ICElement celement = CoreModel.getDefault().create(files[j]);
				if (celement != null) {
					insertChild(celement, element);
					insertCElement(celement);
					handled= true;
				}
			}
		} 
		if (!handled) {
			// insert a folder and then the file under that
			IPath path = IndexLocationFactory.getAbsolutePath(location);
			if (path != null) {
				IPath directory = path.removeLastSegments(1);
				insertChild(location, element);
				insertChild(directory, location);
				insertChild(result, directory);
			} else {
				// URI not representable as a file
				insertChild(IPDOMSearchContentProvider.URI_CONTAINER, location.getURI());
				insertChild(result, IPDOMSearchContentProvider.URI_CONTAINER);
			}
		}
	}
	
	private void insertCElement(ICElement element) {
		if (element instanceof ICProject)
			insertChild(result, element);
		else {
			ICElement parent = element.getParent();
			if (parent instanceof ISourceRoot && parent.getUnderlyingResource() instanceof IProject)
				// Skip source roots that are projects
				parent = parent.getParent();
			insertChild(parent, element);
			insertCElement(parent);
		}
	}
	
	public void elementsChanged(Object[] elements) {
		if (elements != null) {
			for (int i = 0; i < elements.length; ++i) {
				PDOMSearchElement element = (PDOMSearchElement)elements[i];
				if (result.getMatchCount(element) > 0)
					insertSearchElement(element);
				else
					remove(element);
			}
		}
		if (!viewer.getTree().isDisposed()) {
			viewer.refresh();
		}
	}
	
	public void clear() {
		tree.clear();
		viewer.refresh();
	}
	
	protected void remove(Object element) {
		Object parent = getParent(element);
		if (parent == null)
			// reached the search result
			return;
		
		Set siblings = (Set)tree.get(parent);
		siblings.remove(element);
		
		if (siblings.isEmpty()) {
			// remove the parent
			remove(parent);
			tree.remove(parent);
		}
	}
	
}

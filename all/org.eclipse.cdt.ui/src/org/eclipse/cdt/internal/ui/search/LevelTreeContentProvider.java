/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Control;

public class LevelTreeContentProvider extends CSearchContentProvider implements ITreeContentProvider {
	private AbstractTreeViewer fTreeViewer;
	private Map fChildrenMap;
	private CElementContentProvider fContentProvider;

	private static int[][] C_ELEMENT_TYPES= {{ICElement.C_ENUMERATION},
			{ICElement.C_STRUCT, ICElement.C_CLASS},{ICElement.C_UNIT},{ICElement.C_PROJECT}};
	private static int[][] RESOURCE_TYPES= {
			{}, 
			{IResource.FILE},
			{IResource.FOLDER}, 
			{IResource.PROJECT}, 
			{IResource.ROOT}};
	private static final int MAX_LEVEL= C_ELEMENT_TYPES.length - 1;
	private int fCurrentLevel;

	public LevelTreeContentProvider(AbstractTreeViewer viewer, int level) {
		fTreeViewer= viewer;
		fCurrentLevel= level;
		fContentProvider= new CElementContentProvider();
	}

	public Object getParent(Object child) {
		
		BasicSearchMatch tempMatch = (BasicSearchMatch)child;
		child = CCorePlugin.getDefault().getCoreModel().create(tempMatch.getResource());
		Object possibleParent= internalGetParent(child);
		if (possibleParent instanceof ICElement) {
			ICElement javaElement= (ICElement) possibleParent;
			for (int j= fCurrentLevel; j < MAX_LEVEL + 1; j++) {
				for (int i= 0; i < C_ELEMENT_TYPES[j].length; i++) {
					if (javaElement.getElementType() == C_ELEMENT_TYPES[j][i]) {
						return null;
					}
				}
			}
		} else if (possibleParent instanceof IResource) {
			IResource resource= (IResource) possibleParent;
			for (int j= fCurrentLevel; j < MAX_LEVEL + 1; j++) {
				for (int i= 0; i < RESOURCE_TYPES[j].length; i++) {
					if (resource.getType() == RESOURCE_TYPES[j][i]) {
						return null;
					}
				}
			}
		}
		return possibleParent;
	}

	private Object internalGetParent(Object child) {
		return fContentProvider.getParent(child);
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	protected synchronized void initialize(CSearchResult result) {
		super.initialize(result);
		fChildrenMap= new HashMap();
		if (result != null) {
			Object[] elements= result.getElements();
			for (int i= 0; i < elements.length; i++) {
				insert(elements[i], false);
			}
		}
	}

	protected void insert(Object child, boolean refreshViewer) {
		Object parent= getParent(child);
		while (parent != null) {
			if (insertChild(parent, child)) {
				if (refreshViewer)
					fTreeViewer.add(parent, child);
			} else {
				if (refreshViewer)
					fTreeViewer.refresh(parent);
				return;
			}
			child= parent;
			parent= getParent(child);
		}
		if (insertChild(_result, child)) {
			if (refreshViewer)
				fTreeViewer.add(_result, child);
		}
	}

	/**
	 * returns true if the child already was a child of parent.
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	private boolean insertChild(Object parent, Object child) {
		Set children= (Set) fChildrenMap.get(parent);
		if (children == null) {
			children= new HashSet();
			fChildrenMap.put(parent, children);
		}
		return children.add(child);
	}

	protected void remove(Object element, boolean refreshViewer) {
		// precondition here:  _result.getMatchCount(child) <= 0
	
		if (hasChildren(element)) {
			if (refreshViewer)
				fTreeViewer.refresh(element);
		} else {
			if (_result.getMatchCount(element) == 0) {
				fChildrenMap.remove(element);
				Object parent= getParent(element);
				if (parent != null) {
					removeFromSiblings(element, parent);
					remove(parent, refreshViewer);
				} else {
					removeFromSiblings(element, _result);
					if (refreshViewer)
						fTreeViewer.refresh();
				}
			} else {
				if (refreshViewer) {
					fTreeViewer.refresh(element);
				}
			}
		}
	}

	private void removeFromSiblings(Object element, Object parent) {
		Set siblings= (Set) fChildrenMap.get(parent);
		if (siblings != null) {
			siblings.remove(element);
		}
	}

	public Object[] getChildren(Object parentElement) {
		Set children= (Set) fChildrenMap.get(parentElement);
		if (children == null)
			return EMPTY_ARR;
		return children.toArray();
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public synchronized void elementsChanged(Object[] updatedElements) {
		if (_result == null)
			return;
		for (int i= 0; i < updatedElements.length; i++) {
			if (_result.getMatchCount(updatedElements[i]) > 0)
				insert(updatedElements[i], true);
			else
				remove(updatedElements[i], true);
		}
	}

	public void clear() {
		initialize(_result);
		fTreeViewer.refresh();
	}

	public void setLevel(int level) {
		fCurrentLevel= level;
		Control control= fTreeViewer.getControl();
		if (control != null)
			control.setRedraw(false);
		initialize(_result);
		fTreeViewer.refresh();
		if (control != null)
			control.setRedraw(true);
	}
}

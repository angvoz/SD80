/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.navigator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.cview.CViewContentProvider;

/**
 * A content provider populating a Common Navigator view with CDT model content.
 */
public class CNavigatorContentProvider extends CViewContentProvider implements IPipelinedTreeContentProvider {

	/** The input object as supplied in the call to {@link #inputChanged()} */
	private Object fRealInput;
	private IPropertyChangeListener fPropertyChangeListener;

	/*
	 * @see org.eclipse.ui.navigator.ICommonContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	public void init(ICommonContentExtensionSite commonContentExtensionSite) {
		IMemento memento= commonContentExtensionSite.getMemento();
		restoreState(memento);

		fPropertyChangeListener= new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				boolean refreshViewer= false;
				String property= event.getProperty();
				Object newValue= event.getNewValue();
				
				if (property.equals(PreferenceConstants.PREF_SHOW_CU_CHILDREN)) {
					boolean showCUChildren= newValue instanceof Boolean ? ((Boolean)newValue).booleanValue() : false;
					setProvideMembers(showCUChildren);
					refreshViewer= true;
				} else if (property.equals(PreferenceConstants.CVIEW_GROUP_INCLUDES)) {
					boolean groupIncludes= newValue instanceof Boolean ? ((Boolean)newValue).booleanValue() : false;
					setIncludesGrouping(groupIncludes);
					refreshViewer= true;
				}

				if (refreshViewer && getViewer() != null) {
					getViewer().refresh();
				}
			}
		};
		CUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(fPropertyChangeListener);
		// TLETODO [CN] use extension state model for view options persistence
//		fStateModel.addPropertyChangeListener(listener);
	}

	/*
	 * @see org.eclipse.cdt.ui.CElementContentProvider#dispose()
	 */
	public void dispose() {
		CUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
		// TLETODO [CN] use extension state model for view options persistence
//		fStateModel.removePropertyChangeListener(fPropertyChangeListener);
		super.dispose();
	}

	/*
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	public void restoreState(IMemento memento) {
		IPreferenceStore store= PreferenceConstants.getPreferenceStore();
		boolean showCUChildren= store.getBoolean(PreferenceConstants.PREF_SHOW_CU_CHILDREN);
		boolean groupIncludes= store.getBoolean(PreferenceConstants.CVIEW_GROUP_INCLUDES);
		if (memento != null) {
			String mementoValue= memento.getString(PreferenceConstants.PREF_SHOW_CU_CHILDREN);
			if (mementoValue != null) {
				showCUChildren= Boolean.valueOf(mementoValue).booleanValue();
			}
			mementoValue= memento.getString(PreferenceConstants.CVIEW_GROUP_INCLUDES);
			if (mementoValue != null) {
				groupIncludes= Boolean.valueOf(mementoValue).booleanValue();
			}
		}
		setProvideMembers(showCUChildren);
		setIncludesGrouping(groupIncludes);
		setProvideWorkingCopy(true);
	}

	/*
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		if (memento != null) {
			memento.putString(PreferenceConstants.PREF_SHOW_CU_CHILDREN, String.valueOf(getProvideMembers()));
			memento.putString(PreferenceConstants.CVIEW_GROUP_INCLUDES, String.valueOf(areIncludesGroup()));
		}
	}

	/*
	 * @see org.eclipse.cdt.ui.CElementContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) { 
		fRealInput= newInput;
		super.inputChanged(viewer, oldInput, findInputElement(newInput));
	}
	
	private Object findInputElement(Object newInput) {
		if (newInput instanceof IWorkspaceRoot) {
			return CoreModel.create((IWorkspaceRoot) newInput);
		}
		return newInput;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		Object parent= super.getParent(element);
		if (parent instanceof ICModel) {
			return getViewerInput() != null ? fRealInput : parent;
		} else if (parent instanceof ICProject)
			return ((ICProject)parent).getProject();
		return parent;
	}

	/**
	 * Access the viewer input.
	 * @return the viewer input
	 */
	protected Object getViewerInput() {
		return fInput;
	}

	/**
	 * Access the viewer.
	 * @return the viewer
	 */
	protected Viewer getViewer() {
		return fViewer;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.BaseCElementContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		if (parent instanceof IWorkspaceRoot) {
//			return super.getElements(CoreModel.create((IWorkspaceRoot)parent));
			return ((IWorkspaceRoot)parent).getProjects();
		} else if (parent instanceof IProject) {
			return super.getChildren(CoreModel.getDefault().create((IProject)parent));
		} 
		return super.getElements(parent);
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.cview.CViewContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		Object children[];
		if (element instanceof IWorkspaceRoot) {
//			children =  super.getChildren(CoreModel.create((IWorkspaceRoot)element));
			return ((IWorkspaceRoot)element).getProjects();
		} else if (element instanceof IProject) {
			return super.getChildren(CoreModel.getDefault().create((IProject)element));
		} else {
			children = super.getChildren(element);
		}
		return children;
	}

	/*
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedChildren(java.lang.Object, java.util.Set)
	 */
	public void getPipelinedChildren(Object parent, Set currentChildren) { 
		customizeCElements(getChildren(parent), currentChildren);
	}

	/*
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedElements(java.lang.Object, java.util.Set)
	 */
	public void getPipelinedElements(Object input, Set currentElements) {
		// only replace plain resource elements with custom elements
		// and avoid duplicating elements already customized
		// by upstream content providers 
		customizeCElements(getElements(input), currentElements);
	}

	private void customizeCElements(Object[] cChildren, Set proposedChildren) {
		List elementList= Arrays.asList(cChildren);
		for (Iterator iter= proposedChildren.iterator(); iter.hasNext();) {
			Object element= iter.next();
			IResource resource= null;
			if (element instanceof IResource) {
				resource= (IResource)element;
			} else if (element instanceof IAdaptable) {
				resource= (IResource)((IAdaptable)element).getAdapter(IResource.class);
			}
			if (resource != null) {
				int i= elementList.indexOf(resource);
				if (i >= 0) {
					cChildren[i]= null;
				}
			}
		}
		for (int i= 0; i < cChildren.length; i++) {
			Object element= cChildren[i];
			if (element instanceof ICElement) {
				ICElement cElement= (ICElement)element;
				IResource resource= cElement.getResource();
				if (resource != null) {
					proposedChildren.remove(resource);
				}
				proposedChildren.add(element);
			} else if (element != null) {
				proposedChildren.add(element);
			}
		}
	}

	/*
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedParent(java.lang.Object, java.lang.Object)
	 */
	public Object getPipelinedParent(Object object, Object suggestedParent) {
		return getParent(object);
	}

	/*
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptAdd(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification addModification) {
		convertToCElements(addModification);
		return addModification;
	}

	/*
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRefresh(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization) {
		return convertToCElements(refreshSynchronization.getRefreshTargets());
	}

	/*
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRemove(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification removeModification) {
		convertToCElements(removeModification.getChildren());
		return removeModification;
	}

	/*
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptUpdate(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	public boolean interceptUpdate(PipelinedViewerUpdate updateSynchronization) {
		return convertToCElements(updateSynchronization.getRefreshTargets());
	}

	/**
	 * Converts the shape modification to use ICElements.
	 * 
	 * @param modification
	 *            the shape modification to convert
	 * @return <code>true</code> if the shape modification set was modified
	 */
	private boolean convertToCElements(
			PipelinedShapeModification modification) {
		Object parent= modification.getParent();
		// don't convert projects
		if (parent instanceof IContainer) {
			IContainer container= (IContainer)parent;
			IProject project= container.getProject();
			if (project != null && CoreModel.hasCNature(project)) {
				ICElement element= CoreModel.getDefault().create(container);
				if (element != null) {
					// don't convert the root  
					if( !(element instanceof ICModel) && !(element instanceof ICProject) ) {
						modification.setParent(element);
					}
					return convertToCElements(modification.getChildren());
					
				}
			}
		}
		return false;
	}

	/**
	 * Converts the given set to ICElements.
	 * 
	 * @param currentChildren
	 *            The set of current children that would be contributed or 
	 *            refreshed in the viewer.
	 * @return <code>true</code> if the input set was modified
	 */
	private boolean convertToCElements(Set currentChildren) {
		LinkedHashSet convertedChildren= new LinkedHashSet();
		ICElement newChild;
		for (Iterator iter= currentChildren.iterator(); iter.hasNext();) {
			Object child= iter.next();
			// do not convert IProject
			if (child instanceof IFile || child instanceof IFolder) {
				IResource resource= (IResource)child;
				if (resource.isAccessible() && CoreModel.hasCNature(resource.getProject())) {
					if ((newChild= CoreModel.getDefault().create(resource)) != null) {
						iter.remove();
						convertedChildren.add(newChild);
					}
				}
			}
		}
		if (!convertedChildren.isEmpty()) {
			currentChildren.addAll(convertedChildren);
			return true;
		}
		return false;
	}

	protected void postContainerRefresh(final IParent container, final ICProject cproject) {
		postRefreshable(new RefreshContainer(container, cproject.getProject()));
	}
	protected void postRefresh(final Object element) {
		if (element instanceof ICModel) {
			super.postRefresh(fRealInput);
		} else if (element instanceof ICProject) {
			super.postRefresh(((ICProject)element).getProject());
		} else {
			super.postRefresh(element);
		}
	}

	protected void postAdd(final Object parent, final Object element) {
		if (parent instanceof ICModel) {
			super.postAdd(fRealInput, element);
		} else if (parent instanceof ICProject) {
			super.postAdd(((ICProject)parent).getProject(), element);
		} else {
			super.postAdd(parent, element);
		}
	}

	protected void postRemove(final Object element) {
		postRefresh(internalGetParent(element));
	}

	protected void postProjectStateChanged(final Object element) {
		if (element instanceof ICModel) {
			super.postProjectStateChanged(fRealInput);
		} else if (element instanceof ICProject) {
			super.postProjectStateChanged(((ICProject)element).getProject());
		} else {
			super.postProjectStateChanged(element);
		}
	}
}

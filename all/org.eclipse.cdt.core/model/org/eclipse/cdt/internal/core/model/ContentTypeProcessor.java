/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cdt.internal.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOpenable;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.core.runtime.preferences.IScopeContext;
/**
 * ContentType processor
 */
public class ContentTypeProcessor {

	CModelManager fManager;
	CElementDelta fCurrentDelta;

	public void processContentTypeChanges(ContentTypeChangeEvent event) {
		ICElement root = CModelManager.getDefault().getCModel();
		fCurrentDelta = new CElementDelta(root);
		fManager = CModelManager.getDefault();
		IContentType contentType = event.getContentType();
		
		// only interested in our contentTypes
		if (isRegisteredContentTypeId(contentType.getId())) {
			// Go through the events and generate deltas
			ICProject[] cprojects = getAffectedProjects(event);
			for (int k = 0; k < cprojects.length; ++k) {
				ICProject cproject = cprojects[k];
				processContentType(cproject, contentType, event.getContext());
			}
		}
		if (fCurrentDelta.getAffectedChildren().length > 0) {
			fManager.fire(fCurrentDelta, ElementChangedEvent.POST_CHANGE);
		}
	}

	boolean isRegisteredContentTypeId(String id) {
		String[] ids = CoreModel.getRegistedContentTypeIds();
		for (int i = 0; i < ids.length; i++) {
			if (ids[i].equals(id)) {
				return true;
			}
		}
		return false;
	}

	void processContentType(ICElement celement, IContentType contentType, IScopeContext context) {
		if (celement instanceof IOpenable) {
			int type = celement.getElementType();
			// if the type is not a TranslationUnit
			switch (type) {
			case ICElement.C_PROJECT: {
				CElementInfo info = (CElementInfo)fManager.peekAtInfo(celement);
				if (info != null) {
					ICElement[] celements = info.getChildren();
					for (int i = 0; i < celements.length; ++i) {
						processContentType(celements[i], contentType, context);
					}
				}
				break;
			}
			case ICElement.C_CCONTAINER: {
				CElementInfo info = (CElementInfo)fManager.peekAtInfo(celement);
				if (info != null) {
					try {
						ICElement[] celements = info.getChildren();
						IResource resource = celement.getResource();
						IResource[] members = null;
						if (resource instanceof IContainer) {
							members = ((IContainer)resource).members();
						}
						if (members != null) {
							//IContentTypeMatcher matcher = resource.getProject().getContentTypeMatcher();
							for (int i = 0; i < members.length; ++i) {
								if (members[i] instanceof IFile) {
									IFile file = (IFile) members[i];
									//if (contentType.isAssociatedWith(file.getName(), context)) {
									IContentType cType = CCorePlugin.getContentType(file.getProject(), file.getName());
									if (cType != null && cType.equals(contentType)) {
//									if (CoreModel.isValidTranslationUnitName(file.getProject(), file.getName())) {
										ICElement newElement = CoreModel.getDefault().create(file);
										if (newElement != null) {
											elementAdded(newElement, celement);
										}
									}
								}
							}
						}
						for (int i = 0; i < celements.length; ++i) {
							processContentType(celements[i], contentType, context);
						}
					} catch (CoreException e) {
						//
					}
				}
			}
			break;
			case ICElement.C_UNIT: {
				String id =  ((ITranslationUnit)celement).getContentTypeId();
				if (contentType.getId().equals(id)) {
					try {
						if (! contentType.isAssociatedWith(celement.getElementName(), context)) {
							elementRemoved(celement, celement.getParent());
						}
					} catch (CoreException e) {
						//
					}
				}
				break;
			}
			}
		}
	}
	
	
	/**
	 * Add the resource delta to the right CElementDelta tree.
	 * @param parent
	 * @param delta
	 */
	private void nonCResourcesChanged(ICElement parent) {
		if (parent instanceof Openable && ((Openable)parent).isOpen()) {
			try {
				CElementInfo info = ((Openable)parent).getElementInfo();
				switch (parent.getElementType()) {
				case ICElement.C_MODEL:
					((CModelInfo)info).setNonCResources(null);
				return;
				case ICElement.C_PROJECT:
					((CProjectInfo)info).setNonCResources(null);
				break;
				case ICElement.C_CCONTAINER:
					((CContainerInfo)info).setNonCResources(null);
					if (parent instanceof ISourceRoot) {
						// if sourceRoot == Project we nee to update the nonCResource of the project also.
						if (parent.getResource() instanceof IProject) {
							ICElement cproject = parent.getCProject();
							CProjectInfo pinfo = (CProjectInfo)fManager.peekAtInfo(cproject);
							if (pinfo != null) {
								pinfo.setNonCResources(null);
							}
						}
					}
				break;
				}
			} catch (CModelException e) {
				//
			}
		}
	}

	private void elementAdded(ICElement celement, ICElement parent) throws CModelException {

		if (celement instanceof Openable) {
			addToParentInfo((Openable)celement);
		}
		fCurrentDelta.added(celement);
		nonCResourcesChanged(parent);
	}

	/**
	 * Adds the given child handle to its parent's cache of children. 
	 */
	private void addToParentInfo(Openable child) throws CModelException {
		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			CElementInfo info = parent.getElementInfo();
			// Check if the element exits
			if (!info.includesChild(child)) {
				info.addChild(child);
			}
		}
	}

	private void elementRemoved(ICElement celement, ICElement parent) throws CModelException {
		fCurrentDelta.removed(celement);
		nonCResourcesChanged(parent);
		removeFromParentInfo(celement);
		fManager.releaseCElement(celement);
	}

	/**
	 * Removes the given element from its parents cache of children. If the
	 * element does not have a parent, or the parent is not currently open,
	 * this has no effect. 
	 */
	private void removeFromParentInfo(ICElement child) throws CModelException {

		// Remove the child from the parent list.
		ICElement parent = child.getParent();
		if (parent != null && parent instanceof Parent && fManager.peekAtInfo(parent) != null) {
			((Parent)parent).removeChild(child);
		}
	}

	private ICProject[] getAffectedProjects(ContentTypeChangeEvent event) {
		try {
			ICModel cmodel = CoreModel.getDefault().getCModel();
			ICProject[] cprojects = cmodel.getCProjects();

			IScopeContext context = event.getContext();
			if (context != null) {
				if (ProjectScope.SCOPE.equals((context.getName()))) {
					IPath location = context.getLocation();
					for (int i = 0; i < cprojects.length; i++) {
						if (cprojects[i].getProject().getLocation().isPrefixOf(location)) {
							return new ICProject[] { cprojects[i] };
						}
					}
					return new ICProject[0];
				}
			}
			// Assume a workspace resolver
			List list = new ArrayList(cprojects.length);
			for (int i = 0; i < cprojects.length; ++i) {
				list.add(cprojects[i]);
			}
			return (ICProject[]) list.toArray(new ICProject[list.size()]);
		} catch (CModelException e) {
			//
		}
		return new ICProject[0];
	}

}

/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOM;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ElementChangedEvent;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastIndexer implements IPDOMIndexer {

	private PDOM pdom;

	public void setPDOM(IPDOM pdom) {
		if (pdom instanceof PDOM)
			this.pdom = (PDOM)pdom;
	}
	
	private IProgressMonitor progressGroup;
	
	private IProgressMonitor getProgressGroup() {
		if (progressGroup == null)
			progressGroup = Platform.getJobManager().createProgressGroup();
		return progressGroup;
	}
	
	private class ChangeHandler extends Job {
		private final ICElementDelta delta;
		private List addedTUs;
		private List changedTUs;
		private List removedTUs;
		
		public ChangeHandler(ElementChangedEvent event) {
			super("Change Handler");
			delta = event.getDelta();
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			long start = System.currentTimeMillis();

			String taskName = null;
			processDelta(delta);
			taskName = "Update PDOM";

//			if (project != null) {
//				processNewProject(project);
//				taskName = "Rebuild PDOM";
//			}

			int count
				= (addedTUs != null ? addedTUs.size() : 0)
				+ (changedTUs != null ? changedTUs.size() : 0)
				+ (removedTUs != null ? removedTUs.size() : 0);

			if (taskName == null || count == 0)
				return Status.OK_STATUS;

			monitor.beginTask(taskName, count);

			if (addedTUs != null)
				for (Iterator i = addedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					monitor.subTask(String.valueOf(count--)
							+" files remaining - "
							+ tu.getPath().toString());
					new PDOMFastAddTU(pdom, tu).schedule();
				}
			
			if (changedTUs != null)
				for (Iterator i = changedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					monitor.subTask(String.valueOf(count--)
							+" files remaining - "
							+ tu.getPath().toString());
					new PDOMFastChangeTU(pdom, tu).schedule();
					monitor.worked(1);
				}
			
			if (removedTUs != null)
				for (Iterator i = removedTUs.iterator(); i.hasNext();) {
					if (monitor.isCanceled())
						return Status.CANCEL_STATUS;
					ITranslationUnit tu = (ITranslationUnit)i.next();
					monitor.subTask(String.valueOf(count--)
							+" files remaining - "
							+ tu.getPath().toString());
					new PDOMFastRemoveTU(pdom, tu).schedule();
					monitor.worked(1);
				}

			String showTimings = Platform.getDebugOption(CCorePlugin.PLUGIN_ID + "/debug/pdomtimings"); //$NON-NLS-1$
			if (showTimings!= null)
				if (showTimings.equalsIgnoreCase("true")) //$NON-NLS-1$
					System.out.println("Updator Time: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
					
			return Status.OK_STATUS;
		}
		
		private void processDelta(ICElementDelta delta) {
			// First make sure this project is PDOMable
			ICElement element = delta.getElement();
			if (element instanceof ICProject && CCorePlugin.getPDOMManager().getPDOM(((ICProject)element).getProject()) == null)
				return;
			
			// process the children first
			ICElementDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; ++i)
				processDelta(children[i]);

			// what have we got
			if (element.getElementType() == ICElement.C_PROJECT) {
				switch (delta.getKind()) {
				case ICElementDelta.ADDED:
					processNewProject((ICProject)element);
					break;
				}
			} else if (element.getElementType() == ICElement.C_UNIT) {
				ITranslationUnit tu = (ITranslationUnit)element;
				if (tu.isWorkingCopy())
					// Don't care about working copies either
					return;
				
				switch (delta.getKind()) {
				case ICElementDelta.ADDED:
					if (addedTUs == null)
						addedTUs = new LinkedList();
					addedTUs.add(element);
					break;
				case ICElementDelta.CHANGED:
					if (changedTUs == null)
						changedTUs = new LinkedList();
					changedTUs.add(element);
					break;
				case ICElementDelta.REMOVED:
					if (removedTUs == null)
						removedTUs = new LinkedList();
					removedTUs.add(element);
					break;
				}
			}
		}

		private void processNewProject(final ICProject project) {
			try {
				project.getProject().accept(new IResourceProxyVisitor() {
					public boolean visit(IResourceProxy proxy) throws CoreException {
						if (proxy.getType() == IResource.FILE) {
							String fileName = proxy.getName();
							IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(fileName);
							if (contentType == null)
								return true;
							String contentTypeId = contentType.getId();
							
							if (CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(contentTypeId)
									|| CCorePlugin.CONTENT_TYPE_CSOURCE.equals(contentTypeId)) {
								if (addedTUs == null)
									addedTUs = new LinkedList();
								addedTUs.add(CoreModel.getDefault().create((IFile)proxy.requestResource()));
							}
							// TODO handle header files
							return false;
						} else {
							return true;
						}
					}
				}, 0);
			} catch (CoreException e) {
				CCorePlugin.log(e);
			}
		}
	}
	
	public void elementChanged(ElementChangedEvent event) {
		new ChangeHandler(event).schedule();
	}

	public void reindex() {
		
	}
	
}

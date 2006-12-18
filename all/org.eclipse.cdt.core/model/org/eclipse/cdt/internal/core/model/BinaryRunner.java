/*******************************************************************************
 * Copyright (c) 2000, 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.model;


import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser.IBinaryFile;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IOutputEntry;
import org.eclipse.cdt.core.model.IParent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.core.runtime.jobs.Job;

public class BinaryRunner {

	class BinaryRunnerOperation extends CModelOperation {

		BinaryRunnerOperation(ICProject cproj) {
			super(cproj);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.internal.core.model.CModelOperation#isReadOnly()
		 */
		public boolean isReadOnly() {
			return true;
		}

		protected void executeOperation() throws CModelException {
			ICProject cproj = (ICProject)getElementsToProcess()[0];
			IParent[] containers = new IParent[2];
			containers[0] = cproj.getBinaryContainer();
			containers[1] = cproj.getArchiveContainer();
			CModelManager factory = CModelManager.getDefault();
			ICElement root = factory.getCModel();
			CElementDelta cdelta = new CElementDelta(root);
			cdelta.changed(cproj, ICElementDelta.F_CONTENT);
			for (int j = 0; j < containers.length; ++j) {
				IParent container = containers[j];
				ICElement[] children = container.getChildren();
				if (children.length > 0) {
					cdelta.added((ICElement)container);
					for (int i = 0; i < children.length; i++) {
						cdelta.added(children[i]);
					}
				}
			}
			addDelta(cdelta);
		}
		
	}
	ICProject cproject;
	Job runner;

	public BinaryRunner(IProject prj) {
		cproject = CModelManager.getDefault().create(prj);
	}

	public void start() {
		String taskName = CCorePlugin.getResourceString("CoreModel.BinaryRunner.Binary_Search_Thread"); //$NON-NLS-1$
		taskName += " (" + cproject.getElementName() + ")";
		runner = new Job(taskName) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
			 */
			protected IStatus run(IProgressMonitor monitor) {
				IStatus status = Status.OK_STATUS;
				try {
					if (cproject == null || monitor.isCanceled()) {
						status = Status.CANCEL_STATUS;
					} else {
						monitor.beginTask(getName(), IProgressMonitor.UNKNOWN);

						BinaryContainer vbin = (BinaryContainer) cproject.getBinaryContainer();
						ArchiveContainer vlib = (ArchiveContainer) cproject.getArchiveContainer();

						vlib.removeChildren();
						vbin.removeChildren();

						cproject.getProject().accept(new Visitor(monitor), IContainer.INCLUDE_PHANTOMS);

						CModelOperation op = new BinaryRunnerOperation(cproject);
						op.runOperation(monitor);

					}
				} catch (CoreException e) {
					// Ignore the error and just cancel the binary thread
					//status = e.getStatus();
					status = Status.CANCEL_STATUS;
				} finally {
					monitor.done();
				}
				return status;
			}
		};
		runner.setPriority(Job.LONG);
		runner.schedule();
	}

	/**
	 * wrap the wait call and the interrupteException.
	 */
	public void waitIfRunning() {
		if (runner != null) {
			try {
				runner.join();
			} catch (InterruptedException e) {
			}
		}
	}

	public void stop() {
		if (runner != null && runner.getState() == Job.RUNNING) {
			runner.cancel();
		}
	}

	private class Visitor implements IResourceProxyVisitor {
		private IProgressMonitor vMonitor;
		private IProject project;
		private IOutputEntry[] entries = new IOutputEntry[0];
		private IContentType textContentType;

		public Visitor(IProgressMonitor monitor) {
			vMonitor = monitor;
			this.project = cproject.getProject();
			try {
				entries = cproject.getOutputEntries();
			} catch (CModelException e) {
			}
			IContentTypeManager mgr = Platform.getContentTypeManager();
			textContentType = mgr.getContentType("org.eclipse.core.runtime.text"); //$NON-NLS-1$
		}

		public boolean visit(IResourceProxy proxy) throws CoreException {
			if (vMonitor.isCanceled()) {
				return false;
			}
			vMonitor.worked(1);
			// give a hint to the user of what we are doing
			String name = proxy.getName();
			vMonitor.subTask(name);

			// Attempt to speed things up by rejecting up front
			// Things we know should not be Binary files.
			
			// check if it's a file resource
			// and bail out early
			if (proxy.getType() != IResource.FILE) {
				return true;
			}
			
			// check against known content types

			IContentType contentType = CCorePlugin.getContentType(project, name);
			if (contentType != null && textContentType != null) {
				if (contentType != null && contentType.isKindOf(textContentType)) {
					return true;
				} else if (textContentType.isAssociatedWith(name)) {
					return true;
				}
			}

			// we have a candidate
			IPath path = proxy.requestFullPath();
			if (path != null) {
				for (int i = 0; i < entries.length; ++i) {
					if (isOnOutputEntry(entries[i], path)) {
						IFile file = (IFile) proxy.requestResource();
						CModelManager factory = CModelManager.getDefault();
						IBinaryFile bin = factory.createBinaryFile(file);
						if (bin != null) {
							// Create the file will add it to the {Archive,Binary}Containery.
							factory.create(file, bin, cproject);
							return true;
						}
					}
				}
			}
			return true;
		}
		
		private boolean isOnOutputEntry(IOutputEntry entry, IPath path) {
			if (entry.getPath().isPrefixOf(path) && !CoreModelUtil.isExcluded(path, entry.fullExclusionPatternChars())) {
				return true;
			}
			return false;
		}
	}
}

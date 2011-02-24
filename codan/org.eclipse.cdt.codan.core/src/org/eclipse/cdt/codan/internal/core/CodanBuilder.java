/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.core;

import java.util.Map;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.Messages;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICodanBuilder;
import org.eclipse.cdt.codan.core.model.IRunnableInEditorChecker;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Implementation of {@link ICodanBuilder}
 */
public class CodanBuilder extends IncrementalProjectBuilder implements ICodanBuilder {
	/**
	 * codan builder id
	 */
	public static final String BUILDER_ID = "org.eclipse.cdt.codan.core.codanBuilder"; //$NON-NLS-1$

	private class CodanDeltaVisitor implements IResourceDeltaVisitor {
		private IProgressMonitor monitor;

		/**
		 * @param monitor
		 */
		public CodanDeltaVisitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					// handle added resource
					processResourceDelta(resource, monitor);
					break;
				case IResourceDelta.REMOVED:
					// handle removed resource
					break;
				case IResourceDelta.CHANGED:
					// handle changed resource
					processResourceDelta(resource, monitor);
					break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 * java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	public void processResource(IResource resource, IProgressMonitor monitor) {
		processResource(resource, monitor, null, CheckerLaunchMode.RUN_ON_FULL_BUILD);
	}

	public void processResourceDelta(IResource resource, IProgressMonitor monitor) {
		processResource(resource, monitor, null, CheckerLaunchMode.RUN_ON_INC_BUILD);
	}

	protected void processResource(IResource resource, IProgressMonitor monitor, Object model, CheckerLaunchMode checkerLaunchMode) {
		CheckersRegistry chegistry = CheckersRegistry.getInstance();
		int checkers = chegistry.getCheckersSize();
		int memsize = 0;
		if (resource instanceof IContainer) {
			try {
				IResource[] members = ((IContainer) resource).members();
				memsize = members.length;
			} catch (CoreException e) {
				CodanCorePlugin.log(e);
			}
		}
		int tick = 1000;
		// System.err.println("processing " + resource);
		monitor.beginTask(Messages.CodanBuilder_Code_Analysis_On + resource, checkers + memsize * tick);
		try {
			for (IChecker checker : chegistry) {
				try {
					if (monitor.isCanceled())
						return;
					if (checker.enabledInContext(resource) && chegistry.isCheckerEnabledForLaunchMode(checker, resource, checkerLaunchMode)) {
						synchronized (checker) {
							try {
								checker.before(resource);
								if (chegistry.isCheckerEnabled(checker, resource)) {
									//long time = System.currentTimeMillis();
									if (checkerLaunchMode == CheckerLaunchMode.RUN_AS_YOU_TYPE) {
										if (checker.runInEditor() && checker instanceof IRunnableInEditorChecker) {
											((IRunnableInEditorChecker) checker).processModel(model);
										}
									} else {
										checker.processResource(resource);
									}
									//	System.err
									//	.println("Checker "
									//	+ checker.getClass()
									//	+ " worked "
									//	+ (System
									//	.currentTimeMillis() - time));
								}
							} finally {
								checker.after(resource);
							}
						}
					}
					monitor.worked(1);
				} catch (Throwable e) {
					CodanCorePlugin.log(e);
				}
			}
			if (resource instanceof IContainer && (checkerLaunchMode == CheckerLaunchMode.RUN_ON_FULL_BUILD)) {
				try {
					IResource[] members = ((IContainer) resource).members();
					for (int i = 0; i < members.length; i++) {
						if (monitor.isCanceled())
							return;
						IResource member = members[i];
						processResource(member, new SubProgressMonitor(monitor, tick));
					}
				} catch (CoreException e) {
					CodanCorePlugin.log(e);
				}
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * @param checker
	 * @return
	 */
	private boolean isEnabledForLaunchMode(IChecker checker) {
		// TODO Auto-generated method stub
		return true;
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		processResource(getProject(), monitor);
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new CodanDeltaVisitor(monitor));
	}

	/**
	 * Run all checkers that support "check as you type" mode
	 * 
	 * @param model - model of given resource such as ast
	 * @param resource - resource to process
	 * @param monitor - progress monitor
	 */
	public void runInEditor(Object model, IResource resource, IProgressMonitor monitor) {
		if (model == null)
			return;
		processResource(resource, monitor, model, CheckerLaunchMode.RUN_AS_YOU_TYPE);
	}
}

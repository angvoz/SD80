/*******************************************************************************
 * Copyright (c) 2000, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.ui;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetListener;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeTargetEvent;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class MakeContentProvider implements ITreeContentProvider, IMakeTargetListener, IResourceChangeListener {
	protected boolean bFlatten;

	protected StructuredViewer viewer;

	/**
	 * Constructor for MakeContentProvider
	 */
	public MakeContentProvider() {
		this(false);
	}

	public MakeContentProvider(boolean flat) {
		bFlatten = flat;
	}

	public Object[] getChildren(Object obj) {
		if (obj instanceof IWorkspaceRoot) {
			try {
				return MakeCorePlugin.getDefault().getTargetManager().getTargetBuilderProjects();
			} catch (CoreException e) {
				// ignore
			}
		} else if (obj instanceof IContainer) {
			ArrayList<IAdaptable> children = new ArrayList<IAdaptable>();
			try {
				IResource[] resource = ((IContainer)obj).members();
				for (int i = 0; i < resource.length; i++) {
					if (resource[i] instanceof IContainer) {
						children.add(resource[i]);
					}
				}
				children.addAll(Arrays.asList(MakeCorePlugin.getDefault().getTargetManager().getTargets((IContainer)obj)));
			} catch (CoreException e) {
				// ignore
			}
			return children.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object obj) {
		if (obj instanceof IMakeTarget) {
			return ((IMakeTarget)obj).getContainer();
		} else if (obj instanceof IContainer) {
			return ((IContainer)obj).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object obj) {
		return getChildren(obj).length > 0;
	}

	public Object[] getElements(Object obj) {
		if (bFlatten) {
			List<Object> list = new ArrayList<Object>();
			Object[] children = getChildren(obj);
			for (int i = 0; i < children.length; i++) {
				list.add(children[i]);
				list.addAll(Arrays.asList(getElements(children[i])));
			}
			return list.toArray();
		}
		return getChildren(obj);
	}

	public void dispose() {
		if (viewer != null) {
			MakeCorePlugin.getDefault().getTargetManager().removeListener(this);
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (this.viewer == null) {
			MakeCorePlugin.getDefault().getTargetManager().addListener(this);
		}
		this.viewer = (StructuredViewer) viewer;
		IWorkspace oldWorkspace = null;
		IWorkspace newWorkspace = null;
		if (oldInput instanceof IWorkspace) {
			oldWorkspace = (IWorkspace) oldInput;
		} else if (oldInput instanceof IContainer) {
			oldWorkspace = ((IContainer) oldInput).getWorkspace();
		}
		if (newInput instanceof IWorkspace) {
			newWorkspace = (IWorkspace) newInput;
		} else if (newInput instanceof IContainer) {
			newWorkspace = ((IContainer) newInput).getWorkspace();
		}
		if (oldWorkspace != newWorkspace) {
			if (oldWorkspace != null) {
				oldWorkspace.removeResourceChangeListener(this);
			}
			if (newWorkspace != null) {
				newWorkspace.addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
			}
		}
	}

	public void targetChanged(final MakeTargetEvent event) {
		final Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed()) {
			switch (event.getType()) {
				case MakeTargetEvent.PROJECT_ADDED :
				case MakeTargetEvent.PROJECT_REMOVED :
					ctrl.getDisplay().asyncExec(new Runnable() {

						public void run() {
							if (ctrl != null && !ctrl.isDisposed()) {
								viewer.refresh();
							}
						}
					});
					break;
				case MakeTargetEvent.TARGET_ADD :
				case MakeTargetEvent.TARGET_CHANGED :
				case MakeTargetEvent.TARGET_REMOVED :
					ctrl.getDisplay().asyncExec(new Runnable() {

						public void run() {
							if (ctrl != null && !ctrl.isDisposed()) {
								if (bFlatten) {
									viewer.refresh();
								} else {
									//We can't just call refresh on the container target that
									//has been created since it may be that the container has
									//been filtered out and the fiters in the viewer don't know
									//any better how to call out to the filter selection again.
									//Instead we walk to the root container and refresh it.
									IContainer container = event.getTarget().getContainer();
									while(container.getParent() != null) {
										container = container.getParent();
									}
									viewer.refresh(container);
								}
							}
						}
					});
					break;
			}
		}
	}

	void processDelta(IResourceDelta delta) {
		// Bail out if the widget was disposed.
		Control ctrl = viewer.getControl();
		if (ctrl == null || ctrl.isDisposed() || delta == null) {
			return;
		}

		IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED);

		// Not interested in Content changes.
		for (int i = 0; i < affectedChildren.length; i++) {
			if ((affectedChildren[i].getFlags() & IResourceDelta.TYPE) != 0) {
				return;
			}
		}

		// Handle changed children recursively.
		for (int i = 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}

		// Get the affected resource
		final IResource resource = delta.getResource();

		// Handle removed children. Issue one update for all removals.
		affectedChildren = delta.getAffectedChildren(IResourceDelta.REMOVED);
		if (affectedChildren.length > 0) {
			final ArrayList<IResource> affected = new ArrayList<IResource>(affectedChildren.length);
			for (int i = 0; i < affectedChildren.length; i++) {
				if (affectedChildren[i].getResource().getType() == IResource.FOLDER) {
					affected.add(affectedChildren[i].getResource());
				}
			}
			if (!affected.isEmpty()) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed())
							return;
						if (viewer instanceof AbstractTreeViewer) {
							((AbstractTreeViewer) viewer).remove(affected.toArray());
						} else {
							viewer.refresh(resource);
						}
					}
				});
			}
		}

		// Handle added children. Issue one update for all insertions.
		affectedChildren = delta.getAffectedChildren(IResourceDelta.ADDED);
		if (affectedChildren.length > 0) {
			final ArrayList<IResource> affected = new ArrayList<IResource>(affectedChildren.length);
			for (int i = 0; i < affectedChildren.length; i++) {
				if (affectedChildren[i].getResource().getType() == IResource.FOLDER) {
					affected.add(affectedChildren[i].getResource());
				}
			}
			if (!affected.isEmpty()) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (viewer == null || viewer.getControl() == null || viewer.getControl().isDisposed())
							return;
						if (viewer instanceof AbstractTreeViewer) {
							((AbstractTreeViewer) viewer).add(resource, affected.toArray());
						} else {
							viewer.refresh(resource);
						}
					}
				});
			}
		}
	}

	public void resourceChanged(IResourceChangeEvent event) {
		final IResourceDelta delta = event.getDelta();
		Control ctrl = viewer.getControl();
		if (ctrl != null && !ctrl.isDisposed())
			processDelta(delta);
	}
}

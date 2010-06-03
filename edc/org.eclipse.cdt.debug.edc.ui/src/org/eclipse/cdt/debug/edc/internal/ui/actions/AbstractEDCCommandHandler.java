/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.actions;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

abstract public class AbstractEDCCommandHandler extends AbstractHandler implements IDebugContextListener {

	private final IDebugContextService contextService;
	private ISelection debugSelectionContext;
	private IExecutionDMContext selectionExecutionDMC;
	private IAlbum albumContext;
	private boolean snapshotSession;

	public AbstractEDCCommandHandler() {
		super();
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		contextService = DebugUITools.getDebugContextManager().getContextService(window);
		contextService.addPostDebugContextListener(this);
		setDebugSelectionContext(contextService.getActiveContext());
	}

	private IExecutionDMContext getContext(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (!ss.isEmpty()) {
				Object object = ss.getFirstElement();
				if (object instanceof IDMVMContext) {
					return DMContexts.getAncestorOfType(((IDMVMContext) object).getDMContext(),
							IExecutionDMContext.class);
				}
			}
		}

		return null;
	}

	public void debugContextChanged(DebugContextEvent event) {
		setDebugSelectionContext(event.getContext());
		setBaseEnabled(selectionExecutionDMC instanceof IEDCExecutionDMC);
	}

	public IExecutionDMContext getSelectionExecutionDMC() {
		return selectionExecutionDMC;
	}

	public void setDebugSelectionContext(ISelection debugSelectionContext) {
		this.debugSelectionContext = debugSelectionContext;
		this.selectionExecutionDMC = getContext(getDebugSelectionContext());
		if (selectionExecutionDMC != null) {
			this.albumContext = Album.getAlbumBySession(selectionExecutionDMC.getSessionId());
			if (albumContext == null)
				albumContext = Album.getRecordingForSession(selectionExecutionDMC.getSessionId());
			this.snapshotSession = Album.isSnapshotSession(selectionExecutionDMC.getSessionId());
		}
	}

	public IAlbum getAlbumContext() {
		return albumContext;
	}

	public boolean isSnapshotSession() {
		return snapshotSession;
	}

	public ISelection getDebugSelectionContext() {
		return debugSelectionContext;
	}

}

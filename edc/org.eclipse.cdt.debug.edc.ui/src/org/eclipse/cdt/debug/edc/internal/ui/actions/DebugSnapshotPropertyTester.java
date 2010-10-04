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
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;

public class DebugSnapshotPropertyTester extends PropertyTester {

	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		IDMVMContext testContext = null;
		
		boolean isEDCSession = false;
		boolean isSnapshotSession = false;
		boolean previousAvailable = false;

		if (receiver instanceof IWorkbenchPart) {
			Object selection = getContextSelectionForPart((IWorkbenchPart) receiver);
			if (selection instanceof IDMVMContext) {
				testContext = ((IDMVMContext) selection);
			}
			else if (selection instanceof EDCLaunch)
			{
				isEDCSession = true;
			}
		} else if (receiver instanceof IDMVMContext) {
			testContext = ((IDMVMContext) receiver);
		}

		if (testContext != null) {
			String sessionID = testContext.getDMContext().getSessionId();
			isSnapshotSession = Album.isSnapshotSession(sessionID);
			IDMContext context = testContext.getDMContext();
			if (context instanceof IEDCDMContext) {
				isEDCSession = true;
				IAlbum album = Album.getAlbumBySession(sessionID);
				IAlbum albumRecording = Album.getRecordingForSession(sessionID);
				previousAvailable = album != null || albumRecording != null;
			}
		}
		
		if (property.equals("isSnapshotCreationAvailable")) {
			return !isSnapshotSession;
		} else if (property.equals("isEDCSession")) {
			return isEDCSession;
		} else if (property.equals("isSnapshotSession")) {
			return isSnapshotSession;
		} else if (property.equals("isPreviousSnapshotAvailable")) {
			return previousAvailable;
		}

		return false;
	}

	private static Object getContextSelectionForPart(IWorkbenchPart part) {
		IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(
				part.getSite().getWorkbenchWindow());

		ISelection debugContext = contextService.getActiveContext(getPartId(part));
		if (debugContext == null) {
			debugContext = contextService.getActiveContext();
		}

		if (debugContext instanceof IStructuredSelection) {
			return ((IStructuredSelection) debugContext).getFirstElement();
		}

		return null;
	}

	private static String getPartId(IWorkbenchPart part) {
		if (part instanceof IViewPart) {
			IViewSite site = (IViewSite) part.getSite();
			return site.getId() + (site.getSecondaryId() != null ? (":" + site.getSecondaryId()) : ""); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return part.getSite().getId();
		}
	}

}

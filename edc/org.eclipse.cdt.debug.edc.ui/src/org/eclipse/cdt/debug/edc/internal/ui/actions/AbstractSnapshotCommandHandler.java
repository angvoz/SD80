/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.actions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

abstract public class AbstractSnapshotCommandHandler extends AbstractEDCCommandHandler {

	public AbstractSnapshotCommandHandler() {
		super();
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		super.debugContextChanged(event);
		if (isEnabled()) {
			if (isSnapshotSession())
				removePlatformDebugUI();
			else
				restorePlatformDebugUI();
		} else
			restorePlatformDebugUI();
	}

	@SuppressWarnings("unchecked")
	private void removePlatformDebugUI() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
				IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
				Set<String> enabledActivityIds = new HashSet<String>(activityManager.getEnabledActivityIds());
				if (enabledActivityIds.remove("org.eclipse.cdt.debug.edc.ui.platformDebugActivity"))
					workbenchActivitySupport.setEnabledActivityIds(enabledActivityIds);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void restorePlatformDebugUI() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
				IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
				Set<String> enabledActivityIds = new HashSet<String>(activityManager.getEnabledActivityIds());
				if (enabledActivityIds.add("org.eclipse.cdt.debug.edc.ui.platformDebugActivity"))
					workbenchActivitySupport.setEnabledActivityIds(enabledActivityIds);
			}
		});
	}

}

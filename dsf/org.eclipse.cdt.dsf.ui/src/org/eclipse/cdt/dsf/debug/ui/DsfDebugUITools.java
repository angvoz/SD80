/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.dsf.internal.ui.DsfUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.IActivityManager;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;

/**
 * @since 1.1
 */
public class DsfDebugUITools {
	
	public static IPreferenceStore getPreferenceStore()
	{
		return DsfUIPlugin.getDefault().getPreferenceStore();
	}

	@SuppressWarnings("unchecked")
	public static void enableActivity(String activityID, boolean enableit)
	{
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
		IActivityManager activityManager = workbenchActivitySupport.getActivityManager();
		Set<String> enabledActivityIds = new HashSet<String>(activityManager.getEnabledActivityIds());
		boolean changed = false;
		if (enableit)
			changed = enabledActivityIds.add(activityID);		
		else
			changed = enabledActivityIds.remove(activityID);		
		if (changed)
			workbenchActivitySupport.setEnabledActivityIds(enabledActivityIds);
	}

}

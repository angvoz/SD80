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
package org.eclipse.cdt.debug.edc.internal.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.internal.core.LaunchManager;

@SuppressWarnings("restriction")
public class Launcher {

	static private LaunchManager getLaunchManager() {
		return (LaunchManager) DebugPlugin.getDefault().getLaunchManager();
	}

	@SuppressWarnings("unchecked")
	static public Map<String, Object>[] getConfigurations() {
		ArrayList<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		ILaunchConfiguration[] launchConfigs = getLaunchManager().getLaunchConfigurations();
		for (ILaunchConfiguration launchConfig : launchConfigs) {
			result.add(new LaunchConfiguration(launchConfig).getProperties());
		}
		return result.toArray(new HashMap[result.size()]);
	}
}

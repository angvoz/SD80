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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
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
	
	@SuppressWarnings({ "unchecked" })
	static public Map<String, Object> createLaunchConfiguration(String launchType, String configName, Map<String, Object> properties) throws CoreException
	{
		ILaunchConfigurationWorkingCopy launchConfigResult = null;
		ILaunchConfiguration[] launchConfigs = getLaunchManager().getLaunchConfigurations();
		for (ILaunchConfiguration launchConfig : launchConfigs) {
			if (launchConfig.getName().equals(configName) && launchConfig.getType().getIdentifier().equals(launchType)) {
				launchConfigResult = launchConfig.getWorkingCopy();
			}
		}
		if (launchConfigResult == null)
		{
			ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType lcType = lm.getLaunchConfigurationType(launchType);
			launchConfigResult = lcType.newInstance(null, configName);
		}

		Map<String, Object> lcAttributes = launchConfigResult.getAttributes();
		lcAttributes.putAll(properties);
		launchConfigResult.setAttributes(lcAttributes);

		return new LaunchConfiguration(launchConfigResult.doSave()).getProperties();
	}
}

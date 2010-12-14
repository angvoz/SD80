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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.core.LaunchManager;

@SuppressWarnings("restriction")
public class LaunchConfiguration {

	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String IDENTIFIER = "identifier";

	private ILaunchConfiguration config;
	private final Map<String, Object> properties;

	public LaunchConfiguration() {
		properties = new HashMap<String, Object>();
	}

	public LaunchConfiguration(ILaunchConfiguration lc) {
		this.config = lc;
		properties = new HashMap<String, Object>();
		properties.put(NAME, getName());
		properties.put(TYPE, getType());
		properties.put(IDENTIFIER, getIdentifier());
	}

	static private LaunchManager getLaunchManager() {
		return (LaunchManager) DebugPlugin.getDefault().getLaunchManager();
	}

	public String getName() {
		return config.getName();
	}

	static public String getName(Map<String, Object> configData) {
		String name = (String) configData.get(NAME);
		return name;
	}

	public String getType() {
		try {
			return config.getType().getName();
		} catch (CoreException e) {
			return "";
		}
	}

	static public String getType(Map<String, Object> configData) {
		String type = (String) configData.get(TYPE);
		return type;
	}

	public String getIdentifier() {
		try {
			return config.getType().getIdentifier();
		} catch (CoreException e) {
			return "";
		}
	}

	static public String getIdentifier(Map<String, Object> configData) {
		String identifier = (String) configData.get(IDENTIFIER);
		return identifier;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	static public void launch(String configName) {
		ILaunchConfiguration[] launchConfigs = getLaunchManager().getLaunchConfigurations();
		for (ILaunchConfiguration launchConfig : launchConfigs) {
			if (launchConfig.getName().equals(configName)) {
				try {
					DebugEventListener.getListener(); // Make sure there is a listener ready
					launchConfig.launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor());
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}
}

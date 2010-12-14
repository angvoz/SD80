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
package org.eclipse.cdt.debug.edc.internal;

import org.eclipse.cdt.scripting.ScriptingPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

@SuppressWarnings("restriction")
public class EDCApplication implements IApplication {

	boolean running;
	
	public Object start(IApplicationContext context) throws Exception {

		System.out.println("This is the EDC Application");
		
		running = true;
	
	 	IEclipsePreferences prefs = ((IScopeContext) new InstanceScope()).getNode(ScriptingPlugin.PLUGIN_ID);
	 	prefs.putBoolean(ScriptingPlugin.SCRIPTING_ENABLED, true);

	 	prefs = ((IScopeContext) new InstanceScope()).getNode(DebugPlugin.getUniqueIdentifier());
	 	prefs.putBoolean(IInternalDebugCoreConstants.PREF_ENABLE_STATUS_HANDLERS, false);

	 	ScriptingPlugin.getBundleContext();

		while (running)
		{
			Thread.sleep(1000);
		}
		
		return null;
	}

	public void stop() {
		running = false;
	}

}

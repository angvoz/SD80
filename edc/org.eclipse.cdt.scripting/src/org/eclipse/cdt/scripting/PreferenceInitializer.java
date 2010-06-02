/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.scripting;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
	 	IEclipsePreferences defaultPreferences = ((IScopeContext) new DefaultScope()).getNode(ScriptingPlugin.PLUGIN_ID);
		defaultPreferences.putBoolean(ScriptingPlugin.SCRIPTING_ENABLED, false);
		defaultPreferences.putInt(ScriptingPlugin.PORT_NUMBER, 5660);
	}

}

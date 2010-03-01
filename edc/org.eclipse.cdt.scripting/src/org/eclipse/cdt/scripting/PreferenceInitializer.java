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

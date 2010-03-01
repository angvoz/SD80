/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.scripting;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class ScriptingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * Create the preference page.
	 */
	public ScriptingPreferencePage() {
		super(FLAT);
		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(),ScriptingPlugin.getDefault().getBundle().getSymbolicName());
		setPreferenceStore(store);
	}

	/**
	 * Create contents of the preference page.
	 */
	@Override
	protected void createFieldEditors() {
		// Create the field editors
		addField(new BooleanFieldEditor(ScriptingPlugin.SCRIPTING_ENABLED, "Enable Remote Scripting", getFieldEditorParent()));
		IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor(ScriptingPlugin.PORT_NUMBER, "Port Number:", getFieldEditorParent());
		integerFieldEditor.setValidRange(1, 500000);
		addField(integerFieldEditor);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), ScriptingPlugin.HELP_CONTEXT_ID);
	}

	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		// Initialize the preference page
	}

}

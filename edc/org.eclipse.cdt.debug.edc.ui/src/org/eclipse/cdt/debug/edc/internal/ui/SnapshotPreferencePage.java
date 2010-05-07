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
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class SnapshotPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * Create the preference page.
	 */
	public SnapshotPreferencePage() {
		super(FLAT);
		IPreferenceStore store= EDCDebugUI.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	/**
	 * Create contents of the preference page.
	 */
	@Override
	protected void createFieldEditors() {
		// Create the field editors
		addField(new ComboFieldEditor(Album.PREF_CREATION_CONTROL, "Create snapshots:", new String[][]{{"Manually", Album.CREATE_MANUAL}, {"When stopped", Album.CREATE_WHEN_STOPPED}, {"At breakpoints", Album.CREATE_AT_BEAKPOINTS}}, getFieldEditorParent()));
		{
			IntegerFieldEditor integerFieldEditor = new IntegerFieldEditor(Album.PREF_VARIABLE_CAPTURE_DEPTH, "Variable expansion level:", getFieldEditorParent());
			integerFieldEditor.setValidRange(1, 500);
			addField(integerFieldEditor);
		}
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), IEDCHelpContextIds.SNAPSHOT_PREFERENCE_PAGE);
	}

	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		// Initialize the preference page
	}

}

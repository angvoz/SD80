/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.ui;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for CppUnit settings. Supports to define the failure
 * stack filter patterns.
 */
public class CppUnitPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{
	private Button fShowOnErrorCheck;

	public CppUnitPreferencePage()
	{
		super();
		setPreferenceStore(CppUnitPlugin.getDefault().getPreferenceStore());
	}

	protected Control createContents(Composite parent)
	{
		Composite composite= new Composite(parent, SWT.NULL);
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		composite.setLayout(layout);
		GridData data= new GridData();
		data.verticalAlignment= GridData.FILL;
		data.horizontalAlignment= GridData.FILL;
		composite.setLayoutData(data);

		createShowCheck(composite);
//		createStackFilterPreferences(composite);

		return composite;
	}

	/**
	 * Create a group to contain the step filter related widgetry
	 */
//	private void createStackFilterPreferences(Composite composite)
//	{
//		Composite container= new Composite(composite, SWT.NONE);
//		GridLayout layout= new GridLayout();
//		layout.numColumns= 2;
//		layout.marginHeight= 0;
//		layout.marginWidth= 0;
//		container.setLayout(layout);
//		GridData gd= new GridData(GridData.FILL_BOTH);
//		container.setLayoutData(gd);
//
//		createShowCheck(container);
//	}

	private void createShowCheck(Composite composite)
	{
		GridData data;
		fShowOnErrorCheck= new Button(composite, SWT.CHECK);
		fShowOnErrorCheck.setText(CppUnitMessages.getString("CppUnitPreferencePage.showcheck.label")); //$NON-NLS-1$
		data= new GridData();
		data.horizontalAlignment= GridData.FILL;
		data.horizontalSpan= 2;
		fShowOnErrorCheck.setLayoutData(data);
		fShowOnErrorCheck.setSelection(getShowOnErrorOnly());
	}
	public void init(IWorkbench workbench)
	{
	}
	public boolean performOk()
	{
		IPreferenceStore store= getPreferenceStore();
		store.setValue(ICppUnitPreferencesConstants.SHOW_ON_ERROR_ONLY, fShowOnErrorCheck.getSelection());
		return true;
	}
	protected void performDefaults()
	{
		super.performDefaults();
	}
	public static void initializeDefaults(IPreferenceStore store)
	{
		store.setDefault(ICppUnitPreferencesConstants.SHOW_ON_ERROR_ONLY, false);
	}
	public static boolean getShowOnErrorOnly()
	{
		IPreferenceStore store= CppUnitPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(ICppUnitPreferencesConstants.SHOW_ON_ERROR_ONLY);
	}
}

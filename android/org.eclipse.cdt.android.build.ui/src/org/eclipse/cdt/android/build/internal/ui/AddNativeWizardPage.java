/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.android.build.internal.ui;

import java.util.Map;

import org.eclipse.cdt.android.build.core.NDKManager;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddNativeWizardPage extends WizardPage {
	
	private final String defaultLibraryName;
	private Text libraryNameText;

	public AddNativeWizardPage(Map<String, String> templateArgs) {
		super("addNativeWizardPage");
		defaultLibraryName = templateArgs.get(NDKManager.LIBRARY_NAME);
		setDescription("Settings for generated native components for project.");
		setTitle("Add Android Native Support");
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(2, false));
		
		Label lblLibraryName = new Label(container, SWT.NONE);
		lblLibraryName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLibraryName.setText("Library Name:");
		
		Composite composite = new Composite(container, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(3, false));
		
		Label lblLib = new Label(composite, SWT.NONE);
		lblLib.setText("lib");
		
		libraryNameText = new Text(composite, SWT.BORDER);
		libraryNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		libraryNameText.setText(defaultLibraryName);
		
		Label lblso = new Label(composite, SWT.NONE);
		lblso.setText(".so");
	}

	public void updateArgs(Map<String, String> templateArgs) {
		templateArgs.put(NDKManager.LIBRARY_NAME, libraryNameText.getText());
	}
}

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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddNativeWizardPage extends WizardPage {
	
	private final String defaultLibraryName;
	private final String currentNDKLocation;
	private Text libraryNameText;
	private Text ndkLocationText;

	public AddNativeWizardPage(Map<String, String> templateArgs) {
		super("addNativeWizardPage");
		setDescription("Settings for generated native components for project.");
		setTitle("Add Android Native Support");

		defaultLibraryName = templateArgs.get(NDKManager.LIBRARY_NAME);
		currentNDKLocation = NDKManager.getNDKLocation();
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		Group grpWorkspaceSettings = new Group(container, SWT.NONE);
		grpWorkspaceSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpWorkspaceSettings.setText("Workspace Settings");
		grpWorkspaceSettings.setLayout(new GridLayout(2, false));
		
		Label lblNdkLocation = new Label(grpWorkspaceSettings, SWT.NONE);
		lblNdkLocation.setText("NDK Location:");
		
		Composite composite_1 = new Composite(grpWorkspaceSettings, SWT.NONE);
		composite_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite_1.setLayout(new GridLayout(2, false));
		
		ndkLocationText = new Text(composite_1, SWT.BORDER);
		ndkLocationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		ndkLocationText.setText(currentNDKLocation);
		
		Button btnBrowse = new Button(composite_1, SWT.NONE);
		btnBrowse.setText("Browse...");
		btnBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String dir = new DirectoryDialog(getShell()).open();
				if (dir != null)
					ndkLocationText.setText(dir);
			}
		});
		
		Group grpProjectSettings = new Group(container, SWT.NONE);
		grpProjectSettings.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpProjectSettings.setText("Project Settings");
		grpProjectSettings.setLayout(new GridLayout(2, false));
		
		Label lblLibraryName = new Label(grpProjectSettings, SWT.NONE);
		lblLibraryName.setText("Library Name:");
		
		Composite composite = new Composite(grpProjectSettings, SWT.NONE);
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
		
		String newNDKLocation = ndkLocationText.getText();
		if (!newNDKLocation.equals(currentNDKLocation))
			NDKManager.setNDKLocation(newNDKLocation);
	}
}

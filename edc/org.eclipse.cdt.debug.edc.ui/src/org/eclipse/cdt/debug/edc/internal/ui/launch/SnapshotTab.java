/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.launch;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.launch.IEDCLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class SnapshotTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

	private Text albumText;
	private Button browseButton;

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(getControl(), "snapshot_launch_configuration_dialog_main_tab");

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		createVerticalSpacer(comp, 1);
		createAlbumGroup(comp, 1);
	}

	public String getName() {
		return "Main";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		updateAlbumFromConfig(configuration);
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE, albumText.getText());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

	}

	protected void createAlbumGroup(Composite parent, int colSpan) {
		Composite mainComp = new Composite(parent, SWT.NONE);
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 3;
		mainLayout.marginHeight = 0;
		mainLayout.marginWidth = 0;
		mainComp.setLayout(mainLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = colSpan;
		mainComp.setLayoutData(gd);
		Label albumLabel = new Label(mainComp, SWT.NONE);
		albumLabel.setText("Snapshot album:");
		gd = new GridData();
		gd.horizontalSpan = 3;
		albumLabel.setLayoutData(gd);
		albumText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		albumText.setLayoutData(gd);
		albumText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});

		browseButton = createPushButton(mainComp, "Browse...", null); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleBrowseButtonSelected();
				updateLaunchConfigurationDialog();
			}
		});
	}

	protected void handleBrowseButtonSelected() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(albumText.getText());
		String newAlbum = fileDialog.open();
		albumText.setText(newAlbum);
	}

	protected void updateAlbumFromConfig(ILaunchConfiguration config) {
		try {
			String albumName = config.getAttribute(IEDCLaunchConfigurationConstants.ATTR_ALBUM_FILE, "");
			albumText.setText(albumName);
		} catch (CoreException ce) {
			EDCDebugger.getMessageLogger().logError("Can't update launch configuration", ce);
		}
	}

}

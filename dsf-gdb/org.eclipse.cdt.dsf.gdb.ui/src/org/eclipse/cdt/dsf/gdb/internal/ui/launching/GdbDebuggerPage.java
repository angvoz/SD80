/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson             - Modified for DSF
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.launching;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.debug.ui.AbstractCDebuggerPage;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for gdb-based debugger implementations.
 */
public class GdbDebuggerPage extends AbstractCDebuggerPage implements Observer {

	protected TabFolder fTabFolder;
	protected Text fGDBCommandText;
	protected Text fGDBInitText;
	protected Button fNonStopCheckBox;
	protected Button fReverseCheckBox;

	private IMILaunchConfigurationComponent fSolibBlock;
	private boolean fIsInitializing = false;

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTabFolder = new TabFolder(comp, SWT.NONE);
		fTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
		createTabs(fTabFolder);
		fTabFolder.setSelection(0);
		setControl(parent);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, 
				                   IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, 
				                   IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
				                   IGDBLaunchConfigurationConstants.DEBUGGER_NON_STOP_DEFAULT);
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
                				   IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);

		if (fSolibBlock != null)
			fSolibBlock.setDefaults(configuration);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid = fGDBCommandText.getText().length() != 0;
		if (valid) {
			setErrorMessage(null);
			setMessage(null);
		}
		else {
			setErrorMessage(LaunchUIMessages.getString("GDBDebuggerPage.0")); //$NON-NLS-1$
			setMessage(null);
		}
		return valid;
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);
		String gdbCommand = IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT;
		String gdbInit = IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT;
		boolean nonStopMode = IGDBLaunchConfigurationConstants.DEBUGGER_NON_STOP_DEFAULT;
		boolean reverseEnabled = IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT;

		try {
			gdbCommand = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
					                                IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT);
		}
		catch(CoreException e) {
		}
		try {
			gdbInit = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, 
					                             IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT);
		}
		catch(CoreException e) {
		}

		try {
			nonStopMode = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
					                                 IGDBLaunchConfigurationConstants.DEBUGGER_NON_STOP_DEFAULT);
		}
		catch(CoreException e) {
		}
		
		try {
			reverseEnabled = configuration.getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
                    									IGDBLaunchConfigurationConstants.DEBUGGER_REVERSE_DEFAULT);
		}
		catch(CoreException e) {
		}

		if (fSolibBlock != null)
			fSolibBlock.initializeFrom(configuration);
		fGDBCommandText.setText(gdbCommand);
		fGDBInitText.setText(gdbInit);
		fNonStopCheckBox.setSelection(nonStopMode);
		fReverseCheckBox.setSelection(reverseEnabled);
		
		setInitializing(false); 
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, 
				                   fGDBCommandText.getText().trim());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT,
				                   fGDBInitText.getText().trim());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
				                   fNonStopCheckBox.getSelection());
		configuration.setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_REVERSE,
                                   fReverseCheckBox.getSelection());

		if (fSolibBlock != null)
			fSolibBlock.performApply(configuration);
	}

	public String getName() {
		return LaunchUIMessages.getString("GDBDebuggerPage.1"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getShell()
	 */
	@Override
	protected Shell getShell() {
		return super.getShell();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (!isInitializing())
			updateLaunchConfigurationDialog();
	}

	public IMILaunchConfigurationComponent createSolibBlock(Composite parent) {
		IMILaunchConfigurationComponent block = new GDBSolibBlock( new SolibSearchPathBlock(), true, true); 
		block.createControl(parent);
		return block;
	}

	public void createTabs(TabFolder tabFolder) {
		createMainTab(tabFolder);
		createSolibTab(tabFolder);
	}

	public void createMainTab(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(LaunchUIMessages.getString("GDBDebuggerPage.2")); //$NON-NLS-1$
		Composite comp = ControlFactory.createCompositeEx(tabFolder, 1, GridData.FILL_BOTH);
		((GridLayout)comp.getLayout()).makeColumnsEqualWidth = false;
		comp.setFont(tabFolder.getFont());
		tabItem.setControl(comp);
		Composite subComp = ControlFactory.createCompositeEx(comp, 3, GridData.FILL_HORIZONTAL);
		((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false;
		subComp.setFont(tabFolder.getFont());
		Label label = ControlFactory.createLabel(subComp, LaunchUIMessages.getString("GDBDebuggerPage.3")); //$NON-NLS-1$
		GridData gd = new GridData();
		//		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		fGDBCommandText = ControlFactory.createTextField(subComp, SWT.SINGLE | SWT.BORDER);
		fGDBCommandText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				if (!isInitializing())
					updateLaunchConfigurationDialog();
			}
		});
		Button button = createPushButton(subComp, LaunchUIMessages.getString("GDBDebuggerPage.4"), null); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleGDBButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleGDBButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(LaunchUIMessages.getString("GDBDebuggerPage.5")); //$NON-NLS-1$
				String gdbCommand = fGDBCommandText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(gdbCommand.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fGDBCommandText.setText(res);
			}
		});
		label = ControlFactory.createLabel(subComp, LaunchUIMessages.getString("GDBDebuggerPage.6")); //$NON-NLS-1$
		gd = new GridData();
		//		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		fGDBInitText = ControlFactory.createTextField(subComp, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fGDBInitText.setLayoutData(gd);
		fGDBInitText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				if (!isInitializing())
					updateLaunchConfigurationDialog();
			}
		});
		button = createPushButton(subComp, LaunchUIMessages.getString("GDBDebuggerPage.7"), null); //$NON-NLS-1$
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				handleGDBInitButtonSelected();
				updateLaunchConfigurationDialog();
			}

			private void handleGDBInitButtonSelected() {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setText(LaunchUIMessages.getString("GDBDebuggerPage.8")); //$NON-NLS-1$
				String gdbCommand = fGDBInitText.getText().trim();
				int lastSeparatorIndex = gdbCommand.lastIndexOf(File.separator);
				if (lastSeparatorIndex != -1) {
					dialog.setFilterPath(gdbCommand.substring(0, lastSeparatorIndex));
				}
				String res = dialog.open();
				if (res == null) {
					return;
				}
				fGDBInitText.setText(res);
			}
		});

		label = ControlFactory.createLabel(subComp, LaunchUIMessages.getString("GDBDebuggerPage.9"), //$NON-NLS-1$
				200, SWT.DEFAULT, SWT.WRAP);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.widthHint = 200;
		label.setLayoutData(gd);
		
		// TODO: Ideally, this field should be disabled if the back-end doesn't support non-stop debugging
		// TODO: Find a way to determine if non-stop is supported (i.e. find the GDB version) then grey out the check box if necessary 
		fNonStopCheckBox = ControlFactory.createCheckBox(subComp, LaunchUIMessages.getString("GDBDebuggerPage.13")); //$NON-NLS-1$
		fNonStopCheckBox.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateLaunchConfigurationDialog();
				}
			});

		// TODO: Ideally, this field should be disabled if the back-end doesn't support reverse debugging
		// TODO: Find a way to determine if reverse is supported (i.e. find the GDB version) then grey out the check box if necessary 
		fReverseCheckBox = ControlFactory.createCheckBox(subComp, LaunchUIMessages.getString("GDBDebuggerPage.14")); //$NON-NLS-1$
		fReverseCheckBox.addSelectionListener( new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateLaunchConfigurationDialog();
				}
			});

		
		// fit options one per line
		gd = new GridData();
		gd.horizontalSpan = 3;
		fNonStopCheckBox.setLayoutData(gd);
		gd = new GridData();
		gd.horizontalSpan = 3;
		fReverseCheckBox.setLayoutData(gd);
	}

	public void createSolibTab(TabFolder tabFolder) {
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText(LaunchUIMessages.getString("GDBDebuggerPage.10")); //$NON-NLS-1$
		Composite comp = ControlFactory.createCompositeEx(fTabFolder, 1, GridData.FILL_BOTH);
		comp.setFont(tabFolder.getFont());
		tabItem.setControl(comp);
		fSolibBlock = createSolibBlock(comp);
		if (fSolibBlock instanceof Observable)
			((Observable)fSolibBlock).addObserver(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		if (fSolibBlock != null) {
			if (fSolibBlock instanceof Observable)
				((Observable)fSolibBlock).deleteObserver(this);
			fSolibBlock.dispose();
		}
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// Override the default behavior
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	private void setInitializing(boolean isInitializing) {
		fIsInitializing = isInitializing;
	}
}

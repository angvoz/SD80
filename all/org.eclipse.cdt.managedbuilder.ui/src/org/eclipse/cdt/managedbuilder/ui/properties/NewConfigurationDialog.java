package org.eclipse.cdt.managedbuilder.ui.properties;

/**********************************************************************
 * Copyright (c) 2003,2004 IBM Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class NewConfigurationDialog extends Dialog {
	// String constants
	private static final String PREFIX = "NewConfiguration";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String ERROR = PREFIX + ".error";	//$NON-NLS-1$	
	private static final String NAME = LABEL + ".name";	//$NON-NLS-1$
	private static final String GROUP = LABEL + ".group";	//$NON-NLS-1$
	private static final String COPY = LABEL + ".copy";	//$NON-NLS-1$
	private static final String CLONE = LABEL + ".clone";	//$NON-NLS-1$
	private static final String TITLE = ERROR + ".title";	//$NON-NLS-1$	
	private static final String DUPLICATE = ERROR + ".duplicateName";	//$NON-NLS-1$	

	// Widgets
	private Button btnClone;
	private Button btnCopy;
	private Button btnOk;
	private Text configName;
	private Combo copyConfigSelector;
	private Combo cloneConfigSelector;
		
	// Bookeeping
	private boolean clone;
	private IConfiguration[] defaultConfigs;
	private IConfiguration[] definedConfigs;
	private IConfiguration parentConfig;
	private ITarget target;
	private String newName;
	private String title = ""; //$NON-NLS-1$

	
	/**
	 * @param parentShell
	 */
	protected NewConfigurationDialog(Shell parentShell, ITarget managedTarget, String title) {
		super(parentShell);
		this.title = title;
		setShellStyle(getShellStyle()|SWT.RESIZE);
		newName = new String();
		parentConfig = null;
		this.target = managedTarget;
		
		// The default behaviour is to clone the settings
		clone = true;
		
		// Populate the list of default and defined configurations
		definedConfigs = target.getConfigurations();
		ITarget grandparent = target.getParent();
		defaultConfigs = grandparent.getConfigurations();
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog. Cache the name and base config selections.
	 * We don't have to worry that the index or name is wrong because we 
	 * enable the OK button IFF those conditions are met.
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			newName = configName.getText().trim();
			String baseConfigName = new String();
			if (clone) {
				baseConfigName = cloneConfigSelector.getItem(cloneConfigSelector.getSelectionIndex());				
				for (int i = 0; i < definedConfigs.length; i++) {
					IConfiguration config = definedConfigs[i];
					if (config.getName().equals(baseConfigName)) {
						parentConfig = config;
						break;				
					}
				}
			} else {
				// Get the parent config out of the default config list
				baseConfigName = copyConfigSelector.getItem(copyConfigSelector.getSelectionIndex());
				for (int i = 0; i < defaultConfigs.length; i++) {
					IConfiguration config = defaultConfigs[i];
					if (config.getName().equals(baseConfigName)) {
						parentConfig = config;
						break;				
					}
				}
			}
		} else {
			newName = null;
			parentConfig = null;
		}
		super.buttonPressed(buttonId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		btnOk = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		configName.setFocus();
		if (configName != null) {
			configName.setText(newName);
		}
		updateButtonState();
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Add a label and a text widget
		final Label nameLabel = new Label(composite, SWT.LEFT);
		nameLabel.setFont(parent.getFont());
		nameLabel.setText(ManagedBuilderUIMessages.getResourceString(NAME));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		nameLabel.setLayoutData(gd);
		configName = new Text(composite, SWT.SINGLE | SWT.BORDER);
		configName.setFont(composite.getFont());
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		configName.setLayoutData(gd);
		configName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonState();
			}
		});
		
		// Create a group fro the radio buttons 
		final Group group = new Group(composite, SWT.NONE);
		group.setFont(composite.getFont());
		group.setText(ManagedBuilderUIMessages.getResourceString(GROUP));
		GridLayout layout = new GridLayout(3, false);
		group.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		group.setLayoutData(gd);

		SelectionListener radioListener =  new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {				
				clone = btnClone.getSelection();
				updateComboState();
			}
		};
		// Add a radio button and combo box to copy from default config
		btnCopy = new Button(group, SWT.RADIO);
		btnCopy.setFont(group.getFont());
		btnCopy.setText(ManagedBuilderUIMessages.getResourceString(COPY));
		setButtonLayoutData(btnCopy);
		btnCopy.addSelectionListener(radioListener);
		
		copyConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		copyConfigSelector.setFont(group.getFont());
		copyConfigSelector.setItems(getDefaultConfigNames());
		int index = copyConfigSelector.indexOf(newName);
		copyConfigSelector.select(index < 0 ? 0 : index);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		copyConfigSelector.setLayoutData(gd);
		copyConfigSelector.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateButtonState();		
			}
		});	
		copyConfigSelector.setEnabled(false);
		
		// Create a radio button and combo for clonable configs
		btnClone = new Button(group, SWT.RADIO);
		btnClone.setFont(group.getFont());
		btnClone.setText(ManagedBuilderUIMessages.getResourceString(CLONE));
		setButtonLayoutData(btnClone);
		btnClone.addSelectionListener(radioListener);
		btnClone.setSelection(true);
		
		cloneConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		cloneConfigSelector.setFont(group.getFont());
		cloneConfigSelector.setItems(getDefinedConfigNames());
		index = cloneConfigSelector.indexOf(newName);
		cloneConfigSelector.select(index < 0 ? 0 : index);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan = 2;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		cloneConfigSelector.setLayoutData(gd);
		cloneConfigSelector.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateButtonState();		
			}
		});	

		return composite;
	}


	/**
	 * @return the <code>IConfiguration</code> the user selected as 
	 * the parent of the new configuration.
	 */
	public IConfiguration getParentConfiguration() {
		return parentConfig;
	}

	/*
	 * Returns the array of configuration names defined for all targets  
	 * of this type in the plugin manifest. This list will be used to
	 * populate the the configurations to copy default settings from.
	 */
	private String [] getDefaultConfigNames() {
		String [] names = new String[defaultConfigs.length];
		for (int index = 0; index < defaultConfigs.length; ++index) {
			IConfiguration config = defaultConfigs[index];
			names[index] = config.getName();
		}
		return names; 
	}
	
	/*
	 * Returns the array of configuration names defined for this target.
	 * This list will be used to populate the list of configurations to 
	 * clone.
	 */
	private String [] getDefinedConfigNames() {
		String [] names = new String[definedConfigs.length];
		for (int index = 0; index < definedConfigs.length; ++index) {
			IConfiguration config = definedConfigs[index];
			names[index] = config.getName();
		}
		return names; 
	}

	/**
	 * @return <code>String</code> containing the name chosen by the user for the
	 * new configuration.
	 */
	public String getNewName() {
		return newName;
	}
	
	/* (non-Javadoc)
	 * Answers <code>true</code> if the name entered by the user clashes
	 * with an existing configuration name.
	 * 
	 * @param newName
	 * @return
	 */
	protected boolean isDuplicateName(String newName) {
		// Return true if there is already a config of that name defined on the target
		IConfiguration [] configs = target.getConfigurations();
		for (int index = 0; index < configs.length; index++) {
			IConfiguration configuration = configs[index];
			if (configuration.getName() == newName) {
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * Enable the OK button if there is a valid name in the text widget
	 * and there is a valid selection in the base configuration combo 
	 */
	private void updateButtonState() {
		if (btnOk != null) {
			int selectionIndex = copyConfigSelector.getSelectionIndex();			
			btnOk.setEnabled(validateName() && selectionIndex != -1);
		}
	}

	/* (non-Javadoc)
	 * Radio button selection event handler calls this helper method to 
	 * enable or disable the radio buttons.
	 */
	protected void updateComboState() {
		cloneConfigSelector.setEnabled(clone);
		copyConfigSelector.setEnabled(!clone);
	}

	private boolean validateName() {
		String currentName = configName.getText().trim(); 
		int nameLength = currentName.length();
		// Make sure the name is not a duplicate
		if (isDuplicateName(currentName)) {
			MessageDialog.openError(getShell(), 
					ManagedBuilderUIMessages.getResourceString(TITLE), 
					ManagedBuilderUIMessages.getFormattedString(DUPLICATE, currentName)); //$NON-NLS-1$
			return false;
		}
		// TODO make sure there are no invalid chars in name
		return (nameLength > 0);
	}
}

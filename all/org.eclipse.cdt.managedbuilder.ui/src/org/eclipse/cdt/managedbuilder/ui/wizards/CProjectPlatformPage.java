package org.eclipse.cdt.managedbuilder.ui.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderHelpContextIds;
import org.eclipse.cdt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.wizards.NewCProjectWizard;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.help.WorkbenchHelp;

public class CProjectPlatformPage extends WizardPage {
	/*
	 * Bookeeping variables
	 */
	private ArrayList selectedConfigurations;
	protected ITarget selectedTarget;
	protected String[] targetNames;
	protected ArrayList targets;
	protected NewManagedProjectWizard parentWizard;

	/*
	 * Dialog variables and string constants
	 */
	protected Combo platformSelection;
	protected CheckboxTableViewer tableViewer;
	protected Button showAll;
	private static final String PREFIX = "PlatformBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip"; //$NON-NLS-1$
	private static final String TARGET_TIP = TIP + ".platform"; //$NON-NLS-1$
	private static final String TARGET_LABEL = LABEL + ".platform"; //$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".configs"; //$NON-NLS-1$
	private static final String SHOWALL_LABEL = LABEL + ".showall"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param pageName
	 * @param wizard
	 */
	public CProjectPlatformPage(String pageName, NewManagedProjectWizard parentWizard) {
		super(pageName);
		setPageComplete(false);
		selectedTarget = null;
		selectedConfigurations = new ArrayList(0);
		this.parentWizard = parentWizard;
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return validatePage();
	}

	private void createConfigSelectionGroup (Composite parent) {
		// Create the group composite
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Create a check box table of valid configurations
		final Label configLabel = new Label(composite, SWT.LEFT);
		configLabel.setFont(composite.getFont());
		configLabel.setText(ManagedBuilderUIPlugin.getResourceString(CONFIG_LABEL));

		Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.MULTI
								| SWT.SINGLE | SWT.H_SCROLL	| SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		// Add a table layout to the table
		TableLayout tableLayout = new TableLayout();
		table.setHeaderVisible(false);
		table.setLayout(tableLayout);

		// Add the viewer
		tableViewer = new CheckboxTableViewer(table);
		tableViewer.setLabelProvider(new ConfigurationLabelProvider());
		tableViewer.setContentProvider(new ConfigurationContentProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				// will default to false until a selection is made
				handleConfigurationSelectionChange();
			}
		});

	}
	
	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Create the composite control for the tab
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Setup the help information
		WorkbenchHelp.setHelp(composite, ManagedBuilderHelpContextIds.MAN_PROJ_PLATFORM_HELP);

		// Create the widgets
		createTargetSelectGroup(composite);
		createConfigSelectionGroup(composite);
		createShowAllGroup(composite);

		// Select the first target in the list
		populateTargets();
		platformSelection.select(0);
		handleTargetSelection();
		
		// Do the nasty
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	private void createShowAllGroup(Composite parent) {
		// Create the group composite
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		showAll = new Button(composite, SWT.CHECK | SWT.LEFT);
		showAll.setFont(composite.getFont());
		showAll.setText(ManagedBuilderUIPlugin.getResourceString(SHOWALL_LABEL));
		showAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				populateTargets();
				platformSelection.select(0);
				handleTargetSelection();
			}
		});
		showAll.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				showAll = null;
			}
		});
	}
	
	private void createTargetSelectGroup(Composite parent) {
		// Create the group composite
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Create the platform selection label and combo widgets
		final Label platformLabel = new Label(composite, SWT.LEFT);
		platformLabel.setFont(composite.getFont());
		platformLabel.setText(ManagedBuilderUIPlugin.getResourceString(TARGET_LABEL));

		platformSelection = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);	
		platformSelection.setFont(composite.getFont());
		platformSelection.setToolTipText(ManagedBuilderUIPlugin.getResourceString(TARGET_TIP));
		platformSelection.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleTargetSelection();
			}
		});
		platformSelection.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				platformSelection = null;
			}
		});
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		// Make this the same as NewCProjectWizardPage.SIZING_TEXT_FIELD_WIDTH
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 50;
		platformSelection.setLayoutData(gd);
	}
	
	/**
	 * @return
	 */
	public IConfiguration[] getSelectedConfigurations() {
		return (IConfiguration[]) selectedConfigurations.toArray(new IConfiguration[selectedConfigurations.size()]);
	}

	/**
	 * Returns the name of the selected platform.
	 * 
	 * @return String containing platform name or <code>null</code> if an invalid selection
	 * has been made.
	 */
	public ITarget getSelectedTarget() {
		return selectedTarget;
	}

	private void handleConfigurationSelectionChange() {
		// Get the selections from the table viewer
		selectedConfigurations.clear();
		selectedConfigurations.addAll(Arrays.asList(tableViewer.getCheckedElements()));
	}

	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	protected void handleTargetSelection() {
		/*
		 * The index in the combo is the offset into the target list
		 */
		int index;
		if (platformSelection != null
			&& (index = platformSelection.getSelectionIndex()) != -1) {
			if (selectedTarget != (ITarget) targets.get(index)) {
				selectedTarget = (ITarget) targets.get(index);
				parentWizard.updateTargetProperties();
			}
		}
		populateConfigurations();
		setPageComplete(validatePage());
	}

	/**
	 * Populate the table viewer with the known configurations. 
	 * By default, all the configurations are selected.
	 */
	private void populateConfigurations() {
		// Make the root of the content provider the new target
		tableViewer.setInput(selectedTarget);
		tableViewer.setAllChecked(true);
		handleConfigurationSelectionChange();
	}

	/* (non-Javadoc)
	 * Extracts the names from the targets that are valid for the wizard
	 * session and populates the combo widget with them.
	 */
	private void populateTargetNames() {
		targetNames = new String[targets.size()];
		ListIterator iter = targets.listIterator();
		int index = 0;
		while (iter.hasNext()) {
			targetNames[index++] = ((ITarget) iter.next()).getName();
		}
		
		// Now setup the combo
		platformSelection.removeAll();
		platformSelection.setItems(targetNames);
	}

	/* (non-Javadoc)
	 * Collects all the valid targets for the platform Eclipse is running on
	 */
	private void populateTargets() {
		// Get a list of platforms defined by plugins
		ITarget[] allTargets = ManagedBuildManager.getDefinedTargets(null);
		targets = new ArrayList();
		String os = BootLoader.getOS();
		String arch = BootLoader.getOSArch();
		// Add all of the concrete targets to the target list
		for (int index = 0; index < allTargets.length; ++index) {
			ITarget target = allTargets[index];
			if (!target.isAbstract() && !target.isTestTarget()) {
				// If the check box is selected show all the targets
				if (showAll != null && showAll.getSelection() == true) {
					targets.add(target);
				} else {
					// Apply the OS and ARCH filters to determine if the target should be shown
					List targetOSList = Arrays.asList(target.getTargetOSList());
					if (targetOSList.contains("all") || targetOSList.contains(os)) {	//$NON-NLS-1$
						List targetArchList = Arrays.asList(target.getTargetArchList());
						if (targetArchList.contains("all") || targetArchList.contains(arch)) { //$NON-NLS-1$
							targets.add(target);
						}
					}
				}
			}
		}
		targets.trimToSize();
		populateTargetNames();
	}

	/**
	 * @return
	 */
	private boolean validatePage() {
		// TODO some validation ... maybe
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionContainer#getProject()
	 */
	public IProject getProject() {
		return ((NewCProjectWizard)getWizard()).getNewProject();
	}
}

/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.preferences;

import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.internal.ui.language.settings.providers.LanguageSettingsProviderTab.ProviderReference;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Options page for TODO
 *
 */
public final class GCCBuiltinSpecsDetectorOptionPage extends AbstractCOptionPage {
	private ProviderReference fProviderReference;
	private boolean fEditable;

	private Text inputCommand;
	
//	private StatusMessageLine fStatusLine;
	private Button runOnceRadioButton;
	private Button runEveryBuildRadioButton;

	@Override
	public void init(Object providerRef) {
		// must be ProviderReference
		fProviderReference = (ProviderReference) providerRef;
//		fEditable = isEditable;
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
//		Composite optionsPageComposite = new Composite(composite, SWT.NULL);
		fEditable = parent.isEnabled();

		final Composite composite = new Composite(parent, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 1;
			layout.marginHeight = 1;
			layout.marginRight = 1;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
			Dialog.applyDialogFont(composite);
			
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			composite.setLayoutData(gd);
		}


		Group groupRun = new Group(composite, SWT.SHADOW_ETCHED_IN);
//		groupRun.setText("Language Settings Provider Options");

		GridLayout gridLayoutRun = new GridLayout();
//		GridLayout gridLayoutRun = new GridLayout(2, true);
//		gridLayoutRun.makeColumnsEqualWidth = false;
//		gridLayoutRun.marginRight = -10;
//		gridLayoutRun.marginLeft = -4;
		groupRun.setLayout(gridLayoutRun);
//		GridData gdRun = new GridData(GridData.FILL_HORIZONTAL);
//		gdRun.horizontalSpan = 2;
//		groupRun.setLayoutData(gdRun);

		AbstractBuiltinSpecsDetector provider = (AbstractBuiltinSpecsDetector) fProviderReference.getProvider();
		{
			runOnceRadioButton = new Button(groupRun, SWT.RADIO);
			runOnceRadioButton.setText("Run only once"); //$NON-NLS-1$
			//		    b1.setToolTipText(UIMessages.getString("EnvironmentTab.3")); //$NON-NLS-1$
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			runOnceRadioButton.setLayoutData(gd);
			runOnceRadioButton.setSelection(provider.isRunOnce());
			runOnceRadioButton.setEnabled(fEditable);
			runOnceRadioButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					if (runOnceRadioButton.getSelection()) {
						AbstractBuiltinSpecsDetector selectedProvider = (AbstractBuiltinSpecsDetector) fProviderReference.getWorkingCopy();
						if (!LanguageSettingsManager.isWorkspaceProvider(selectedProvider)) {
							selectedProvider.setRunOnce(true);
						} else {
							// TODO: need working copy of the provider
						}
					}
				}
			});
		}
		{
			runEveryBuildRadioButton = new Button(groupRun, SWT.RADIO);
			runEveryBuildRadioButton.setText("Activate on every build"); //$NON-NLS-1$
			runEveryBuildRadioButton.setSelection(!provider.isRunOnce());
			runEveryBuildRadioButton.setEnabled(fEditable);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			runEveryBuildRadioButton.setLayoutData(gd);
			runEveryBuildRadioButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent evt) {
					if (runEveryBuildRadioButton.getSelection()) {
						AbstractBuiltinSpecsDetector selectedProvider = (AbstractBuiltinSpecsDetector) fProviderReference.getWorkingCopy();
						if (!LanguageSettingsManager.isWorkspaceProvider(selectedProvider)) {
							selectedProvider.setRunOnce(false);
						} else {
							// TODO: need working copy of the provider
						}
					}
				}
			});
		}

		// Compiler specs command
		{
			Label label = ControlFactory.createLabel(composite, "Command to get compiler specs:");
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			label.setLayoutData(gd);
			label.setEnabled(fEditable);
		}

		{
			inputCommand = ControlFactory.createTextField(composite, SWT.SINGLE | SWT.BORDER);
			String customParameter = provider.getCustomParameter();
			inputCommand.setText(customParameter!=null ? customParameter : "");
			inputCommand.setEnabled(fEditable);
			inputCommand.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					AbstractBuiltinSpecsDetector selectedProvider = (AbstractBuiltinSpecsDetector) fProviderReference.getWorkingCopy();
					if (!LanguageSettingsManager.isWorkspaceProvider(selectedProvider)) {
						selectedProvider.setCustomParameter(inputCommand.getText());
					} else {
						// TODO: need working copy of the provider
					}
				}
			});
		}

		{
			Button button = ControlFactory.createPushButton(composite, "Browse...");
			button.setEnabled(fEditable);
			button.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent evt) {
//					handleAddr2LineButtonSelected();
					//updateLaunchConfigurationDialog();
				}

			});

		}

//		{
//			final Button button = new Button(composite, SWT.PUSH);
//			button.setFont(parent.getFont());
//			String text = fProvider.isEmpty() ? "Run Now (TODO)" : "Clear";
//			button.setText(text);
////			button.addSelectionListener(this);
//			GridData data = new GridData();
//			data.horizontalSpan = 2;
////			data.horizontalAlignment = GridData.BEGINNING;
////			data.widthHint = 60;
//			button.setLayoutData(data);
//			// TODO
//			button.setEnabled(fEditable && !fProvider.isEmpty());
//
//			button.addSelectionListener(new SelectionAdapter() {
//
//				@Override
//				public void widgetSelected(SelectionEvent evt) {
//					if (fProvider.isEmpty()) {
//						// TODO
//					} else {
//						fProvider.clear();
//					}
//					// TODO
//					button.setEnabled(fEditable && !fProvider.isEmpty());
//					String text = fProvider.isEmpty() ? "Run Now (TODO)" : "Clear";
//					button.setText(text);
//					button.pack();
//				}
//
//			});
//
//		}

//		// Compiler specs command
//		{
//			Label label = ControlFactory.createLabel(composite, "Parsing rules:");
//			GridData gd = new GridData();
//			gd.horizontalSpan = 2;
//			label.setLayoutData(gd);
////		Label newLabel = new Label(composite, SWT.NONE);
//////		((GridData) newLabel.getLayoutData()).horizontalSpan = 1;
////		newLabel.setText("Command to get compiler specs:");
//		}


//		createPatternsTable(group, composite);








//		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
//		group.setText(DialogsMessages.RegexErrorParserOptionPage_Title);
//
//		GridLayout gridLayout = new GridLayout(2, true);
//		gridLayout.makeColumnsEqualWidth = false;
//		gridLayout.marginRight = -10;
//		gridLayout.marginLeft = -4;
//		group.setLayout(gridLayout);
//		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//		Composite composite = new Composite(group, SWT.NONE);
//		GridLayout layout = new GridLayout();
//		layout.numColumns = 2;
//		layout.marginWidth = 1;
//		layout.marginHeight = 1;
//		layout.marginRight = 1;
//		composite.setLayout(layout);
//		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
//		Dialog.applyDialogFont(composite);
//
//		if (!fEditable)
//			createLinkToPreferences(composite);
//
//		createPatternsTable(group, composite);
//
//		if (fEditable) {
//			createButtons(composite);
//		}

//		// Status line
//		if (fEditable) {
//			fStatusLine = new StatusMessageLine(composite, SWT.LEFT, 2);
//			IStatus status = new Status(IStatus.WARNING, CUIPlugin.PLUGIN_ID, "Note that currently the options are applied to provider directly (FIXME)");
//			fStatusLine.setErrorStatus(status);
//		}

		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
//		if (fProviderReference!=null) {
//			AbstractBuiltinSpecsDetector provider = (AbstractBuiltinSpecsDetector) fProviderReference.getWorkingCopy();
//			if (provider!=null) {
//				provider.setRunOnce(!runEveryBuildRadioButton.getSelection());
//
//				String command = inputCommand.getText();
//				provider.setCustomParameter(command);
//			}
//		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		// TODO X.performDefaults() will do all the work
	}
}

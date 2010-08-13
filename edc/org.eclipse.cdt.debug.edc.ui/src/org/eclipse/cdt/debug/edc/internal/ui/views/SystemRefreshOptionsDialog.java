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
package org.eclipse.cdt.debug.edc.internal.ui.views;

import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class SystemRefreshOptionsDialog extends Dialog {
	private Text intervalText;
	private Image refreshImage;
	private boolean autoRefresh;
	private int refreshInterval;
	private Button btnAutomaticallyRefresh;
	private String lastIntervalText;
	

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SystemRefreshOptionsDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.DIALOG_TRIM);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 2;
		
		btnAutomaticallyRefresh = new Button(container, SWT.CHECK);
		btnAutomaticallyRefresh.setText("Automatically Refresh");
		btnAutomaticallyRefresh.setSelection(isAutoRefresh());
		new Label(container, SWT.NONE);
		
		Label lblInterval = new Label(container, SWT.NONE);
		lblInterval.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInterval.setText("Interval (seconds):");
		
		intervalText = new Text(container, SWT.BORDER);
		intervalText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		intervalText.setText(Integer.toString(getRefreshInterval()));
		lastIntervalText = intervalText.getText();
		intervalText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				try {
					Integer.parseInt(intervalText.getText());
				} catch (NumberFormatException e2) {
					intervalText.setText(lastIntervalText);
				}
				lastIntervalText = intervalText.getText();
			}
		});

		getShell().setText("Refresh Options");
		refreshImage = AbstractUIPlugin
				.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
				"/icons/etool16/refresh.gif").createImage();
		getShell().setImage(refreshImage);

		parent.addDisposeListener(new DisposeListener() {
			
			public void widgetDisposed(DisposeEvent e) {
				if (refreshImage != null)
					refreshImage.dispose();
			}
		});

		return container;
	}

	@Override
	public boolean close() {
		autoRefresh = btnAutomaticallyRefresh.getSelection();
		try {
			refreshInterval = Integer.parseInt(intervalText.getText());
		} catch (NumberFormatException e) {
			EDCDebugUI.logError("Invalid interval value", e);
		}
		return super.close();
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(350, 200);
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}

	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
	}

	public int getRefreshInterval() {
		return refreshInterval;
	}

}

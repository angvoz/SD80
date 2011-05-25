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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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


public class SystemRefreshOptionsDialog extends StatusDialog {

	private static final int MIN_INTERVAL = 3; // 3 seconds
	private static final int MAX_INTERVAL = 1800; // 30 minutes
	
	private Label lblInterval;
	private Text intervalText;
	private Image refreshImage;
	private boolean autoRefresh;
	private int refreshInterval;
	private Button btnAutomaticallyRefresh;
	

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public SystemRefreshOptionsDialog(Shell parentShell) {
		super(parentShell);
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
		btnAutomaticallyRefresh.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				if (lblInterval != null) {
					lblInterval.setEnabled(btnAutomaticallyRefresh.getSelection());
				}

				if (intervalText != null) {
					intervalText.setEnabled(btnAutomaticallyRefresh.getSelection());
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		new Label(container, SWT.NONE);
		
		lblInterval = new Label(container, SWT.NONE);
		lblInterval.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInterval.setText("Interval (seconds):");
		lblInterval.setEnabled(btnAutomaticallyRefresh.getSelection());
		
		intervalText = new Text(container, SWT.BORDER);
		intervalText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		intervalText.setText(Integer.toString(getRefreshInterval()));
		intervalText.setEnabled(btnAutomaticallyRefresh.getSelection());
		intervalText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				IStatus status = Status.OK_STATUS;

				try {
					int value = Integer.parseInt(intervalText.getText());
					if (value < MIN_INTERVAL || value > MAX_INTERVAL) {
						throw new Exception();
					}
				} catch (Exception e2) {
					status = new Status(IStatus.ERROR, EDCDebugUI.PLUGIN_ID, "Value must be between " + MIN_INTERVAL + " and " + MAX_INTERVAL);
				}

				updateStatus(status);
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

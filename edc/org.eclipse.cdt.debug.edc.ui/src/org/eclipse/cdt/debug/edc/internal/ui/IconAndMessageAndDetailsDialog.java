/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 *  copied mostly verbatim from ErrorDialog, then changed out
 *  occurrences of {@link org.eclipse.swt.widgets.List} with
 *  {@link org.eclipse.swt.widgets.Text} so that one large details
 *  String could be passed and displayed
 *  <p><i>
 *  (ideally could have extended from ErrorDialog, except
 *  </i><code>ErrorDialog#showDetailsArea</code><i> was made final,
 *  and it calls</i> <code>private ErrorDialog#toggleDetailsArea</code>,
 *  which uses the</i> <code>SWT.List</code> <i>that the override
 *  would be replacing.)
 *  </i>
 *  
 * @see org.eclipse.jface.dialogs.ErrorDialog
 */
public class IconAndMessageAndDetailsDialog extends IconAndMessageDialog {

	/** Reserve room for this many text items in the details text-box. */
	private static final int TEXT_LINE_COUNT = 7;

	/** @see org.eclipse.jface.dialogs.ErrorDialog#detailsButton */
	private Button detailsButton;

	/** @see org.eclipse.jface.dialogs.ErrorDialog#title */
	private String title;

	/** The SWT text control that displays the error details. */
	private Text text;

	/** Indicates whether the error details viewer is currently created. */
	private boolean textCreated = false;

	/** The severity of the dialog. */
	private int severity;

	/** The details to display in the details text-box */
	private String details;

	/** @see org.eclipse.jface.dialogs.ErrorDialog#clipboard */
	private Clipboard clipboard;

	/**
	 * Creates a dialog with details. Note that the dialog will have no visual
	 * representation (no widgets) until it is told to open.
	 * 
	 * @param severity
	 *            the icon to show the user (and level of error in the final log)
	 * @param summary
	 *            a short bit of text to place on the dialog
	 * @param details
	 *            larger details to display if the user chooses (could be a full log)
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog
	 */
	public IconAndMessageAndDetailsDialog(int severity, String summary, String details) {
		super(CDebugUIPlugin.getActiveWorkbenchShell());
		this.title = "EDC Debug Monitor Message";
		this.message
		  = JFaceResources.format("Reason", new Object[] { "Raised by Debug Monitor", summary }); //$NON-NLS-1$
		this.details = details;
		this.severity = severity;
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#buttonPressed */
	protected void buttonPressed(int id) {
		if (id == IDialogConstants.DETAILS_ID) {
			// was the details button pressed?
			toggleDetailsArea();
		} else {
			super.buttonPressed(id);
		}
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#configureShell */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#createButtonsForButtonBar */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Details buttons
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createDetailsButton(parent);
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#createDetailsButton */
	protected void createDetailsButton(Composite parent) {
		if (shouldShowDetailsButton()) {
			detailsButton = createButton(parent, IDialogConstants.DETAILS_ID,
					IDialogConstants.SHOW_DETAILS_LABEL, false);
		}
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#createDialogArea */
	protected Control createDialogArea(Composite parent) {
		// Create a composite with standard margins and spacing
		// Add the messageArea to this composite so that as subclasses add widgets to the messageArea
		// and dialogArea, the number of children of parent remains fixed and with consistent layout.
		// Fixes bug #240135
		Composite composite = new Composite(parent, SWT.NONE);
		createMessageArea(composite);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData childData = new GridData(GridData.FILL_BOTH);
		childData.horizontalSpan = 2;
		childData.grabExcessVerticalSpace = false;
		composite.setLayoutData(childData);
		composite.setFont(parent.getFont());

		return composite;
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#createDialogAndButtonArea */
	protected void createDialogAndButtonArea(Composite parent) {
		super.createDialogAndButtonArea(parent);
		if (this.dialogArea instanceof Composite) {
			// Create a label if there are no children to force a smaller layout
			Composite dialogComposite = (Composite) dialogArea;
			if (dialogComposite.getChildren().length == 0) {
				new Label(dialogComposite, SWT.NULL);
			}
		}
	}

	protected Image getImage() {
		switch (severity)
		{
			case IStatus.WARNING:		return getWarningImage();
			case IStatus.INFO:			return getInfoImage();
		}

		// If it was not info or warning then return the error image
		return getErrorImage();
	}

	/**
	 * Creates a the drop-down area for when the user hits the 
	 * [Details>>] button. This is intended to act the same as
	 * the details button in ErrorDialog, except using 
	 * {@link org.eclipse.swt.widgets.Text} instead of
	 * {@link org.eclipse.swt.widgets.List}
	 * 
	 * @param parent the parent composite
	 * @return the drop-down text component
	 * @see org.eclipse.jface.dialogs.ErrorDialog#createDropDownList
	 */
	protected Text createDropDownText(Composite parent) {
		// create the text
		text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
								| SWT.MULTI | SWT.WRAP);
		// fill the text
		text.setText(details);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL
				| GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL
				| GridData.GRAB_VERTICAL);
		data.heightHint = text.getLineHeight() * TEXT_LINE_COUNT;
		data.horizontalSpan = 2;
		text.setLayoutData(data);
		text.setFont(parent.getFont());
		Menu copyMenu = new Menu(text);
		MenuItem copyItem = new MenuItem(copyMenu, SWT.NONE);
		copyItem.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				copyToClipboard();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				copyToClipboard();
			}
		});
		copyItem.setText(JFaceResources.getString("copy")); //$NON-NLS-1$
		text.setMenu(copyMenu);
		textCreated = true;
		return text;
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#toggleDetailsArea */
	private void toggleDetailsArea() {
		Point windowSize = getShell().getSize();
		Point oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (textCreated) {
			text.dispose();
			textCreated = false;
			detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
		} else {
			text = createDropDownText((Composite) getContents());
			detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
			getContents().getShell().layout();
		}
		Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		getShell()
				.setSize(
						new Point(windowSize.x, windowSize.y
								+ (newSize.y - oldSize.y)));
	}

	/** Copy the contents of the drop-down details to the clipboard. */
	private void copyToClipboard() {
		if (clipboard != null) {
			clipboard.dispose();
		}
		clipboard = new Clipboard(text.getDisplay());
		clipboard.setContents(new Object[] { details + "\n" },//$NON-NLS-1$
				new Transfer[] { TextTransfer.getInstance() });
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#close */
	public boolean close() {
		if (clipboard != null) {
			clipboard.dispose();
		}
		return super.close();
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#showDetailsArea */
	protected final void showDetailsArea() {
		if (!textCreated) {
			Control control = getContents();
			if (control != null && !control.isDisposed()) {
				toggleDetailsArea();
			}
		}
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#shouldShowDetailsButton */
	protected boolean shouldShowDetailsButton() {
		return (details != null) && (0 < details.length());
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#setStatus */
	protected final void setStatus(IStatus status) {
		if (textCreated) {
			if (text != null && !text.isDisposed()) {
				text.setText(status.getMessage());
			}
		}
	}

	/** @see org.eclipse.jface.dialogs.ErrorDialog#isResizable */
    protected boolean isResizable() {
    	return true;
    }

}

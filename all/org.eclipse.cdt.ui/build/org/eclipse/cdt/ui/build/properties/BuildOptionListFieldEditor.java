package org.eclipse.cdt.ui.build.properties;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class BuildOptionListFieldEditor extends FieldEditor {
	// Label constants
	private static final String TITLE = "BuildPropertyCommon.label.title";	//$NON-NLS-1$
	private static final String NEW = "BuildPropertyCommon.label.new"; //$NON-NLS-1$
	private static final String REMOVE = "BuildPropertyCommon.label.remove"; //$NON-NLS-1$
	private static final String UP = "BuildPropertyCommon.label.up"; //$NON-NLS-1$
	private static final String DOWN = "BuildPropertyCommon.label.down"; //$NON-NLS-1$

	// UI constants
	private static final int VERTICAL_DIALOG_UNITS_PER_CHAR = 8;
	private static final int HORIZONTAL_DIALOG_UNITS_PER_CHAR = 4;
	private static final int LIST_HEIGHT_IN_CHARS = 10;
	private static final int LIST_HEIGHT_IN_DLUS = 
		LIST_HEIGHT_IN_CHARS * VERTICAL_DIALOG_UNITS_PER_CHAR;

	// The top-level control for the field editor.
	private Composite top;
	// The list of tags.
	private List list;

	// The group control for the list and button composite
	private Group controlGroup;
	
	private String fieldName;
	private SelectionListener selectionListener;

	// The button for adding the contents of the text field to the list
	private Button addButton;
	// The button for removing the currently-selected list item.
	private Button removeButton;
	// The button for swapping the currently selected item up
	private Button upButton;
	// The button for swapping the currently-selected list item down
	private Button downButton;

	/**
	* @param name the name of the preference this field editor works on
	* @param labelText the label text of the field editor
	* @param parent the parent of the field editor's control
	*/
	public BuildOptionListFieldEditor (String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		this.fieldName = labelText;
	}

	/* (non-Javadoc)
	 * Event handler for the addButton widget
	 */
	protected void addPressed() {
		setPresentsDefaultValue(false);
		// Prompt user for a new item
		String input = getNewInputObject();
		
		// Add it to the list
		if (input != null) {
			int index = list.getSelectionIndex();
			if (index >= 0)
				list.add(input, index + 1);
			else
				list.add(input, 0);
			selectionChanged();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		((GridData)top.getLayoutData()).horizontalSpan = numColumns;
	}

	/* (non-Javadoc)
	 * Creates the Add, Remove, Up, and Down button in the button composite.
	 *
	 * @param container the box for the buttons
	 */
	private void createButtons(Composite container) {
		addButton = createPushButton(container, CUIPlugin.getResourceString(NEW));
		removeButton = createPushButton(container, CUIPlugin.getResourceString(REMOVE));
		upButton = createPushButton(container, CUIPlugin.getResourceString(UP));
		downButton = createPushButton(container, CUIPlugin.getResourceString(DOWN));
	}

	/**
	 * @param items
	 * @return
	 */
	protected String createList(String[] items) {
		return BuildToolsSettingsStore.createList(items);
	}

	/* (non-Javadoc)
	 * Rather than using the ControlFactory helper methods, this field
	 * editor is using this helper method. Other field editors use a similar
	 * set of method calls, so this seems like the safest approach 
	 * 
	 * @param parent the button composite
	 * @param label the label to place in the button
	 * @return
	 */
	private Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.heightHint = convertVerticalDLUsToPixels(button, IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		top = parent;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numColumns;
		top.setLayoutData(gd);

		controlGroup = ControlFactory.createGroup(top, getLabelText(), 2);
		GridData groupData = new GridData(GridData.FILL_HORIZONTAL);
		groupData.horizontalSpan = numColumns;
		controlGroup.setLayoutData(groupData);

		// Make the list
		list = new List(controlGroup, SWT.BORDER);
	
		// Create a grid data that takes up the extra space in the dialog and spans one column.
		GridData listData = new GridData(GridData.FILL_HORIZONTAL);
		listData.heightHint = 
			convertVerticalDLUsToPixels(list, LIST_HEIGHT_IN_DLUS);
		listData.horizontalSpan = 1;
		
		list.setLayoutData(listData);
		list.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selectionChanged();
			}
		});

		list.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				list = null;
			}
		});
		list.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				// Popup the editor on the selected item from the list
				editSelection();
			}
		});

		// Create a composite for the buttons
		Composite buttonGroup = new Composite(controlGroup, SWT.NONE);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 1;
		buttonData.verticalAlignment = GridData.BEGINNING;
		buttonGroup.setLayoutData(buttonData);
	
		GridLayout buttonLayout = new GridLayout();
		buttonLayout.numColumns = 1;
		buttonLayout.marginHeight = 0;
		buttonLayout.marginWidth = 0;
		buttonGroup.setLayout(buttonLayout);
		
		buttonGroup.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				addButton = null;
				removeButton = null;
				upButton = null;
				downButton = null;
			}
		});
	
		// Create the buttons
		createButtons(buttonGroup);
	}

	/* (non-Javadoc)
	 * Creates a selection listener that handles the selection events
	 * for the button controls and single-click events in the list to 
	 * trigger a selection change.
	 */
	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addButton) {
					addPressed();
				} else
					if (widget == removeButton) {
						removePressed();
					} else
						if (widget == upButton) {
							upPressed();
						} else
							if (widget == downButton) {
								downPressed();
							} else
								if (widget == list) {
									selectionChanged();
								}
			}
		};
	}



	/* (non-Javadoc)
	 * Event handler for the down button
	 */
	protected void downPressed() {
		swap(false);		
	}

	/* (non-Javadoc)
	 * 
	 */
	protected void editSelection() {
		// Edit the selection index
		int index = list.getSelectionIndex();
		if (index != -1) {
			String selItem = list.getItem(index);
			if (selItem != null) {
				InputDialog dialog = new InputDialog(getShell(), CUIPlugin.getResourceString(TITLE), fieldName, selItem, null);
				String newItem = null;
				if (dialog.open() == InputDialog.OK) {
					newItem = dialog.getValue();
					if (newItem != null && !newItem.equals(selItem)) {
						list.setItem(index, newItem);
						selectionChanged();
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() {
		if (list != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++){
				list.add(array[i]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		if (list != null) {
			list.removeAll();
			String s = getPreferenceStore().getDefaultString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++){
				list.add(array[i]);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {
		String s = createList(list.getItems());
		if (s != null)
			getPreferenceStore().setValue(getPreferenceName(), s);
	}

	protected String getNewInputObject() {
		// Create a dialog to prompt for a new symbol or path
		InputDialog dialog = new InputDialog(getShell(), CUIPlugin.getResourceString(TITLE), fieldName, new String(), null);
		String input = null;
		if (dialog.open() == InputDialog.OK) {
			input = dialog.getValue();
		}
		return input.length() == 0 ? null : input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		// The group control has a list and buttons so we want it to get at
		// least 2 columns to display in.
		return 2;
	}

	/* (non-Javadoc)
	 * Returns this field editor's selection listener.
	 * The listener is created if nessessary.
	 *
	 * @return the selection listener
	 */
	private SelectionListener getSelectionListener() {
		if (selectionListener == null)
			createSelectionListener();
		return selectionListener;
	}

	/* (non-Javadoc)
	 * Returns this field editor's shell.
	 * 
	 * @return the shell
	 */
	protected Shell getShell() {
		if (addButton == null)
			return null;
		return addButton.getShell();
	}

	/* (non-Javadoc)
	 * @param stringList
	 * @return
	 */
	protected String[] parseString(String stringList) {
		return BuildToolsSettingsStore.parseString(stringList);
	}

	/* (non-Javadoc)
	 * Event handler for the removeButton selected event
	 */
	protected void removePressed() {
		// Remove the selected item from the list
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		if (index >= 0) {
			list.remove(index);
			selectionChanged();
		}
	}

	/* (non-Javadoc)
	 * Clean up the list and button control states after the event 
	 * handlers fire. 
	 */
	protected void selectionChanged() {
		int index = list.getSelectionIndex();
		int size = list.getItemCount();

		// Enable the remove button if there is at least one item in the list
		removeButton.setEnabled(index >= 0);
		// Enable the up button IFF there is more than 1 item and selection index is not first item
		upButton.setEnabled(size > 1 && index > 0);
		// Enable the down button IFF there is more than 1 item and selection index not last item
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}

	/* (non-Javadoc)
	 * Swaps the location of two list elements. If the argument is <code>true</code> 
	 * the list item is swapped with the item preceeding it in the list. Otherwise 
	 *  it is swapped with the item following it.
	 * 
	 * @param moveUp
	 */
	private void swap(boolean moveUp) {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		int target = moveUp ? index - 1 : index + 1;

		if (index >= 0) {
			String[] selection = list.getSelection();
			Assert.isTrue(selection.length == 1);
			list.remove(index);
			list.add(selection[0], target);
			list.setSelection(target);
		}
		selectionChanged();
	}

	/* (non-Javadoc)
	 * Event handler for the up button. It simply swaps the selected 
	 * item with the list item above it.
	 */
	protected void upPressed() {
		swap(true);
	}
}

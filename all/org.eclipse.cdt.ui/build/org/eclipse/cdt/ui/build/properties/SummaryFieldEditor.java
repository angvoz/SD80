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
***********************************************************************/

import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class SummaryFieldEditor extends FieldEditor {

	// The tool this category belongs to
	ITool tool;
	// The text widget to hold summary of all commands for the tool
	Text summary;
	// Whitespace character
	private static final String WHITESPACE = " ";
	
	/**
	 * @param name 
	 * @param labelText
	 * @param parent
	 */
	public SummaryFieldEditor(String name, String labelText, ITool tool, Composite parent) {
		super(name, labelText, parent);
		this.tool = tool;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		// For now grab the excess space
		GridData gd = (GridData) summary.getLayoutData();
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		// Add the label
		getLabelControl(parent);

		// Create the multi-line, read-only field
		summary = new Text(parent, SWT.MULTI|SWT.READ_ONLY|SWT.WRAP);
		GridData data = new GridData();
		data.horizontalSpan = numColumns - 1;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.verticalAlignment = GridData.CENTER;
		data.grabExcessVerticalSpace = true;
		summary.setLayoutData(data);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	protected void doLoad() {
		// Look at the data store for every option defined for the tool
		IOption[] options = tool.getOptions();
		for (int index = 0; index < options.length; ++index) {
			IOption option = options[index];
			String command = option.getCommand();
			if (command == null) {
				command = "";
			}
			String id = option.getId();
			String values = getPreferenceStore().getString(id);
			String[] valuesList = BuildToolsSettingsStore.parseString(values);
			for (int j = 0; j < valuesList.length; ++j) {
				String entry = valuesList[j];
				summary.append(command + entry + WHITESPACE);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	protected void doLoadDefault() {
		doLoad();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	protected void doStore() {
		// This is a read-only summary field, so don't store data
		return;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		// There is just the label from the parent and the text field
		return 2;
	}
}

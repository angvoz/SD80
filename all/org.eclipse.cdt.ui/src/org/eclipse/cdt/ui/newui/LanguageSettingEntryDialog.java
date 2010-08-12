/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ImageCombo;
import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingEntryDialog extends AbstractPropertyDialog {
	public String sdata;
	private Button b_add2confs;
	private Button b_add2langs;
	public Text text;
	private Button b_work;
	private Button b_file;
	private Button b_vars;
	private Button b_ok;
	private Button b_ko;
	private int mode;
	private Button c_wsp;
	private Button c_BuiltIn;
	private ICConfigurationDescription cfgd;
	private boolean isWsp = false;
	private ImageCombo comboKind;
	private Label comboKindLabel;
	
	static final int NEW_FILE = 0;
	static final int NEW_DIR  = 1;
	static final int OLD_FILE = 2;
	static final int OLD_DIR  = 3;
	
	static final int DIR_MASK = 1;	
	static final int OLD_MASK = 2;	
	
	public LanguageSettingEntryDialog(Shell parent, int _mode,
		String title, String _data, ICConfigurationDescription _cfgd, int flags) {
		super(parent, title);
		mode = _mode;
		sdata = _data;
		cfgd = _cfgd;
		if (flags == ICSettingEntry.VALUE_WORKSPACE_PATH)
			isWsp = true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setLayout(new GridLayout(4, false));
		GridData gd;
		
		final String [] comboKindItems = {
				"Include Directory",
				"Preprocessor Macro",
				"Include File",
				"Preprocessor Macros File",
				"Library Path",
				"Library",
		};
		final Image[] comboKindImages = {
				CPluginImages.get(CPluginImages.IMG_OBJS_INCLUDES_FOLDER),
				CPluginImages.get(CPluginImages.IMG_OBJS_MACRO),
				CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT_HEADER),
				CPluginImages.get(CPluginImages.IMG_OBJS_MACROS_FILE),
				CPluginImages.get(CPluginImages.IMG_OBJS_LIBRARY_FOLDER),
				CPluginImages.get(CPluginImages.IMG_OBJS_LIBRARY),
		};
		
		// Icon and Title for the dialog itself
		shell.setImage(comboKindImages[0]);
		shell.setText("Add " + comboKindItems[0]);
		
		// Composite for kind and its icon
		Composite comp1 = new Composite (parent, SWT.NONE); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 5;
		comp1.setLayoutData(gd);
		comp1.setLayout(new GridLayout(5, false));
		
		// Icon for kind
		comboKindLabel = new Label (comp1, SWT.NONE);
		gd = new GridData(SWT.RIGHT);
		gd.verticalAlignment = SWT.TOP;
//		gd.horizontalSpan = 4;
		comboKindLabel.setLayoutData(gd);
		comboKindLabel.setText("Select Kind:");
		comboKindLabel.setImage(comboKindImages[0]);

		// Combo for the setting entry kind
		comboKind = new ImageCombo(comp1, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < comboKindItems.length; i++) {
			comboKind.add(comboKindItems[i], comboKindImages[i]);
		}
		comboKind.setText(comboKindItems[0]);
		
		comboKind.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = comboKind.getSelectionIndex();
				comboKindLabel.setImage(comboKindImages[index]);
				shell.setText("Add " + comboKindItems[index]);
				shell.setImage(comboKindImages[index]);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				
			}
		});
		
		final String [] pathCategories = {
				"In Project",
				"In Workspace",
				"Filesystem",
		};
		final Image[] pathCategoryImages = {
				CPluginImages.get(CPluginImages.IMG_OBJS_PROJECT),
				CPluginImages.get(CPluginImages.IMG_WORKSPACE),
				CPluginImages.get(CPluginImages.IMG_FILESYSTEM),
		};
		

		//
		// Icon for path category
		final Label comboPathCategoryLabel = new Label (comp1, SWT.NONE);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
		gd.verticalAlignment = SWT.TOP;
		gd.widthHint = 15;
//		gd.horizontalSpan = 4;
		comboPathCategoryLabel.setLayoutData(gd);
		comboPathCategoryLabel.setText("");

		// Combo for path category
		final ImageCombo comboPathCategory = new ImageCombo(comp1, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < pathCategories.length; i++) {
			comboPathCategory.add(pathCategories[i], pathCategoryImages[i]);
		}
		comboPathCategory.setText(pathCategories[0]);
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.verticalAlignment = SWT.TOP;
//		gd.widthHint = 15;
		gd.horizontalSpan = 2;
		comboPathCategory.setLayoutData(gd);
		
		comboPathCategory.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = comboPathCategory.getSelectionIndex();
				b_work.setImage(pathCategoryImages[index]);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				
			}
		});
		
		
		Label l1 = new Label(comp1, SWT.NONE);
		if ((mode & DIR_MASK) == DIR_MASK)
			l1.setText("Dir:"); 
		else
			l1.setText(Messages.IncludeDialog_1); 
		gd = new GridData();
//		gd.horizontalSpan = 2;
		l1.setLayoutData(gd);
		
		text = new Text(comp1, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = 200;
		text.setLayoutData(gd);
		if ((mode & OLD_MASK) == OLD_MASK) { text.setText(sdata); }
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setButtons();
			}});
		
		text.setFocus();
		
		//////////////////////////////////////////////////////////////
		// TODO TODO TODO TODO TODO
		//////////////////////////////////////////////////////////////
		// Path button
//		b_work = setupButton(comp1, "..."/*AbstractCPropertyTab.WORKSPACEBUTTON_NAME*/);
		b_work = new Button(comp1, SWT.PUSH);
		b_work.setText("...");
		b_work.setImage(pathCategoryImages[0]);
		b_work.setLayoutData(new GridData());
		b_work.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	buttonPressed(event);
	    }});
		
		// Variables button
//		b_vars = setupButton(comp1, AbstractCPropertyTab.VARIABLESBUTTON_NAME);
		b_vars = new Button(comp1, SWT.PUSH);
		b_vars.setText(AbstractCPropertyTab.VARIABLESBUTTON_NAME);
		b_vars.setLayoutData(new GridData());
		b_vars.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	buttonPressed(event);
	    }});


		// Checkboxes
		Composite c1 = new Composite (parent, SWT.NONE); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 4;
		c1.setLayoutData(gd);
		c1.setLayout(new GridLayout(1, false));
		
		c_BuiltIn = new Button(c1, SWT.CHECK);
		c_BuiltIn.setText("Treat as Built-In (Ignore during build)"); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if (((mode & OLD_MASK) == OLD_MASK) /*|| (cfgd instanceof ICMultiConfigDescription)*/) {
			gd.heightHint = 1;
			c_BuiltIn.setVisible(false);
		}
		c_BuiltIn.setLayoutData(gd);

		b_add2confs = new Button(c1, SWT.CHECK);
		b_add2confs.setText(Messages.IncludeDialog_2); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if (((mode & OLD_MASK) == OLD_MASK) /*|| (cfgd instanceof ICMultiConfigDescription)*/) {
			gd.heightHint = 1;
			b_add2confs.setVisible(false);
		}
		b_add2confs.setLayoutData(gd);
		
		b_add2langs = new Button(c1, SWT.CHECK);
		b_add2langs.setText(Messages.IncludeDialog_3); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if ((mode & OLD_MASK) == OLD_MASK) {
			gd.heightHint = 1;
			b_add2langs.setVisible(false);
		}
		b_add2langs.setLayoutData(gd);

// Buttons		
		Composite c3 = new Composite (parent, SWT.FILL); 
		gd = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false);
		gd.horizontalSpan = 4;
		gd.grabExcessVerticalSpace = true;
		c3.setLayoutData(gd);
		c3.setLayout(new GridLayout(4, false));
		
		new Label(c3, 0).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL)); // placeholder

//		b_ok = setupButton(c3, IDialogConstants.OK_LABEL);
		b_ok = new Button(c3, SWT.PUSH);
		b_ok.setText(IDialogConstants.OK_LABEL);
		gd = new GridData();
		gd.widthHint = b_vars.computeSize(SWT.DEFAULT,SWT.NONE).x;
		b_ok.setLayoutData(gd);
		b_ok.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	buttonPressed(event);
	    }});

//		b_ko = setupButton(c3, IDialogConstants.CANCEL_LABEL);
		b_ko = new Button(c3, SWT.PUSH);
		b_ko.setText(IDialogConstants.CANCEL_LABEL);
		gd = new GridData();
		gd.widthHint = b_vars.computeSize(SWT.DEFAULT,SWT.NONE).x;
		b_ko.setLayoutData(gd);
		b_ko.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	buttonPressed(event);
	    }});
		
		parent.getShell().setDefaultButton(b_ok);
		parent.pack();
		
//		// resize (bug #189333)
//		int x = b_ko.getBounds().width * 3 + 10;
//		int y = parent.getBounds().width - 10; 
//		if (x > y) {
//			((GridData)(text.getLayoutData())).widthHint = x;
//			parent.pack();
//		}
		
		setButtons();
		return parent;
	}	
	
	private void setButtons() {
		b_ok.setEnabled(text.getText().trim().length() > 0);
	}
	
	@Override
	public void buttonPressed(SelectionEvent e) {
		String s;
		if (e.widget.equals(b_ok)) { 
			text1 = text.getText();
			check1 = b_add2confs.getSelection();
			check2 = c_wsp.getSelection();
			check3 = b_add2langs.getSelection();
			result = true;
			shell.dispose(); 
		} else if (e.widget.equals(b_ko)) {
			shell.dispose();
		} else if (e.widget.equals(b_work)) {
			if ((mode & DIR_MASK)== DIR_MASK)
				s = AbstractCPropertyTab.getWorkspaceDirDialog(shell, text.getText());
			else 
				s = AbstractCPropertyTab.getWorkspaceFileDialog(shell, text.getText());
			if (s != null) {
				s = strip_wsp(s);
				text.setText(s);
				c_wsp.setSelection(true);
				c_wsp.setImage(getWspImage(c_wsp.getSelection()));
			}
		} else if (e.widget.equals(b_file)) {
			if ((mode & DIR_MASK)== DIR_MASK)
				s = AbstractCPropertyTab.getFileSystemDirDialog(shell, text.getText());
			else 
				s = AbstractCPropertyTab.getFileSystemFileDialog(shell, text.getText());
			if (s != null) {
				text.setText(s);
				c_wsp.setSelection(false);
				c_wsp.setImage(getWspImage(c_wsp.getSelection()));
			}
		} else if (e.widget.equals(b_vars)) {
			s = AbstractCPropertyTab.getVariableDialog(shell, cfgd);
			if (s != null) text.insert(s);
		}
	}
	
	static private Image getWspImage(boolean isWsp) {
		final Image IMG_WORKSPACE = CPluginImages.get(CPluginImages.IMG_WORKSPACE); 
		final Image IMG_FILESYSTEM = CPluginImages.get(CPluginImages.IMG_OBJS_FOLDER); 
		return isWsp ? IMG_WORKSPACE : IMG_FILESYSTEM;
	}

}

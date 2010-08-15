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

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.ImageCombo;
import org.eclipse.cdt.internal.ui.newui.LanguageSettingsEntryImages;
import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingEntryDialog extends AbstractPropertyDialog {
	public String entryName;
	private Button checkBoxAllCfgs;
	private Button checkBoxAllLangs;
	public Text text;
	private Button buttonBrowse;
//	private Button b_file;
	private Button buttonVars;
	private Button buttonOk;
	private Button buttonCancel;
	private int mode;
//	private Button c_wsp;
	private Button checkBoxBuiltIn;
	private ICConfigurationDescription cfgd;
	private ImageCombo comboKind;
	private Label labelComboKind;
	private ImageCombo comboPathCategory;
	
	
	private static final int COMBO_INDEX_INCLUDE_PATH = 0;
	private static final int COMBO_INDEX_MACRO = 1;
	private static final int COMBO_INDEX_INCLUDE_FILE = 2;
	private static final int COMBO_INDEX_MACRO_FILE = 3;
	private static final int COMBO_INDEX_LIBRARY_PATH = 4;
	private static final int COMBO_INDEX_LIBRARY_FILE = 5;

	final private String [] comboKindItems = {
			"Include Directory",
			"Preprocessor Macro",
			"Include File",
			"Preprocessor Macros File",
			"Library Path",
			"Library",
	};
	final private Image[] comboKindImages = {
			CPluginImages.get(CPluginImages.IMG_OBJS_INCLUDES_FOLDER),
			CPluginImages.get(CPluginImages.IMG_OBJS_MACRO),
			CPluginImages.get(CPluginImages.IMG_OBJS_TUNIT_HEADER),
			CPluginImages.get(CPluginImages.IMG_OBJS_MACROS_FILE),
			CPluginImages.get(CPluginImages.IMG_OBJS_LIBRARY_FOLDER),
			CPluginImages.get(CPluginImages.IMG_OBJS_LIBRARY),
	};
	
	private static final int COMBO_PATH_INDEX_PROJECT = 0;
	private static final int COMBO_PATH_INDEX_WORKSPACE = 1;
	private static final int COMBO_PATH_INDEX_FILESYSTEM = 2;
	final private String [] pathCategories = {
			"Project-Relative",
			"Workspace Path",
			"Filesystem",
	};
	final private Image[] pathCategoryImages = {
			CPluginImages.get(CPluginImages.IMG_OBJS_PROJECT),
			CPluginImages.get(CPluginImages.IMG_WORKSPACE),
			CPluginImages.get(CPluginImages.IMG_FILESYSTEM),
	};
	

	
	private ICLanguageSettingEntry[] entries;
	
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
		entryName = _data;
		cfgd = _cfgd;
	}

	private int comboIndexToKind(int index) {
		int kind=0;
		switch (index) {
		case COMBO_INDEX_INCLUDE_PATH:
			kind = ICSettingEntry.INCLUDE_PATH;
			break;
		case COMBO_INDEX_MACRO:
			kind = ICSettingEntry.MACRO;
			break;
		case COMBO_INDEX_INCLUDE_FILE:
			kind = ICSettingEntry.INCLUDE_FILE;
			break;
		case COMBO_INDEX_MACRO_FILE:
			kind = ICSettingEntry.MACRO_FILE;
			break;
		case COMBO_INDEX_LIBRARY_PATH:
			kind = ICSettingEntry.LIBRARY_PATH;
			break;
		case COMBO_INDEX_LIBRARY_FILE:
			kind = ICSettingEntry.LIBRARY_FILE;
			break;
		}
		return kind;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setLayout(new GridLayout(4, false));
		GridData gd;
		
		// Composite for kind and its icon
		Composite comp1 = new Composite (parent, SWT.NONE); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 5;
		comp1.setLayoutData(gd);
		comp1.setLayout(new GridLayout(5, false));
		
		// Icon for kind
		labelComboKind = new Label (comp1, SWT.NONE);
		gd = new GridData(SWT.RIGHT);
		gd.verticalAlignment = SWT.TOP;
//		gd.horizontalSpan = 4;
		labelComboKind.setLayoutData(gd);
		labelComboKind.setText("Select Kind:");
		labelComboKind.setImage(comboKindImages[0]);

		// Combo for the setting entry kind
		comboKind = new ImageCombo(comp1, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < comboKindItems.length; i++) {
			comboKind.add(comboKindItems[i], comboKindImages[i]);
		}
		comboKind.setText(comboKindItems[0]);
		
		comboKind.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				
			}
		});
		

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
		comboPathCategory = new ImageCombo(comp1, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
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
				updateImages();
				setButtons();
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
		if ((mode & OLD_MASK) == OLD_MASK) { text.setText(entryName); }
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
		buttonBrowse = new Button(comp1, SWT.PUSH);
		buttonBrowse.setText("...");
		buttonBrowse.setImage(pathCategoryImages[0]);
		buttonBrowse.setLayoutData(new GridData());
		buttonBrowse.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	buttonPressed(event);
	    }});
		
		// Variables button
//		b_vars = setupButton(comp1, AbstractCPropertyTab.VARIABLESBUTTON_NAME);
		buttonVars = new Button(comp1, SWT.PUSH);
		buttonVars.setText(AbstractCPropertyTab.VARIABLESBUTTON_NAME);
		buttonVars.setLayoutData(new GridData());
		buttonVars.addSelectionListener(new SelectionAdapter() {
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
		
		checkBoxBuiltIn = new Button(c1, SWT.CHECK);
		checkBoxBuiltIn.setText("Treat as Built-In (Ignore during build)"); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if (((mode & OLD_MASK) == OLD_MASK) /*|| (cfgd instanceof ICMultiConfigDescription)*/) {
			gd.heightHint = 1;
			checkBoxBuiltIn.setVisible(false);
		}
		checkBoxBuiltIn.setLayoutData(gd);
		checkBoxBuiltIn.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		Label separator = new Label(c1, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);

		checkBoxAllCfgs = new Button(c1, SWT.CHECK);
		checkBoxAllCfgs.setText(Messages.IncludeDialog_2); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if (((mode & OLD_MASK) == OLD_MASK) /*|| (cfgd instanceof ICMultiConfigDescription)*/) {
			gd.heightHint = 1;
			checkBoxAllCfgs.setVisible(false);
		}
		checkBoxAllCfgs.setLayoutData(gd);
		checkBoxAllCfgs.setEnabled(false);
		checkBoxAllCfgs.setToolTipText("Not implemented yet");
		
		checkBoxAllLangs = new Button(c1, SWT.CHECK);
		checkBoxAllLangs.setText(Messages.IncludeDialog_3); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if ((mode & OLD_MASK) == OLD_MASK) {
			gd.heightHint = 1;
			checkBoxAllLangs.setVisible(false);
		}
		checkBoxAllLangs.setLayoutData(gd);
		checkBoxAllLangs.setEnabled(false);
		checkBoxAllLangs.setToolTipText("Not implemented yet");

// Buttons		
		Composite c3 = new Composite (parent, SWT.FILL); 
		gd = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false);
		gd.horizontalSpan = 4;
		gd.grabExcessVerticalSpace = true;
		c3.setLayoutData(gd);
		c3.setLayout(new GridLayout(4, false));
		
		new Label(c3, 0).setLayoutData(new GridData(GridData.GRAB_HORIZONTAL)); // placeholder

//		b_ok = setupButton(c3, IDialogConstants.OK_LABEL);
		buttonOk = new Button(c3, SWT.PUSH);
		buttonOk.setText(IDialogConstants.OK_LABEL);
		gd = new GridData();
		gd.widthHint = buttonVars.computeSize(SWT.DEFAULT,SWT.NONE).x;
		buttonOk.setLayoutData(gd);
		buttonOk.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	buttonPressed(event);
	    }});

//		b_ko = setupButton(c3, IDialogConstants.CANCEL_LABEL);
		buttonCancel = new Button(c3, SWT.PUSH);
		buttonCancel.setText(IDialogConstants.CANCEL_LABEL);
		gd = new GridData();
		gd.widthHint = buttonVars.computeSize(SWT.DEFAULT,SWT.NONE).x;
		buttonCancel.setLayoutData(gd);
		buttonCancel.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	buttonPressed(event);
	    }});
		
		parent.getShell().setDefaultButton(buttonOk);
		parent.pack();
		
//		// resize (bug #189333)
//		int x = b_ko.getBounds().width * 3 + 10;
//		int y = parent.getBounds().width - 10; 
//		if (x > y) {
//			((GridData)(text.getLayoutData())).widthHint = x;
//			parent.pack();
//		}
		
		updateImages();
		setButtons();
		return parent;
	}	
	
	private void setButtons() {
		int indexPathKind = comboPathCategory.getSelectionIndex();
		boolean isProjectSelected = indexPathKind==COMBO_PATH_INDEX_PROJECT;
		boolean isWorkspaceSelected = indexPathKind==COMBO_PATH_INDEX_WORKSPACE;
		boolean isFilesystemSelected = indexPathKind==COMBO_PATH_INDEX_FILESYSTEM;

		String path = text.getText();
		if (path.trim().length()==0) {
			buttonOk.setEnabled(false);
		} else {
			buttonOk.setEnabled((isProjectSelected && !path.startsWith("/")) ||
					(isWorkspaceSelected && path.startsWith("/")) || isFilesystemSelected);
		}
		
		buttonVars.setEnabled(isFilesystemSelected);
	}
	
	@Override
	public void buttonPressed(SelectionEvent e) {
		String s;
		if (e.widget.equals(buttonOk)) { 
			text1 = text.getText();
			check1 = checkBoxAllCfgs.getSelection();
//			check2 = c_wsp.getSelection();
			check3 = checkBoxAllLangs.getSelection();
			result = true;
			
			int flagBuiltIn = checkBoxBuiltIn.getSelection() ? ICSettingEntry.BUILTIN : 0;
			int indexPathKind = comboPathCategory.getSelectionIndex();
			boolean isProjectPath = indexPathKind==COMBO_PATH_INDEX_PROJECT;
			boolean isWorkspacePath = isProjectPath || indexPathKind==COMBO_PATH_INDEX_WORKSPACE;
			int flagWorkspace = isWorkspacePath ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0;
			int flags = flagBuiltIn | flagWorkspace;

			ICLanguageSettingEntry entry=null;
			switch (comboKind.getSelectionIndex()) {
			case COMBO_INDEX_INCLUDE_PATH:
				entry = new CIncludePathEntry(text1, flags);
				break;
			case COMBO_INDEX_MACRO:
				entry = new CMacroEntry(text1, "FIXME", flags);
				break;
			case COMBO_INDEX_INCLUDE_FILE:
				entry = new CIncludeFileEntry(text1, flags);
				break;
			case COMBO_INDEX_MACRO_FILE:
				entry = new CMacroFileEntry(text1, flags);
				break;
			case COMBO_INDEX_LIBRARY_PATH:
				entry = new CLibraryPathEntry(text1, flags);
				break;
			case COMBO_INDEX_LIBRARY_FILE:
				entry = new CLibraryFileEntry(text1, flags);
				break;
			default:
				result = false;
			}
			
			entries = new ICLanguageSettingEntry[] {entry};
			shell.dispose(); 
		} else if (e.widget.equals(buttonCancel)) {
			shell.dispose();
		} else if (e.widget.equals(buttonBrowse)) {
			if ((mode & DIR_MASK)== DIR_MASK)
				s = AbstractCPropertyTab.getWorkspaceDirDialog(shell, text.getText());
			else 
				s = AbstractCPropertyTab.getWorkspaceFileDialog(shell, text.getText());
			if (s != null) {
				s = strip_wsp(s);
				text.setText(s);
//				c_wsp.setSelection(true);
//				c_wsp.setImage(getWspImage(c_wsp.getSelection()));
			}
//		} else if (e.widget.equals(b_file)) {
//			if ((mode & DIR_MASK)== DIR_MASK)
//				s = AbstractCPropertyTab.getFileSystemDirDialog(shell, text.getText());
//			else 
//				s = AbstractCPropertyTab.getFileSystemFileDialog(shell, text.getText());
//			if (s != null) {
//				text.setText(s);
////				c_wsp.setSelection(false);
////				c_wsp.setImage(getWspImage(c_wsp.getSelection()));
//			}
		} else if (e.widget.equals(buttonVars)) {
			s = AbstractCPropertyTab.getVariableDialog(shell, cfgd);
			if (s != null) text.insert(s);
		}
	}
	
	public ICLanguageSettingEntry[] getEntries() {
		return entries;
	}
	
	private void updateImages() {
		int indexEntryKind = comboKind.getSelectionIndex();
		int indexPathKind = comboPathCategory.getSelectionIndex();
		shell.setText("Add " + comboKindItems[indexEntryKind]);
		
		int kind = comboIndexToKind(indexEntryKind);
		int flagBuiltin = checkBoxBuiltIn.getSelection() ? ICSettingEntry.BUILTIN : 0;
		boolean isWorkspacePath = indexPathKind==COMBO_PATH_INDEX_PROJECT || indexPathKind==COMBO_PATH_INDEX_WORKSPACE;
		int flagWorkspace = isWorkspacePath ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0;
		int flags = flagBuiltin | flagWorkspace;
		Image image = LanguageSettingsEntryImages.getImage(kind, flags, indexPathKind==COMBO_PATH_INDEX_PROJECT);
		
		labelComboKind.setImage(image);
		shell.setImage(image);
		
		buttonBrowse.setImage(pathCategoryImages[indexPathKind]);
	}

	static private Image getWspImage(boolean isWsp) {
		final Image IMG_WORKSPACE = CPluginImages.get(CPluginImages.IMG_WORKSPACE); 
		final Image IMG_FILESYSTEM = CPluginImages.get(CPluginImages.IMG_OBJS_FOLDER); 
		return isWsp ? IMG_WORKSPACE : IMG_FILESYSTEM;
	}

}

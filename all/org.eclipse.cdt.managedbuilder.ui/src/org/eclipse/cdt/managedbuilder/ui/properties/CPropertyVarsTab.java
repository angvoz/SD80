/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableStatus;
import org.eclipse.cdt.core.cdtvariables.IStorableCdtVariables;
import org.eclipse.cdt.core.cdtvariables.IUserVarSupplier;
import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.ui.newui.PrefPage_Abstract;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.envvar.EnvVarOperationProcessor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * displays the build macros for the given context
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CPropertyVarsTab extends AbstractCPropertyTab {
	/*
	 * String constants
	 */
	private static final String PREFIX = "MacrosBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$

	private static final String HEADER = LABEL + ".header";  //$NON-NLS-1$
	private static final String HEADER_NAME = HEADER + ".name";  //$NON-NLS-1$
	private static final String HEADER_TYPE = HEADER + ".type";  //$NON-NLS-1$
	private static final String HEADER_VALUE = HEADER + ".value";  //$NON-NLS-1$

	private static final String TYPE = LABEL + ".type";	//$NON-NLS-1$
	private static final String TYPE_TEXT = TYPE + ".text";	//$NON-NLS-1$
	private static final String TYPE_TEXT_LIST = TYPE + ".text.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_FILE = TYPE + ".path.file";	//$NON-NLS-1$
	private static final String TYPE_PATH_FILE_LIST = TYPE + ".path.file.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_DIR = TYPE + ".path.dir";	//$NON-NLS-1$
	private static final String TYPE_PATH_DIR_LIST = TYPE + ".path.dir.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_ANY = TYPE + ".path.any";	//$NON-NLS-1$
	private static final String TYPE_PATH_ANY_LIST = TYPE + ".path.any.list";	//$NON-NLS-1$

	private static final String DELETE_CONFIRM_TITLE = LABEL + ".delete.confirm.title";	//$NON-NLS-1$
	private static final String DELETE_CONFIRM_MESSAGE = LABEL + ".delete.confirm.message";	//$NON-NLS-1$

	private static final String DELETE_ALL_CONFIRM_TITLE = LABEL + ".delete.all.confirm.title";	//$NON-NLS-1$
	private static final String DELETE_ALL_CONFIRM_MESSAGE = LABEL + ".delete.all.confirm.message";	//$NON-NLS-1$
	
	private static final String VALUE = LABEL + ".value";	//$NON-NLS-1$
	private static final String VALUE_ECLIPSE_DYNAMIC = VALUE + ".eclipse.dynamic";	//$NON-NLS-1$
	
	private static final String VALUE_DELIMITER = " || ";	//$NON-NLS-1$

	private static final ICdtVariableManager vmgr = CCorePlugin.getDefault().getCdtVariableManager();
	private static final IUserVarSupplier fUserSup = CCorePlugin.getUserVarSupplier();
	private static final EnvCmp comparator = new EnvCmp(); 

	private ICConfigurationDescription cfgd = null;
	private IStorableCdtVariables prefvars = null;
	
	//currently the "CWD" and "PWD" macros are not displayed in UI
	private static final String fHiddenMacros[] = new String[]{
			"CWD",   //$NON-NLS-1$
			"PWD"	  //$NON-NLS-1$
		};
	
	private boolean fShowSysMacros = false;
	private Set<String> fIncorrectlyDefinedMacrosNames = new HashSet<String>();
	
	private TableViewer tv;
	private Label fStatusLabel;
	private Label  lb1, lb2;
	
	private static final String[] fEditableTableColumnProps = new String[] {
		"editable name",	//$NON-NLS-1$
		"editable type",	//$NON-NLS-1$
		"editable value",	//$NON-NLS-1$
	};

	private static final String[] fTableColumnNames = new String[] {
		UIMessages.getString(HEADER_NAME),
		UIMessages.getString(HEADER_TYPE),
		UIMessages.getString(HEADER_VALUE),
	};

	private static final ColumnLayoutData[] fTableColumnLayouts = {new ColumnPixelData(100), new ColumnPixelData(100), new ColumnPixelData(250)};
	
	private class MacroContentProvider implements IStructuredContentProvider{
		public Object[] getElements(Object inputElement) { return (Object[])inputElement; }
		public void dispose() {	}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
	}
	
	private class MacroLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider , ITableFontProvider, IColorProvider{
		@Override
		public Image getImage(Object element) { return null; }
		@Override
		public String getText(Object element) { return getColumnText(element, 0); }
		public Font getFont(Object element) { return getFont(element, 0); }
		public Image getColumnImage(Object element, int columnIndex) { return null; }

		public Color getBackground(Object element){
			ICdtVariable var = (ICdtVariable)element;
			if(isUserVar(var))
				return BACKGROUND_FOR_USER_VAR;
			return null; 
		}
		
		public String getColumnText(Object element, int columnIndex) {
			ICdtVariable var = (ICdtVariable)element;
			switch(columnIndex){
			case 0:
				return var.getName();
			case 1:
				switch(var.getValueType()){
				case ICdtVariable.VALUE_PATH_FILE:
					return UIMessages.getString(TYPE_PATH_FILE);
				case ICdtVariable.VALUE_PATH_FILE_LIST:
					return UIMessages.getString(TYPE_PATH_FILE_LIST);
				case ICdtVariable.VALUE_PATH_DIR:
					return UIMessages.getString(TYPE_PATH_DIR);
				case ICdtVariable.VALUE_PATH_DIR_LIST:
					return UIMessages.getString(TYPE_PATH_DIR_LIST);
				case ICdtVariable.VALUE_PATH_ANY:
					return UIMessages.getString(TYPE_PATH_ANY);
				case ICdtVariable.VALUE_PATH_ANY_LIST:
					return UIMessages.getString(TYPE_PATH_ANY_LIST);
				case ICdtVariable.VALUE_TEXT:
					return UIMessages.getString(TYPE_TEXT);
				case ICdtVariable.VALUE_TEXT_LIST:
					return UIMessages.getString(TYPE_TEXT_LIST);
				default:
					return "? " + var.getValueType();   //$NON-NLS-1$
				}
			case 2:
				return getString(var);  
			}
			return EMPTY_STR;
		}
		
		public Font getFont(Object element, int columnIndex) {
			ICdtVariable var = (ICdtVariable)element;
			if(columnIndex == 0 && isUserVar(var))
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
			return null;
		}

		public Color getForeground(Object element){
			if(fIncorrectlyDefinedMacrosNames.contains(((ICdtVariable)element).getName()))
				return JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR);
			return null;
	    }
	}

	/*
	 * called when the user macro selection was changed 
	 */
	private void handleSelectionChanged(SelectionChangedEvent event){
		updateButtons();
	}
	
	@Override
	protected void updateButtons() {
		Object[] obs = ((IStructuredSelection)tv.getSelection()).toArray();
		boolean canEdit = false;
		boolean canDel  = false;
		if (obs != null && obs.length > 0) {
			canEdit = (obs.length == 1);
			for (int i=0; i<obs.length; i++) {
				if (obs[i] instanceof ICdtVariable && isUserVar((ICdtVariable)obs[i])) { 
					canDel = true;
					break;
				}
			}
		}
		buttonSetEnabled(1, canEdit);
		buttonSetEnabled(2, canDel);
	}
	
	/*
	 * called when a custom button was pressed
	 */
	@Override
	public void buttonPressed(int index){
		switch(index){
		case 0:
			handleAddButton();
			break;
		case 1:
			handleEditButton();
			break;
		case 2:
			handleDelButton();
			break;
		}
		tv.getTable().setFocus();
	}
	
	private void replaceMacros() {
		if (!page.isMultiCfg() || 
				cfgd == null ||
				CDTPrefUtil.getInt(CDTPrefUtil.KEY_WMODE) != CDTPrefUtil.WMODE_REPLACE)
			return;
		ICdtVariable[] vars = getVariables();
		for (int i=0; i<vars.length; i++)
			if (!isUserVar(vars[i]))
				vars[i] = null;
		for (ICConfigurationDescription c : getCfs()) {
			fUserSup.deleteAll(c);
			for (ICdtVariable macro : vars)
				if (macro != null)
					fUserSup.createMacro(macro, c);
		}
	}

	private ICConfigurationDescription[] getCfs() {
		if (cfgd instanceof ICMultiItemsHolder)
			return (ICConfigurationDescription[])
				((ICMultiItemsHolder)cfgd).getItems();
		else 
			return new ICConfigurationDescription[] {cfgd};
	}
	
	private void addOrEdit(ICdtVariable macro, boolean forAll) {
		if( ! canCreate(macro)) 
			return;
		if (cfgd != null) {
			if (forAll) {
				for (ICConfigurationDescription c : page.getCfgsEditable()) 
					fUserSup.createMacro(macro, c);
			} else {
				if (page.isMultiCfg() && cfgd instanceof ICMultiItemsHolder) {
					for (ICConfigurationDescription c : getCfs())
						fUserSup.createMacro(macro, c);
					replaceMacros();
				} else
					fUserSup.createMacro(macro, cfgd);
			}
		}
		else if (chkVars())
			prefvars.createMacro(macro);
		updateData();
	}
	
	private void handleAddButton() {
		NewVarDialog dlg = new NewVarDialog(usercomp.getShell(), null, cfgd, getVariables());
		if(dlg.open() == Dialog.OK)
			addOrEdit(dlg.getDefinedMacro(), dlg.isForAllCfgs);
	}
	private void handleEditButton() {
		ICdtVariable _vars[] = getSelectedUserMacros();
		if(_vars != null && _vars.length == 1){
			NewVarDialog dlg = new NewVarDialog(usercomp.getShell() ,_vars[0], cfgd, getVariables());
			if(dlg.open() == Dialog.OK)
				addOrEdit(dlg.getDefinedMacro(), false);
		}
	}

	private void handleDelButton() {
		ICdtVariable macros[] = getSelectedUserMacros();
		if(macros != null && macros.length > 0){
			if(MessageDialog.openQuestion(usercomp.getShell(),
					UIMessages.getString(DELETE_CONFIRM_TITLE),
					UIMessages.getString(DELETE_CONFIRM_MESSAGE))){
				for(int i = 0; i < macros.length; i++){
					if (cfgd != null) {
						if (page.isMultiCfg() && cfgd instanceof ICMultiItemsHolder) {
							ICConfigurationDescription[] cfs = (ICConfigurationDescription[])((ICMultiItemsHolder)cfgd).getItems();
							for (int k=0; k<cfs.length; k++)
								fUserSup.deleteMacro(macros[i].getName(), cfs[k]);
							replaceMacros();
						}
						else		
							fUserSup.deleteMacro(macros[i].getName(), cfgd);
					}
					else if (chkVars())
						prefvars.deleteMacro(macros[i].getName());
				}
				updateData();
			}
		}
	}
	
	/*
	 * returnes the selected user-defined macros
	 */
	@SuppressWarnings("unchecked")
	private ICdtVariable[] getSelectedUserMacros(){
		if(tv == null)	return null;
		List<ICdtVariable> list = ((IStructuredSelection)tv.getSelection()).toList();
		return list.toArray(new ICdtVariable[list.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		if(MessageDialog.openQuestion(usercomp.getShell(),
				UIMessages.getString(DELETE_ALL_CONFIRM_TITLE),
				UIMessages.getString(DELETE_ALL_CONFIRM_MESSAGE))){
			if (cfgd != null) {
				if (page.isMultiCfg() && cfgd instanceof ICMultiItemsHolder) {
					ICConfigurationDescription[] cfs = (ICConfigurationDescription[])((ICMultiItemsHolder)cfgd).getItems();
					for (int i=0; i<cfs.length; i++)
						fUserSup.deleteAll(cfs[i]);
				} else
					fUserSup.deleteAll(cfgd);
			}
			else if (chkVars())
				prefvars.deleteAll();
			updateData();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		initButtons(new String[] {ADD_STR, EDIT_STR, DEL_STR});
		usercomp.setLayout(new GridLayout(2, true));
		createTableControl();
		
		// Create a "show parent levels" button 
		final Button b = new Button(usercomp, SWT.CHECK);
		b.setFont(usercomp.getFont());
		b.setText(Messages.getString("CPropertyVarsTab.0")); //$NON-NLS-1$
		b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b.setSelection(fShowSysMacros);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fShowSysMacros = b.getSelection();
				updateData(getResDesc());
			}
		});
		
	    lb1 = new Label(usercomp, SWT.BORDER | SWT.CENTER);
	    lb1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    lb1.setToolTipText(UIMessages.getString("EnvironmentTab.15")); //$NON-NLS-1$
	    lb1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CDTPrefUtil.spinDMode();
				updateData();
			}});
	    
		
		fStatusLabel = new Label(usercomp, SWT.LEFT);
		fStatusLabel.setFont(usercomp.getFont());
		fStatusLabel.setText(EMPTY_STR);
		fStatusLabel.setLayoutData(new GridData(GridData.BEGINNING));
		fStatusLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));

	    lb2 = new Label(usercomp, SWT.BORDER | SWT.CENTER);
	    lb2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    lb2.setToolTipText(UIMessages.getString("EnvironmentTab.23")); //$NON-NLS-1$
	    lb2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CDTPrefUtil.spinWMode();
				updateLbs(null, lb2);
			}});
	}
	
	private void createTableControl(){
		TableViewer tableViewer;
		tableViewer = new TableViewer(usercomp, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION);
		
		Table table = tableViewer.getTable();
		TableLayout tableLayout = new TableLayout();
		for (int i = 0; i < fTableColumnNames.length; i++) {
			tableLayout.addColumnData(fTableColumnLayouts[i]);
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			tc.setResizable(fTableColumnLayouts[i].resizable);
			tc.setText(fTableColumnNames[i]);
		}
		table.setLayout(tableLayout);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		tableViewer.setContentProvider(new MacroContentProvider());
		tableViewer.setLabelProvider(new MacroLabelProvider());
		tableViewer.setSorter(new ViewerSorter());
		
		tableViewer.setColumnProperties(fEditableTableColumnProps);
		tv = tableViewer;
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged(event);
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
	
			public void doubleClick(DoubleClickEvent event) {
				if (!tv.getSelection().isEmpty()) {	buttonPressed(1); }
			}
		});
	
		table.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e){
				if(e.keyCode == SWT.DEL) buttonPressed(2);
			}

			public void keyReleased(KeyEvent e){}
		});
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		table.setLayoutData(gd);
	}
	

	/*
	 * answers whether the macro of a given name can be sreated
	 */
	private boolean canCreate(ICdtVariable v){
		if (v == null) return false;
		String name = v.getName();
		if(name == null || (name = name.trim()).length() == 0)
			return false;
		if(fHiddenMacros != null){
			for(int i = 0; i < fHiddenMacros.length; i++){
				if(fHiddenMacros[i].equals(EnvVarOperationProcessor.normalizeName(name)))
					return false;
			}
		}
		return true; 
	}
		
	@Override
	public void updateData(ICResourceDescription _cfgd) {
		if (_cfgd == null) {
			cfgd = null;
			chkVars();
		} else {
			cfgd = _cfgd.getConfiguration();
			prefvars = null;
		}
		updateData();
	}
	
	private boolean chkVars() {
		if (prefvars == null)
			prefvars = fUserSup.getWorkspaceVariablesCopy();
		return (prefvars != null);
	}
	
	private void checkVariableIntegrity() {
		try{
			if (page.isMultiCfg() && cfgd instanceof ICMultiItemsHolder) {
				ICConfigurationDescription[] cfs = (ICConfigurationDescription[])((ICMultiItemsHolder)cfgd).getItems();
				for (int i=0; i<cfs.length; i++)
					vmgr.checkVariableIntegrity(cfs[i]);
			} else
				vmgr.checkVariableIntegrity(cfgd);
			updateState(null);
		} catch (CdtVariableException e){
			updateState(e);
		}
	}

	private ICdtVariable[] getVariables() {
		if (page.isMultiCfg() && cfgd instanceof ICMultiItemsHolder) {
			ICMultiItemsHolder mih = (ICMultiItemsHolder)cfgd;
			ICConfigurationDescription[] cfs = (ICConfigurationDescription[])mih.getItems();
			ICdtVariable[][] vs = new ICdtVariable[cfs.length][];
			for (int i=0; i<cfs.length; i++)
				vs[i] = vmgr.getVariables(cfs[i]);
			Object[] obs = CDTPrefUtil.getListForDisplay(vs, comparator);
			ICdtVariable[] v = new ICdtVariable[obs.length];
			System.arraycopy(obs, 0, v, 0, obs.length);
			return v;
		} else
			return vmgr.getVariables(cfgd);
	}
	
	private void updateData() {
		if(tv == null) return;
			
		checkVariableIntegrity();
		// get variables
		ICdtVariable[] _vars = getVariables();
		if (_vars == null) return;

		updateLbs(lb1, lb2);
		
		if (cfgd == null) {
			chkVars();
			if (fShowSysMacros) {
				List<ICdtVariable> lst = new ArrayList<ICdtVariable>(_vars.length);
				ICdtVariable[] uvars = prefvars.getMacros();
				for (int i=0; i<uvars.length; i++) {
					lst.add(uvars[i]);
					for (int j=0; j<_vars.length; j++) {
						if (_vars[j] != null && _vars[j].getName().equals(uvars[i].getName())) {
							_vars[j] = null;
							break;
						}
					}
				}
				// add system vars not rewritten by user's
				for (int j=0; j<_vars.length; j++) {
					if (_vars[j] != null && !vmgr.isUserVariable(_vars[j], null)) 
						lst.add(_vars[j]);
				}
				_vars = lst.toArray(new ICdtVariable[lst.size()]);
			} else {
				_vars = prefvars.getMacros();
			}
		}
		
		ArrayList<ICdtVariable> list = new ArrayList<ICdtVariable>(_vars.length);
		for(int i = 0; i < _vars.length; i++){
			if(_vars[i] != null && (fShowSysMacros || isUserVar(_vars[i]))) 
				list.add(_vars[i]);
		}
		Collections.sort(list, CDTListComparator.getInstance());
		tv.setInput(list.toArray(new ICdtVariable[list.size()]));
		updateButtons();
	}
	
	private void updateState(CdtVariableException e){
		fIncorrectlyDefinedMacrosNames.clear();
		if(e != null){
			fStatusLabel.setText(e.getMessage());
			fStatusLabel.setVisible(true);
			ICdtVariableStatus statuses[] = e.getVariableStatuses();
			for(int i = 0; i < statuses.length; i++){
				String name = statuses[i].getVariableName();
				if(name != null)
					fIncorrectlyDefinedMacrosNames.add(name);
			}
		}
		else
			fStatusLabel.setVisible(false);
	}
	
	/**
	 * Checks whether variable is user's
	 * @param v - variable to check
	 * @return 
	 */
	private boolean isUserVar(ICdtVariable v) {
		if (cfgd == null)
			return chkVars() && prefvars.contains(v);
		if (page.isMultiCfg() && cfgd instanceof ICMultiItemsHolder) {
			ICConfigurationDescription[] cfs = (ICConfigurationDescription[])((ICMultiItemsHolder)cfgd).getItems();
			for (int i=0; i<cfs.length; i++)
				if (vmgr.isUserVariable(v, cfs[i]))
					return true;
			return false;
		} else 
			return vmgr.isUserVariable(v, cfgd); 
	}
	
	private String getString(ICdtVariable v) {
		if (fUserSup.isDynamic(v)) 
			return UIMessages.getString(VALUE_ECLIPSE_DYNAMIC);
		String value = EMPTY_STR; 
		try {			
			if (CdtVariableResolver.isStringListVariable(v.getValueType()))
				value = vmgr.convertStringListToString(v.getStringListValue(), VALUE_DELIMITER);
			else
				value = v.getStringValue();
		} catch (CdtVariableException e1) {}
		return value;
	}
	
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		if (cfgd != null) {// only for project, not for prefs
			if (page.isMultiCfg()) {
				if (src instanceof ICMultiItemsHolder && 
					dst instanceof ICMultiItemsHolder) {
					ICMultiItemsHolder s = (ICMultiItemsHolder)src;
					ICMultiItemsHolder d = (ICMultiItemsHolder)dst;
					ICResourceDescription[] r0 = (ICResourceDescription[])s.getItems();
					ICResourceDescription[] r1 = (ICResourceDescription[])d.getItems();
					if (r0.length != r1.length)
						return; // unprobable
					for (int i=0; i<r0.length; i++) {
						ICdtVariable[] vs = fUserSup.getMacros(r0[i].getConfiguration());
						fUserSup.setMacros(vs, r1[i].getConfiguration());
					}
				}
			} else {
				ICdtVariable[] vs = fUserSup.getMacros(src.getConfiguration());
				fUserSup.setMacros(vs, dst.getConfiguration());
			}
		} else if (chkVars())
			fUserSup.storeWorkspaceVariables(true);
	}

	/**
	 * Unlike other pages, workspace variables 
	 * should be stored explicitly on "OK".  
	 */
	@Override
	protected void performOK() {
		if (chkVars()) try {
			if (fUserSup.setWorkspaceVariables(prefvars))
				if (page instanceof PrefPage_Abstract)
					PrefPage_Abstract.isChanged = true;
		} catch (CoreException e) {}
		prefvars = null;
		super.performOK();
	}
	
	@Override
	protected void performCancel() {
		prefvars = null;
		super.performCancel();
	}

	// This page can be displayed for project only
	@Override
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}

	private static class EnvCmp implements Comparator<Object> {
		
		public int compare(Object a0, Object a1) {
			if (a0 == null || a1 == null)
				return 0;
			if (a0 instanceof ICdtVariable &&
				a1 instanceof ICdtVariable) {
				ICdtVariable x0 = (ICdtVariable)a0;
				ICdtVariable x1 = (ICdtVariable)a1;
				String s0 = x0.getName();
				if (s0 == null)
					s0 = AbstractPage.EMPTY_STR;
				String s1 = x1.getName();
				if (s1 == null)
					s1 = AbstractPage.EMPTY_STR;
				return(s0.compareTo(s1));
			} else 
				return 0;
		}
	}

}

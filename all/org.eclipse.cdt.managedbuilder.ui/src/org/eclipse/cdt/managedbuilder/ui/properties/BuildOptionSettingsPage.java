/*******************************************************************************
 * Copyright (c) 2003, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler;
import org.eclipse.cdt.managedbuilder.core.IOptionApplicability;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import java.lang.AssertionError;

public class BuildOptionSettingsPage extends BuildSettingsPage {
	private Map fieldsMap = new HashMap();
	private IOptionCategory category;
	private boolean isItResourceConfigPage;
	private Map fieldEditorsToParentMap = new HashMap();

	public BuildOptionSettingsPage(IConfiguration configuration, IOptionCategory category) {
		// Cache the configuration and option category this page is created for
		super(configuration);
		this.category = category;
		isItResourceConfigPage = false;
	}
	
	public BuildOptionSettingsPage(IResourceConfiguration resConfig, IOptionCategory category) {
		// Cache the configuration and option category this page is created for
		super(resConfig);
		this.category = category;
		isItResourceConfigPage = true;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#computeSize()
	 */
	public Point computeSize() {
		return super.computeSize();
	}
	/* (non-Javadoc)
	 * Private access function which returns the correct configuration
	 * argument for valueHandler call-backs.
	 */	
	private IBuildObject getConfigurationHandle() {
		if ( isItResourceConfigPage ) {
			return resConfig;
		} else {
			return configuration;
		}		
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	protected void createFieldEditors() {
		// Get the preference store for the build settings
		super.createFieldEditors();
		// Iterate over the options in the category and create a field editor
		// for each
		Object[][] options;
		if ( isItResourceConfigPage ) {
			options = category.getOptions(resConfig);
		} else {
			options = category.getOptions(configuration);
		}
		
		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions)options[index][0];
			if (holder == null) break;	//  The array may not be full
			IOption opt = (IOption)options[index][1];

			
			// check to see if the option has an applicability calculator
			IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();
			
			// is the option visible?
			IBuildObject config;
			if ( isItResourceConfigPage ) {
				config = resConfig;
			} else {
				config = configuration;
			}
			if (applicabilityCalculator == null || applicabilityCalculator.isOptionVisible(config, holder, opt)) {
		
			try {
				// Figure out which type the option is and add a proper field
				// editor for it
				switch (opt.getValueType()) {
					case IOption.STRING:
						// fix for PR 63973
						// Check browse type.
						// If browsing is set, use a field editor that has a
						// browse button of
						// the appropriate type.
						// Otherwise, use a regular text field.
						switch (opt.getBrowseType()) {
						case IOption.BROWSE_DIR:
							Composite fieldEditorParent2 = getFieldEditorParent();
							DirectoryFieldEditor dirFieldEditor = new DirectoryFieldEditor(
									opt.getId(), opt.getName(),	fieldEditorParent2);

							setFieldEditorEnablement(holder,
									opt, applicabilityCalculator, dirFieldEditor, fieldEditorParent2);

							addField(dirFieldEditor);
							fieldsMap.put(opt.getId(), dirFieldEditor);
							fieldEditorsToParentMap.put(dirFieldEditor,	fieldEditorParent2);

							break;

						case IOption.BROWSE_FILE:
							Composite fieldEditorParent3 = getFieldEditorParent();
							FileFieldEditor fileFieldEditor = new FileFieldEditor(
									opt.getId(), opt.getName(),	fieldEditorParent3);

							setFieldEditorEnablement(holder,
									opt, applicabilityCalculator, fileFieldEditor, fieldEditorParent3);

							addField(fileFieldEditor);
							fieldsMap.put(opt.getId(), fileFieldEditor);
							fieldEditorsToParentMap.put(fileFieldEditor, fieldEditorParent3);
							break;

						case IOption.BROWSE_NONE:
							Composite fieldEditorParent4 = getFieldEditorParent();
							StringFieldEditor stringField = new StringFieldEditor(
									opt.getId(), opt.getName(),	fieldEditorParent4);

							setFieldEditorEnablement(holder,
									opt, applicabilityCalculator, stringField, fieldEditorParent4);

							addField(stringField);
							fieldsMap.put(opt.getId(), stringField);
							fieldEditorsToParentMap.put(stringField, fieldEditorParent4);
							break;

						default:
							// should not be possible
							throw (new AssertionError());
						}
						// end fix for 63973
						break;
					case IOption.BOOLEAN:
						Composite fieldEditorParent5 = getFieldEditorParent();
						BooleanFieldEditor booleanField = new BooleanFieldEditor(
								opt.getId(), opt.getName(), fieldEditorParent5);

						setFieldEditorEnablement(holder, 
								opt, applicabilityCalculator, booleanField, fieldEditorParent5);

						addField(booleanField);
						fieldsMap.put(opt.getId(), booleanField);
						fieldEditorsToParentMap.put(booleanField, fieldEditorParent5);
						break;
					case IOption.ENUMERATED:
						String selId;
						String sel;
						try {
							selId = opt.getSelectedEnum();
							sel = opt.getEnumName(selId);
						} catch (BuildException e) {
							// If we get this exception, then the option type is
							// wrong
							break;
						}
						// Get all applicable values for this enumerated Option, But display
						// only the enumerated values that are valid (static set of enumerated values defined
						// in the plugin.xml file) in the UI Combobox. This refrains the user from selecting an
						// invalid value and avoids issuing an error message.
						String[] enumNames = opt.getApplicableValues();
						Vector enumValidList = new Vector();
						for (int i = 0; i < enumNames.length; ++i) {
							if (opt.getValueHandler().isEnumValueAppropriate(getConfigurationHandle(), 
									opt.getOptionHolder(), opt, opt.getValueHandlerExtraArgument(), enumNames[i])) {
								enumValidList.add(enumNames[i]);
							}
						}
						String[] enumValidNames = new String[enumValidList.size()];
						enumValidList.copyInto(enumValidNames);

						Composite fieldEditorParent6 = getFieldEditorParent();
						BuildOptionComboFieldEditor comboField = new BuildOptionComboFieldEditor(
								opt.getId(), opt.getName(), enumValidNames, sel, fieldEditorParent6);

						setFieldEditorEnablement(holder, 
								opt, applicabilityCalculator, comboField, fieldEditorParent6);

						addField(comboField);
						fieldsMap.put(opt.getId(), comboField);
						fieldEditorsToParentMap.put(comboField,	fieldEditorParent6);
						break;
					case IOption.INCLUDE_PATH:
					case IOption.STRING_LIST:
					case IOption.PREPROCESSOR_SYMBOLS:
					case IOption.LIBRARIES:
					case IOption.OBJECTS:

						Composite fieldEditorParent7 = getFieldEditorParent();
						FileListControlFieldEditor listField = new FileListControlFieldEditor(
								opt.getId(), opt.getName(), fieldEditorParent7,	opt.getBrowseType());

						setFieldEditorEnablement(holder, 
								opt, applicabilityCalculator, listField, fieldEditorParent7);

						addField(listField);
						fieldsMap.put(opt.getId(), listField);
						fieldEditorsToParentMap.put(listField, fieldEditorParent7);
						break;
					default:
						break;
					}
			} catch (BuildException e) {
			}
			}
		}
	}
	
	/**
	 * Answers <code>true</code> if the settings page has been created for the
	 * option category specified in the argument.
	 * 
	 * @param category
	 * @return
	 */
	public boolean isForCategory(IOptionCategory category) {
		if (category != null) {
			return category.equals(this.category);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		// Write the field editor contents out to the preference store
		boolean ok = super.performOk();
		// Write the preference store values back to the build model
		
		Object[][] options;
		if (isItResourceConfigPage){
			options = category.getOptions(resConfig);
		} else {
			options = category.getOptions(configuration);
		}
		
		for (int i = 0; i < options.length; i++) {
			IHoldsOptions holder = (IHoldsOptions)options[i][0];
			if (holder == null) break;	//  The array may not be full
			IOption option = (IOption)options[i][1];

			try {
				// Transfer value from preference store to options
				IOption setOption = null;
				switch (option.getValueType()) {
					case IOption.BOOLEAN :
						boolean boolVal = getToolSettingsPreferenceStore().getBoolean(option.getId());
						if(isItResourceConfigPage) {
							setOption = ManagedBuildManager.setOption(resConfig, holder, option, boolVal);
						} else {
							setOption = ManagedBuildManager.setOption(configuration, holder, option, boolVal);
						}
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), boolVal);
							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
							fe.setPreferenceName(setOption.getId());
						}
						break;
					case IOption.ENUMERATED :
						String enumVal = getToolSettingsPreferenceStore().getString(option.getId());
						String enumId = option.getEnumeratedId(enumVal);
						if(isItResourceConfigPage) {
							setOption = ManagedBuildManager.setOption(resConfig, holder, option, 
									(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						} else {
							setOption = ManagedBuildManager.setOption(configuration, holder, option, 
									(enumId != null && enumId.length() > 0) ? enumId : enumVal);
						}
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), enumVal);
							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
							fe.setPreferenceName(setOption.getId());
						}
						break;
					case IOption.STRING :
						String strVal = getToolSettingsPreferenceStore().getString(option.getId());
						if(isItResourceConfigPage){
							setOption = ManagedBuildManager.setOption(resConfig, holder, option, strVal);
						} else {
							setOption = ManagedBuildManager.setOption(configuration, holder, option, strVal);	
						}
						
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), strVal);
							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
							fe.setPreferenceName(setOption.getId());
						}
						break;
					case IOption.STRING_LIST :
					case IOption.INCLUDE_PATH :
					case IOption.PREPROCESSOR_SYMBOLS :
					case IOption.LIBRARIES :
					case IOption.OBJECTS :
						String listStr = getToolSettingsPreferenceStore().getString(option.getId());
						String[] listVal = BuildToolsSettingsStore.parseString(listStr);
						if( isItResourceConfigPage){
							setOption = ManagedBuildManager.setOption(resConfig, holder, option, listVal);
						}else {
							setOption = ManagedBuildManager.setOption(configuration, holder, option, listVal);	
						}
						
						// Reset the preference store since the Id may have changed
						if (setOption != option) {
							getToolSettingsPreferenceStore().setValue(setOption.getId(), listStr);
							FieldEditor fe = (FieldEditor)fieldsMap.get(option.getId());
							fe.setPreferenceName(setOption.getId());
						}
						break;
					default :
						break;
				}

				// Call an MBS CallBack function to inform that Settings related to Apply/OK button 
				// press have been applied.
				if (setOption == null)
					setOption = option;
				
				if (setOption.getValueHandler().handleValue(
						getConfigurationHandle(), 
						setOption.getOptionHolder(), 
						setOption,
						setOption.getValueHandlerExtraArgument(), 
						IManagedOptionValueHandler.EVENT_APPLY)) {
					// TODO : Event is handled successfully and returned true.
					// May need to do something here say log a message.
				} else {
					// Event handling Failed. 
				} 
			} catch (BuildException e) {}

		}
		return ok;
	}
	
	/**
	 * Update field editors in this page when the page is loaded.
	 */
	public void updateFields() {
		Collection fieldsList = fieldsMap.values();
		Iterator iter = fieldsList.iterator();
		while (iter.hasNext()) {
			FieldEditor editor = (FieldEditor) iter.next();
			//  TODO: Why was loadDefault called before load?  It results in String fields 
			//        setting the "dirty" flag
			//editor.loadDefault();
			editor.load();
		}
	}
	
	/**
	 * saves all field editors
	 */
	public void storeSettings() {
		super.performOk();
	}
	
	private void setFieldEditorEnablement(IHoldsOptions holder, IOption option,
			IOptionApplicability optionApplicability, FieldEditor fieldEditor, Composite parent) {
		if (optionApplicability == null)
			return;

		// if the option is not enabled then disable it
		IBuildObject config;
		if ( isItResourceConfigPage ) {
			config = resConfig;
		} else {
			config = configuration;
		}
		if (!optionApplicability.isOptionEnabled(config, holder, option )) {
			fieldEditor.setEnabled(false, parent);
		} else {
			fieldEditor.setEnabled(true, parent);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		// allow superclass to handle as well
		super.propertyChange(event);

		// some option has changed on this page... update enabled/disabled state for all options

		Object[][] options;
		if (isItResourceConfigPage) {
			options = category.getOptions(resConfig);
		} else {
			options = category.getOptions(configuration);
		}

		for (int index = 0; index < options.length; ++index) {
			// Get the option
			IHoldsOptions holder = (IHoldsOptions) options[index][0];
			if (holder == null)
				break; //  The array may not be full
			IOption opt = (IOption) options[index][1];

			// is the option on this page?
			if (fieldsMap.containsKey(opt.getId())) {
				// check to see if the option has an applicability calculator
				IOptionApplicability applicabilityCalculator = opt.getApplicabilityCalculator();

				if (applicabilityCalculator != null) {
					FieldEditor fieldEditor = (FieldEditor) fieldsMap.get(opt.getId());
					Composite parent = (Composite) fieldEditorsToParentMap.get(fieldEditor);
					setFieldEditorEnablement(holder, opt, applicabilityCalculator, fieldEditor, parent);
				}
			}

		}
	}

}

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.build.managed.BuildException;
import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.IOption;
import org.eclipse.cdt.core.build.managed.IOptionCategory;
import org.eclipse.cdt.core.build.managed.ITool;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

public class BuildToolsSettingsStore implements IPreferenceStore {
	public static final String DEFAULT_SEPERATOR = ";";

	// List of listeners on the property store
	private ListenerList listenerList;
	private Map optionMap;
	private boolean dirtyFlag;
	private IConfiguration owner;
	
	public BuildToolsSettingsStore (IConfiguration config) {	
		listenerList = new ListenerList();
		dirtyFlag = false;
		owner = config;
			
		// Now populate the options map
		populateOptionMap();
	}


	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		listenerList.add(listener);
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		return getOptionMap().containsKey(name);
	}

	/**
	 * Answers a <code>String</code> containing the strings passed in the 
	 * argument separated by the DEFAULT_SEPERATOR
	 * 
	 * @param items An array of strings
	 * @return 
	 */
	public static String createList(String[] items) {
		StringBuffer path = new StringBuffer(""); //$NON-NLS-1$
	
		for (int i = 0; i < items.length; i++) {
			path.append(items[i]);
			if (i < (items.length - 1)) {
				path.append(DEFAULT_SEPERATOR);
			}
		}
		return path.toString();
	}
	
	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
			Object[] listeners = listenerList.getListeners();
			if (listeners.length > 0 && (oldValue == null || !oldValue.equals(newValue))) 
			{
				PropertyChangeEvent pe = new PropertyChangeEvent(this, name, oldValue, newValue);
				for (int i = 0; i < listeners.length; ++i) 
				{
					IPropertyChangeListener l = (IPropertyChangeListener)listeners[i];
					l.propertyChange( pe );
				}
			}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String name) {
		Object b = getOptionMap().get(name);
		if (b instanceof Boolean)
		{
			return ((Boolean)b).booleanValue();
		}
		return false;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
	 */
	public boolean getDefaultBoolean(String name) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
	 */
	public double getDefaultDouble(String name) {
		return 0;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
	 */
	public float getDefaultFloat(String name) {
		return 0;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
	 */
	public int getDefaultInt(String name) {
		return 0;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
	 */
	public long getDefaultLong(String name) {
		return 0;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
	 */
	public String getDefaultString(String name) {
		return new String();
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
	 */
	public double getDouble(String name) {
		return getDefaultDouble(name);
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
	 */
	public float getFloat(String name) {
		return getDefaultFloat(name);
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
	 */
	public int getInt(String name) {
		return getDefaultInt(name);
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
	 */
	public long getLong(String name) {
		return getDefaultLong(name);
	}

	/* (non-javadoc)
	 * Answers the map containing the strings associated with each option 
	 * ID.
	 * 
	 * @return
	 */
	private Map getOptionMap() {
		if (optionMap == null) {
			optionMap = new HashMap();
		}
		return optionMap;
	}

	private void getOptionsForCategory(IOptionCategory cat) {
		IOptionCategory [] children = cat.getChildCategories();
		// If there are child categories, add their options
		for (int i = 0; i < children.length; ++i) {
			getOptionsForCategory(children[i]);
		}
		// Else get the options for this category and add them to the map
		IOption [] options = cat.getOptions(owner);
		for (int j = 0; j < options.length; ++j) {
			IOption opt = options[j];
			String name = opt.getId();
			Object value;
			// Switch on the type of option
			switch (opt.getValueType()) {
				case IOption.BOOLEAN :
					try {
						value = new Boolean(opt.getBooleanValue());
					} catch (BuildException e) {
						// Exception occurs if there's an option value type mismatch
						break;
					}
					getOptionMap().put(name, value);
					break;

				case IOption.ENUMERATED :
					value = createList(opt.getApplicableValues());
					getOptionMap().put(name, value);					
					break;
					
				case IOption.STRING :
					try {
						value = opt.getStringValue();
					} catch (BuildException e) {
						break;
					}
					getOptionMap().put(name, value);
					break;
					
				case IOption.STRING_LIST :
					try {
						value = createList(opt.getStringListValue());
					} catch (BuildException e) {
						break;
					}
					getOptionMap().put(name, value);
					break;
				case IOption.INCLUDE_PATH :
					try {
						value = createList(opt.getIncludePaths());
					} catch (BuildException e) {
						break;
					}
					getOptionMap().put(name, value);
					break;
				case IOption.PREPROCESSOR_SYMBOLS :
					try {
						value = createList(opt.getDefinedSymbols());
					} catch (BuildException e) {
						break;
					}
					getOptionMap().put(name, value);
					break;
				case IOption.LIBRARIES :
					try {
						value = createList(opt.getLibraries());
					} catch (BuildException e) {
						break;
					}
					getOptionMap().put(name, value);
					break;
				default :
					break;
			}
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
	 */
	public String getString(String name) {
		Object s = getOptionMap().get(name);

		if ( s instanceof String )
		{
			return (String)s;
		}
		return getDefaultString(name);
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
	 */
	public boolean isDefault(String name) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		return dirtyFlag;
	}

	public static String[] parseString(String stringList) {
		StringTokenizer tokenizer = new StringTokenizer(stringList, BuildToolsSettingsStore.DEFAULT_SEPERATOR);
		ArrayList list = new ArrayList();
		while (tokenizer.hasMoreElements()) {
			list.add(tokenizer.nextElement());
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * 
	 */
	private void populateOptionMap() {
		// Each configuration has a list of tools
		ITool [] tools = owner.getTools();
		for (int index = 0; index < tools.length; ++index) {
			ITool tool = tools[index];
			IOptionCategory cat = tool.getTopOptionCategory();
			getOptionsForCategory(cat);
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String, java.lang.String)
	 */
	public void putValue(String name, String value) {
		Object oldValue = getOptionMap().get(name);
		if (oldValue == null || !oldValue.equals(value))
		{
			getOptionMap().put(name, value);
			setDirty(true);
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, double)
	 */
	public void setDefault(String name, double value) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, float)
	 */
	public void setDefault(String name, float value) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, int)
	 */
	public void setDefault(String name, int value) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, long)
	 */
	public void setDefault(String name, long value) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, java.lang.String)
	 */
	public void setDefault(String name, String defaultObject) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String, boolean)
	 */
	public void setDefault(String name, boolean value) {
	}

	protected void setDirty( boolean isDirty )
	{
		dirtyFlag = isDirty;
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
	 */
	public void setToDefault(String name) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, double)
	 */
	public void setValue(String name, double value) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, float)
	 */
	public void setValue(String name, float value) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, int)
	 */
	public void setValue(String name, int value) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, long)
	 */
	public void setValue(String name, long value) {
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, java.lang.String)
	 */
	public void setValue(String name, String value) {
		Object oldValue = getString(name);
		if (oldValue == null || !oldValue.equals(value))
		{
			getOptionMap().put(name, value);
			setDirty(true);
			firePropertyChangeEvent(name, oldValue, value);
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferenceStore#setValue(java.lang.String, boolean)
	 */
	public void setValue(String name, boolean value) {
		boolean oldValue = getBoolean(name);
		if (oldValue != value)
		{
			getOptionMap().put(name, new Boolean(value));
			setDirty(true);
			firePropertyChangeEvent(name, new Boolean(oldValue), new Boolean(value));
		}
		
	}

}

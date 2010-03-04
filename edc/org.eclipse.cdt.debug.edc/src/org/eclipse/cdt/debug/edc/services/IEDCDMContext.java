package org.eclipse.cdt.debug.edc.services;

import java.util.Map;

public interface IEDCDMContext {

	/**
	 * Context property names.
	 */

	public static final String PROP_DESCRIPTION = "Description";
	/**
	 * Context property names.
	 */

	public static final String PROP_ID = "ID";
	/**
	 * Context property names.
	 */

	public static final String PROP_NAME = "Name";
	/**
	 * Context property names.
	 */

	public static final String PROP_VALUE = "Value";

	public Object getProperty(String key);

	public Map<String, Object> getProperties();

	public String getName();

	public void setName(String name);

	public void setProperty(String name, Object object);

	public String getID();

}
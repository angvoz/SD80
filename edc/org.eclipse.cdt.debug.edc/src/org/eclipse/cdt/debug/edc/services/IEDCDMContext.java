/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.services;

import java.util.Map;

public interface IEDCDMContext {

	/**
	 * Context property description.
	 */
	public static final String PROP_DESCRIPTION = "Description";

	/**
	 * Context property id.
	 */
	public static final String PROP_ID = "ID";

	/**
	 * Context property name.
	 */
	public static final String PROP_NAME = "Name";

	/**
	 * Context property value.
	 */
	public static final String PROP_VALUE = "Value";

	public Object getProperty(String key);

	public Map<String, Object> getProperties();

	public String getName();

	public void setName(String name);

	public void setProperty(String name, Object object);

	public String getID();

}
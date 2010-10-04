/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.views;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

public interface ISystemVMContainer {

	/**
	 * Context property id.
	 */
	public static final String PROP_ID = "ID";
	/**
	 * Context property name.
	 */
	public static final String PROP_NAME = "Name";
	public static final String PROP_SORT_PROPERTY = "Sort_Property";
	public static final String PROP_SORT_DIRECTION = "Sort_Direction";
	public static final String PROP_COLUMN_KEYS = "Column_Keys";
	public static final String PROP_COLUMN_NAMES = "Column_Names";

	public Map<String, Object> getProperties();

	public List<SystemVMContainer> getChildren();

	public String getName();

	public ISystemVMContainer getParent();

	public Image getImage();

	public int getChildCount();

}
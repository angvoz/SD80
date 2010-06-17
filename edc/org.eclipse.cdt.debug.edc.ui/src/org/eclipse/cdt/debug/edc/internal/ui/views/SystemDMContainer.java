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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SystemDMContainer {

	private final List<SystemDMContainer> children = Collections.synchronizedList(new ArrayList<SystemDMContainer>());
	private SystemDMContainer parent;

	public static final String PROP_NAME = "Name";

	protected Map<String, Object> properties = Collections.synchronizedMap(new HashMap<String, Object>());

	public SystemDMContainer() {
		super();
	}

	public SystemDMContainer(Map<String, Object> props) {
		if (props != null) {
			properties.putAll(props);
		}
	}

	public SystemDMContainer(SystemDMContainer parent, Map<String, Object> props) {
		this(props);
		parent.addChild(this);
		this.setParent(parent);
	}

	public SystemDMContainer(SystemDMContainer parent, String name,
			Map<String, Object> props) {
   		this(parent, props);
   		properties.put(SystemVMContainer.PROP_NAME, name);
  	}

	protected int getChildCount() {
		return children.size();	
	}

	public List<SystemDMContainer> getChildren() {
		return Collections.unmodifiableList(children) ;
	}

	protected void addChild(SystemDMContainer systemDMC) {
		children.add(systemDMC);
	}

	public void setParent(SystemDMContainer parent) {
		this.parent = parent;
	}

	public SystemDMContainer getParent() {
		return parent;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public String getName() {
		String name = (String) getProperties().get(PROP_NAME);
		if (name != null)
			return name;
		return "";
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
}
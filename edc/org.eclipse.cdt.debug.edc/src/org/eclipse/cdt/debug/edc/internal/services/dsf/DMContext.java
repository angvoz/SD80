/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.IDsfService;

public abstract class DMContext extends AbstractDMContext {

	/**
	 * Context property names.
	 */

	public static final String PROP_ID = "ID", PROP_NAME = "Name", PROP_DESCRIPTION = "Description",
			PROP_VALUE = "Value";

	protected Map<String, Object> properties = Collections.synchronizedMap(new HashMap<String, Object>());
	private String id;

	public DMContext(IDsfService service, IDMContext[] parents, String name, String id) {
		super(service, parents);
		properties.put(PROP_NAME, name);
		properties.put(PROP_ID, id);
		this.id = id;
	}

	public DMContext(String sessionId, IDMContext[] parents, String id) {
		super(sessionId, parents);
		properties.put(PROP_NAME, id);
		properties.put(PROP_ID, id);
		this.id = id;
	}

	public DMContext(IDsfService service, IDMContext[] parents, String id, Map<String, Object> props) {
		super(service, parents);
		if (props != null) {
			properties.putAll(props);
			this.id = id;
			properties.put(PROP_ID, id);
		}
	}

	public DMContext(String sessionId, IDMContext[] parents, Map<String, Object> props) {
		super(sessionId, parents);
		if (props != null) {
			properties.putAll(props);
			id = (String) properties.get(PROP_ID);
		}
	}

	public DMContext(IDsfService service, IDMContext[] parents, Map<String, Object> props) {
		super(service, parents);
		if (props != null) {
			properties.putAll(props);
			id = (String) properties.get(PROP_ID);
		}
	}

	public Object getProperty(String key) {
		synchronized (properties) {
			return properties.get(key);
		}
	}

	public Map<String, Object> getProperties() {
		Map<String, Object> result = new HashMap<String, Object>();
		synchronized (properties) {
			result.putAll(properties);
		}
		return result;
	}

	public String getName() {
		return (String) getProperty(PROP_NAME);
	}

	public void setName(String name) {
		synchronized (properties) {
			properties.put(PROP_NAME, name);
		}
	}

	public void setProperty(String name, Object object) {
		synchronized (properties) {
			properties.put(name, object);
		}
	}

	public String getID() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DMContext)
			return super.baseEquals(obj) && this.getID().equals(((DMContext) obj).getID());
		return false;
	}

	@Override
	public int hashCode() {
		return super.baseHashCode() ^ getID().hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DMContext [");
		if (id != null) {
			builder.append("id=");
			builder.append(id);
			builder.append(", ");
		}
		if (properties != null) {
			builder.append("properties=");
			builder.append(properties);
		}
		builder.append("]");
		return builder.toString();
	}

}

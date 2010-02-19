/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.scripting;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.scripting.ScriptingPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCResult;
import org.json.JSONException;
import org.json.JSONObject;

public class RPCBridge extends JSONRPCBridge {
	private static final long serialVersionUID = -1723700628999037991L;
	private static RPCBridge rcpBridge;
	private static Map<String, IConfigurationElement> unregisteredClassMap;

	private RPCBridge() {
		unregisteredClassMap = new HashMap<String, IConfigurationElement>();
	}

	public static RPCBridge instance() {
		if (rcpBridge == null)
			rcpBridge = new RPCBridge();
		return rcpBridge;
	}
	
	@Override
	public JSONRPCResult call(Object[] context, JSONObject jsonReq) {
		ensureRegisteredClass(getClassName(jsonReq));
		return super.call(context, jsonReq);
	}

	private String getClassName(JSONObject jsonReq) {
		try {
			String method = jsonReq.getString("method");
			int dotpos = method.indexOf('.');
			if (dotpos < 0)
				ScriptingPlugin.log("Could not parse class name: " + method, null);
			else
				return method.substring(0, dotpos);
		} catch (JSONException e) {
			ScriptingPlugin.log(MessageFormat.format("Could not get method name from request: {0}", jsonReq), e);
		}
		return null;
	}

	private void ensureRegisteredClass(String name) {
		if (name == null) {
			ScriptingPlugin.log("Could not find class: " + name, null);
			return;
		}
		IConfigurationElement element = unregisteredClassMap.remove(name);
		if (element != null) {
			try {
				Object object = element.createExecutableExtension("class");
				instance().registerClass(name, object.getClass());
			} catch (CoreException e) {
				ScriptingPlugin.log(e.getStatus());
			} catch (Exception e) {
				ScriptingPlugin.log(MessageFormat.format("Could not register class: {0}", name), e);
			}
		}
	}
	
	public void addExtension(IConfigurationElement element) {
		String name = element.getAttribute("name");
		unregisteredClassMap.put(name, element);
	}
}
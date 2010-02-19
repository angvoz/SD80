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

import javax.servlet.http.HttpServletRequest;

import org.jabsorb.JSONRPCBridge;
import org.jabsorb.JSONRPCServlet;

public class RCPServlet extends JSONRPCServlet {

	private static final long serialVersionUID = -6497658838954960846L;

	@Override
	protected JSONRPCBridge findBridge(HttpServletRequest request) {
		return RPCBridge.instance();
	}

}

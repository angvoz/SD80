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
package org.eclipse.cdt.debug.edc.windows;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.ITCFAgentDescriptor;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;

public class Win32AgentDescriptor implements ITCFAgentDescriptor {

	private static final String NAME = "Win32 Debug Agent";

	public Map<String, String> getAttributes() {
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(IPeer.ATTR_NAME, NAME);
		attrs.put(DEBUG_SUPPORT, "Win32 Debug API");
		return attrs;
	}

	public String getName() {
		return NAME;
	}

	public List<String> getServiceNames() {
		List<String> serviceNames = new ArrayList<String>();
		serviceNames.add(IProcesses.NAME);
		serviceNames.add(IRunControl.NAME);
		serviceNames.add(ISimpleRegisters.NAME);
		serviceNames.add(IMemory.NAME);
		return serviceNames;
	}

	public void launch() throws Exception {
		try {
			URL url = FileLocator.find(WindowsDebugger.getDefault().getBundle(), new Path(
					"$os$/EDCWindowsDebugAgent.exe"), null); //$NON-NLS-1$
			if (url != null) {
				url = FileLocator.resolve(url);
				ProcessFactory.getFactory().exec(new String[] { new Path(url.getFile()).toOSString() });
			}
		} catch (IOException e) {
			throw e;
		}
	}

}

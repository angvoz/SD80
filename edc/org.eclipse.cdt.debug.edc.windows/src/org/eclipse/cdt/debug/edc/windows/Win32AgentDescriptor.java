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
package org.eclipse.cdt.debug.edc.windows;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.IEDCConstants;
import org.eclipse.cdt.debug.edc.ITCFAgentLauncher;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;

public class Win32AgentDescriptor implements ITCFAgentLauncher {

	private static final String NAME = "Win32 Debug Agent";
	private static Process agentProcess;

	public Map<String, String> getPeerAttributes() {
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(IPeer.ATTR_NAME, NAME);
		attrs.put(IEDCConstants.PEER_ATTR_DEBUG_SUPPORT, "Win32 Debug API");
		return attrs;
	}

	public String getPeerName() {
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.ITCFAgentLauncher#isLaunchable()
	 */
	public boolean isLaunchable() {
		// TODO: expose and use HostOS from org.eclipse.cdt.debug.edc
		return File.separatorChar == '\\';
	}

	public void launch() throws Exception {
		try {
			URL url = FileLocator.find(WindowsDebugger.getDefault().getBundle(), new Path(
					"$os$/EDCWindowsDebugAgent.exe"), null); //$NON-NLS-1$
			if (url != null) {
				url = FileLocator.resolve(url);
				agentProcess = ProcessFactory.getFactory().exec(new String[] { new Path(url.getFile()).toOSString() });
			}
		} catch (IOException e) {
			throw e;
		}
	}

	public void shutdown() throws Exception {
		if (agentProcess != null) {
			agentProcess.destroy();
		}
	}

}

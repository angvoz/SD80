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
package org.eclipse.cdt.debug.edc.linux.x86;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.debug.edc.IEDCConstants;
import org.eclipse.cdt.debug.edc.ITCFAgentLauncher;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ISimpleRegisters;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IRunControl;

public class GdbserverAgentDescriptor implements ITCFAgentLauncher {

	private static final String NAME = "Gdbserver TCF Agent";

	private Process agentProcess;
	
	public Map<String, String> getPeerAttributes() {
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put(IPeer.ATTR_NAME, NAME);
		attrs.put(IEDCConstants.PEER_ATTR_DEBUG_SUPPORT, "GDB Remote Protocol");
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

	public boolean isLaunchable() {
		// this runs on a remote device only, so we can support it on any host
		return true;
	}
	
	public void launch() throws Exception {
		IPath path = null;

		try {
			URL url = FileLocator.find(LinuxDebugger.getDefault().getBundle(),
					new Path("$os$/GdbserverAgent.jar"), null); //$NON-NLS-1$
			if (url != null) {
				url = FileLocator.resolve(url);
				path = new Path(url.getPath());
			}
		} catch (IOException e) {
			throw e;
		}

		if (path == null)
			return;
			
		if (!path.toFile().exists())
			throw new Exception("GdbserverAgent.jar file does not exist at " + path.toOSString());

		final CommandLauncher launcher = new CommandLauncher();
		String[] cmdarray = new String[] { "-jar", path.toOSString() };

		agentProcess = launcher.execute(new Path("java"), cmdarray, null, null, new NullProgressMonitor());
	}

	public void shutdown() throws Exception {
		if (agentProcess != null)
			agentProcess.destroy();
	}
}

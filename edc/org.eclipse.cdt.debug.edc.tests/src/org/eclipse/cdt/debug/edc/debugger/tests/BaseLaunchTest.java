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
package org.eclipse.cdt.debug.edc.debugger.tests;

import java.util.HashSet;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.tests.AbstractLaunchTest;
import org.eclipse.cdt.debug.edc.tests.EDCTestPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

public abstract class BaseLaunchTest extends AbstractLaunchTest {

	private static final String CDT_LOCAL_LAUNCH_TYPE = ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP;
	private static final String EDC_LAUNCH_DELEGATE_LINUX = "org.eclipse.cdt.debug.edc.linux.x86.launchDelegate";
	private static final String EDC_LAUNCH_DELEGATE_WINDOWS = "org.eclipse.cdt.debug.edc.windows.localLaunchDelegate";

	protected EDCLaunch createLaunch() throws Exception {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationWorkingCopy configuration = lm.getLaunchConfigurationType(CDT_LOCAL_LAUNCH_TYPE)
				.newInstance(null, "EDCTestLaunch");

		String exePath = getTestExecutable();
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, exePath);

		String launchDelegateID = EDC_LAUNCH_DELEGATE_LINUX;
		if (Platform.getOS().equals(Platform.OS_WIN32))
			launchDelegateID = EDC_LAUNCH_DELEGATE_WINDOWS;

		HashSet<String> set = new HashSet<String>();
		set.add(ILaunchManager.DEBUG_MODE);
		configuration.setPreferredLaunchDelegate(set, launchDelegateID);

		return (EDCLaunch) configuration.doSave().launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor(), true);
	}

	protected String getTestExecutable() {
		/*
		 * String exePath = System.getenv("EXEPATH"); if (exePath == null ||
		 * !new File(exePath).exists()) throw new
		 * Exception("EXEPATH env var not set to valid executable to launch!");
		 */
		String res_folder = null;
		try {
			res_folder = EDCTestPlugin.projectRelativePath("resources/SymbolFiles");
		} catch (Exception e) {
			fail("Folder resources/SymbolFiles is missing in the test project.");
		}
		IPath dataPath = new Path(res_folder);
		if (Platform.getOS().equals(Platform.OS_WIN32))
			dataPath = dataPath.append("BlackFlagMinGW.exe");
		else
			dataPath = dataPath.append("BlackFlag_linuxgcc.exe");

		return dataPath.toOSString();
	}

}
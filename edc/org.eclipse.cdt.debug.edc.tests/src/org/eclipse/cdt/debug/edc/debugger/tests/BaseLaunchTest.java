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

/**
 * Test based on live debug session for an x86 application on local host.
 * It runs with EDC Windows debugger or EDC Linux debugger.
 */
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

		// do more configuration.
		configureLaunchConfiguration(configuration);
		
		String launchDelegateID = EDC_LAUNCH_DELEGATE_LINUX;
		if (Platform.getOS().equals(Platform.OS_WIN32))
			launchDelegateID = EDC_LAUNCH_DELEGATE_WINDOWS;

		HashSet<String> set = new HashSet<String>();
		set.add(ILaunchManager.DEBUG_MODE);
		configuration.setPreferredLaunchDelegate(set, launchDelegateID);

		return (EDCLaunch) configuration.doSave().launch(ILaunchManager.DEBUG_MODE, new NullProgressMonitor(), true);
	}

	/**
	 * Set more settings for the launch configuration so that subclass can do whatever it likes.
	 * 
	 * @param configuration
	 */
	protected void configureLaunchConfiguration(
			ILaunchConfigurationWorkingCopy configuration) {
		// default do nothing.
	}

	/**
	 * Get full path of executable file in {plugin}/resources/SymbolFiles folder.
	 * 
	 * @return a full path
	 */
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
		
		String exeFileName = getExeFileName();
		dataPath = dataPath.append(exeFileName);

		return dataPath.toOSString();
	}

	/**
	 * Get executable file name without path. The file is supposed to be in 
	 * {plugin}/resources/SymbolFiles folder.
	 */
	protected String getExeFileName() {
		
		String ret = null;
		if (Platform.getOS().equals(Platform.OS_WIN32))
			// This is an executable with hard-coded breakpoint (a divide-by-zero statement)
			// so that it will suspend by itself after launch.
			ret = "BlackFlagMinGW.exe";
		else
			ret = "BlackFlag_linuxgcc.exe";
		
		return ret;
	}

}
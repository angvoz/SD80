/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.runner;

import org.eclipse.cdt.launch.ui.CArgumentsTab;
import org.eclipse.cdt.launch.ui.CDebuggerTab;
import org.eclipse.cdt.launch.ui.CEnvironmentTab;
// New Tab !! import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class CppUnitLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)  {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
			new CppUnitMainTab(),
			new CArgumentsTab(),
			new CEnvironmentTab(),
			new CDebuggerTab(),
			new CommonTab() 
		};
		setTabs(tabs);
	}
}

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
package org.eclipse.cdt.debug.edc.internal.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

public class RegularDebuggerTab extends AbstractDebuggerTab {
	private static final String TAB_ID = "org.eclipse.cdt.debug.edc.ui.launch.regularDebuggerTab";
	private static final String HELP_ID = "org.eclipse.cdt.debug.edc.ui.launch_regularDebuggerTab";

	/*
	 * When the launch configuration is created for Run mode,
	 * this Debugger tab is not created because it is not used
	 * for Run mode but only for Debug mode.
	 * When we then open the same configuration in Debug mode, the launch
	 * configuration already exists and initializeFrom() is called
	 * instead of setDefaults().
	 * We therefore call setDefaults() ourselves.
	 * Bug 281970
	 */
	private boolean fSetDefaultCalled;
	
	public RegularDebuggerTab() {
		super(false);
	}

	@Override
	protected String getHelpID() {
		return HELP_ID;
	}

	@Override
	protected String getTabID() {
		return TAB_ID;
	}
	
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
    	fSetDefaultCalled = true;
    	
    	super.setDefaults(config);
    }
    
    @Override
    public void initializeFrom(ILaunchConfiguration config) {
		if (fSetDefaultCalled == false) {
			try {
				ILaunchConfigurationWorkingCopy wc;
				wc = config.getWorkingCopy();
				setDefaults(wc);
				wc.doSave();
			} catch (CoreException e) {
			}
		}

		super.initializeFrom(config);
    }
}

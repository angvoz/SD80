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
package org.eclipse.cdt.debug.edc.windows.launch;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.launch.EDCLaunchDelegate;
import org.eclipse.cdt.debug.edc.windows.WindowsDebugger;
import org.eclipse.cdt.debug.edc.x86.DebugServicesFactoryX86;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;

public class WindowsLaunchDelegate extends EDCLaunchDelegate {

	public final static String WINDOWS_DEBUG_MODEL_ID = "org.eclipse.cdt.debug.edc.windows"; //$NON-NLS-1$

	@Override
	public String getDebugModelID() {
		return WINDOWS_DEBUG_MODEL_ID;
	}

	@Override
	protected Sequence getLiveLaunchSequence(DsfExecutor executor, EDCLaunch launch, IProgressMonitor pm) {
		return new WindowsFinalLaunchSequence(executor, launch, pm);
	}

	@Override
	protected IDsfDebugServicesFactory newServiceFactory() {
		return new DebugServicesFactoryX86();
	}

	@Override
	protected String getPluginID() {
		return WindowsDebugger.getUniqueIdentifier();
	}

	@Override
	protected boolean isSameTarget(EDCLaunch existingLaunch,
			ILaunchConfiguration configuration, String mode) {
		try {
			ILaunchConfiguration existingConfiguration = existingLaunch.getLaunchConfiguration();
			if (existingConfiguration != null)
			{
				ILaunchConfigurationType existingType = existingConfiguration.getType();
				ILaunchConfigurationType newType = configuration.getType();
				if (existingType.equals(newType))
				{
					Set<String> modes = new HashSet<String>();
					modes.add(mode);
					ILaunchDelegate existingDelegate = existingConfiguration.getPreferredDelegate(modes);
					ILaunchDelegate delegate = configuration.getPreferredDelegate(modes);
					if (existingDelegate.equals(delegate))
					{
						return true;
					}
				}
			}
		} catch (CoreException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
		return false;
	}

}

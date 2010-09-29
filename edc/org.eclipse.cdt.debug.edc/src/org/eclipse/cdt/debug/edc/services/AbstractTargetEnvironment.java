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

package org.eclipse.cdt.debug.edc.services;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Common base for ITargetEnvironment services.
 * 
 */
public abstract class AbstractTargetEnvironment extends AbstractEDCService implements ITargetEnvironment {
	private ILaunch launch;

	/**
	 * @param session
	 * @param classNames
	 *            the type names the service will be registered under. See
	 *            AbstractDsfService#register for details. We tack on
	 *            ITargetEnvironment if not provided.
	 * 
	 * @param launch
	 *            must be non-null
	 */
	public AbstractTargetEnvironment(DsfSession session, String[] classNames, ILaunch launch) {
		super(session,
				massageClassNames(classNames, new String[] {ITargetEnvironment.class.getName()}));
		assert launch != null;
		this.launch = launch;
	}

	public ILaunchConfiguration getLaunchConfiguration() {
		return launch.getLaunchConfiguration();
	}

	public ILaunch getLaunch() {
		return launch;
	}

	/*
	 * This implementation works for most CDT debug sessions.<br> If your
	 * debugger is using different preference UI, please override this method.
	 */
	public String getStartupStopAtPoint() {
		String ret = null;
		try {
			if (getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN,
					false)) {
				ret = getLaunchConfiguration().getAttribute(
						ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL,
						ICDTLaunchConfigurationConstants.DEBUGGER_STOP_AT_MAIN_SYMBOL_DEFAULT);
			}
		} catch (CoreException e) {
			// ignore
		}

		return ret;
	}

	/**
	 * @since 2.0
	 */
	public boolean needStartupBreakpointInExecutable(String exeName) {
		// By default EDC will try to install startup breakpoint in
		// any loaded module until it succeeds.
		return true;
	}
}

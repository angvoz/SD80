package org.eclipse.cdt.debug.edc.windows.launch;

import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

/**
 * @since 2.0
 */
public class WindowsLaunch extends EDCLaunch {

	public WindowsLaunch(ILaunchConfiguration launchConfiguration, String mode,
			ISourceLocator locator, String ownerID) {
		super(launchConfiguration, mode, locator, ownerID);
	}

}

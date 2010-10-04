package org.eclipse.cdt.debug.edc.linux.x86.launch;

import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

public class LinuxLaunch extends EDCLaunch {

	public LinuxLaunch(ILaunchConfiguration launchConfiguration, String mode,
			ISourceLocator locator, String ownerID) {
		super(launchConfiguration, mode, locator, ownerID);
	}

}

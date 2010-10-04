package org.eclipse.cdt.debug.edc.internal.launch;

import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ISourceLocator;

public class SnapshotLaunch extends EDCLaunch {

	public SnapshotLaunch(ILaunchConfiguration launchConfiguration, String mode,
			ISourceLocator locator, String ownerID) {
		super(launchConfiguration, mode, locator, ownerID);
	}

}

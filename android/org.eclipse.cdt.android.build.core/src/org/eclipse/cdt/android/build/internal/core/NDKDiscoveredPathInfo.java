package org.eclipse.cdt.android.build.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class NDKDiscoveredPathInfo implements IDiscoveredPathInfo {

	private final IProject project;
	
	public NDKDiscoveredPathInfo(IProject project) {
		this.project = project;
	}
	
	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public IPath[] getIncludePaths() {
		return new IPath[] { new Path("C:\\Android") };
	}

	@Override
	public Map<String, String> getSymbols() {
		return new HashMap<String, String>();
	}

	@Override
	public IDiscoveredScannerInfoSerializable getSerializable() {
		return null;
	}

	public void update(IProgressMonitor monitor) {
		// Only check if Android.mk is newer
		// Run ndk-build -nB to get the list of commands
		// Run the unique commands with special gcc options to extract the symbols and paths
	}

	public void delete() {
		// Force a rebuild next time
	}
	
}

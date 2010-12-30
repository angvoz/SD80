package org.eclipse.cdt.android.build.internal.core.discovery;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class NDKDiscoveredPathInfo implements IDiscoveredPathInfo {

	private final IProject project;
	private long lastUpdate = IFile.NULL_STAMP;
	
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

	public void update(IProgressMonitor monitor) throws CoreException {
		if (!needsUpdating())
			return;
		
		new NDKDiscoveryUpdater(this).runUpdate(monitor);
		
		recordUpdate();
	}

	private boolean needsUpdating() {
		if (lastUpdate == IFile.NULL_STAMP)
			return true;
		return project.getFile(new Path("jni/Android.mk")).getLocalTimeStamp() > lastUpdate;
	}
	
	private void recordUpdate() {
		lastUpdate = project.getFile(new Path("jni/Android.mk")).getLocalTimeStamp();
	}
	
	public void delete() {
		lastUpdate = IFile.NULL_STAMP;
	}
	
}

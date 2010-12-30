package org.eclipse.cdt.android.build.internal.core.discovery;

import java.util.HashMap;
import java.util.List;
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
	private IPath[] includePaths;
	private Map<String, String> symbols;
	
	public NDKDiscoveredPathInfo(IProject project) {
		this.project = project;
	}
	
	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public IPath[] getIncludePaths() {
		return includePaths;
	}

	void setIncludePaths(List<String> pathStrings) {
		includePaths = new IPath[pathStrings.size()];
		int i = 0;
		for (String path : pathStrings)
			includePaths[i++] = new Path(path);
	}
	
	@Override
	public Map<String, String> getSymbols() {
		if (symbols == null)
			symbols = new HashMap<String, String>();
		return symbols;
	}

	void setSymbols(Map<String, String> symbols) {
		this.symbols = symbols;
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

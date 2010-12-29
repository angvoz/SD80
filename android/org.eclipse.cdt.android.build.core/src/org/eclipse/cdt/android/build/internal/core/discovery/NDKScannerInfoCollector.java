package org.eclipse.cdt.android.build.internal.core.discovery;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


public class NDKScannerInfoCollector implements IScannerInfoCollector3, IScannerInfoCollectorCleaner, IManagedScannerInfoCollector {

	private NDKDiscoveredPathInfo pathInfo;
	
	@Override
	public void contributeToScannerConfig(Object resource, @SuppressWarnings("rawtypes") Map scannerInfo) {
		throw new Error("Not implemented");
	}

	@Override
	public @SuppressWarnings("rawtypes") List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
		throw new Error("Not implemented");
	}

	@Override
	public void setProject(IProject project) {
		throw new Error("Not implemented");
	}

	@Override
	public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
		pathInfo.update(monitor);
	}

	@Override
	public IDiscoveredPathInfo createPathInfoObject() {
		return pathInfo;
	}

	@Override
	public Map<String, String> getDefinedSymbols() {
		throw new Error("Not implemented");
	}
	
	@Override
	public @SuppressWarnings("rawtypes") List getIncludePaths() {
		throw new Error("Not implemented");
	}

	@Override
	public void setInfoContext(InfoContext context) {
		pathInfo = new NDKDiscoveredPathInfo(context.getProject());
	}

	@Override
	public void deleteAllPaths(IResource resource) {
		throw new Error("Not implemented");
	}

	@Override
	public void deleteAllSymbols(IResource resource) {
		throw new Error("Not implemented");
	}

	@Override
	public void deletePath(IResource resource, String path) {
		throw new Error("Not implemented");
	}

	@Override
	public void deleteSymbol(IResource resource, String symbol) {
		throw new Error("Not implemented");
	}

	@Override
	public void deleteAll(IResource resource) {
		pathInfo.delete();
	}

}

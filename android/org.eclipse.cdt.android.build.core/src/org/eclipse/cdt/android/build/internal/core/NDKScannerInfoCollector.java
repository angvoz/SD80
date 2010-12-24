package org.eclipse.cdt.android.build.internal.core;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Element;


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
		// The general strategy is to create the discovered path info only if
		// the Android.mk file is newer than the last time we looked
		// To update run ndk-build -nvh (or something) to fake the build and extract the commands
		// Then run them through a specs parser thing
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

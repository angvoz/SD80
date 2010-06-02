/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM - Initial API and implementation
 *  Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.io.OutputStream;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeBuilderUtil;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * A factory that creates a ConsoleOutputStreamSniffer,
 * ScannerInfoConsoleParser and optionally a ScannerInfoConsoleParserUtility.
 * 
 * @author vhirsl
 */
public class ScannerInfoConsoleParserFactory {

    /**
     * Creates a ConsoleOutputStreamSniffer, make builder scanner info console parser
     * and a utility.
     */
    public static ConsoleOutputSniffer getESIProviderOutputSniffer(
                                            OutputStream outputStream,
                                            OutputStream errorStream,
                                            IProject currentProject,
                                            String providerId,
                                            IScannerConfigBuilderInfo2 scBuildInfo,
                                            IScannerInfoCollector collector,
                                            IMarkerGenerator markerGenerator) {
    	return getESIProviderOutputSniffer(outputStream, errorStream, currentProject, new InfoContext(currentProject), providerId, scBuildInfo, collector, markerGenerator);
    }
    /**
     * Creates a ConsoleOutputStreamSniffer, make builder scanner info console parser
     * and a utility.
     */
    public static ConsoleOutputSniffer getESIProviderOutputSniffer(
                                            OutputStream outputStream,
                                            OutputStream errorStream,
                                            IProject currentProject,
                                            InfoContext context,
                                            String providerId,
                                            IScannerConfigBuilderInfo2 scBuildInfo,
                                            IScannerInfoCollector collector,
                                            IMarkerGenerator markerGenerator) {
        if (scBuildInfo.isProviderOutputParserEnabled(providerId)) {
            // get the ESIProvider console parser 
            SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
                    getSCProfileInstance(currentProject, context, scBuildInfo.getSelectedProfileId());
            IScannerInfoConsoleParser clParser = profileInstance.createExternalScannerInfoParser(providerId);
            IPath buildDirectory = MakeBuilderUtil.getBuildDirectory(currentProject, MakeBuilder.BUILDER_ID);
            clParser.startup(currentProject, buildDirectory, collector, markerGenerator);
            // create an output stream sniffer
            return new ConsoleOutputSniffer(outputStream, errorStream, new 
                IScannerInfoConsoleParser[] {clParser});
        }
        return null;
    }

	/**
	 * Creates a ConsoleOutputStreamSniffer, ESI provider scanner info console parser
	 * and a utility.
	 */
	public static ConsoleOutputSniffer getMakeBuilderOutputSniffer(
											OutputStream outputStream,
											OutputStream errorStream,
											IProject currentProject,
											IPath workingDirectory,
                                            IScannerConfigBuilderInfo2 scBuildInfo,
											IMarkerGenerator markerGenerator,
                                            IScannerInfoCollector collector) {
		return getMakeBuilderOutputSniffer(outputStream, errorStream, currentProject, new InfoContext(currentProject), workingDirectory, scBuildInfo, markerGenerator, collector);
	}

	/**
	 * Creates a ConsoleOutputStreamSniffer, ESI provider scanner info console parser
	 * and a utility.
	 */
	public static ConsoleOutputSniffer getMakeBuilderOutputSniffer(
											OutputStream outputStream,
											OutputStream errorStream,
											IProject currentProject,
											InfoContext context,
											IPath workingDirectory,
                                            IScannerConfigBuilderInfo2 scBuildInfo,
											IMarkerGenerator markerGenerator,
                                            IScannerInfoCollector collector) {
//		try {
			// get the SC builder settings
			/*if (currentProject.hasNature(ScannerConfigNature.NATURE_ID))*/ {
				if (scBuildInfo == null) {
    				try {
    					IScannerConfigBuilderInfo2Set container = ScannerConfigProfileManager.
						createScannerConfigBuildInfo2Set(currentProject);
    					scBuildInfo = container.getInfo(context);
    				}
    				catch (CoreException e) {
    					// builder not installed or disabled
    				}
                }
				if (scBuildInfo != null && 
						scBuildInfo.isAutoDiscoveryEnabled() &&
						scBuildInfo.isBuildOutputParserEnabled()) {
					// get the make builder console parser 
					SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
							getSCProfileInstance(currentProject, context, scBuildInfo.getSelectedProfileId());
					IScannerInfoConsoleParser clParser = profileInstance.createBuildOutputParser();
                    if (collector == null) {
                        collector = profileInstance.getScannerInfoCollector();
                    }
                    if(clParser != null){
						clParser.startup(currentProject, workingDirectory, collector,
	                            scBuildInfo.isProblemReportingEnabled() ? markerGenerator : null);
						// create an output stream sniffer
						return new ConsoleOutputSniffer(outputStream, errorStream, new 
							IScannerInfoConsoleParser[] {clParser});
                    }
				}
			}
//		} 
//		catch (CoreException e) {
//			MakeCorePlugin.log(e.getStatus());
//		}
		return null;
	}
}

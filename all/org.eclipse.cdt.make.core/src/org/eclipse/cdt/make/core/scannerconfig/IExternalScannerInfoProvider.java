/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.Properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for providers of C/C++ scanner info 
 * 
 * @author vhirsl
 */
public interface IExternalScannerInfoProvider {
    /**
     * Invokes a provider to generate scanner info.
     * 
     * @param monitor
     * @param resource project - current project being built
     * @param providerId - id of the provider 
     * @param buildInfo - settings for ScannerConfigBuilder
     * @param collector - scanner info collector for the resource (project)
     */
    public boolean invokeProvider(IProgressMonitor monitor, 
                                  IResource resource,
                                  String providerId,
                                  IScannerConfigBuilderInfo2 buildInfo,
                                  IScannerInfoCollector collector); 

    /**
     * Alternative interface to pass down the environment.
     * 
     * @param monitor
     * @param resource
     * @param providerId
     * @param buildInfo
     * @param collector
     * @param env
     * @return
     */
    public boolean invokeProvider(IProgressMonitor monitor, 
            IResource resource,
            InfoContext context,
            String providerId,
            IScannerConfigBuilderInfo2 buildInfo,
            IScannerInfoCollector collector,
            Properties env); 
 
}

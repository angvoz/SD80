/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CCommandDSC;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Per file scanner info collector
 * 
 * @author vhirsl
 */
public class PerFileSICollector implements IScannerInfoCollector2 {
    private IProject project;
    
    private Map commandIdToFilesMap; // command id and list of files it applies to
    private Map fileToCommandIdsMap; // maps each file to the list of corresponding command ids
    private Map commandIdCommandMap; // map of all commands

    private int commandIdCounter = 0;
    /**
     * 
     */
    public PerFileSICollector() {
        commandIdToFilesMap = new HashMap(); // [commandId, List of files]
        fileToCommandIdsMap = new HashMap(); // [file, List of commands]
        commandIdCommandMap = new LinkedHashMap(); // [commandId, command]
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#setProject(org.eclipse.core.resources.IProject)
     */
    public void setProject(IProject project) {
        this.project = project;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(java.lang.Object, java.util.Map)
     */
    public synchronized void contributeToScannerConfig(Object resource, Map scannerInfo) {
        // check the resource
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        }
        else if (resource instanceof Integer) {
            addScannerInfo(((Integer)resource), scannerInfo);
            return;
        }
        else if (!(resource instanceof IFile)) {
            errorMessage = "resource is not an IFile";//$NON-NLS-1$
        }
        else if (((IFile) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (((IFile) resource).getProject() != project) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        if (errorMessage != null) {
            TraceUtil.outputError("PerFileSICollector.contributeToScannerConfig : ", errorMessage); //$NON-NLS-1$
            return;
        }
        IFile file = (IFile) resource;
       
        for (Iterator i = scannerInfo.keySet().iterator(); i.hasNext(); ) {
            ScannerInfoTypes type = (ScannerInfoTypes) i.next();
            if (type.equals(ScannerInfoTypes.COMPILER_COMMAND)) {
                addCompilerCommands(file, (List) scannerInfo.get(type));
            }
            else {
                addScannerInfo(type, (List) scannerInfo.get(type));
            }
        }
    }

    /**
     * @param commandId
     * @param scannerInfo
     */
    private void addScannerInfo(Integer commandId, Map scannerInfo) {
        CCommandDSC cmd = (CCommandDSC) commandIdCommandMap.get(commandId);
        if (cmd != null) {
            List symbols = (List) scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
            List includes = (List) scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
            cmd.setSymbols(symbols);
            cmd.setIncludes(includes);
            cmd.setDiscovered(true);
        }
    }

    /**
     * @param file 
     * @param object
     */
    private void addCompilerCommands(IFile file, List commandList) {
        if (commandList != null) {
            List existingCommands = new ArrayList(commandIdCommandMap.values());
            for (Iterator i = commandList.iterator(); i.hasNext(); ) {
                CCommandDSC cmd = (CCommandDSC) i.next();
                int index = existingCommands.indexOf(cmd);
                if (index != -1) {
                    cmd = (CCommandDSC) existingCommands.get(index);
                }
                else {
                    cmd.setCommandId(++commandIdCounter);
                    commandIdCommandMap.put(cmd.getCommandIdAsInteger(), cmd);
                }
                Integer commandId = cmd.getCommandIdAsInteger();
                // update commandIdToFilesMap
                Set fileSet = (Set) commandIdToFilesMap.get(commandId);
                if (fileSet == null) {
                    fileSet = new HashSet();
                    commandIdToFilesMap.put(commandId, fileSet);
                }
                fileSet.add(file);
                // update fileToCommandIdsMap
                List commandIds = (List) fileToCommandIdsMap.get(file);
                if (commandIds == null) {
                    commandIds = new ArrayList();
                    fileToCommandIdsMap.put(file, commandIds);
                }
                if (!commandIds.contains(commandId)) {
                    commandIds.add(commandId);
                }
            }
        }
    }

    /**
     * @param type
     * @param object
     */
    private void addScannerInfo(ScannerInfoTypes type, List delta) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#updateScannerConfiguration(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
        List rv = null;
        // check the resource
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        } 
        else if (!(resource instanceof IResource)) {
            errorMessage = "resource is not an IResource";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() != project) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        
        if (errorMessage != null) {
            TraceUtil.outputError("PerProjectSICollector.getCollectedScannerInfo : ", errorMessage); //$NON-NLS-1$
        }
        else if (project.equals(((IResource)resource).getProject())) {
            if (type.equals(ScannerInfoTypes.COMPILER_COMMAND)) {
                rv = new ArrayList(commandIdCommandMap.values());
            }
        }
        return rv;
    }

}

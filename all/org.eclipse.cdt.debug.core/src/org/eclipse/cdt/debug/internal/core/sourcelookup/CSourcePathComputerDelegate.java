/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Nokia - Added support for AbsoluteSourceContainer( 159833 ) 
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.sourcelookup; 

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
 
/**
 * Computes the default source lookup path for a launch configuration.
 */
public class CSourcePathComputerDelegate implements ISourcePathComputerDelegate {

	/** 
	 * Constructor for CSourcePathComputerDelegate. 
	 */
	public CSourcePathComputerDelegate() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ISourceContainer[] computeSourceContainers( ILaunchConfiguration configuration, IProgressMonitor monitor ) throws CoreException {
		ISourceContainer[] common = CDebugCorePlugin.getDefault().getCommonSourceLookupDirector().getSourceContainers();
		ArrayList containers = new ArrayList( common.length + 1 );
		for ( int i = 0; i < common.length; ++i ) {
			ISourceContainer sc = common[i];
			if ( sc.getType().getId().equals( MappingSourceContainer.TYPE_ID ) )
				sc = ((MappingSourceContainer)sc).copy();
			containers.add( sc );
		}
		String projectName = configuration.getAttribute( ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null );
		if (projectName != null && projectName.length() > 0) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( projectName );
			if ( project.exists() ) {
				containers.add( 0, new ProjectSourceContainer( project, true ) );
			}
		}
		containers.add( 0, new AbsolutePathSourceContainer() );
		return (ISourceContainer[])containers.toArray( new ISourceContainer[containers.size()] );
	}
}

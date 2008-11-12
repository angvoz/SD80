/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
 
/**
 * C/C++ source lookup director.
 */
public class CSourceLookupDirector extends AbstractSourceLookupDirector {

	private static Set<String> fSupportedTypes;

	static {
		fSupportedTypes = new HashSet<String>();
		fSupportedTypes.add( WorkspaceSourceContainer.TYPE_ID );
		fSupportedTypes.add( ProjectSourceContainer.TYPE_ID );
		fSupportedTypes.add( FolderSourceContainer.TYPE_ID );
		fSupportedTypes.add( DirectorySourceContainer.TYPE_ID );
		fSupportedTypes.add( MappingSourceContainer.TYPE_ID );
		fSupportedTypes.add( AbsolutePathSourceContainer.TYPE_ID );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#initializeParticipants()
	 */
	public void initializeParticipants() {
		addParticipants( new ISourceLookupParticipant[]{ new CSourceLookupParticipant() } );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.sourcelookup.ISourceLookupDirector#supportsSourceContainerType(org.eclipse.debug.core.sourcelookup.ISourceContainerType)
	 */
	public boolean supportsSourceContainerType( ISourceContainerType type ) {
		return fSupportedTypes.contains( type.getId() );
	}

	public boolean contains( String source ) {
		ISourceContainer[] containers = getSourceContainers();
		for ( int i = 0; i < containers.length; ++i ) {
			if ( contains( containers[i], source ) )
				return true;
		}

		return false;
	}

	public boolean contains( ICBreakpoint breakpoint ) {
		try {
			String handle = breakpoint.getSourceHandle();
			ISourceContainer[] containers = getSourceContainers();
			for ( int i = 0; i < containers.length; ++i ) {
				if ( contains( containers[i], handle ) )
					return true;
			}
		}
		catch( CoreException e ) {
		}
		return false;
	}

	public boolean contains( IProject project ) {
		ISourceContainer[] containers = getSourceContainers();
		for ( int i = 0; i < containers.length; ++i ) {
			if ( contains( containers[i], project ) )
				return true;
		}
		return false;
	}

	private boolean contains( ISourceContainer container, IProject project ) {
		if ( container instanceof ProjectSourceContainer && ((ProjectSourceContainer)container).getProject().equals( project ) ) {
			return true;
		}
		try {
			ISourceContainer[] containers;
			containers = container.getSourceContainers();
			for ( int i = 0; i < containers.length; ++i ) {
				if ( contains( containers[i], project ) )
					return true;
			}
		}
		catch( CoreException e ) {
		}
		return false;
	}

	private boolean contains( ISourceContainer container, String sourceName ) {
		IPath path = new Path( sourceName );
		if ( !path.isValidPath( sourceName ) )
			return false;
		if ( container instanceof ProjectSourceContainer ) {
			IProject project = ((ProjectSourceContainer)container).getProject();
			IPath projPath = project.getLocation();
			if ( projPath != null && projPath.isPrefixOf( path ) ) {
				IFile file = ((ProjectSourceContainer)container).getProject().getFile( path.removeFirstSegments( projPath.segmentCount() ) );
				return ( file != null && file.exists() );
			}
		}
		if ( container instanceof FolderSourceContainer ) {
			IContainer folder = ((FolderSourceContainer)container).getContainer();
			IPath folderPath = folder.getLocation();
			if ( folderPath != null && folderPath.isPrefixOf( path ) ) {
				IFile file = ((FolderSourceContainer)container).getContainer().getFile( path.removeFirstSegments( folderPath.segmentCount() ) );
				return ( file != null && file.exists() );
			}
		}
		if ( container instanceof DirectorySourceContainer ) {
			File dir = ((DirectorySourceContainer)container).getDirectory();
			boolean searchSubfolders = ((DirectorySourceContainer)container).isComposite();
			IPath dirPath = new Path( dir.getAbsolutePath() );
			if ( searchSubfolders || dirPath.segmentCount() + 1 == path.segmentCount() )
				return dirPath.isPrefixOf( path );
		}
		if ( container instanceof MappingSourceContainer ) {
			return ( ((MappingSourceContainer)container).getCompilationPath( sourceName ) != null ); 
		}
		if ( container instanceof AbsolutePathSourceContainer ) {
			return ( ((AbsolutePathSourceContainer)container).isValidAbsoluteFilePath( sourceName ) ); 
		}
		try {
			ISourceContainer[] containers;
			containers = container.getSourceContainers();
			for ( int i = 0; i < containers.length; ++i ) {
				if ( contains( containers[i], sourceName ) )
					return true;
			}
		}
		catch( CoreException e ) {
		}
		return false;
	}

	public IPath getCompilationPath( String sourceName ) {
		IPath path = null;
		ISourceContainer[] containers = getSourceContainers();
		for ( int i = 0; i < containers.length; ++i ) {
			IPath cp = getCompilationPath( containers[i], sourceName );
			if ( cp != null ) {
				path = cp;
				break;
			}
		}
		return path;
	}

	private IPath getCompilationPath( ISourceContainer container, String sourceName ) {
		IPath path = null;
		if ( container instanceof MappingSourceContainer ) {
			path = ((MappingSourceContainer)container).getCompilationPath( sourceName );
		}
		else {
			try {
				ISourceContainer[] containers;
				containers = container.getSourceContainers();
				for ( int i = 0; i < containers.length; ++i ) {
					path = getCompilationPath( containers[i], sourceName );
					if ( path != null )
						break;
				}
			}
			catch( CoreException e ) {
			}
		}
		return path;
	}
}

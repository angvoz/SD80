/*******************************************************************************
 * Copyright (c) 2002, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.ParserLogService;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author jcamelon
 *
 */
public class ParserUtil
{
	
	public static IParserLogService getParserLogService()
	{
		return parserLogService;
	}
		
	private static IParserLogService parserLogService = new ParserLogService(IDebugLogConstants.PARSER );
	private static IParserLogService scannerLogService = new ParserLogService(IDebugLogConstants.SCANNER );

	/**
	 * @return
	 */
	public static IParserLogService getScannerLogService() {
		return scannerLogService;
	}
	
	public static char [] findWorkingCopyBuffer( String path, Iterator workingCopies )
	{
		IResource resultingResource = getResourceForFilename(path);
			
		if( resultingResource != null && resultingResource.getType() == IResource.FILE )
		{
			// this is the file for sure
			// check the working copy
			if( workingCopies.hasNext() )
				return findWorkingCopy( resultingResource, workingCopies );
		}
		return null;
	}
	
	public static CodeReader createReader( String finalPath, Iterator workingCopies )
	{
		// check to see if the file which this path points to points to an 
		// IResource in the workspace
		try
		{
			IResource resultingResource = getResourceForFilename(finalPath);
			
			if( resultingResource != null && resultingResource.getType() == IResource.FILE )
			{
				// this is the file for sure
				// check the working copy
				if( workingCopies.hasNext() )
				{
					char[] buffer = findWorkingCopy( resultingResource, workingCopies );
					if( buffer != null )
						return new CodeReader(finalPath, buffer); 
				}
				InputStream in = null;
				try
				{
					in = ((IFile)resultingResource).getContents();
					return new CodeReader(finalPath, ((IFile)resultingResource).getCharset(), in);
				} finally {
					if (in != null)
					{
						in.close();
					}
				}
			}
		}
		catch( CoreException ce )
		{
		}
		catch( IOException e )
		{
		}
		catch( IllegalStateException e ) 
		{
		}
		return InternalParserUtil.createFileReader(finalPath);
	}

	/**
	 * @param finalPath
	 * @return
	 */
	public static IResource getResourceForFilename(String finalPath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if( workspace == null )
			return null;
		IPath path = new Path( finalPath );
		
		IWorkspaceRoot root = workspace.getRoot();
        if( root.getLocation().isPrefixOf( path ) )
			path = path.removeFirstSegments(root.getLocation().segmentCount() );

        try
        {
    		IFile resultingResource = root.getFile(path);
    		if( resultingResource != null && resultingResource.exists() ) 
    		    return resultingResource;
            resultingResource = root.getFileForLocation( path );
            if( resultingResource != null && resultingResource.exists() ) 
                return resultingResource;

            // check for linked resources
            IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(path);
            if( files != null && files.length > 0 && files[0] != null) {
            	return files[0];
            }
            
    		return null;
        }
        catch( IllegalArgumentException iae ) //thrown on invalid paths
        {
            return null;
        }
	}

	/**
	 * @param resultingResource
	 * @param workingCopies
	 * @return
	 */
	protected static char[] findWorkingCopy(IResource resultingResource, Iterator workingCopies) {
		if( parserLogService.isTracing() )
			parserLogService.traceLog( "Attempting to find the working copy for " + resultingResource.getName() ); //$NON-NLS-1$
		while( workingCopies.hasNext() )
		{
			Object next = workingCopies.next();
			if( !( next instanceof IWorkingCopy)) continue;
			IWorkingCopy copy = (IWorkingCopy) next;
			if( copy.getResource().equals(resultingResource ))
			{
				if( parserLogService.isTracing() )
					parserLogService.traceLog( "Working copy found!!" ); //$NON-NLS-1$
				return copy.getContents();
			}
		}
		if( parserLogService.isTracing() )
			parserLogService.traceLog( "Working copy not found." ); //$NON-NLS-1$

		return null;
	}
}

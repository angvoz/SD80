/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.core.model.IDebugLogConstants;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.parser.ParserLogService;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
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
	
	public static Reader createReader( String finalPath, Iterator workingCopies )
	{
		// check to see if the file which this path points to points to an 
		// IResource in the workspace
		try
		{
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IPath path = new Path( finalPath );
			
			if( workspace.getRoot().getLocation().isPrefixOf( path ) )
				path = path.removeFirstSegments(workspace.getRoot().getLocation().segmentCount() );

			IResource resultingResource = workspace.getRoot().findMember(path);
			
			if( resultingResource != null && resultingResource.getType() == IResource.FILE )
			{
				// this is the file for sure
				// check the working copy
				if( workingCopies.hasNext() )
				{
					Reader r = findWorkingCopy( resultingResource, workingCopies );
					if( r != null ) return r;
				}
				BufferedInputStream bufferedStream = new BufferedInputStream( ((IFile) resultingResource).getContents() );
				InputStreamReader inputReader  = new InputStreamReader( bufferedStream );
				return new BufferedReader( inputReader );
			}
		}
		catch( CoreException ce )
		{
		}
		return InternalParserUtil.createFileReader(finalPath);
	}

	/**
	 * @param resultingResource
	 * @param workingCopies
	 * @return
	 */
	protected static Reader findWorkingCopy(IResource resultingResource, Iterator workingCopies) {
		if( parserLogService.isTracing() )
			parserLogService.traceLog( "Attempting to find the working copy for " + resultingResource.getName() );
		while( workingCopies.hasNext() )
		{
			Object next = workingCopies.next();
			if( !( next instanceof IWorkingCopy)) continue;
			IWorkingCopy copy = (IWorkingCopy) next;
			if( copy.getResource().equals(resultingResource ))
			{
				CharArrayReader arrayReader = new CharArrayReader( copy.getContents() );
				if( parserLogService.isTracing() )
					parserLogService.traceLog( "Working copy found!!" );
				return new BufferedReader( arrayReader );
			}
		}
		if( parserLogService.isTracing() )
			parserLogService.traceLog( "Working copy not found." );

		return null;
	}
}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.search.ui.text.Match;

public class NewSearchResultCollector extends BasicSearchResultCollector {
	private CSearchResult fSearch;
	private IProgressMonitor fProgressMonitor;
	private int fMatchCount;
	

	public NewSearchResultCollector(CSearchResult search, IProgressMonitor monitor) {
		super();
		fSearch= search;
		fProgressMonitor= monitor;
		fMatchCount = 0;
	}

	
	public void accept(IResource resource, int start, int end, ICElement enclosingElement, int accuracy) {
		fMatchCount++;
		fSearch.addMatch(new Match(enclosingElement, start, end-start));
	}


	public void done() {
	}

	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
	}
	
	public int getMatchCount() {
		return fMatchCount;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#acceptMatch(org.eclipse.cdt.core.search.IMatch)
	 */
	public boolean acceptMatch(IMatch match) throws CoreException {

      BasicSearchMatch searchMatch = (BasicSearchMatch) match;
		
      if( !super.acceptMatch( match ) )
      		return false;

      if( searchMatch.resource == null &&
      	  searchMatch.path == null)
      		return false;
	 
      if (searchMatch.resource != null){
		fMatchCount++;
		int start = match.getStartOffset();
		int end = match.getEndOffset();
		fSearch.addMatch(new Match(match,start,end-start));
		return true;
      }
      else {
      	//Check to see if external markers are enabled
      	IPreferenceStore store =  CUIPlugin.getDefault().getPreferenceStore();
      	if (store.getBoolean(CSearchPage.EXTERNALMATCH_ENABLED)){
          	//Create Link in referring file's project
          	IPath refLocation = searchMatch.getReferenceLocation();
          	IFile refFile = CCorePlugin.getWorkspace().getRoot().getFileForLocation(refLocation);
          	IProject refProject = refFile.getProject();
          	IPath externalMatchLocation = searchMatch.getLocation();
          	IFile linksFile = getUniqueFile(externalMatchLocation, refProject);
            //Delete links file to keep up to date with latest prefs
          	if (linksFile.exists() &&
          		linksFile.isLinked())
          		linksFile.delete(true,null);
          	
          	//Check to see if the file already exists - create if doesn't, mark team private 
          	if (!linksFile.exists()){
          		linksFile.createLink(externalMatchLocation,IResource.NONE,null);
          		int number = store.getInt(CSearchPage.EXTERNALMATCH_VISIBLE);
          		if (number==0){
          			linksFile.setDerived(true);
          		}
          		else{
          			linksFile.setTeamPrivateMember(true);
          		}
          		
          	}
          	searchMatch.resource = linksFile;
          	searchMatch.path = externalMatchLocation;
        	fMatchCount++;
    		int start = match.getStartOffset();
    		int end = match.getEndOffset();
    		fSearch.addMatch(new Match(match,start,end-start));
    		return true;        	
      		}
      	}	

      return false;
	
	}


	/**
	 * @param externalMatchLocation
	 * @param refProject
	 * @return
	 */
	private IFile getUniqueFile(IPath externalMatchLocation, IProject refProject) {
		IFile file = null;
		String fileName = ""; //$NON-NLS-1$
		//Total number of segments in file name
		int segments = externalMatchLocation.segmentCount() - 1;
		for (int linkNumber=0; linkNumber<Integer.MAX_VALUE; linkNumber++){
			if (fileName !="") //$NON-NLS-1$
				fileName = ICSearchConstants.EXTERNAL_SEARCH_LINK_PREFIX + linkNumber + "_" + externalMatchLocation.segment(segments); //$NON-NLS-1$ //$NON-NLS-2$
			else
				fileName = externalMatchLocation.segment(segments);
			
			file=refProject.getFile(fileName);
		    IPath path = file.getLocation();
			
		    //If the location passed in is equal to a location of a file
		    //that is already linked then return the file
		    if (externalMatchLocation.equals(path))
		    	break;
		    
		    //If the file doesn't already exist in the workspace return
		    if (!file.exists())
				break;
		}
		return file;
	}
	
	
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index.domsourceindexer;

import java.io.IOException;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceEntry;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.CIndexStorage;
import org.eclipse.cdt.internal.core.model.CModel;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.model.SourceRoot;
import org.eclipse.cdt.internal.core.search.SimpleLookupTable;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;


public class DOMIndexAllProject extends DOMIndexRequest {
	IProject project;

	public DOMIndexAllProject(IProject project, DOMSourceIndexer indexer) {
		super(project.getFullPath(), indexer);
		this.project = project;
	}
	
	public boolean equals(Object o) {
		if (o instanceof DOMIndexAllProject)
			return this.project.equals(((DOMIndexAllProject) o).project);
		return false;
	}
	/**
	 * Ensure consistency of a project index. Need to walk all nested resources,
	 * and discover resources which have either been changed, added or deleted
	 * since the index was produced.
	 */
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return true;
		if (!project.isAccessible()) return true; // nothing to do
		
		IIndex index = indexer.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = indexer.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
		    if (AbstractIndexerRunner.TIMING)
		        //reset the total index timer
		        indexer.setTotalIndexTime(0);
		    
			monitor.enterRead(); // ask permission to read
			saveIfNecessary(index, monitor);

			IQueryResult[] results = index.queryInDocumentNames(""); // all file names //$NON-NLS-1$
			int max = results == null ? 0 : results.length;
			final SimpleLookupTable indexedFileNames = new SimpleLookupTable(max == 0 ? 33 : max + 11);
			final String DELETED = "DELETED"; //$NON-NLS-1$
			for (int i = 0; i < max; i++)
				indexedFileNames.put(results[i].getPath(), DELETED);

			CModel model = CModelManager.getDefault().getCModel();
		
			if (model == null)
				return false;
			
			ICProject cProject = model.getCProject(project.getName());		
			
			if (cProject == null)
				return false;
			
            // first clean encountered headers
            CleanEncounteredHeaders cleanHeaders = new CleanEncounteredHeaders(this.indexer);
            this.indexer.request(cleanHeaders);

			//Get the source roots for this project
			ISourceRoot[] sourceRoot = cProject.getSourceRoots();
			for (int i=0;i<sourceRoot.length;i++){
				if (sourceRoot[i] instanceof SourceRoot){
					ISourceEntry tempEntry = ((SourceRoot) sourceRoot[i]).getSourceEntry();
					
					indexer.request(new DOMAddFolderToIndex(sourceRoot[i].getPath(), project, tempEntry.fullExclusionPatternChars(), indexer));
				}
			}
			
            // request to save index when all cus have been indexed
			indexer.request(new DOMSaveIndex(this.indexPath, indexer));
		} catch (CoreException e) {
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.project + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			indexer.removeIndex(this.indexPath);
			return false;
		} catch (IOException e) {
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.project + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			indexer.removeIndex(this.indexPath);
			return false;
		} finally {
			monitor.exitRead(); // free read lock
		}
		return true;
	}
	
	public int hashCode() {
		return this.project.hashCode();
	}
	
	protected Integer updatedIndexState() {
		return CIndexStorage.REBUILDING_STATE;
	}
	
	public String toString() {
		return "indexing project " + this.project.getFullPath(); //$NON-NLS-1$
	}
}

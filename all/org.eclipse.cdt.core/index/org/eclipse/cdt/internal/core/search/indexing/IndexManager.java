/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.search.indexing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.internal.core.index.ctagsindexer.CTagsIndexRequest;
import org.eclipse.cdt.internal.core.index.sourceindexer.CIndexStorage;
import org.eclipse.cdt.internal.core.index.sourceindexer.IndexRequest;
import org.eclipse.cdt.internal.core.index.sourceindexer.SourceIndexer;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Bogdan Gheorghe
 */
public class IndexManager extends JobManager{
	
	public final static String INDEX_MODEL_ID = CCorePlugin.PLUGIN_ID + ".cdtindexers"; //$NON-NLS-1$
	public final static String INDEXERID = "indexerID"; //$NON-NLS-1$
	public final static QualifiedName indexerIDKey = new QualifiedName(INDEX_MODEL_ID, INDEXERID);
	
	public static final String nullIndexerID = "org.eclipse.cdt.core.nullindexer"; //$NON-NLS-1$
	
	public static final String CDT_INDEXER = "cdt_indexer"; //$NON-NLS-1$
	public static final String INDEXER_ID = "indexerID"; //$NON-NLS-1$
	public static final String INDEXER_ID_VALUE = "indexerIDValue"; //$NON-NLS-1$

	public static boolean VERBOSE = false;
	
    //Map of Persisted Indexers; keyed by project
    private HashMap indexerMap = null;

	private ReadWriteMonitor monitor = new ReadWriteMonitor();

	/**
	 * Flush current state
	 */
	public void reset() {
		try{
			monitor.enterWrite();
			super.reset();
			//Set default upgrade values
			CCorePlugin.getDefault().getPluginPreferences().setValue(CCorePlugin.PREF_INDEXER, CCorePlugin.DEFAULT_INDEXER_UNIQ_ID);
			this.indexerMap = new HashMap(5);
		} finally{
			monitor.exitWrite();
		}
	}
	 /**
	 * Notify indexer which scheduled this job that the job has completed  
	 * 
	 */
	protected void jobFinishedNotification(IIndexJob job) {
		if (job instanceof IndexRequest ){
			IndexRequest indexRequest = (IndexRequest) job;
			IPath path = indexRequest.getIndexPath();
			IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(path.toOSString()); 
			ICDTIndexer indexer = getIndexerForProject(project);
			
			if (indexer != null)
				indexer.indexJobFinishedNotification(job);
		}
		
		//TODO: Standardize on jobs
		if (job instanceof CTagsIndexRequest){
		    CTagsIndexRequest indexRequest = (CTagsIndexRequest) job;
			IPath path = indexRequest.getIndexPath();
			IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(path.toOSString()); 
			ICDTIndexer indexer = getIndexerForProject(project);
			
			if (indexer != null)
				indexer.indexJobFinishedNotification(job);
		}
	}
	/**
	 * @param project
	 * @param element
	 * @param delta
	 */
	public void addResourceEvent(IProject project, IResourceDelta delta, int kind) {
		//Get indexer for this project
		ICDTIndexer indexer = getIndexerForProject(project);	
		
		if (indexer != null)
			indexer.addRequest(project, delta, kind);
	}

	/**
	 * @param project
	 * @param element
	 * @param delta
	 */
	public void removeResourceEvent(IProject project, IResourceDelta delta, int kind) {
		//Get the indexer for this project
		ICDTIndexer indexer = null;
		indexer = (ICDTIndexer) indexerMap.get(project);
		
		if (indexer != null)
			indexer.removeRequest(project, delta, kind);
	}
	
	
	/**
	 * Name of the background process
	 */
	public String processName(){
		return org.eclipse.cdt.internal.core.Util.bind("process.name"); //$NON-NLS-1$
	}
	


	public void shutdown() {
		//Send shutdown messages to all indexers
		
		/*if (IndexManager.VERBOSE)
			JobManager.verbose("Shutdown"); //$NON-NLS-1$
		//Get index entries for all projects in the workspace, store their absolute paths
		IndexSelector indexSelector = new IndexSelector(new CWorkspaceScope(), null, false, this);
		IIndex[] selectedIndexes = indexSelector.getIndexes();
		SimpleLookupTable knownPaths = new SimpleLookupTable();
		for (int i = 0, max = selectedIndexes.length; i < max; i++) {
			String path = selectedIndexes[i].getIndexFile().getAbsolutePath();
			knownPaths.put(path, path);
		}
		//Any index entries that are in the index state must have a corresponding
		//path entry - if not they are removed from the saved indexes file
		if (indexStates != null) {
			Object[] indexNames = indexStates.keyTable;
			for (int i = 0, l = indexNames.length; i < l; i++) {
				String key = (String) indexNames[i];
				if (key != null && !knownPaths.containsKey(key)) //here is an index that is in t
					updateIndexState(key, null);
			}
		}

		//Clean up the .metadata folder - if there are any files in the directory that
		//are not associated to an index we delete them
		File indexesDirectory = new File(getCCorePluginWorkingLocation().toOSString());
		if (indexesDirectory.isDirectory()) {
			File[] indexesFiles = indexesDirectory.listFiles();
			if (indexesFiles != null) {
				for (int i = 0, indexesFilesLength = indexesFiles.length; i < indexesFilesLength; i++) {
					String fileName = indexesFiles[i].getAbsolutePath();
					if (!knownPaths.containsKey(fileName) && fileName.toLowerCase().endsWith(".index")) { //$NON-NLS-1$
						if (IndexManager.VERBOSE)
							JobManager.verbose("Deleting index file " + indexesFiles[i]); //$NON-NLS-1$
						indexesFiles[i].delete();
					}
				}
			}
		}
		
		indexModelListener.shutdown();
		
		this.timeoutThread = null;*/
		
		//Send shutdown notification to all indexers
		if (indexerMap != null){
			Set projects = indexerMap.keySet();
			Iterator i = projects.iterator();
			while (i.hasNext()){
				IProject tempProject = (IProject) i.next();
		   		ICDTIndexer indexer = (ICDTIndexer) indexerMap.get(tempProject);
		   		if (indexer != null)
		   			indexer.shutdown();
			}
		}
		
		super.shutdown();
	}
	
	public IIndexStorage getIndexStorageForIndexer(ICDTIndexer indexer){
		//For now we have only one index storage format that all indexers are to use
		return new CIndexStorage(indexer);
	}
	
	public int getJobStart(){
		return jobStart;
	}
	
	public int getJobEnd(){
		return jobEnd;
	}
	/**
	 * Returns the job at position in the awaiting job queue
	 * @param position
	 * @return
	 */
	public IIndexJob getAwaitingJobAt(int position){
		return this.awaitingJobs[position];
	}
	/**
	 * Check to see if the indexer associated with this project
	 * requires dependency update notifications
	 * @param resource
	 * @param resource2
	 */
	public void updateDependencies(IProject project, IResource resource) {
		ICDTIndexer indexer = getIndexerForProject(project);
		if (indexer instanceof SourceIndexer)
			((SourceIndexer) indexer).updateDependencies(resource);
		
	}
	
	public ICDTIndexer getIndexerForProject(IProject project){
		ICDTIndexer indexer = null;
		try {
			//Make sure we're not updating list
			monitor.enterRead();
			
			//See if indexer exists already
			indexer = (ICDTIndexer) indexerMap.get(project);
			
			//Create the indexer and store it
			if (indexer == null) {
				monitor.exitRead();
				try {
					monitor.enterWrite();
					indexer = getIndexer(project);
					//Make sure we're not putting null in map
					if (indexer != null)
						indexerMap.put(project,indexer);
				} finally{
					monitor.exitWriteEnterRead();
				}
			}
			return indexer;
				
			}finally {
				monitor.exitRead();
			}
	}
	
	public ICDTIndexer getDefaultIndexer(IProject project) throws CoreException {
		ICDTIndexer indexer = null;
		String id = CCorePlugin.getDefault().getPluginPreferences().getDefaultString(CCorePlugin.PREF_INDEXER);
		if (id == null || id.length() == 0) {
			id = CCorePlugin.DEFAULT_INDEXER_UNIQ_ID;
		}
		
        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.INDEXER_SIMPLE_ID);
		IExtension extension = extensionPoint.getExtension(id);
		if (extension != null) {
			IConfigurationElement element[] = extension.getConfigurationElements();
			for (int i = 0; i < element.length; i++) {
				if (element[i].getName().equalsIgnoreCase("cextension")) { //$NON-NLS-1$
					indexer = (ICDTIndexer) element[i].createExecutableExtension("run"); //$NON-NLS-1$
					break;
				}
			}
		} else {
			IStatus s = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, -1,"No Indexer Found", null); //$NON-NLS-1$
			throw new CoreException(s);
		}
		return indexer;
	}
	
   protected ICDTIndexer getIndexer(IProject project) {
   	ICDTIndexer indexer = null;
   	try{
		ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project,true);
		ICExtensionReference[] cextensions = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID, true);
		
		if (cextensions != null && cextensions.length > 0)
			indexer = (ICDTIndexer) cextensions[0].createExtension();
	
   	} catch (CoreException e){}
   	
	if (indexer == null)
		try {
			indexer = getDefaultIndexer(project);
		} catch (CoreException e1) {}
	
	 return indexer;
   }
   
   protected void notifyIdle(long idlingTime) {
   	//Notify all indexers
   	monitor.enterRead();
   	try{
	   	if (indexerMap == null)
	   		return;
	   		
	   	Set mapKeys = indexerMap.keySet();
	   	Iterator i = mapKeys.iterator();
	   	while (i.hasNext()){
	   		IProject tempProject = (IProject) i.next();
	   		ICDTIndexer indexer = (ICDTIndexer) indexerMap.get(tempProject);
	   		if (indexer != null)
	   			indexer.notifyIdle(idlingTime);
	   	}
   	} finally{
   		monitor.exitRead();
   	}
   }
   
   /**
	* The indexer previously associated with this project has been changed to a
	* new value. Next time project gets asked for indexer, a new one will be created
	* of the new type.
	* 
	* @param project
	*/
	public void indexerChangeNotification(IProject project) {
	    monitor.enterWrite();
	    try{
	        //Get rid of any jobs scheduled by the old indexer
	        this.discardJobs(project.getName());
	        //Purge the old indexer from the indexer map
	        Object e = indexerMap.remove(project);   
	    } finally { 
	        monitor.exitWrite();
	        final ICDTIndexer indexer = this.getIndexerForProject(project);
	        final IProject finalProject = project;
	        
	    	//Notify new indexer in a job of change
			Job job = new Job("Index Change Notification"){ //$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor)	{	
					Platform.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							CCorePlugin.log(exception);
						}
						public void run() throws Exception {
						    indexer.notifyIndexerChange(finalProject);
						}
					});
					
					return Status.OK_STATUS;
				}
			};

			job.schedule();
	    }
	}
}

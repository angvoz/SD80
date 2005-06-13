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
package org.eclipse.cdt.internal.core.index.domsourceindexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.core.index.IndexChangeEvent;
import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.cindexstorage.CIndexStorage;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.cdt.internal.core.search.SimpleLookupTable;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.cdt.internal.core.sourcedependency.UpdateDependency;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
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
public class DOMSourceIndexer extends AbstractCExtension implements ICDTIndexer {
	
	public static boolean VERBOSE = false;
	
	//private IndexerModelListener indexModelListener = null;
	
	/**
	 * Collection of listeners for indexer deltas
	 */
	protected List indexChangeListeners = Collections.synchronizedList(new ArrayList());
	public static final String INDEX_NOTIFICATION_NAME = Util.bind( "indexNotificationJob" ); //$NON-NLS-1$
	
	public final static String INDEX_MODEL_ID = CCorePlugin.PLUGIN_ID + ".newindexmodel"; //$NON-NLS-1$
	public final static String ACTIVATION = "enable"; //$NON-NLS-1$
	public final static String PROBLEM_ACTIVATION = "problemEnable"; //$NON-NLS-1$
	public final static QualifiedName activationKey = new QualifiedName(INDEX_MODEL_ID, ACTIVATION);
	public final static QualifiedName problemsActivationKey = new QualifiedName( INDEX_MODEL_ID, PROBLEM_ACTIVATION );
	
	public static final int PREPROCESSOR_PROBLEMS_BIT = 1;
	public static final int SEMANTIC_PROBLEMS_BIT = 1 << 1;
	public static final int SYNTACTIC_PROBLEMS_BIT = 1 << 2;
    public static final int INCLUSION_PROBLEMS_BIT = 1 << 3;

	public static final String SOURCE_INDEXER_ID = "originalsourceindexer"; //$NON-NLS-1$
	public static final String SOURCE_INDEXER_UNIQUE_ID = CCorePlugin.PLUGIN_ID + "." + SOURCE_INDEXER_ID; //$NON-NLS-1$;
	
	
	private CIndexStorage		indexStorage = null;
	protected 	ReadWriteMonitor	storageMonitor = null;
    protected IndexManager  	indexManager = null; 
	
	protected HashSet 			jobSet = null;
	
    protected long		    	totalIndexTime = 0;
	
    public static String ID = CCorePlugin.PLUGIN_ID + ".domsourceindexer"; //$NON-NLS-1$
    
    public DOMSourceIndexer(){
    	this.indexManager = CCorePlugin.getDefault().getCoreModel().getIndexManager();
    	this.indexStorage = (CIndexStorage) indexManager.getIndexStorageForIndexer(this);
    	this.jobSet	= new HashSet();
    	this.storageMonitor = new ReadWriteMonitor();
    }
 
	/**
	 * @return
	 */
	public IIndexStorage getIndexStorage() {
		return indexStorage;
	}


	public void addSource(IFile resource, IPath indexedContainers){
		this.addSource(resource,indexedContainers, false);
	}
	
	/**
	 * Trigger addition of a resource to an index
	 * Note: the actual operation is performed in background
	 * @param checkEncounteredHeaders TODO
	 */
	 public void addSource(IFile resource, IPath indexedContainers, boolean checkEncounteredHeaders) {
	        IProject project = resource.getProject();
	        
	        boolean indexEnabled = false;
	        if (project != null)
	            indexEnabled = isIndexEnabled(project);
	        else
	            org.eclipse.cdt.internal.core.model.Util.log(null, "IndexManager addSource: File has no project associated : " + resource.getName(), ICLogConstants.CDT); //$NON-NLS-1$ 
	            
	        if (CCorePlugin.getDefault() == null) return;   
	        
	        if (indexEnabled){
	            DOMAddCompilationUnitToIndex job = new DOMAddCompilationUnitToIndex(resource, indexedContainers, this, checkEncounteredHeaders);

	            //If we are in WAITING mode, we need to kick ourselves into enablement
	            if (!jobSet.add(resource.getLocation()) &&
	                indexManager.enabledState()==IndexManager.ENABLED)
	                return;
	            
	            
	            if (indexManager.awaitingJobsCount() < CIndexStorage.MAX_FILES_IN_MEMORY) {
	                // reduces the chance that the file is open later on, preventing it from being deleted
	                if (!job.initializeContents()) return;
	            }
	            
	            this.indexManager.request(job);
	        }
	   }

	

	
	public void updateDependencies(IResource resource){
		if (CCorePlugin.getDefault() == null || !isIndexEnabled( resource.getProject() ) )
			return;	
	
		UpdateDependency job = new UpdateDependency(resource, this);
		indexManager.request(job);
	}
	
	/**
	 * 
	 * 
	 * Warning: Does not check whether index is consistent (not being used)
	 */
	public static int trimed = 0;
	public static int added = 0;
	
	public synchronized boolean haveEncounteredHeader(IPath projectPath, IPath filePath, boolean add) {
		SimpleLookupTable headerTable = indexStorage.getEncounteredHeaders(); 
		
		// Path is already canonical per construction
		ObjectSet headers = (ObjectSet) headerTable.get(projectPath);
		if (headers == null) {
			//First time for the project, must create a new ObjectSet
			headers = new ObjectSet(4);
			headerTable.put(projectPath, headers);
		 }
		
		if (headers.containsKey(filePath.toOSString())) {
			trimed++;
			return true;
		}
		
		if (add) {
			headers.put(filePath.toOSString());
            added++;
		}
		
		return false;
	}
	

	
	/**
	 * Trigger addition of the entire content of a project
	 * Note: the actual operation is performed in background 
	 */
	public void indexAll(IProject project) {
		if (CCorePlugin.getDefault() == null) return;
	 
		//check to see if indexing isEnabled for this project
		boolean indexEnabled = isIndexEnabled(project);
		
		if (indexEnabled){
			if( indexManager.enabledState() == IndexManager.WAITING ){
				//if we are paused because the user cancelled a previous index, this is a good
				//enough reason to restart
				indexManager.enable();
			}
			// check if the same request is not already in the queue
			DOMIndexRequest request = new DOMIndexAllProject(project, this);
			for (int i = indexManager.getJobEnd(); i > indexManager.getJobStart(); i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
				if (request.equals(indexManager.getAwaitingJobAt(i))) return;
			indexManager.request(request);
		}
	}
	
	/**
	 * @param project
	 * @return
	 */
	public boolean isIndexEnabled(IProject project) {
		if( project == null || !project.exists() || !project.isOpen() )
			return false;
		
		return true;
		/*Boolean indexValue = null;
		
		try {
			indexValue = (Boolean) project.getSessionProperty(activationKey);
		} catch (CoreException e) {}
		
		if (indexValue != null)
			return indexValue.booleanValue();
		
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
			if (cdesc == null)
				return false;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				//initializeIndexerId();
				for (int i = 0; i < cext.length; i++) {
					String id = cext[i].getID();
						String orig = cext[i].getExtensionData("indexenabled"); //$NON-NLS-1$
						if (orig != null){
							Boolean tempBool = new Boolean(orig);
							indexEnabled = tempBool.booleanValue();
						}
				}
			}
		
			
		} catch (CoreException e) {}
		
		return indexEnabled;*/
	}
	

	
	public int indexProblemsEnabled(IProject project) {
		
		if( project == null || !project.exists() || !project.isOpen() )
			return 0;
		
		int indexProblemsEnabled = 0;
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(project, false);
			if (cdesc == null)
				return 0;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				//initializeIndexerId();
				for (int i = 0; i < cext.length; i++) {
						String orig = cext[i].getExtensionData("indexmarkers"); //$NON-NLS-1$
						if (orig != null){
							Integer tempInt = new Integer(orig);
							indexProblemsEnabled = tempInt.intValue();
						}
				}
			}
		
			
		} catch (CoreException e) {}
		
		return indexProblemsEnabled;
	}
	/**
	 * Index the content of the given source folder.
	 */
	public void indexSourceFolder(IProject project, IPath sourceFolder, final char[][] exclusionPattern) {
		if( !isIndexEnabled( project ) )
			return;
		if (indexManager.getJobEnd() > indexManager.getJobStart()) {
			// check if a job to index the project is not already in the queue
			DOMIndexRequest request = new DOMIndexAllProject(project, this);
			for (int i = indexManager.getJobEnd(); i > indexManager.getJobStart(); i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
				if (request.equals(indexManager.getAwaitingJobAt(i))) return;
		}
		this.request(new DOMAddFolderToIndex(sourceFolder, project, exclusionPattern, this));
	}
	
	/**
	 * Trigger removal of a resource to an index
	 * Note: the actual operation is performed in background
	 */
	public void remove(String resourceName, IPath indexedContainer){
		IProject project = CCorePlugin.getWorkspace().getRoot().getProject(indexedContainer.toString());
      	if( isIndexEnabled( project ) )
      		request(new DOMRemoveFromIndex(resourceName, indexedContainer, this));
	}
	

	/**
	 * Remove the content of the given source folder from the index.
	 */
	public void removeSourceFolderFromIndex(IProject project, IPath sourceFolder, char[][] exclusionPatterns) {
		
		if( !isIndexEnabled( project ) )
			return;
		
		if (indexManager.getJobEnd()> indexManager.getJobStart()) {
			// check if a job to index the project is not already in the queue
			DOMIndexRequest request = new DOMIndexAllProject(project, this);
			for (int i = indexManager.getJobEnd(); i > indexManager.getJobStart(); i--) // NB: don't check job at jobStart, as it may have already started (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=32488)
				if (request.equals(indexManager.getAwaitingJobAt(i))) return;
		}

		this.request(new DOMRemoveFolderFromIndex(sourceFolder, exclusionPatterns, project, this));
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.processing.JobManager#jobFinishedNotification(org.eclipse.cdt.internal.core.search.processing.IJob)
	 */
	public void jobFinishedNotification(IIndexJob job) {
		this.indexJobFinishedNotification(job);
		
	}
	
	public void removeIndexerProblems( IResource resource){
        indexManager.removeIndexerProblems(resource);
    }
	
	public void addIndexChangeListener(IIndexChangeListener listener) {
		synchronized(indexChangeListeners) {
			if (!indexChangeListeners.contains(listener)) {
				indexChangeListeners.add(listener);
			}
		}
	}
	
	public void removeIndexChangeListener(IIndexChangeListener listener) {
		synchronized(indexChangeListeners) {
			int i = indexChangeListeners.indexOf(listener);
			if (i != -1) {
				indexChangeListeners.remove(i);
			}
		}
	}
	/**
	 * @param indexDelta
	 */
	public void notifyListeners(IndexDelta indexDelta) {
		final IndexChangeEvent indexEvent = new IndexChangeEvent(indexDelta);
		for (int i= 0; i < indexChangeListeners.size(); i++) {
			    IIndexChangeListener tempListener = null;
			    synchronized(indexChangeListeners){
			    	tempListener = (IIndexChangeListener) indexChangeListeners.get(i);
			    }
			    final IIndexChangeListener listener = tempListener;
				long start = -1;
				if (VERBOSE) {
					System.out.print("Listener #" + (i+1) + "=" + listener.toString());//$NON-NLS-1$//$NON-NLS-2$
					start = System.currentTimeMillis();
				}
				
				// wrap callbacks with Safe runnable for subsequent listeners to be called when some are causing grief
				Job job = new Job(INDEX_NOTIFICATION_NAME){
					protected IStatus run(IProgressMonitor monitor)	{	
						Platform.run(new ISafeRunnable() {
							public void handleException(Throwable exception) {
								CCorePlugin.log(exception);
							}
							public void run() throws Exception {
								listener.indexChanged(indexEvent);
							}
						});
						
						return Status.OK_STATUS;
					}
				};
				
				job.schedule();
				if (VERBOSE) {
					System.out.println(" -> " + (System.currentTimeMillis()-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		
		}
	
	/**
	 * No more job awaiting.
	 */
	public void notifyIdle(long idlingTime){
		if (idlingTime > 1000 && indexStorage.getNeedToSave()) 
			indexStorage.saveIndexes();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexer#getIndexerFeatures()
	 */
	public int getIndexerFeatures() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexer#addRequest(org.eclipse.cdt.core.model.ICElement, org.eclipse.core.resources.IResourceDelta, org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void addRequest(IProject project, IResourceDelta delta, int kind) {
		    
		switch (kind) {
			case ICDTIndexer.PROJECT :
					this.indexAll(project);
				break;
	        
			case ICDTIndexer.FOLDER : 
				this.indexSourceFolder(project,delta.getFullPath(),null);
			break;
			
			case ICDTIndexer.COMPILATION_UNIT:
				IFile file = (IFile) delta.getResource();
				this.addSource(file, project.getFullPath());
				break;						
		}
		
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexer#removeRequest(org.eclipse.cdt.core.model.ICElement, org.eclipse.core.resources.IResourceDelta, org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void removeRequest(IProject project, IResourceDelta delta, int kind) {
		switch (kind) {
			case ICDTIndexer.PROJECT :
				IPath fullPath = project.getFullPath();
				if( delta.getKind() == IResourceDelta.CHANGED )
					indexManager.discardJobs(fullPath.segment(0));
				indexStorage.removeIndexFamily(fullPath);
				// NB: Discarding index jobs belonging to this project was done during PRE_DELETE
				break;
				// NB: Update of index if project is opened, closed, or its c nature is added or removed
				//     is done in updateCurrentDeltaAndIndex
			
			case ICDTIndexer.FOLDER :
				this.removeSourceFolderFromIndex(project,delta.getFullPath(),null);
				break;
			
			case ICDTIndexer.COMPILATION_UNIT:
				IFile file = (IFile) delta.getResource();
				this.remove(file.getFullPath().toString(), file.getProject().getFullPath());
				break;				
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexer#indexJobFinishedNotification(org.eclipse.cdt.internal.core.search.processing.IIndexJob)
	 */
	public void indexJobFinishedNotification(IIndexJob job) {
		indexStorage.setNeedToSave(true);
		
		if (job instanceof DOMAddCompilationUnitToIndex){
			DOMAddCompilationUnitToIndex tempJob = (DOMAddCompilationUnitToIndex) job;
			jobSet.remove(tempJob.getResource().getLocation());
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.index2.IIndexer#shutdown()
	 */
	public void shutdown() {
		indexStorage.shutdown();
		//indexModelListener.shutdown();
	}

	/**
	 * Forward job request to Index Manager
	 * @param cleanHeaders
	 */
	public void request(IIndexJob indexJob) {
		this.indexManager.request(indexJob);
	}

	public ReadWriteMonitor getStorageMonitor() {
		return storageMonitor;
	}
	/**
	 * 
	 */
	public void resetEncounteredHeaders() {
		try{
			storageMonitor.enterWrite();
			indexStorage.resetEncounteredHeaders();
		}
		finally {
			storageMonitor.exitWrite();
		}
	}
	

	/**
	 * @param path
	 * @param reuseIndexFile
	 * @param createIfMissing
	 * @return
	 */
	public synchronized IIndex getIndex(IPath path, boolean reuseExistingFile, boolean createIfMissing) {
		IIndex index = null;
		try{
			storageMonitor.enterRead();
			index = indexStorage.getIndex(path,reuseExistingFile, createIfMissing);
		}
		finally{
			storageMonitor.exitRead();
		}
		return index;
	}

	/**
	 * @param index
	 * @return
	 */
	public ReadWriteMonitor getMonitorFor(IIndex index) {
		ReadWriteMonitor monitor = null;
		try{
			storageMonitor.enterRead();
			monitor=indexStorage.getMonitorForIndex();
		}
		finally{
			storageMonitor.exitRead();
		}
		return monitor;
	}

	/**
	 * @param path
	 */
	public void removeIndex(IPath path) {
		try{
			storageMonitor.enterWrite();
			indexStorage.removeIndex(path);
		}
		finally{
			storageMonitor.exitWrite();
		}
		
	}

	/**
	 * @param path
	 */
	public void jobWasCancelled(IPath path) {
	 try{
	 	storageMonitor.enterWrite();
	 	indexStorage.jobWasCancelled(path);
	 }
	 finally{
	 	storageMonitor.exitWrite();
	 }
	}

	/**
	 * @param index
	 */
	public void saveIndex(IIndex index) throws IOException {
		try{
			storageMonitor.enterWrite();
			indexStorage.saveIndex(index);
		} 
		finally {
			storageMonitor.exitWrite();
		}
		
	}

	/**
	 * @param indexPath
	 * @param indexState
	 */
	public void aboutToUpdateIndex(IPath indexPath, Integer indexState) {
		storageMonitor.enterRead();
		try{
			indexStorage.aboutToUpdateIndex(indexPath, indexState);
		}
		finally {
			storageMonitor.exitRead();
		}
	}
    /**
     * @return Returns the totalIndexTime.
     */
    public long getTotalIndexTime() {
        return totalIndexTime;
    }
    /**
     * @param totalIndexTime The totalIndexTime to set.
     */
    public void setTotalIndexTime(long totalIndexTime) {
        this.totalIndexTime = totalIndexTime;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.index.ICDTIndexer#notifyIndexerChange()
     */
    public void notifyIndexerChange(IProject project) {
        this.indexAll(project);
    }

	public void indexerRemoved(IProject project) {
        removeIndexerProblems(project);
	}

	public void addResource(IProject project, IResource resource) {
		
		if (resource instanceof IProject){
			this.indexAll(project);
		}
		else if (resource instanceof IFolder){
			this.indexSourceFolder(project,resource.getFullPath(),null);
		}
		else if (resource instanceof IFile){
			IFile file = (IFile) resource;
			this.addSource(file, project.getFullPath());
		}	
	}

	public void removeResource(IProject project, IResource resource) {
		// TODO Auto-generated method stub
		
	}

	public void addResourceByPath(IProject project, IPath path, int resourceType) {
		// TODO Auto-generated method stub
		
	}
	
}

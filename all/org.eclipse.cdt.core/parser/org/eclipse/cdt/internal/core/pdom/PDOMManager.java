/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.dom.IPDOMIndexer;
import org.eclipse.cdt.core.dom.IPDOMIndexerTask;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexChangeListener;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.index.IIndexerStateListener;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementDelta;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguageMappingChangeListener;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IWritableIndex;
import org.eclipse.cdt.internal.core.index.IWritableIndexManager;
import org.eclipse.cdt.internal.core.index.IndexChangeEvent;
import org.eclipse.cdt.internal.core.index.IndexFactory;
import org.eclipse.cdt.internal.core.index.IndexerStateEvent;
import org.eclipse.cdt.internal.core.index.provider.IndexProviderManager;
import org.eclipse.cdt.internal.core.pdom.PDOM.IListener;
import org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMProjectIndexLocationConverter;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMRebuildTask;
import org.eclipse.cdt.internal.core.pdom.indexer.PDOMUpdateTask;
import org.eclipse.cdt.internal.core.pdom.indexer.TriggerNotificationTask;
import org.eclipse.cdt.internal.core.pdom.indexer.nulli.PDOMNullIndexer;
import org.eclipse.cdt.internal.core.settings.model.CProjectDescriptionManager;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

/**
 * The PDOM Provider. This is likely temporary since I hope
 * to integrate the PDOM directly into the core once it has
 * stabilized.
 * 
 * @author Doug Schaefer
 */
public class PDOMManager implements IWritableIndexManager, IListener {
	private static final class PerInstanceSchedulingRule implements ISchedulingRule {
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}
	
	private final class PCL implements IPreferenceChangeListener, IPropertyChangeListener {
		private ICProject fProject;
		public PCL(ICProject prj) {
			fProject= prj;
		}
		public void preferenceChange(PreferenceChangeEvent event) {
			if (fProject.getProject().isOpen()) {
				onPreferenceChange(fProject, event);
			}
		}
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			if (property.equals(CCorePreferenceConstants.TODO_TASK_TAGS) ||
					property.equals(CCorePreferenceConstants.TODO_TASK_PRIORITIES) ||
					property.equals(CCorePreferenceConstants.TODO_TASK_CASE_SENSITIVE)) {
				// Rebuild index if task tag preferences change.
				reindex(fProject);
			}
		}
	}

	private static final String SETTINGS_FOLDER_NAME = ".settings"; //$NON-NLS-1$
	private static final QualifiedName dbNameProperty= new QualifiedName(CCorePlugin.PLUGIN_ID, "pdomName"); //$NON-NLS-1$

	private static final ISchedulingRule NOTIFICATION_SCHEDULING_RULE = new PerInstanceSchedulingRule();
	private static final ISchedulingRule INDEXER_SCHEDULING_RULE = new PerInstanceSchedulingRule();

	/**
	 * Protects indexerJob, currentTask and taskQueue.
	 */
    private Object fTaskQueueMutex = new Object();
    private PDOMIndexerJob fIndexerJob;
	private IPDOMIndexerTask fCurrentTask;
	private LinkedList fTaskQueue = new LinkedList();
	private int fCompletedSources;
	private int fCompletedHeaders;
	
    /**
     * Stores mapping from pdom to project, used to serialize\ creation of new pdoms.
     */
    private Map fProjectToPDOM= new HashMap();
    private Map fFileToProject= new HashMap();
	private ListenerList fChangeListeners= new ListenerList();
	private ListenerList fStateListeners= new ListenerList();
	
	private IndexChangeEvent fIndexChangeEvent= new IndexChangeEvent();
	private IndexerStateEvent fIndexerStateEvent= new IndexerStateEvent();

	private CModelListener fCModelListener= new CModelListener(this);
	private ILanguageMappingChangeListener fLanguageChangeListener = new LanguageMappingChangeListener(this);
	private ICProjectDescriptionListener fProjectDescriptionListener= new CProjectDescriptionListener(this);
	
	private IndexFactory fIndexFactory= new IndexFactory(this);
    private IndexProviderManager fIndexProviderManager = new IndexProviderManager();

    
	/**
	 * Serializes creation of new indexer, when acquiring the lock you are 
	 * not allowed to hold a lock on fPDOMs.
	 */
	private HashMap fUpdatePolicies= new HashMap();
	private HashMap fPrefListeners= new HashMap();
    
	public Job startup() {
		Job postStartupJob= new Job(CCorePlugin.getResourceString("CCorePlugin.startupJob")) { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				postStartup();
				return Status.OK_STATUS;
			}
			public boolean belongsTo(Object family) {
				return family == PDOMManager.this;
			}
		};
		postStartupJob.setSystem(true);
		return postStartupJob;	
	}

	/** 
	 * Called from a job after plugin start.
	 */
	protected void postStartup() {
		// the model listener is attached outside of the job in
		// order to avoid a race condition where its not noticed
		// that new projects are being created
		initializeDatabaseCache();

		fIndexProviderManager.startup();
		
		final CoreModel model = CoreModel.getDefault();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(fCModelListener, IResourceChangeEvent.POST_BUILD);
		model.addElementChangedListener(fCModelListener);
		LanguageManager.getInstance().registerLanguageChangeListener(fLanguageChangeListener);
		final int types= CProjectDescriptionEvent.DATA_APPLIED;
		CCorePlugin.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(fProjectDescriptionListener, types);

		try {
			ICProject[] projects= model.getCModel().getCProjects();
			for (int i = 0; i < projects.length; i++) {
				addProject(projects[i]);
			}
		} catch (CModelException e) {
			CCorePlugin.log(e);
		}
	}
	
	public void shutdown() {
		CCorePlugin.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(fProjectDescriptionListener);
		final CoreModel model = CoreModel.getDefault();
		model.removeElementChangedListener(fCModelListener);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(fCModelListener);
		LanguageManager.getInstance().unregisterLanguageChangeListener(fLanguageChangeListener);
		PDOMIndexerJob jobToCancel= null;
		synchronized (fTaskQueueMutex) {
			fTaskQueue.clear();
			jobToCancel= fIndexerJob;
		}
		
		if (jobToCancel != null) {
			assert !Thread.holdsLock(fTaskQueueMutex);
			jobToCancel.cancelJobs(null, false);
		}
	}

	private void initializeDatabaseCache() {
		adjustCacheSize();
		CCorePlugin.getDefault().getPluginPreferences().addPropertyChangeListener(
			new IPropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent event) {
					String prop= event.getProperty();
					if (prop.equals(CCorePreferenceConstants.INDEX_DB_CACHE_SIZE_PCT) || 
							prop.equals(CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB)) {
						adjustCacheSize();
					}
				}
			}
		);
	}

	protected void adjustCacheSize() {
		final Preferences prefs= CCorePlugin.getDefault().getPluginPreferences();
		int cachePct= prefs.getInt(CCorePreferenceConstants.INDEX_DB_CACHE_SIZE_PCT);
		int cacheMax= prefs.getInt(CCorePreferenceConstants.MAX_INDEX_DB_CACHE_SIZE_MB);
		cachePct= Math.max(1, Math.min(50, cachePct));   // 1%-50%
		cacheMax= Math.max(1, cacheMax);                 // >= 1mb
		long m1= Runtime.getRuntime().maxMemory()/100L * cachePct;
		long m2= Math.min(m1, cacheMax * 1024L * 1024L);
		ChunkCache.getSharedInstance().setMaxSize(m2);
	}

	public IndexProviderManager getIndexProviderManager() {
		return fIndexProviderManager;
	}

	/**
	 * Returns the pdom for the project. 
	 * @throws CoreException
	 */
	public IPDOM getPDOM(ICProject project) throws CoreException {
		synchronized (fProjectToPDOM) {
			IProject rproject = project.getProject();
			IPDOM pdom = (IPDOM) fProjectToPDOM.get(rproject);
			if (pdom == null) {
				pdom= new PDOMProxy();
				fProjectToPDOM.put(rproject, pdom);
			}
			return pdom;
		}
	}
	
	/**
	 * Returns the pdom for the project. The call to the method may cause 
	 * opening the database. In case there is a version mismatch the data
	 * base is cleared, in case it does not exist it is created. In any
	 * case a pdom ready to use is returned.
	 * @throws CoreException
	 */
	private WritablePDOM getOrCreatePDOM(ICProject project) throws CoreException {
		synchronized (fProjectToPDOM) {
			IProject rproject = project.getProject();
			IPDOM pdomProxy= (IPDOM) fProjectToPDOM.get(rproject);
			if (pdomProxy instanceof WritablePDOM) {
				return (WritablePDOM) pdomProxy;
			}

			String dbName= rproject.getPersistentProperty(dbNameProperty);
			File dbFile= null;
			if (dbName != null) {
				dbFile= fileFromDatabaseName(dbName);
				if (!dbFile.exists()) {
					dbFile= null;
					dbName= null;
				}
				else {
					ICProject currentCOwner= (ICProject) fFileToProject.get(dbFile);
					if (currentCOwner != null) {
						IProject currentOwner= currentCOwner.getProject();
						if (!currentOwner.exists()) {
							fFileToProject.remove(dbFile);
							dbFile.delete();
						}
						dbName= null;
						dbFile= null;
					}
				}
			}

			boolean fromScratch= false;
			if (dbName == null) {
				dbName = createNewDatabaseName(project);
				dbFile= fileFromDatabaseName(dbName);
				storeDatabaseName(rproject, dbName);
				fromScratch= true;
			}

			WritablePDOM pdom= new WritablePDOM(dbFile, new PDOMProjectIndexLocationConverter(rproject), LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
			if (!pdom.isSupportedVersion() || fromScratch) {
				try {
					pdom.acquireWriteLock();
				} catch (InterruptedException e) {
					throw new CoreException(CCorePlugin.createStatus(Messages.PDOMManager_creationOfIndexInterrupted, e));
				}
				if (fromScratch) {
					pdom.setCreatedFromScratch(true);
				}
				else {
					pdom.clear();
					pdom.setClearedBecauseOfVersionMismatch(true);
				}
				writeProjectPDOMProperties(pdom, rproject);
				pdom.releaseWriteLock();
			}
			pdom.addListener(this);
			
			fFileToProject.put(dbFile, project);
			fProjectToPDOM.put(rproject, pdom);
			if (pdomProxy instanceof PDOMProxy) {
				((PDOMProxy) pdomProxy).setDelegate(pdom);
			}
			return pdom;
		}
	}

	private void storeDatabaseName(IProject rproject, String dbName)
			throws CoreException {
		rproject.setPersistentProperty(dbNameProperty, dbName);
	}

	private String createNewDatabaseName(ICProject project) {
		String dbName;
		long time= System.currentTimeMillis();
		File file;
		do {
			dbName= getDefaultName(project, time++);
			file= fileFromDatabaseName(dbName);
		}
		while (file.exists());
		return dbName;
	}

	private File fileFromDatabaseName(String dbName) {
		return CCorePlugin.getDefault().getStateLocation().append(dbName).toFile();
	}

	private String getDefaultName(ICProject project, long time) {
		return project.getElementName() + "." + time + ".pdom";  //$NON-NLS-1$//$NON-NLS-2$
	}

	public String getDefaultIndexerId() {
		return getIndexerId(null);
	}

	public void setDefaultIndexerId(String indexerId) {
		IndexerPreferences.setDefaultIndexerId(indexerId);
	}
	
    public String getIndexerId(ICProject project) {
    	IProject prj= project != null ? project.getProject() : null;
    	return IndexerPreferences.get(prj, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_NO_INDEXER);
    }

    public void setIndexerId(final ICProject project, String indexerId) {
    	IProject prj= project.getProject();
    	IndexerPreferences.set(prj, IndexerPreferences.KEY_INDEXER_ID, indexerId);
    	CCoreInternals.savePreferences(prj);
    }
	
	protected void onPreferenceChange(ICProject cproject, PreferenceChangeEvent event) {
		if (IndexerPreferences.KEY_UPDATE_POLICY.equals(event.getKey())) {
			changeUpdatePolicy(cproject);
		}
		else {
			IProject project= cproject.getProject();
			if (project.exists() && project.isOpen()) {
				try {
					changeIndexer(cproject);
				}
				catch (Exception e) {
					CCorePlugin.log(e);
				}
			}
		}
	}

	private void changeUpdatePolicy(ICProject cproject) {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			IndexUpdatePolicy policy= getPolicy(cproject);
			if (policy != null) {
				IPDOMIndexerTask task= policy.changePolicy(IndexerPreferences.getUpdatePolicy(cproject.getProject()));
				if (task != null) {
					enqueue(task);
				}
			}
		}
	}

	private void changeIndexer(ICProject cproject) throws CoreException {
		assert !Thread.holdsLock(fProjectToPDOM);
		
		// if there is no indexer, don't touch the preferences.
		IPDOMIndexer oldIndexer= getIndexer(cproject);
		if (oldIndexer == null) {
			return;
		}
		
		IProject prj= cproject.getProject();
		String newid= IndexerPreferences.get(prj, IndexerPreferences.KEY_INDEXER_ID, IPDOMManager.ID_NO_INDEXER);
		Properties props= IndexerPreferences.getProperties(prj);
		
		synchronized (fUpdatePolicies) {
			oldIndexer= getIndexer(cproject);
			if (oldIndexer != null) {
				if (oldIndexer.getID().equals(newid)) {
					if (!oldIndexer.needsToRebuildForProperties(props)) {
						oldIndexer.setProperties(props);
						return;
					}
				}
				IPDOMIndexer indexer= createIndexer(cproject, newid, props);
				registerIndexer(cproject, indexer);
				createPolicy(cproject).clearTUs();
				enqueue(new PDOMRebuildTask(indexer));
			}
		}
		
		if (oldIndexer != null) {
			stopIndexer(oldIndexer);
		}
	}

	private void registerIndexer(ICProject project, IPDOMIndexer indexer) throws CoreException {
		assert Thread.holdsLock(fUpdatePolicies);
		indexer.setProject(project);
		registerPreferenceListener(project);
		createPolicy(project).setIndexer(indexer);
	}

	IPDOMIndexer getIndexer(ICProject project) {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			IndexUpdatePolicy policy= getPolicy(project);
			if (policy != null) {
				return policy.getIndexer();
			}
		}
		return null;
	}

	private void createIndexer(ICProject project, IProgressMonitor pm) {
		assert !Thread.holdsLock(fProjectToPDOM);
		IProject prj= project.getProject();
		try {
			synchronized (fUpdatePolicies) {
				WritablePDOM pdom= getOrCreatePDOM(project);
				Properties props= IndexerPreferences.getProperties(prj);
				IPDOMIndexer indexer= createIndexer(project, getIndexerId(project), props);

				boolean rebuild= 
					pdom.isClearedBecauseOfVersionMismatch() ||
					pdom.isCreatedFromScratch();
				if (rebuild) {
					if (IPDOMManager.ID_NO_INDEXER.equals(indexer.getID())) {
						rebuild= false;
					}
					pdom.setClearedBecauseOfVersionMismatch(false);
					pdom.setCreatedFromScratch(false);
				}
				if (!rebuild) {
					registerIndexer(project, indexer);
					IPDOMIndexerTask task= createPolicy(project).createTask();
					if (task != null) {
						enqueue(task);
					}
					else {
						enqueue(new TriggerNotificationTask(this, pdom));
					}
					return;
				}
			}

			// rebuild is required, try import first.
			TeamPDOMImportOperation operation= new TeamPDOMImportOperation(project);
			operation.run(pm);

			synchronized (fUpdatePolicies) {
				Properties props= IndexerPreferences.getProperties(prj);
				IPDOMIndexer indexer = createIndexer(project, getIndexerId(project), props);
				registerIndexer(project, indexer);
				createPolicy(project).clearTUs();

				IPDOMIndexerTask task= null;
				if (operation.wasSuccessful()) {
					task= new PDOMUpdateTask(indexer, IIndexManager.UPDATE_CHECK_TIMESTAMPS);
				}
				else {
					task= new PDOMRebuildTask(indexer);
				}
				enqueue(task);
			}
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
		
    private IPDOMIndexer createIndexer(ICProject project, String indexerId, Properties props) throws CoreException  {
    	IPDOMIndexer indexer = null;
    	// Look up in extension point
    	IExtension indexerExt = Platform.getExtensionRegistry().getExtension(CCorePlugin.INDEXER_UNIQ_ID, indexerId);
    	if (indexerExt != null) {
    		IConfigurationElement[] elements = indexerExt.getConfigurationElements();
    		for (int i = 0; i < elements.length; ++i) {
    			IConfigurationElement element = elements[i];
    			if ("run".equals(element.getName())) { //$NON-NLS-1$
    				try {
						indexer = (IPDOMIndexer)element.createExecutableExtension("class"); //$NON-NLS-1$
						indexer.setProperties(props);
					} catch (CoreException e) {
						CCorePlugin.log(e);
					} 
    				break;
    			}
    		}
    	}

    	// Unknown index, default to the null one
    	if (indexer == null) 
    		indexer = new PDOMNullIndexer();

		return indexer;
    }

	public void enqueue(IPDOMIndexerTask subjob) {
		final HashSet referencing= new HashSet();
		final IPDOMIndexer indexer = subjob.getIndexer();
		if (indexer != null) {
			getReferencingProjects(indexer.getProject().getProject(), referencing);
		}
    	synchronized (fTaskQueueMutex) {
    		int i=0;
    		for (Iterator it = fTaskQueue.iterator(); it.hasNext();) {
				final IPDOMIndexerTask task= (IPDOMIndexerTask) it.next();
				final IPDOMIndexer ti = task.getIndexer();
				if (ti != null && referencing.contains(ti.getProject().getProject())) {
					fTaskQueue.add(i, subjob);
					break;
				}
				i++;
			}
    		if (i == fTaskQueue.size()) {
        		fTaskQueue.addLast(subjob);
    		}
			if (fIndexerJob == null) {
				fCompletedSources= 0;
				fCompletedHeaders= 0;
				fIndexerJob = new PDOMIndexerJob(this);
				fIndexerJob.setRule(INDEXER_SCHEDULING_RULE);
				fIndexerJob.schedule();
	    		notifyState(IndexerStateEvent.STATE_BUSY);
			}
		}
    }
    
	private void getReferencingProjects(IProject prj, HashSet result) {
		LinkedList projectsToSearch= new LinkedList();
		projectsToSearch.add(prj);
		while (!projectsToSearch.isEmpty()) {
			prj= (IProject) projectsToSearch.removeFirst();
			if (result.add(prj)) {
				projectsToSearch.addAll(Arrays.asList(prj.getReferencingProjects()));
			}
		}
	}

	IPDOMIndexerTask getNextTask() {
		IPDOMIndexerTask result= null;
    	synchronized (fTaskQueueMutex) {
    		if (fTaskQueue.isEmpty()) {
    			fCurrentTask= null;
        		fIndexerJob= null;
        		notifyState(IndexerStateEvent.STATE_IDLE);
    		}
    		else {
    			if (fCurrentTask != null) {
    				IndexerProgress info= fCurrentTask.getProgressInformation();
    				fCompletedSources+= info.fCompletedSources;
    				fCompletedHeaders+= info.fCompletedHeaders;
    			}
    			result= fCurrentTask= (IPDOMIndexerTask)fTaskQueue.removeFirst();
    		}
		}
    	return result;
    }
    
    void cancelledJob(boolean byManager) {
    	synchronized (fTaskQueueMutex) {
    		fCurrentTask= null;
    		if (!byManager) {
    			fTaskQueue.clear();
    		}
    		if (fTaskQueue.isEmpty()) {
        		fIndexerJob= null;
        		notifyState(IndexerStateEvent.STATE_IDLE);
    		}
    		else {
    			fIndexerJob = new PDOMIndexerJob(this);
    			fIndexerJob.setRule(INDEXER_SCHEDULING_RULE);
    			fIndexerJob.schedule();
    		}
    	}
    }
        
    public boolean isIndexerIdle() {
    	synchronized (fTaskQueueMutex) {
    		return fCurrentTask == null && fTaskQueue.isEmpty();
    	}
    }

	void addProject(final ICProject cproject) {
		final IProject project = cproject.getProject();
		Job addProject= new Job(Messages.PDOMManager_StartJob_name) {
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("", 100); //$NON-NLS-1$
				if (project.isOpen() && isFullyCreated(project)) {
					syncronizeProjectSettings(project, new SubProgressMonitor(monitor, 1));
					if (getIndexer(cproject) == null) {
						createIndexer(cproject, new SubProgressMonitor(monitor, 99));
					}
				}
				return Status.OK_STATUS;
			}

			private void syncronizeProjectSettings(IProject project, IProgressMonitor monitor) {
				try {
					IFolder settings= project.getFolder(SETTINGS_FOLDER_NAME);  
					settings.refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (CoreException e) {
					CCorePlugin.log(e);
				}
				monitor.done();
			}

			public boolean belongsTo(Object family) {
				return family == PDOMManager.this;
			}
		};
		// in case a team provider does not implement a rule-factory, the
		// platform makes a pessimistic choice and locks the workspace. We
		// have to check for that.
		ISchedulingRule rule= project.getWorkspace().getRuleFactory().refreshRule(project.getFolder(SETTINGS_FOLDER_NAME));
		if (project.contains(rule)) {
			rule= new MultiRule(new ISchedulingRule[] {project, INDEXER_SCHEDULING_RULE });
		}
		else if (!rule.contains(project)) {
			rule= new MultiRule(new ISchedulingRule[] {rule, project, INDEXER_SCHEDULING_RULE });
		}
		addProject.setRule(rule); 
		addProject.setSystem(true);
		addProject.schedule();
	}

	private boolean isFullyCreated(IProject project) {
		ICProjectDescription desc= CProjectDescriptionManager.getInstance().getProjectDescription(project, false);
		if (desc != null && !desc.isCdtProjectCreating()) {
			return true;
		}
		return false;
	}

	private void registerPreferenceListener(ICProject project) {
		IProject prj= project.getProject();
		PCL pcl= (PCL) fPrefListeners.get(prj);
		if (pcl == null) {
			pcl= new PCL(project);
			fPrefListeners.put(prj, pcl);
		}
		IndexerPreferences.addChangeListener(prj, pcl);
        Preferences pref = CCorePlugin.getDefault().getPluginPreferences();
		pref.addPropertyChangeListener(pcl);
	}

	private void unregisterPreferenceListener(ICProject project) {
		IProject prj= project.getProject();
		PCL pcl= (PCL) fPrefListeners.remove(prj);
		if (pcl != null) {
			IndexerPreferences.removeChangeListener(prj, pcl);
	        Preferences pref = CCorePlugin.getDefault().getPluginPreferences();
			pref.removePropertyChangeListener(pcl);
		}
	}

	void changeProject(ICProject project, ITranslationUnit[] added, ITranslationUnit[] changed, ITranslationUnit[] removed) {
		assert !Thread.holdsLock(fProjectToPDOM);
		IPDOMIndexer indexer = getIndexer(project);
		if (indexer != null && indexer.getID().equals(IPDOMManager.ID_NO_INDEXER)) {
			return;
		}
		
		if (added.length > 0 || changed.length > 0 || removed.length > 0) {
			synchronized (fUpdatePolicies) {
				IndexUpdatePolicy policy= createPolicy(project);
				IPDOMIndexerTask task= policy.handleDelta(added, changed, removed);
				if (task != null) {
					enqueue(task);
				}
			}
		}
	}

	private IndexUpdatePolicy createPolicy(final ICProject project) {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			IndexUpdatePolicy policy= (IndexUpdatePolicy) fUpdatePolicies.get(project);
			if (policy == null) {
				policy= new IndexUpdatePolicy(project, IndexerPreferences.getUpdatePolicy(project.getProject()));
				fUpdatePolicies.put(project, policy);
			}
			return policy;
		}
	}

	private IndexUpdatePolicy getPolicy(final ICProject project) {
		synchronized (fUpdatePolicies) {
			return (IndexUpdatePolicy) fUpdatePolicies.get(project);
		}
	}

	public void preDeleteProject(ICProject cproject) {
		preRemoveProject(cproject, true); 
	}

	public void preCloseProject(ICProject cproject) {
		preRemoveProject(cproject, false);
	}

	private void preRemoveProject(ICProject cproject, final boolean delete) {
		assert !Thread.holdsLock(fProjectToPDOM);
		IPDOMIndexer indexer= getIndexer(cproject);
		if (indexer != null) {
			stopIndexer(indexer);
		}
    	unregisterPreferenceListener(cproject);
    	Object pdom= null;
    	synchronized (fProjectToPDOM) {
			IProject rproject= cproject.getProject();
    		pdom = fProjectToPDOM.remove(rproject);
    		// if the project is closed allow to reuse the pdom.
    		if (pdom instanceof WritablePDOM && !delete) {
    			fFileToProject.remove(((WritablePDOM) pdom).getDB().getLocation());
    		}
    	}

    	if (pdom instanceof WritablePDOM) {
    		final WritablePDOM finalpdom= (WritablePDOM) pdom;
    		Job job= new Job(Messages.PDOMManager_ClosePDOMJob) {
    			protected IStatus run(IProgressMonitor monitor) {
        			try {
        				finalpdom.acquireWriteLock();
        				try {
        					finalpdom.close();
        					if (delete) {
        						finalpdom.getDB().getLocation().delete();
        					}
        				} catch (CoreException e) {
        					CCorePlugin.log(e);
        				}
        				finally {
        					finalpdom.releaseWriteLock();
        				}
        			} catch (InterruptedException e) {
        			}
    				return Status.OK_STATUS;
    			}
    		};
    		job.setSystem(true);
    		job.schedule();
    	}
    	
		synchronized (fUpdatePolicies) {
			fUpdatePolicies.remove(cproject);
		}
	}
	
	void removeProject(ICProject cproject, ICElementDelta delta) {
    	synchronized (fProjectToPDOM) {
			IProject rproject= cproject.getProject();
			fProjectToPDOM.remove(rproject);
			// don't remove the location, because it may not be reused when the project was deleted.
    	}
	}

	private void stopIndexer(IPDOMIndexer indexer) {
		assert !Thread.holdsLock(fProjectToPDOM);
		ICProject project= indexer.getProject();
		synchronized (fUpdatePolicies) {
			IndexUpdatePolicy policy= getPolicy(project);
			if (policy != null) {
				if (policy.getIndexer() == indexer) {
					policy.clearTUs();
					policy.setIndexer(null);
				}
			}					
		}
		cancelIndexerJobs(indexer);
	}

	private void cancelIndexerJobs(IPDOMIndexer indexer) {
		PDOMIndexerJob jobToCancel= null;
		synchronized (fTaskQueueMutex) {
			for (Iterator iter = fTaskQueue.iterator(); iter.hasNext();) {
				IPDOMIndexerTask task= (IPDOMIndexerTask) iter.next();
				if (task.getIndexer() == indexer) {
					iter.remove();
				}
			}
			jobToCancel= fIndexerJob;
		}
		
		if (jobToCancel != null) {
			assert !Thread.holdsLock(fTaskQueueMutex);
			jobToCancel.cancelJobs(indexer, true);
		}
	}    

	public void reindex(ICProject project) {
		assert !Thread.holdsLock(fProjectToPDOM);
		IPDOMIndexer indexer= null;
		synchronized (fUpdatePolicies) {
			indexer= getIndexer(project);
		}
		// don't attempt to hold lock on indexerMutex while cancelling
		if (indexer != null) {
			cancelIndexerJobs(indexer);
		}
		
		synchronized(fUpdatePolicies) {
			indexer= getIndexer(project);
			if (indexer != null) {
				createPolicy(project).clearTUs();
				enqueue(new PDOMRebuildTask(indexer));
			}
		}
	}

	public void addIndexChangeListener(IIndexChangeListener listener) {
		fChangeListeners.add(listener);
	}

	public void removeIndexChangeListener(IIndexChangeListener listener) {
		fChangeListeners.remove(listener);
	}
	
	public void addIndexerStateListener(IIndexerStateListener listener) {
		fStateListeners.add(listener);
	}

	public void removeIndexerStateListener(IIndexerStateListener listener) {
		fStateListeners.remove(listener);
	}

    private void notifyState(final int state) {
    	if (state == IndexerStateEvent.STATE_IDLE) {
    		synchronized(fTaskQueueMutex) {
    			fTaskQueueMutex.notifyAll();
    		}
    	}
    	
    	if (fStateListeners.isEmpty()) {
    		return;
    	}
    	Job notify= new Job(Messages.PDOMManager_notifyJob_label) {
    		protected IStatus run(IProgressMonitor monitor) {
    			fIndexerStateEvent.setState(state);
    			Object[] listeners= fStateListeners.getListeners();
    			monitor.beginTask(Messages.PDOMManager_notifyTask_message, listeners.length);
    			for (int i = 0; i < listeners.length; i++) {
    				final IIndexerStateListener listener = (IIndexerStateListener) listeners[i];
    				SafeRunner.run(new ISafeRunnable(){
    					public void handleException(Throwable exception) {
    						CCorePlugin.log(exception);
    					}
    					public void run() throws Exception {
    						listener.indexChanged(fIndexerStateEvent);
    					}
    				});
    				monitor.worked(1);
    			}
    			return Status.OK_STATUS;
    		}
    	};
		notify.setRule(NOTIFICATION_SCHEDULING_RULE);
    	notify.setSystem(true);
    	notify.schedule();
	}

	public void handleChange(PDOM pdom) {
		if (fChangeListeners.isEmpty()) {
			return;
		}
		
		ICProject project;
		synchronized (fProjectToPDOM) {
			project = (ICProject) fFileToProject.get(pdom.getPath());
		}		
		
		if (project != null) {
			final ICProject finalProject= project;
			Job notify= new Job(Messages.PDOMManager_notifyJob_label) {
				protected IStatus run(IProgressMonitor monitor) {
					fIndexChangeEvent.setAffectedProject(finalProject);
					Object[] listeners= fChangeListeners.getListeners();
					monitor.beginTask(Messages.PDOMManager_notifyTask_message, listeners.length);
					for (int i = 0; i < listeners.length; i++) {
						final IIndexChangeListener listener = (IIndexChangeListener) listeners[i];
						SafeRunner.run(new ISafeRunnable(){
							public void handleException(Throwable exception) {
								CCorePlugin.log(exception);
							}
							public void run() throws Exception {
								listener.indexChanged(fIndexChangeEvent);
							}
						});
						monitor.worked(1);
					}
					return Status.OK_STATUS;
				}
			};
			notify.setRule(NOTIFICATION_SCHEDULING_RULE);
			notify.setSystem(true);
			notify.schedule();
		}
	}

	public boolean joinIndexer(final int waitMaxMillis, final IProgressMonitor monitor) {
		assert monitor != null;
		Thread th= null;
		if (waitMaxMillis != FOREVER) {
			th= new Thread() {
				public void run() {
					try {
						Thread.sleep(waitMaxMillis);
						monitor.setCanceled(true);
					}
					catch (InterruptedException e) {
					}
				}
			};
			th.setDaemon(true);
			th.start();
		}
		try {
			try {
				Job.getJobManager().join(this, monitor);
				return true;
			} catch (OperationCanceledException e1) {
			} catch (InterruptedException e1) {
			}
			return Job.getJobManager().find(this).length == 0;
		}
		finally {
			if (th != null) {
				th.interrupt();
			}
		}
	}
	
	public boolean joinIndexerOld(int waitMaxMillis, IProgressMonitor monitor) {
		final int totalTicks = 1000;
		monitor.beginTask(Messages.PDOMManager_JoinIndexerTask, totalTicks);
		long limit= System.currentTimeMillis()+waitMaxMillis;
		try {
			int currentTicks= 0;
			while (true) {
				if (monitor.isCanceled()) {
					return false;
				}
				currentTicks= getMonitorMessage(monitor, currentTicks, totalTicks);
				synchronized(fTaskQueueMutex) {
					if (isIndexerIdle()) {
						return true;
					}
					int wait= 1000;
					if (waitMaxMillis >= 0) {
						int rest= (int) (limit - System.currentTimeMillis());
						if (rest < wait) {
							if (rest <= 0) {
								return false;
							}
							wait= rest;
						}
					}

					try {
						fTaskQueueMutex.wait(wait);
					} catch (InterruptedException e) {
						return false;
					}
				}
			}
		}
		finally {
			monitor.done();
		}
	}
	
	int getMonitorMessage(IProgressMonitor monitor, int currentTicks, int base) {
		assert !Thread.holdsLock(fTaskQueueMutex);
		int remainingSources= 0;
		int completedSources= 0;
		int completedHeaders= 0;
		int totalEstimate= 0;
		String detail= null;
		IndexerProgress info;
		synchronized (fTaskQueueMutex) {
			completedHeaders= fCompletedHeaders;
			completedSources= fCompletedSources;
			totalEstimate= fCompletedHeaders+fCompletedSources;
			for (Iterator iter = fTaskQueue.iterator(); iter.hasNext();) {
				IPDOMIndexerTask task = (IPDOMIndexerTask) iter.next();
				info= task.getProgressInformation();
				remainingSources+= info.getRemainingSources();
				totalEstimate+= info.getTimeEstimate();
			}
			if (fCurrentTask != null) {
				info= fCurrentTask.getProgressInformation();
				remainingSources+= info.getRemainingSources();
				completedHeaders+= info.fCompletedHeaders;
				completedSources+= info.fCompletedSources;
				detail= PDOMIndexerJob.sMonitorDetail;
				totalEstimate+= info.getTimeEstimate();
			}
		}
		
		int totalSources = remainingSources+completedSources;
		String msg= MessageFormat.format(Messages.PDOMManager_indexMonitorDetail, new Object[] { 
					new Integer(completedSources), new Integer(totalSources), 
					new Integer(completedHeaders)}); 
		if (detail != null) {
			msg= msg+ ": " + detail; //$NON-NLS-1$
		}
		monitor.subTask(msg);
		
		if (completedSources > 0 && totalEstimate >= completedSources) {
			int newTick= completedSources*base/totalEstimate;
			if (newTick > currentTicks) {
				monitor.worked(newTick-currentTicks);
				return newTick;
			}
		}
		return currentTicks;
	}


	public IWritableIndex getWritableIndex(ICProject project) throws CoreException {
		return fIndexFactory.getWritableIndex(project);
	}

	public IIndex getIndex(ICProject project) throws CoreException {
		return fIndexFactory.getIndex(new ICProject[] {project}, 0);
	}

	public IIndex getIndex(ICProject[] projects) throws CoreException {
		return fIndexFactory.getIndex(projects, 0);
	}

	public IIndex getIndex(ICProject project, int options) throws CoreException {
		return fIndexFactory.getIndex(new ICProject[] {project}, options);
	}

	public IIndex getIndex(ICProject[] projects, int options) throws CoreException {
		return fIndexFactory.getIndex(projects, options);
	}
	
	/**
     * Exports the project PDOM to the specified location, rewriting locations with
     * the specified location converter.
     * <br>
     * Note. This will acquire a write lock while the pdom is exported
	 * @param targetLocation a location that does not currently exist
	 * @param newConverter
	 * @throws CoreException
	 * @throws IllegalArgumentException if a file exists at targetLocation
	 */
	public void exportProjectPDOM(ICProject cproject, File targetLocation, final IIndexLocationConverter newConverter) throws CoreException {
		if(targetLocation.exists()) {
			boolean deleted= targetLocation.delete();
			if(!deleted) {
				throw new IllegalArgumentException(
						MessageFormat.format(Messages.PDOMManager_ExistingFileCollides,
								new Object[] {targetLocation})
				);
			}
		}
		try {
			// copy it
			PDOM pdom= getOrCreatePDOM(cproject);
			pdom.acquireReadLock();
			String oldID= null;
			try {
				oldID= pdom.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
				pdom.flush();
				FileChannel to = new FileOutputStream(targetLocation).getChannel();
				pdom.getDB().transferTo(to);
				to.close();
			} finally {
				pdom.releaseReadLock();
			}

			// overwrite internal location representations
			final WritablePDOM newPDOM = new WritablePDOM(targetLocation, pdom.getLocationConverter(), LanguageManager.getInstance().getPDOMLinkageFactoryMappings());			
			newPDOM.acquireWriteLock();
			try {
				newPDOM.rewriteLocations(newConverter);

				// ensure fragment id has a sensible value, in case callee's do not
				// overwrite their own values
				newPDOM.setProperty(IIndexFragment.PROPERTY_FRAGMENT_ID, "exported."+oldID); //$NON-NLS-1$
				newPDOM.close();
			} finally {
				newPDOM.releaseWriteLock();
			}
		} catch(IOException ioe) {
			throw new CoreException(CCorePlugin.createStatus(ioe.getMessage()));
		} catch(InterruptedException ie) {
			throw new CoreException(CCorePlugin.createStatus(ie.getMessage()));
		}
	}

	/**
	 * Resets the pdom for the project with the provided stream. 
	 * @throws CoreException
	 * @throws OperationCanceledException in case the thread was interrupted
	 * @since 4.0
	 */
	public void importProjectPDOM(ICProject project, InputStream stream) throws CoreException, IOException {
		// make a copy of the database
		String newName= createNewDatabaseName(project);
		File newFile= fileFromDatabaseName(newName);
		OutputStream out= new FileOutputStream(newFile);
		try {
			byte[] buffer= new byte[2048];
			int read;
			while ((read= stream.read(buffer)) >= 0) {
				out.write(buffer, 0, read);
			}
		}
		finally {
			out.close();
		}
		
		WritablePDOM pdom= (WritablePDOM) getPDOM(project);
		try {
			pdom.acquireWriteLock();
		} catch (InterruptedException e) {
			throw new OperationCanceledException();
		}
		try {
			pdom.reloadFromFile(newFile);
			storeDatabaseName(project.getProject(), newName);
			writeProjectPDOMProperties(pdom, project.getProject());
		}
		finally {
			pdom.releaseWriteLock();
		}
	}
	
	public void export(ICProject project, String location, int options, IProgressMonitor monitor) throws CoreException {
		TeamPDOMExportOperation operation= new TeamPDOMExportOperation(project);
		operation.setTargetLocation(location);
		operation.setOptions(options);
		operation.run(monitor);
	}
	
	/**
	 * Write metadata appropriate for a project pdom
	 * @param pdom the pdom to write to
	 * @param project the project to write metadata about
	 * @throws CoreException
	 */
	public static void writeProjectPDOMProperties(WritablePDOM pdom, IProject project) throws CoreException {
		String DELIM = "\0"; //$NON-NLS-1$
		String id= CCorePlugin.PLUGIN_ID + ".pdom.project." + DELIM + project.getName() + DELIM; //$NON-NLS-1$
		pdom.setProperty(IIndexFragment.PROPERTY_FRAGMENT_ID, id);
	}

	public boolean isProjectIndexed(ICProject proj) {
		return !IPDOMManager.ID_NO_INDEXER.equals(getIndexerId(proj));
	}

	public void update(ICElement[] tuSelection, int options) throws CoreException {
		Map projectsToElements= splitSelection(tuSelection);
		for (Iterator i = projectsToElements.entrySet().iterator(); i
				.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			ICProject project = (ICProject) entry.getKey();
			List filesAndFolders = (List) entry.getValue();
			
			update(project, filesAndFolders, options);
		}
	}

	/**
	 * computes a map from projects to a collection containing the minimal
	 * set of folders and files specifying the selection.
	 */
	private Map splitSelection(ICElement[] tuSelection) {
		HashMap result= new HashMap();
		allElements: for (int i = 0; i < tuSelection.length; i++) {
			ICElement element = tuSelection[i];
			if (element instanceof ICProject || element instanceof ICContainer || element instanceof ITranslationUnit) {
				ICProject project= element.getCProject();
				ArrayList set= (ArrayList) result.get(project);
				if (set == null) {
					set= new ArrayList();
					result.put(project, set);
				}
				for (int j= 0; j<set.size(); j++) {
					ICElement other= (ICElement) set.get(j);
					if (contains(other, element)) {
						continue allElements;
					}
					else if (contains(element, other)) {
						set.set(j, element);
						continue allElements;
					}					
				}
				set.add(element);
			}
		}
		return result;
	}
	
	private boolean contains(final ICElement a, ICElement b) {
		if (a.equals(b)) {
			return true;
		}
		b= b.getParent();
		if (b == null) {
			return false;
		}
		return contains(a, b);
	}

	private void update(ICProject project, List filesAndFolders, int options) throws CoreException {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			IPDOMIndexer indexer= getIndexer(project);
			PDOMUpdateTask task= new PDOMUpdateTask(indexer, options);
			task.setTranslationUnitSelection(filesAndFolders);
			if (indexer != null) {
				enqueue(task);
			}
		}
	}

	void handlePostBuildEvent() {
		assert !Thread.holdsLock(fProjectToPDOM);
		synchronized (fUpdatePolicies) {
			for (Iterator i = fUpdatePolicies.values().iterator(); i.hasNext();) {
				IndexUpdatePolicy policy= (IndexUpdatePolicy) i.next();
				IPDOMIndexerTask task= policy.createTask();
				if (task != null) {
					enqueue(task);
				}
			}
		}
	}
}

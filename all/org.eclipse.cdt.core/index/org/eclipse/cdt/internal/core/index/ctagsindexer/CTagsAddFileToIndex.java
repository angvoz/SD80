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
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.IOException;

import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Bogdan Gheorghe
 */
public abstract class CTagsAddFileToIndex extends CTagsIndexRequest {
    IFile resource;

	public CTagsAddFileToIndex(IFile resource, IPath indexPath, CTagsIndexer indexer) {
		super(indexPath, indexer);
		this.resource = resource;
	}
	
	
	public boolean execute(IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) return true;
	
		/* ensure no concurrent write access to index */
		IIndex index = indexer.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = indexer.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired
		try {
			monitor.enterWrite(); // ask permission to write
			if (!indexDocument(index)) return false;
		} catch (IOException e) {
			org.eclipse.cdt.internal.core.model.Util.log(null, "Index I/O Exception: " + e.getMessage() + " on File: " + resource.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.resource + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		} finally {
			monitor.exitWrite(); // free write lock
		}
		return true;
	}
	
	protected abstract boolean indexDocument(IIndex index) throws IOException;
	
	public String toString() {
		return "indexing " + this.resource.getFullPath(); //$NON-NLS-1$
	}
	
	/**
	 * The resource being indexed
	 * @return
	 */
	public IResource getResource(){
		return resource;
	}
}

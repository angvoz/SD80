/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.IOException;

import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.cdt.internal.core.search.processing.JobManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsRemoveFromIndex extends CTagsIndexRequest {
    String resourceName;

	public CTagsRemoveFromIndex(String resourceName, IPath indexPath, CTagsIndexer indexer) {
		super(indexPath, indexer);
		this.resourceName = resourceName;
	}
	
	public boolean execute(IProgressMonitor progressMonitor) {

		if (progressMonitor != null && progressMonitor.isCanceled()) return true;

		/* ensure no concurrent write access to index */
		IIndex index = indexer.getIndex(this.indexPath, true, /*reuse index file*/ false /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = indexer.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired

		try {
			monitor.enterWrite(); // ask permission to write
			index.remove(resourceName);
		} catch (IOException e) {
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to remove " + this.resourceName + " from index because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		} finally {
			monitor.exitWrite(); // free write lock
		}
		return true;
	}
	
	public String toString() {
		return "removing " + this.resourceName + " from index " + this.indexPath; //$NON-NLS-1$ //$NON-NLS-2$
	}
}

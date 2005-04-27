package org.eclipse.cdt.internal.core.index.nullindexer;

import java.io.IOException;

import org.eclipse.cdt.core.AbstractCExtension;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexStorage;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.search.processing.IIndexJob;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;

public class NullIndexer extends AbstractCExtension implements ICDTIndexer {

	public int getIndexerFeatures() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void addRequest(IProject project, IResourceDelta delta, int kind) {
		// TODO Auto-generated method stub

	}

	public void removeRequest(IProject project, IResourceDelta delta, int kind) {
		// TODO Auto-generated method stub

	}

	public void indexJobFinishedNotification(IIndexJob job) {
		// TODO Auto-generated method stub

	}

	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public void notifyIdle(long idlingTime) {
		// TODO Auto-generated method stub

	}

	public void notifyIndexerChange(IProject project) {
		// TODO Auto-generated method stub

	}

	public boolean isIndexEnabled(IProject project) {
		// TODO Auto-generated method stub
		return false;
	}

	public IIndexStorage getIndexStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	public IIndex getIndex(IPath path, boolean reuseExistingFile,
			boolean createIfMissing) {
		// TODO Auto-generated method stub
		return null;
	}

	public void indexerRemoved(IProject project) {
		// TODO Auto-generated method stub

	}

	public void index(IFile document, IIndexerOutput output) throws IOException {
		// TODO Auto-generated method stub

	}

	public boolean shouldIndex(IFile file) {
		// TODO Auto-generated method stub
		return false;
	}

}

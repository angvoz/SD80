package org.eclipse.ffs.internal.core;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.internal.localstore.FileSystemResourceManager;
import org.eclipse.core.internal.localstore.IHistoryStore;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

@SuppressWarnings("restriction")
public class FFSResourceManager extends FileSystemResourceManager {

	public FFSResourceManager(Workspace workspace, IHistoryStore historyStore) {
		super(workspace);
		_historyStore = historyStore;
	}

	@Override
	public IPath locationFor(IResource target) {
		URI locationURI = locationURIFor(target);
		if (locationURI != null)
		{
			try {
				IFileStore testLocationStore = EFS.getStore(locationURI);
				if (testLocationStore != null)
				{
					java.io.File storeAsFile = testLocationStore.toLocalFile(EFS.NONE, null);
					if (storeAsFile != null)
						return new Path(storeAsFile.getAbsolutePath());
				}
			} catch (CoreException e) { }		
		}			
		return super.locationFor(target);
	}

}

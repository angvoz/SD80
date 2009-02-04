/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.core.parser;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;
import org.eclipse.cdt.internal.core.util.ILRUCacheable;
import org.eclipse.cdt.internal.core.util.OverflowingLRUCache;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * This is the CodeReaderBuffer used to cache CodeReaders for the ICodeReaderFactory
 * when working with saved copies (primarily SavedCodeReaderFactory).
 * 
 * @author dsteffle
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CodeReaderCache implements ICodeReaderCache {
	/**
	 * The string used to identify this CodeReaderCache.  Mainly used for preferences.
	 */
	public static final String CODE_READER_BUFFER = CCorePlugin.PLUGIN_ID + ".codeReaderCache"; //$NON-NLS-1$
	
	/**
	 * The default size of the cache in MB.
	 */
	public static final int DEFAULT_CACHE_SIZE_IN_MB = 64;
	
	/**
	 * A String value of the default size of the cache.
	 */
	public static final String DEFAULT_CACHE_SIZE_IN_MB_STRING = String.valueOf(DEFAULT_CACHE_SIZE_IN_MB);
	private static final int MB_TO_KB_FACTOR = 1024;
	private CodeReaderLRUCache cache = null; // the actual cache
	private final IResourceChangeListener listener = new UpdateCodeReaderCacheListener(this);

	private class UpdateCodeReaderCacheListener implements IResourceChangeListener {
		ICodeReaderCache c = null;
		
		/**
		 * Create the UpdateCodeReaderCacheListener used to dispatch events to remove CodeReaders 
		 * from the cache when a resource is changed and detected in Eclipse.
		 * @param cache
		 */
		public UpdateCodeReaderCacheListener(ICodeReaderCache cache) {
			this.c = cache;
		}
		
		private class RemoveCacheJob extends Job {
			private static final String REMOVE_CACHE = "Remove Cache"; //$NON-NLS-1$
			ICodeReaderCache cache1 = null;
			IResourceChangeEvent event = null;
			
			/**
			 * Create a RemoveCacheJob used to run as a separate thread to remove a CodeReader from the cache.
			 */
			public RemoveCacheJob(ICodeReaderCache cache, IResourceChangeEvent event) {
				super(REMOVE_CACHE);
				this.cache1 = cache;
				this.event = event;
			}
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (event.getSource() instanceof IWorkspace && event.getDelta() != null) {
					removeKeys(event.getDelta().getAffectedChildren());
				}
				event = null;
				return Status.OK_STATUS;
			}
            
            private void removeKeys(IResourceDelta[] deltas) {
                for (IResourceDelta delta : deltas) {
                    if (delta.getResource().getType() == IResource.PROJECT || delta.getResource().getType() == IResource.FOLDER) {
                        removeKeys(delta.getAffectedChildren());
                    } else if (delta.getResource() instanceof IFile && ((IFile)delta.getResource()).getLocation() != null) {
                        removeKey(((IFile)delta.getResource()).getLocation().toOSString());
                    }
                }
            }
            
            private void removeKey(String key) {
                if (key != null && cache1 != null)
                    cache1.remove(key);
            }
			
		}
		
		/**
		 * Identifies when a resource was chaned and schedules a new RemoveCacheJob.
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			if (c instanceof CodeReaderCache)
				new RemoveCacheJob(c, event).schedule();
		}
	}
	
	/**
	 * Creates a CodeReaderCache and sets the size of the CodeReaderCache in MB.  Creating a new
	 * CodeReaderCache also adds an UpdateCodeReaderCacheListener to the workspace so that when 
	 * a resource is changed then the CodeReader for that resource is removed from this cache.
	 * 
	 * @param size initial size of the CodeReaderCache in terms of MB
	 */
	public CodeReaderCache(int size) {
		cache = new CodeReaderLRUCache(size * MB_TO_KB_FACTOR);
	}
	
	@Override
	protected void finalize() throws Throwable {
		flush();
		super.finalize();
	}
	
	/**
	 * Get a CodeReader from the cache.  The key is the char[] filename of the CodeReader to retrieve.
	 * @param key the path of the CodeReader to retrieve
	 */
	public synchronized CodeReader get(String key) {
		CodeReader result = null;
		if (cache.getSpaceLimit() > 0) 
			result= cache.get(key);
		
		if (result != null)
			return result;
		
		// for efficiency: check File.exists before ParserUtil#createReader()
		// bug 100947 fix: don't want to attempt to create a code reader if there is no file for the key
		final File jfile = new File(key);
		if (!(jfile.exists()))
			return null;
			
		try {
			IResource file = ParserUtil.getResourceForFilename(key);
			if (file instanceof IFile) {
				key= InternalParserUtil.normalizePath(key, (IFile) file);
				result= cache.get(key);
				if (result != null)
					return result;

				result=  InternalParserUtil.createWorkspaceFileReader(key, (IFile) file);
			}
			key= jfile.getCanonicalPath();
			result= cache.get(key);
			if (result != null)
				return result;

			result= InternalParserUtil.createExternalFileReader(key);
			if (cache.getSpaceLimit() > 0) 
				put(result);

			return result;
		} catch (CoreException ce) {
		} catch (IOException e) {
		} catch (IllegalStateException e) {
		}
		return null;
	}
	
	/**
	 * Put a CodeReader into the Cache. 
	 * @param key
	 * @param value
	 * @return
	 */
	private synchronized CodeReader put(CodeReader value) {
		if (value==null) return null;
		if (cache.isEmpty()) {
			if (ResourcesPlugin.getWorkspace() != null)
				ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
		}
		return cache.put(String.valueOf(value.filename), value);
	}
	
	/**
	 * Sets the max cache size of this cache in terms of MB.
	 * @param size
	 */
	public void setCacheSize(int size) {
		cache.setSpaceLimit(size * MB_TO_KB_FACTOR);
	}
	
	
	/**
	 * This is a wrapper for entries to put into the OverflowingLRUCache (required to determine the
	 * size of entries relative to the CodeReader's file size).
	 * 
	 * Although the size of the CodeReaderCache is specified in terms of MB, the actual granularity of
	 * the cache is KB.
	 * 
	 * @author dsteffle
	 *
	 */
	private class CodeReaderCacheEntry implements ILRUCacheable {

		private static final double CHAR_TO_KB_FACTOR = 1024;
		CodeReader reader = null;
		int size = 0; // used to specify the size of the CodeReader in terms of KB

		public CodeReaderCacheEntry(CodeReader value) {
			this.reader = value;
			size = (int)Math.ceil(reader.buffer.length / CHAR_TO_KB_FACTOR); // get the size of the file in terms of KB 
		}

		public int getCacheFootprint() {
			return size;
		}
		
		public CodeReader getCodeReader() {
			return reader;
		}
	}

	/**
	 * This class is a wrapper/implementor class for OverflowingLRUCache.
	 * 
	 * It uses CodeReaderCacheEntry (which implements ILRUCacheable) to specify that the size of
	 * the cache should be relative to the size of the entries and not the number of entries. 
	 * 
	 * @author dsteffle
	 *
	 */
	private class CodeReaderLRUCache extends OverflowingLRUCache<String, CodeReaderCacheEntry> {
		
		
		/**
		 * Creates a new CodeReaderLRUCache with a specified initial maximum size.
		 * @param size the maximum size of the cache in terms of MB
		 */
		public CodeReaderLRUCache(int size) {
			super(); // need to initialize the LRUCache with super() so that the size of the hashtable isn't relative to the size in MB
			this.setSpaceLimit(size);
		}
		
		// must be overloaded, required to remove entries from the cache
		@Override
		protected boolean close(LRUCacheEntry<String,CodeReaderCacheEntry> entry) {
			Object obj = remove(entry._fKey);
			
			if (obj != null) 
				return true;
						
			return false;
		}

		@Override
		protected OverflowingLRUCache<String,CodeReaderCacheEntry> newInstance(int size, int overflow) {
			return null;
		}
		
		/**
		 * Removes an entry from the cache and returns the entry that was removed if found.
		 * Otherwise null is returned. 
		 */
		@Override
		public CodeReader remove(String key) {
			Object removed = removeKey(key);
						
			if (removed instanceof CodeReaderCacheEntry)
				return ((CodeReaderCacheEntry)removed).getCodeReader();
			
			return null;
		}

		/**
		 * Puts a CodeReader into the cache by wrapping it with a CodeReaderCacheEntry first. 
		 * This way the proper size of the element in the cache can be determined
		 * via the CodeReaderCacheEntry.
		 */
		public CodeReader put(String key, CodeReader value) {
			CodeReaderCacheEntry entry = new CodeReaderCacheEntry(value);
			CodeReaderCacheEntry ret = put(key, entry);
			if (ret != null)
				return ret.getCodeReader();
			
			return null;
		}

		/**
		 * Retrieves a CodeReader from the cache corresponding to the path specified by the key.
		 */
		public CodeReader get(String key) {
			CodeReaderCacheEntry obj = peek(key);
			if (obj != null)
				return obj.getCodeReader();

			return null;
		}

	}
	
	/**
	 * Removes the CodeReader from the cache corresponding to the path specified by the key and 
	 * returns the CodeReader that was removed.  If no CodeReader is removed then null is returned.
	 * @param key 
	 */
	public synchronized CodeReader remove(String key) {
		CodeReader removed= cache.remove(key);
		if (cache.isEmpty()) {
			if (ResourcesPlugin.getWorkspace() != null)
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		}
		return removed;
	}
	
	/**
	 * Returns the current size of the cache.  For the CodeReaderCache this is in MB.
	 */
	public int getCurrentSpace() {
		return cache.getCurrentSpace(); 
	}

	public void flush() {
		cache.flush();
		if (ResourcesPlugin.getWorkspace() != null)
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
	}

}

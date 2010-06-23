/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;

public class PersistentCache {

	private class CacheEntry {

		private String identifier;
		private long freshness;
		private Serializable data;
		private IPath location;

		public CacheEntry(String identifier, Serializable data, long freshness) {
			this.identifier = identifier;
			this.freshness = freshness;
			this.data = data;
			this.location = getDefaultLocation().append(Integer.toString(identifier.hashCode())).addFileExtension("txt");;
		}

		public CacheEntry(ObjectInputStream ois) throws Exception {		
			this.identifier = (String) ois.readObject();
			this.freshness = (Long) ois.readObject();
			this.data = (Serializable) ois.readObject();		
			this.location = getDefaultLocation().append(Integer.toString(identifier.hashCode())).addFileExtension("txt");;
		}
		
		public IPath getLocation() {
			return location;
		}

		@SuppressWarnings("unchecked")
		private <T> T getData(Class<T> expectedClass) {
			if (expectedClass.isInstance(data))
				return (T) data;
			else
				return null;
		}

		private long getFreshness() {
			return freshness;
		}

		private void flush() throws Exception {
			File cacheFile = getLocation().toFile();
			if (!cacheFile.exists())
			{
				cacheFile.getParentFile().mkdirs();
				cacheFile.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(cacheFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(identifier);
			oos.writeObject(freshness);
			oos.writeObject(data);
			fos.close();
		}

		public void delete() {
			File cacheFile = getLocation().toFile();
			if (cacheFile.exists())
			{
				cacheFile.delete();
			}
		}
		
	}
	
	private Map<String, CacheEntry> caches = Collections.synchronizedMap(new HashMap<String, CacheEntry>());
	private IPath defaultLocation;

	public PersistentCache(IPath defaultLocation) {
		this.defaultLocation = defaultLocation;
	}

	public CacheEntry getCache(String identifier){
		CacheEntry result = caches.get(identifier);
		return result;		
	}

	public <T> T getCachedData(String cacheIdentifier, Class<T> expectedClass, long freshness) {
	// 	freshness  = 0;
		CacheEntry cache = caches.get(cacheIdentifier);
		
		if (cache == null)
			cache = loadCachedData(getDefaultLocation(), cacheIdentifier);
		
		if (cache != null)
		{
			long cachedFreshness = cache.getFreshness();
			T result = cache.getData(expectedClass);
			if (cachedFreshness == freshness && result != null)
			{
				return result;
			}
			else
			{
				caches.remove(cache);
				cache.delete();
			}
		}		
		return null;
	}

	private CacheEntry loadCachedData(IPath location, String cacheIdentifier) {
		IPath flushPath = location.append(Integer.toString(cacheIdentifier.hashCode())).addFileExtension("txt");

		if (flushPath.toFile().exists())
		{
			try {
				final ClassLoader classLoader = EDCDebugger.getDefault().getClass().getClassLoader();
				FileInputStream fis = new FileInputStream(flushPath.toFile());
				ObjectInputStream ois = new ObjectInputStream(fis) {

					@Override
					protected Class<?> resolveClass(ObjectStreamClass desc)
					throws IOException, ClassNotFoundException {
						String name = desc.getName();
						try {
							return classLoader.loadClass(name);
						} catch (ClassNotFoundException e) {
							return super.resolveClass(desc);
						}
					}};
					return new CacheEntry(ois);
			} catch (Exception e) {}
		}

		return null;
	}

	public void putCachedData(String cacheIdentifier, Serializable data, long freshness)
	{
		CacheEntry cache = new CacheEntry(cacheIdentifier, data, freshness);
		caches.put(cacheIdentifier, cache);
	}

	public void flushAll() throws Exception {
		Collection<CacheEntry> allCaches = caches.values();
		for (CacheEntry entry : allCaches) {
			entry.flush();
		}
		caches.clear();
	}

	public IPath getDefaultLocation() {
		return defaultLocation;
	}

	public void setDefaultLocation(IPath defaultLocation) {
		this.defaultLocation = defaultLocation;
	}

}

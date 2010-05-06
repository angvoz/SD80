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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
		}
		
		public IPath getLocation() {
			return location;
		}

		private Object getData() {
			return data;
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

	public Object getCachedData(String cacheIdentifier, long freshness) {
		CacheEntry cache = caches.get(cacheIdentifier);
		
		if (cache == null)
			cache = loadCachedData(getDefaultLocation(), cacheIdentifier);
		
		if (cache != null)
		{
			long cachedFreshness = cache.getFreshness();			
			if (cachedFreshness == freshness)
			{
				return cache.getData();
			}
			else
			{
				caches.remove(cache);
				cache.getLocation().toFile().delete();
			}
		}		
		return null;
	}

	private CacheEntry loadCachedData(IPath location, String cacheIdentifier) {
		IPath flushPath = location.append(Integer.toString(cacheIdentifier.hashCode())).addFileExtension("txt");
		
		if (flushPath.toFile().exists())
		{
			try {
				FileInputStream fis = new FileInputStream(flushPath.toFile());
				ObjectInputStream ois = new ObjectInputStream(fis);
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

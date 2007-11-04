/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.tcmodification;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;

public class CollectionMap {
	private HashMap fMap;
	private CollectionEntrySet fCollectionEntrySet;

	public CollectionMap(){
		fMap = new HashMap();
	}
	
	public class ValueIter {
		private Map fIterMap; 
		
		public ValueIter() {
			fIterMap = new HashMap(fMap);
			for(Iterator iter = fIterMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				Collection c = (Collection)entry.getValue();
				entry.setValue(c.iterator());
			}
		}
		
		public Iterator get(Object key){
			Iterator iter = (Iterator)fIterMap.get(key);
			if(iter != null && !iter.hasNext()){
				fIterMap.remove(key);
				return null;
			}
			return iter;
		}
	}
	
	public class CollectionEntry {
		private Map.Entry fEntry;
		
		CollectionEntry(Map.Entry entry){
			fEntry = entry;
		}
		
		public Object getKey(){
			return fEntry.getKey();
		}
		
		public Collection getValue(){
			return (Collection)fEntry.getValue();
		}

		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			
			if(obj == null)
				return false;
			
			if(!(obj instanceof CollectionEntry))
				return false;
			
			return fEntry.equals(((CollectionEntry)obj).fEntry);
		}

		public int hashCode() {
			return fEntry.hashCode();
		}
	}
	
	private class CollectionEntrySet extends AbstractSet {
		private Collection fMapEntrySet;

		private class Iter implements Iterator {
			private Iterator fIter;
			
			private Iter(){
				fIter = fMapEntrySet.iterator();
			}
			public boolean hasNext() {
				return fIter.hasNext();
			}

			public Object next() {
				return new CollectionEntry((Map.Entry)fIter.next());
			}

			public void remove() {
				fIter.remove();
			}
			
		}

		private CollectionEntrySet(){
			fMapEntrySet = fMap.entrySet();
		}

		public Iterator iterator() {
			return new Iter();
		}

		public int size() {
			return fMapEntrySet.size();
		}
	}


	public void add(Object key, Object value){
		Collection l = get(key, true);
		l.add(value);
	}
	
	public Collection removeAll(Object key){
		return (Collection)fMap.remove(key);
	}

	public Collection get(Object key, boolean create){
		Collection l = (Collection)fMap.get(key);
		if(l == null && create){
			l = newCollection(1);
			fMap.put(key, l);
		}
		
		return l;
	}
	
	public Collection valuesToCollection(Collection c){
		if(c == null)
			c = newCollection(20);
		
		for(Iterator iter = fMap.values().iterator(); iter.hasNext(); ){
			Collection l = (Collection)iter.next();
			c.addAll(l);
		}
		
		return c;
	}
	
	public Collection getValues(){
		return (Collection)valuesToCollection(null);
	}

	public Object[] getValuesArray(Class clazz){
		Collection list = getValues();
		Object[] result = (Object[])Array.newInstance(clazz, list.size());
		return list.toArray(result);
	}

	protected Collection newCollection(int size){
		return new ArrayList(size);
	}

	protected Collection cloneCollection(Collection l){
		return (Collection)((ArrayList)l).clone();
	}
	
	public Collection putValuesToCollection(Collection c){
		for(Iterator iter = collectionEntrySet().iterator(); iter.hasNext(); ){
			Collection l = ((CollectionEntry)iter.next()).getValue();
			c.addAll(l);
		}
		return c;
	}

	public void remove(Object key, Object value){
		Collection c = get(key, false);
		if(c != null){
			if(c.remove(value) && c.size() == 0){
				fMap.remove(key);
			}
		}
	}

//	public Object get(Object key, int num){
//		Collection l = get(key, false);
//		if(l != null){
//			return l.get(num);
//		}
//		return null;
//	}

//	public Object remove(Object key, int num){
//		List l = get(key, false);
//		if(l != null){
//			Object result = null;
//			if(l.size() > num){
//				result = l.remove(num);
//			}
//			
//			return result;
//		}
//		return null;
//	}

//	public Object removeLast(Object key){
//		Collection l = get(key, false);
//		if(l != null){
//			Object result = null;
//			if(l.size() > 0){
//				result = l.remove(l.size() - 1);
//			}
//			return result;
//		}
//		return null;
//	}

	public void removeAll(Object key, Collection values){
		Collection c = get(key, false);
		if(c != null){
			if(c.removeAll(values) && c.size() == 0){
				fMap.remove(key);
			}
		}
	}
	
	public void clearEmptyLists(){
		for(Iterator iter = fMap.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry)iter.next();
			if(((Collection)entry.getValue()).size() == 0)
				iter.remove();
		}
	}

	public Set collectionEntrySet(){
		if(fCollectionEntrySet == null)
			fCollectionEntrySet = new CollectionEntrySet();
		return fCollectionEntrySet;
	}

	public void difference(CollectionMap map){
		for(Iterator iter = map.fMap.entrySet().iterator(); iter.hasNext(); ){
			Map.Entry entry = (Map.Entry)iter.next();
			Collection thisC = (Collection)fMap.get(entry.getKey());
			if(thisC != null){
				if(thisC.removeAll((Collection)entry.getValue()) && thisC == null){
					fMap.remove(entry.getKey());
				}
			}
		}
	}
	
	public ValueIter valueIter(){
		return new ValueIter();
	}

//	protected Collection createCollection(Object key){
//		return new ArrayList(1);
//	}

	public Object clone() {
		try {
			CollectionMap clone = (CollectionMap)super.clone();
			clone.fMap = (HashMap)fMap.clone();
			for(Iterator iter = clone.fMap.entrySet().iterator(); iter.hasNext();){
				Map.Entry entry = (Map.Entry)iter.next();
				entry.setValue(cloneCollection((Collection)entry.getValue()));
			}
		} catch (CloneNotSupportedException e) {
			ManagedBuilderCorePlugin.log(e);
		}
		return null;
	}
	
//	protected Map getMap(boolean create){
//		if(fMap == null && create)
//			fMap = createMap();
//		return fMap;
//	}
//	
//	protected Map createMap(){
//		return new HashMap();
//	}
}

package org.eclipse.cdt.internal.core.dom.parser.c99.preprocessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectTagger<OBJ, TAG> {

	private Map<ObjectWrapper<OBJ>, Set<TAG>> tagMap = new HashMap<ObjectWrapper<OBJ>, Set<TAG>>();
	
	
	private static class ObjectWrapper<O> {
		O x;
		
		ObjectWrapper(O o) { 
			this.x = o; 
		}
		public int hashCode() {
			return System.identityHashCode(x);
		}
		public boolean equals(Object y) {
			return x == ((ObjectWrapper)y).x;
		}
		public String toString() {
			return x + ": " + hashCode();
		}
	}
	
	
	
	public void tag(OBJ x, TAG tag) {
		ObjectWrapper<OBJ> wrapped = new ObjectWrapper<OBJ>(x);
		Set<TAG> set = tagMap.get(wrapped);
		if(set == null) {
			set = new HashSet<TAG>();
			tagMap.put(wrapped, set);
		}
		set.add(tag);
	}
	
	
	public boolean hasTag(OBJ x, TAG tag) {
		Set<TAG> set = tagMap.get(new ObjectWrapper<OBJ>(x));		
		return set == null ? false : set.contains(tag);
	}
	
	
	public void removeAllTags(OBJ x) {
		tagMap.remove(new ObjectWrapper<OBJ>(x));
	}
	
	
	public void shareTags(OBJ hasTags, OBJ toTag) {
		Set<TAG> tags = tagMap.get(new ObjectWrapper<OBJ>(hasTags));
		if(tags != null) {
			tagMap.put(new ObjectWrapper<OBJ>(toTag), tags);
		}
	}
	
	public void clear() {
		tagMap.clear();
	}
}

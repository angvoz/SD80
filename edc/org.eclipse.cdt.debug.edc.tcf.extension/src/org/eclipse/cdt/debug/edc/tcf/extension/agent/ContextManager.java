/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia Corporation and/or its subsidiaries
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Wang (Nokia) - initial version. Oct 19, 2009
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager of all contexts (threads and processes, etc) in the debug session.
 */
public class ContextManager {
	/**
	 * All contexts under debug.
	 */
	private static Map<String, ContextInAgent> gDebuggedContexts = new HashMap<String, ContextInAgent>();
	
	/**
	 * Running contexts in the target OS that may or may not be under debug.
	 */
	private static Map<String, ContextInAgent> gRunningContexts = new HashMap<String, ContextInAgent>();
	
	static public void addDebuggedContext(ContextInAgent c) {
		assert c.getID() != null;	// internal ID has been given.
		gDebuggedContexts.put(c.getID(), c);
	}
	
	/**
	 * Remove given context from the debugged context cache.
	 * 
	 * @param id internal context ID of the context to remove.
	 * @param removeChildren whether to remove children of the context from the cache.
	 */
	static public void removeDebuggedContext(String id, boolean removeChildren) {
		if (removeChildren) {
			ContextInAgent cia = findDebuggedContext(id);
			if (cia != null)
			{
				for (String c : cia.getChildren()) {
					removeDebuggedContext(c, true);
				}				
			}
		}
		
		gDebuggedContexts.remove(id);
	}
	
	/**
	 * Get list of context IDs under debug.
	 * 
	 * @return a list of IDs of contexts under debug.
	 */
	static public List<String> getDebuggedContexts() {
		List<String> ret = new ArrayList<String>();
		for (ContextInAgent c : gDebuggedContexts.values())
			ret.add(c.getID());
		
		return ret;
	}
	
	/**
	 * Whether there are still context in the cache of debugged contexts.
	 * 
	 * @return
	 */
	static public boolean hasDebuggedContext() {
		return gDebuggedContexts.size() > 0;
	}
	
	/**
	 * Find the context with given ID.
	 * @param id  internal ID of the context
	 * @return the context found, null if not found.
	 */
	static public ContextInAgent findDebuggedContext(String id) {
		return gDebuggedContexts.get(id);
	}

	static public void addRunningContext(ContextInAgent c) {
		gRunningContexts.put(c.getID(), c);
	}

	static public void removeRunningContext(String id) {
		gRunningContexts.remove(id);
	}
	
	/**
	 * Find the running context with given ID.
	 * @param id  internal ID of the context
	 * @return the context found, null if not found.
	 */
	static public ContextInAgent findRunningContext(String id) {
		return gRunningContexts.get(id);
	}
	
	static public void clearRunningContextCache() {
		gRunningContexts.clear();
	}

	static public void clearDebuggedContextCache() {
		gDebuggedContexts.clear();
	}

	/**
	 * Clear all context caches.
	 */
	static public void clearContextCache() {
		clearRunningContextCache();
		clearDebuggedContextCache();
	}
}

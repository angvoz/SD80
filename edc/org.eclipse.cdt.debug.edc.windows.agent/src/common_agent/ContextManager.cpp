/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
#include "ContextManager.h"
#include "AgentUtils.h"

std::map<ContextID, Context*> ContextManager::gContexts;


void ContextManager::addContext(Context* context) {
	if (context != NULL)
		gContexts[context->GetID()] = context;
}

/** Find a context with the given id. */
Context* ContextManager::findContext(const ContextID& id) {
	// NOTE that if "id" is not in the map a, a[id] does not return NULL !
	// See STL Map document for more.
	std::map<ContextID, Context*>::iterator iter = gContexts.find(id);
	if (iter != gContexts.end())
		return gContexts[id];
	else
		return NULL;
}

/**
 * Remove a context with the given id, and return the context if it was found.
 * This does not delete the context.
 */
Context* ContextManager::removeContext(const ContextID& id) {
	std::map<ContextID, Context*>::const_iterator iter = gContexts.find(id);
	if (iter == gContexts.end())
		return NULL;
	Context* context = iter->second;
	gContexts.erase(id);
	return context;
}

/** Delete all Contexts and clear cache. */
void ContextManager::deleteContextCache() {
	// Note: if we delete a tree, then that tree may delete its own children.
	// Avoid double-delete and invalid iterators by iterating a copy
	// of the map and checking against the current contexts before deleting.

	std::map<ContextID, Context*> copy(gContexts);
	for (std::map<ContextID, Context*>::iterator iter = copy.begin(); iter != copy.end(); ) {
		Context* current = findContext(iter->first);
		if (current) {
			delete iter->second;
		}
		iter++;
	}

	gContexts.clear();
}

/** Get a copy of all the current context IDs. */
std::list<ContextID> ContextManager::getContexts() {
	std::list<ContextID> contexts;
	for (std::map<ContextID, Context*>::iterator iter = gContexts.begin();
			iter != gContexts.end(); iter++) {
		contexts.push_back(iter->first);
	}
	return contexts;
}


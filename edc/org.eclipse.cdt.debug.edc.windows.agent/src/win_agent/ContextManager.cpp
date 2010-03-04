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

std::map<ContextID, Context*> ContextManager::gDebuggedContexts;
std::map<ContextID, Context*> ContextManager::gRunningContexts;


void ContextManager::AddDebuggedContext(Context* context) {
	if (context != NULL)
		gDebuggedContexts[context->GetID()] = context;
}

void ContextManager::AddRunningContext(Context* context) {
	if (context != NULL)
		gRunningContexts[context->GetID()] = context;
}

/*
 * return NULL pointer if not found.
 */
Context* ContextManager::FindDebuggedContext(ContextID id) {
	// NOTE that if "id" is not in the map a, a[id] does not return NULL !
	// See STL Map document for more.
	std::map<ContextID, Context*>::iterator iter = gDebuggedContexts.find(id);
	if (iter != gDebuggedContexts.end())
		return gDebuggedContexts[id];
	else
		return NULL;
}

/*
 * return NULL pointer if not found.
 */
Context* ContextManager::FindRunningContext(ContextID id) {
	// NOTE that if "id" is not in the map a, a[id] does not return NULL !
	// See STL Map document for more.
	std::map<ContextID, Context*>::iterator iter = gRunningContexts.find(id);
	if (iter != gRunningContexts.end())
		return gRunningContexts[id];
	else
		return NULL;
}

/*
 * Remove given context from the cache.
 * If the given context is not in the cache, this is a void operation.
 */
void ContextManager::RemoveDebuggedContext(ContextID id) {
	gDebuggedContexts.erase(id);
}

/**
 * Delete all cached contexts and flush the cache.
 */
void ContextManager::ClearRunningContextCache() {
	std::map<ContextID, Context*>::iterator iter = gRunningContexts.begin();
	while (iter != gRunningContexts.end()) {
		delete iter->second;
		iter++;
	}

	gRunningContexts.clear();
}

/**
 * Delete all cached contexts and flush the cache.
 */
void ContextManager::ClearDebuggedContextCache() {
	std::map<ContextID, Context*>::iterator iter = gDebuggedContexts.begin();
	while (iter != gDebuggedContexts.end()) {
		delete iter->second;
		iter++;
	}

	gDebuggedContexts.clear();
}

/**
 * Delete all cached contexts and flush the cache.
 */
void ContextManager::ClearContextCache() {
	ClearRunningContextCache();
	ClearDebuggedContextCache();
}

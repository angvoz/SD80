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
std::map<std::string, Context*> ContextManager::gRunningContexts;


ContextID ContextManager::GenerateInternalID() {
	static int baseID = 100;
	return AgentUtils::IntToString(baseID++);
}

void ContextManager::AddDebuggedContext(Context* context) {
	if (context != NULL)
		gDebuggedContexts[context->GetID()] = context;
}

void ContextManager::AddRunningContext(Context* context) {
	if (context != NULL)
		gRunningContexts[context->GetID()] = context;
}

Context* ContextManager::FindDebuggedContext(ContextID id) {
	return gDebuggedContexts[id];
}

Context* ContextManager::FindRunningContext(ContextID id) {
	return gRunningContexts[id];
}

void ContextManager::RemoveDebuggedContext(ContextID id) {
	gDebuggedContexts.erase(id);
}

void ContextManager::FlushRunningContextCache() {
	std::map<ContextID, Context*>::iterator iter = gRunningContexts.begin();
	while (iter != gRunningContexts.end()) {
		delete iter->second;
		iter++;
	}

	gRunningContexts.clear();
}


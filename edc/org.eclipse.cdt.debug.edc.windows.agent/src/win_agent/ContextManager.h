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
#ifndef CONTEXTMANAGER_H_
#define CONTEXTMANAGER_H_

#include <map>
#include <string>

#include "Context.h"
#include "AgentException.h"

/**
 * Manager for context objects.
 * This should be a singleton.
 */
class ContextManager {
public:

	static void AddDebuggedContext(Context* context);

	static void AddRunningContext(Context* context);

	static Context* FindDebuggedContext(ContextID id);

	static Context* FindRunningContext(ContextID id);

	static void RemoveDebuggedContext(ContextID id);

	static void ClearContextCache();

	static void ClearRunningContextCache();

	static void ClearDebuggedContextCache();

protected:
	ContextManager() {
	}
	;
	virtual ~ContextManager() {
	}
	;

private:
	// Contexts that are being debugged/attached.
	static std::map<ContextID, Context*> gDebuggedContexts;

	// Contexts that are running in the OS, which
	// may not be on control of the debugger.
	// Note this cache is cleared and populated when needed.
	// Don't store debugged context in it.
	// When a context gets debugged (attached), a new context object
	// is created and stored in gDebuggedContexts cache.
	static std::map<std::string, Context*> gRunningContexts;
};

#endif /* CONTEXTMANAGER_H_ */

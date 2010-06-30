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

#include "TCFContext.h"
#include "AgentException.h"

/**
 * Manager for context objects.
 * This should be a singleton.
 *
 * This is global storage of all contexts that are known to the debugger.
 * These may be any kind of context (register groups, registers,
 * processes, threads).
 *
 * For contexts implementing RunControlContext, there
 * will be one entry in the manager, whether or not it's being debugged.
 * Use RunControlContext#IsDebugged() to distinguish.
 *
 * Note: clients maintain allocation of Context objects, except
 * for the "panic mode" call to #deleteContextCache().
 *
 */
class ContextManager {

public:
	/** Add a context and take ownership of its memory. */
	static void addContext(Context* context);

	/** Find a context with the given id. */
	static Context* findContext(const ContextID& id);

	/** Remove a context with the given id, and return the context if it was found.
	 * This does not delete the context. */
	static Context* removeContext(const ContextID& id);

	/** Delete all Contexts and clear cache. */
	static void deleteContextCache();

	/** Get a copy of all the current context IDs. */
	static std::list<ContextID> getContexts();

protected:
	ContextManager() {};
	virtual ~ContextManager() {};

private:
	static std::map<ContextID, Context*> gContexts;
};

#endif /* CONTEXTMANAGER_H_ */

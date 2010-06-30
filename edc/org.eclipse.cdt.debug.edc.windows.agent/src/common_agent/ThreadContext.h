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
#ifndef THREADCONTEXT_H
#define THREADCONTEXT_H

#include "RunControlContext.h"
#include "AgentUtils.h"

class ThreadContext : public RunControlContext {
public:
	ThreadContext(ContextOSID osid, const ContextID& parentID, const ContextID& internalID)
		: RunControlContext(osid, parentID, internalID)
	{ }
	virtual ~ThreadContext() { };

	static ContextID CreateInternalID(ContextOSID osID, const ContextID& parentID);
};

#endif

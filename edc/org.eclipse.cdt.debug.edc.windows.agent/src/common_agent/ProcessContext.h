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
#ifndef PROCESSCONTEXT_H
#define PROCESSCONTEXT_H

#include "RunControlContext.h"

class ProcessContext : public RunControlContext {
public:
	ProcessContext(ContextOSID osid, const ContextID& parentID, const ContextID& internalID)
		: RunControlContext(osid, parentID, internalID)
	{}

	virtual ~ProcessContext() {}

	//
	//	Overrides of RunControlContext:  these do the default action of
	//	invoking the given command on each of the ThreadContext children.
	//	Subclasses will probably need to override to add process-specific
	//	behavior.
	//
	virtual void Resume(const AgentActionParams& params) throw (AgentException);

	virtual void Suspend(const AgentActionParams& params) throw (AgentException);
	//
	//	end overrides

	static ContextID CreateInternalID(ContextOSID osID);
};

#endif

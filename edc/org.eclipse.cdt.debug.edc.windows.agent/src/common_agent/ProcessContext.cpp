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

#include "ProcessContext.h"
#include "ThreadContext.h"
#include "AgentUtils.h"

ContextID ProcessContext::CreateInternalID(ContextOSID osID) {
	// return:  pnnn
	// No parent for a process
	std::string ret = "p";	// a prefix
	ret += AgentUtils::IntToString(osID);

	return ret;
}

void ProcessContext::Suspend(const AgentActionParams& params) throw (AgentException) {
	std::list<Context*> kids = GetChildren();

	AgentActionParams subParams(params.subParams());

	std::list<Context *>::iterator iter;
	for (iter = kids.begin(); iter != kids.end(); iter++)
	{
		ThreadContext* thread = dynamic_cast<ThreadContext*>(*iter);
		if (thread != NULL) {
			// let exception propagate
			thread->Suspend(subParams);
		}
	}

	params.reportSuccessForAction();

}

void ProcessContext::Resume(const AgentActionParams& params) throw (AgentException) {
	std::list<Context*> kids = GetChildren();

	AgentActionParams subParams(params.subParams());

	std::list<Context *>::iterator iter;
	for (iter = kids.begin(); iter != kids.end(); iter++)
	{
		ThreadContext* thread = dynamic_cast<ThreadContext*>(*iter);
		if (thread != NULL) {
			// let exception propagate
			thread->Resume(subParams);
		}
	}

	params.reportSuccessForAction();
}


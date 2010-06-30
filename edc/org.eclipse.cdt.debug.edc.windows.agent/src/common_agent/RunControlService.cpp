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
#include <string>

#include "RunControlService.h"
#include "ContextManager.h"
#include "EventClientNotifier.h"
#include "Logger.h"
#include "TCFChannel.h"
#include "RunControlContext.h"

#include "TCFHeaders.h"

static const char* sServiceName = "RunControl";

RunControlService::RunControlService(Protocol * proto) :
	TCFService(proto) {
	AddCommand("getContext", command_get_context);
	AddCommand("getChildren", command_get_children);
	AddCommand("getState", command_get_state);
	AddCommand("resume", command_resume);
	AddCommand("suspend", command_suspend);
	AddCommand("terminate", command_terminate);
}

RunControlService::~RunControlService(void) {
}

const char* RunControlService::GetName() {
	return sServiceName;
}

void RunControlService::command_get_context(char * token, Channel * c) {
	LogTrace("RunControl::command_get_context", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	Context* context = ContextManager::findContext(id);

	if (context == NULL) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 1);
		return;
	}
	channel.writeReplyHeader(token);
	channel.writeError(0);
	EventClientNotifier::WriteContext(*context, channel);
	channel.writeZero(); // end of context
	channel.writeComplete();
}

void RunControlService::command_get_children(char * token, Channel * c) {
	LogTrace("RunControl::command_get_children", "token: %s", token);
	TCFChannel channel(c);

	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	std::string parentID = id;
	if (parentID.length() == 0)
		parentID = "root";

	channel.writeReplyHeader(token);
	channel.writeError(0);

	channel.writeCharacter('[');

	const std::list<Context*>& children =
			ContextManager::findContext(parentID)->GetChildren();

	std::list<Context*>::const_iterator itr;
	for (itr = children.begin(); itr != children.end(); itr++) {
		if (itr != children.begin())
			channel.writeCharacter(',');
		std::string contextID = ((Context*) *itr)->GetID();
		channel.writeString(contextID);
	}

	channel.writeCharacter(']');

	channel.writeZero(); // end of info
	channel.writeComplete();
}

void RunControlService::command_get_state(char * token, Channel * c) {
	LogTrace("RunControl::command_get_state", "token: %s", token);
}

void RunControlService::command_resume(char * token, Channel * c) {
	LogTrace("RunControl::command_resume", "token: %s", token);

	TCFChannel channel(c);

	std::string id = channel.readString();
	channel.readZero();
	long mode = channel.readLong();
	channel.readZero();
	/*long count =*/ channel.readLong();
	channel.readZero();

	channel.readComplete();


	RunControlContext* context = dynamic_cast<RunControlContext*>(ContextManager::findContext(id));
	if (context == NULL || !context->IsDebugging()) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT);
	}
	else {
		try {
			// implementation must report reply
			if (mode == RM_STEP_INTO)
				context->SingleStep(AgentActionParams(token, c));
			else
				context->Resume(AgentActionParams(token, c));
		} catch (const AgentException& e) {
			channel.writeCompleteReply(token, ERR_OTHER, 0, e.what());
		}
	}
}

void RunControlService::command_suspend(char * token, Channel * c) {
	LogTrace("RunControl::command_suspend", "token: %s", token);
	TCFChannel channel(c);

	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();


	RunControlContext* context = dynamic_cast<RunControlContext*>(ContextManager::findContext(id));
	if (context == NULL || !context->IsDebugging()) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT);
	}
	else {
		try {
			// implementation must report reply
			context->Suspend(AgentActionParams(token, c));
		} catch (const AgentException& e) {
			channel.writeCompleteReply(token, ERR_OTHER, 0, e.what());
		}
	}
}

void RunControlService::command_terminate(char * token, Channel * c) {
	LogTrace("RunControl::command_terminate", "token: %s", token);

	TCFChannel channel(c);

	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	RunControlContext* context = dynamic_cast<RunControlContext*>(ContextManager::findContext(id));
	if (context == NULL || !context->IsDebugging()) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT);
	}
	else {
		try {
			// implementation must report reply
			context->Terminate(AgentActionParams(token, c));
		} catch (const AgentException& e) {
			channel.writeCompleteReply(token, ERR_OTHER, 0, e.what());
		}
	}
}


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
#include "EventClientNotifier.h"
#include "TCFHeaders.h"
#include "Logger.h"
#include "AgentUtils.h"
#include "TCFOutputStream.h"
#include "ProtocolConstants.h"

TCFBroadcastGroup * EventClientNotifier::broadcastGroup = NULL;

static const char RUN_CONTROL[] = "RunControl";

SendExeEventParams::SendExeEventParams(Context* context, bool isLoaded,
		unsigned long pcAddress, std::string exePath,
		unsigned long baseAddress, unsigned long codeSize) {
	context_ = context;
	isLoaded_ = isLoaded;
	pcAddress_ = pcAddress;
	exePath_ = exePath;
	baseAddress_ = baseAddress;
	codeSize_ = codeSize;
}

SendExceptionEventParams::SendExceptionEventParams(Context* context,
		std::string description) {
	context_ = context;
	description_ = description;
}

void EventClientNotifier::SendContextAdded(Context* context) {
	post_event(EventClientNotifier::SendContextAddedCallback, context);
}

void EventClientNotifier::SendContextAddedCallback(void* context) {
	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextAdded");

	// <array of context data>
	out.writeCharacter('[');
	WriteContext(*(Context*) context, out);
	out.writeCharacter(']');
	out.writeZero();

	out.writeComplete();
	out.flush();

	std::string contextID = ((Context*) context)->GetID();

	LogTrace("DebugEvents::SendContextAdded ", "context id: %s",
			contextID.c_str());

}

void EventClientNotifier::SendContextRemoved(Context* context) {
	std::string* contextID = new std::string(context->GetID());
	post_event(EventClientNotifier::SendContextRemovedCallback, contextID);
}

void EventClientNotifier::SendContextRemovedCallback(void* context) {
	std::string* contextID = (std::string*) context;
	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextRemoved");

	/* <array of context data> */
	out.writeCharacter('[');
	out.writeString(contextID->c_str());
	out.writeCharacter(']');
	out.writeZero();

	out.writeComplete();
	out.flush();

	LogTrace("DebugEvents::SendContextRemoved ", "context id: %d",
			contextID->c_str());
	delete contextID;
}

void EventClientNotifier::SendExecutableEvent(Context* context, bool isLoaded,
		unsigned long pcAddress, std::string exePath,
		unsigned long baseAddress, unsigned long codeSize) {
	post_event(EventClientNotifier::SendExecutableEventCallback,
			new SendExeEventParams(context, isLoaded, pcAddress, exePath,
					baseAddress, codeSize));
}

void EventClientNotifier::SendExecutableEventCallback(void* params) {
	SendExeEventParams* eventParams = (SendExeEventParams*) params;

	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextSuspended");

	out.writeString(eventParams->context_->GetID());
	out.writeZero();

	out.writeLong(eventParams->pcAddress_);
	out.writeZero();

	out.writeString("Shared Library");
	out.writeZero();

	out.writeCharacter('{');
	out.writeString(PROP_MODULE_LOADED);
	out.writeCharacter(':');
	out.writeBoolean(eventParams->isLoaded_);
	out.writeCharacter(',');
	out.writeString("Name");
	out.writeCharacter(':');
	out.writeString(AgentUtils::GetFileNameFromPath(eventParams->exePath_));
	out.writeCharacter(',');
	out.writeString(PROP_FILE);
	out.writeCharacter(':');
	out.writeString(eventParams->exePath_);
	out.writeCharacter(',');
	out.writeString(PROP_IMAGE_BASE_ADDRESS);
	out.writeCharacter(':');
	out.writeLong(eventParams->baseAddress_);
	out.writeCharacter(',');
	out.writeString(PROP_CODE_SIZE);
	out.writeCharacter(':');
	out.writeLong(eventParams->codeSize_);
	out.writeCharacter('}');
	out.writeZero();

	out.writeComplete();
	out.flush();

	LogTrace("DebugEvents::SendExecutableEvent",
			"context id: %s executable: %s address: %X",
			eventParams->context_->GetID().c_str(),
			eventParams->exePath_.c_str(), eventParams->baseAddress_);

	delete eventParams;
}

void EventClientNotifier::SendContextSuspended(Context* context) {
	post_event(EventClientNotifier::SendContextSuspendedCallback, context);
}

void EventClientNotifier::SendContextSuspendedCallback(void* params) {
	Context& context = (*(Context*) params);
	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextSuspended");

	out.writeString(context.GetID().c_str());
	out.writeZero();

	out.writeLong(context.GetPCAddress());
	out.writeZero();

	out.writeString(context.GetSuspendReason().c_str());
	out.writeZero();

	out.writeCharacter('{');
	out.writeString("Indy");
	out.writeCharacter(':');
	out.writeLong(43);
	out.writeCharacter('}');
	out.writeZero();

	out.writeComplete();
	out.flush();

	LogTrace("DebugEvents::SendContextSuspended ",
			"context id: %s address: %X", context.GetID().c_str(),
			context.GetPCAddress());
}

void EventClientNotifier::SendContextException(Context* context,
		std::string description) {
	post_event(EventClientNotifier::SendContextExceptionCallback,
			new SendExceptionEventParams(context, description));
}

void EventClientNotifier::SendContextExceptionCallback(void* params) {
	SendExceptionEventParams* eventParams = (SendExceptionEventParams*) params;

	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextException");

	out.writeString(eventParams->context_->GetID());
	out.writeZero();

	out.writeCharacter('"');
	out.writeString(eventParams->description_.c_str());
	out.writeCharacter('"');
	out.writeZero();

	out.writeComplete();
	out.flush();

	LogTrace("DebugEvents::SendContextException ", "context id: %s",
			eventParams->context_->GetID().c_str());

	delete eventParams;
}

void EventClientNotifier::WriteContext(Context& context, TCFOutputStream& out) {
	out.writeCharacter('{');

	out.writeString(PROP_ID);
	out.writeCharacter(':');
	out.writeString(context.GetID());
	out.writeCharacter(',');

	if (context.GetParentID() != INVALID_CONTEXT_ID) {
		out.writeString(PROP_PARENT_ID);
		out.writeCharacter(':');
		out.writeString(context.GetParentID());
		out.writeCharacter(',');
	}

	out.writeString(PROP_PROCESS_ID);
	out.writeCharacter(':');
	out.writeString(AgentUtils::IntToString(context.GetOSID()));
	out.writeCharacter(',');

	out.writeString(PROP_CAN_SUSPEND);
	out.writeCharacter(':');
	out.writeBoolean(context.CanSuspend());
	out.writeCharacter(',');

	out.writeString(PROP_CAN_RESUME);
	out.writeCharacter(':');
	long supportedResumeModes = (1 << RM_RESUME) | (1 << RM_STEP_INTO);
	out.writeLong(supportedResumeModes);
	out.writeCharacter(',');

	out.writeString(PROP_CAN_TERMINATE);
	out.writeCharacter(':');
	out.writeBoolean(context.CanTerminate());

	std::map<std::string, std::string> properties = context.GetProperties();

	for (std::map<std::string, std::string>::iterator iter = properties.begin(); iter
			!= properties.end(); iter++) {
		out.writeCharacter(',');
		out.writeString(iter->first);
		out.writeCharacter(':');
		out.writeString(iter->second);
	}
	out.writeCharacter('}');

}

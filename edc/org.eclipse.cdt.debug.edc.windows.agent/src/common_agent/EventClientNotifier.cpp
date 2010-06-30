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
#include "EventClientNotifier.h"
#include "TCFHeaders.h"
#include "Logger.h"
#include "AgentUtils.h"
#include "TCFOutputStream.h"
#include "ProtocolConstants.h"
#include "ThreadContext.h"

TCFBroadcastGroup * EventClientNotifier::broadcastGroup = NULL;

static const char RUN_CONTROL[] = "RunControl";

SendRemovedEventParams::SendRemovedEventParams(Context* context_, bool delete_) {
	context = context_;
	deleteContext = delete_;
}

SendExeEventParams::SendExeEventParams(Context* context_, unsigned long pcAddress_, const Properties& properties_) {
	context = context_;
	properties = properties_;
	pcAddress = pcAddress_;
}

SendExceptionEventParams::SendExceptionEventParams(Context* context_,
		const std::string& description_) {
	context = context_;
	description = description_;
}

SendSuspendEventParams::SendSuspendEventParams(Context* context_, unsigned long pcAddress_,
		const char* reason_, const std::string& message_) {
	context = context_;
	pcAddress = pcAddress_;
	reason = reason_;
	message = message_;
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

	LogTrace("EventClientNotifier::SendContextAdded ", "context id: %s",
			contextID.c_str());

}

void EventClientNotifier::SendContextRemoved(Context* context, bool deleteContext) {
	SendRemovedEventParams* params = new SendRemovedEventParams(context, deleteContext);
	post_event(EventClientNotifier::SendContextRemovedCallback, params);
}

void EventClientNotifier::SendContextRemovedCallback(void* context) {
	SendRemovedEventParams* params = (SendRemovedEventParams*) context;
	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextRemoved");

	/* <array of context data> */
	out.writeCharacter('[');
	out.writeString(params->context->GetID().c_str());
	out.writeCharacter(']');
	out.writeZero();

	out.writeComplete();
	out.flush();

	LogTrace("EventClientNotifier::SendContextRemoved ", "context id: %d",
			params->context->GetID().c_str());

	if (params->deleteContext)
		delete params->context;

	delete params;

}

void EventClientNotifier::SendExecutableEvent(Context* context, unsigned long pcAddress, const Properties& properties) { 
	post_event(EventClientNotifier::SendExecutableEventCallback,
			new SendExeEventParams(context, pcAddress, properties));
}

void EventClientNotifier::SendExecutableEventCallback(void* params) {
	SendExeEventParams* eventParams = (SendExeEventParams*) params;

	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextSuspended");

	out.writeString(eventParams->context->GetID());
	out.writeZero();

	out.writeLong(eventParams->pcAddress);
	out.writeZero();

	out.writeString(REASON_SHAREDLIB);
	out.writeZero();

	WriteProperties(eventParams->properties, out);

	out.writeZero();

	out.writeComplete();
	out.flush();

#if ENABLE_Trace
	PropertyValue* exe = 0;
	Properties::const_iterator iter = eventParams->properties.find(PROP_NAME);
	if (iter != eventParams->properties.end())
		exe = iter->second;
	LogTrace("EventClientNotifier::SendExecutableEvent",
			"context id: %s executable: %s address: %X",
			eventParams->context->GetID().c_str(),
			exe ? exe->getStringValue().c_str() : "<none>", 
			eventParams->pcAddress);
#endif
	
	delete eventParams;
}

void EventClientNotifier::SendContextSuspended(Context* context,
		unsigned long pcAddress, const char* reason, const std::string& message) {
	post_event(EventClientNotifier::SendContextSuspendedCallback,
			new SendSuspendEventParams(context, pcAddress, reason, message));
}

void EventClientNotifier::SendContextSuspendedCallback(void* params_) {
	SendSuspendEventParams* params = (SendSuspendEventParams*) params_;
	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextSuspended");

	out.writeString(params->context->GetID().c_str());
	out.writeZero();

	out.writeLong(params->pcAddress);
	out.writeZero();

	out.writeString(params->reason);
	out.writeZero();

	out.writeCharacter('{');
	if (params->message.length() > 0) {
		out.writeString("message");
		out.writeCharacter(':');
		out.writeString(params->message.c_str());
	}
	out.writeCharacter('}');
	out.writeZero();

	out.writeComplete();
	out.flush();

	LogTrace("EventClientNotifier::SendContextSuspended ",
			"context id: %s address: %X", params->context->GetID().c_str(),
			params->pcAddress);

	delete params;
}



void EventClientNotifier::SendContextException(Context* context,
		const std::string& description) {
	post_event(EventClientNotifier::SendContextExceptionCallback,
			new SendExceptionEventParams(context, description));
}

void EventClientNotifier::SendContextExceptionCallback(void* params) {
	SendExceptionEventParams* eventParams = (SendExceptionEventParams*) params;

	TCFOutputStream out(&broadcastGroup->out);

	out.writeStringZ("E");
	out.writeStringZ(RUN_CONTROL);
	out.writeStringZ("contextException");

	out.writeString(eventParams->context->GetID());
	out.writeZero();

	out.writeCharacter('"');
	out.writeString(eventParams->description.c_str());
	out.writeCharacter('"');
	out.writeZero();

	out.writeComplete();
	out.flush();

	LogTrace("EventClientNotifier::SendContextException ", "context id: %s",
			eventParams->context->GetID().c_str());

	delete eventParams;
}

void EventClientNotifier::WriteContext(Context& context, TCFOutputStream& out) {
	WriteProperties(context.GetProperties(), out);
}

void EventClientNotifier::WriteProperties(const Properties& properties, TCFOutputStream& out) {
	out.writeCharacter('{');

	for (Properties::const_iterator iter = properties.begin();
			iter != properties.end(); iter++)
	{
		if (iter != properties.begin())
			out.writeCharacter(',');

		out.writeString(iter->first);
		out.writeCharacter(':');
		iter->second->writeToTCFChannel(out);
	}

	out.writeCharacter('}');
}

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
#ifndef EVENTCLIENTNOTIFIER_H
#define EVENTCLIENTNOTIFIER_H

#include "TCFContext.h"

struct TCFBroadcastGroup;
class TCFOutputStream;
struct OutputStream;

class SendRemovedEventParams {
public:
	SendRemovedEventParams(Context* context, bool deleteContext);

	Context* context;
	bool deleteContext;
};

class SendExeEventParams {
public:
	SendExeEventParams(Context* context, unsigned long pcAddress, const Properties& properties);

	Context* context;
	Properties properties;
	unsigned long pcAddress;
};

class SendExceptionEventParams {
public:
	SendExceptionEventParams(Context* context, const std::string& description);

	Context* context;
	std::string description;

};

class SendSuspendEventParams {
public:
	SendSuspendEventParams(Context* context,
			unsigned long pcAddress, const char* reason, const std::string& message);

	Context* context;
	unsigned long pcAddress;
	const char* reason;
	std::string message;

};

/**
 * Notify clients about any event.
 *
 * Note: all of the "send" methods are asynchronous and hold onto
 * a Context.  Be careful about lifetime issues (@see SendContextRemoved).
 */
class EventClientNotifier {
public:

	/**
	 * Send a notification that a context was added.  The context should
	 * be added to the ContextManager beforehand.
	 */
	static void SendContextAdded(Context* context);

	/**
	 * Send a contextRemoved event, and optionally delete (destroy) the context
	 * once sent.
	 *
	 * Note: this is the only safe way to delete a context along with this
	 * notification.  (Do not delete immediately after calling this method
	 * or else the callback will use a deleted context.)
	 *
	 * The context should be (already) removed from the ContextManager, though.
	 */
	static void SendContextRemoved(Context* context, bool deleteContext);

	/** @reason should be REASON_xx from ProtocolConstants.h */
	static void SendContextSuspended(Context* context,
			unsigned long pcAddress, const char* reason, const std::string& message);

	static void SendContextException(Context* context, const std::string& description);

	static void SendExecutableEvent(Context* context, unsigned long pcAddress, const Properties& properties); 

	static void WriteContext(Context& context, TCFOutputStream& out);

	static TCFBroadcastGroup * broadcastGroup;

private:
	/** Write a property map, with '{' ... '}' enclosing */
	static void WriteProperties(const Properties& properties, TCFOutputStream& out);

	static void SendContextAddedCallback(void* context);

	static void SendContextRemovedCallback(void* context);

	static void SendContextSuspendedCallback(void* context);

	static void SendContextExceptionCallback(void* context);

	static void SendExecutableEventCallback(void* context);
};
#endif

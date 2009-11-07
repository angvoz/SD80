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
#pragma once

#include "Context.h"

struct TCFBroadcastGroup;
class TCFOutputStream;
struct OutputStream;

/*
 * Notify clients about any event.
 * This should be created on start of the agent.
 *
 * TODO: Is this a singleton ?
 */

class SendExeEventParams {
public:
	SendExeEventParams(Context* context, bool isLoaded,
			unsigned long pcAddress, std::string exePath,
			unsigned long baseAddress, unsigned long codeSize);

	Context* context_;
	bool isLoaded_;
	unsigned long pcAddress_;
	std::string exePath_;
	unsigned long baseAddress_;
	unsigned long codeSize_;

};

class SendExceptionEventParams {
public:
	SendExceptionEventParams(Context* context, std::string description);

	Context* context_;
	std::string description_;

};

class EventClientNotifier {
public:

	static void SendContextAdded(Context* context);

	static void SendContextRemoved(Context* context);

	static void SendContextSuspended(Context* context);

	static void SendContextException(Context* context, std::string description);

	static void SendExecutableEvent(Context* context, bool isLoaded,
			unsigned long pcAddress, std::string exePath,
			unsigned long baseAddress, unsigned long codeSize);

	static void WriteContext(Context& context, TCFOutputStream& out);

	static TCFBroadcastGroup * broadcastGroup;

private:

	static void SendContextAddedCallback(void* context);

	static void SendContextRemovedCallback(void* context);

	static void SendContextSuspendedCallback(void* context);

	static void SendContextExceptionCallback(void* context);

	static void SendExecutableEventCallback(void* context);

};

/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Nokia - Initial API and implementation
 *******************************************************************************/
#include "DetachProcessAction.h"
#include "TCFChannel.h"
#include "WinDebugMonitor.h"
#include "ContextManager.h"
#include "EventClientNotifier.h"
#include "WinProcess.h"

#ifdef __cplusplus
extern "C" {
#endif
	#include "errors.h"
#ifdef __cplusplus
}
#endif


DetachProcessAction::DetachProcessAction(const AgentActionParams& params, ContextOSID processID) 
	: AgentAction(params), processID(processID) {
}

DetachProcessAction::~DetachProcessAction(void) {
}

void DetachProcessAction::Run() {
	if (! DebugActiveProcessStop(processID)) {
		DWORD err = GetLastError();

		postReply(err);
	}
	else {
		postReply(0);

		Context* context = ContextManager::removeContext(WinProcess::CreateInternalID(processID));
		if (context) {
			EventClientNotifier::SendContextRemoved(context, true);
		}
	}

}

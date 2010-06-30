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
#include "TCFHeaders.h"
#include "TerminateProcessAction.h"
#include "WinProcess.h"
#include "WinThread.h"
#include "WinDebugMonitor.h"	// ReplyInfo

TerminateProcessAction::TerminateProcessAction(const AgentActionParams& params, ContextOSID processID) :
	AgentAction(params), processID_(processID) {
}

TerminateProcessAction::~TerminateProcessAction(void) {
}

void TerminateProcessAction::Run() {
	WinProcess* process = WinProcess::GetProcessByID(processID_);

	std::list<Context*>& threads = process->GetChildren();

	AgentActionParams subParams(params.subParams());

	std::list<Context*>::iterator itr;
	for (itr = threads.begin(); itr != threads.end(); itr++) {
		try {
			((WinThread*) *itr)->PrepareForTermination(subParams);
		} catch (const AgentException& e) {
			postReply(&e, 0);
			return;
		}
			
	}

	if (!TerminateProcess(process->GetProcessHandle(), 0)) {
		DWORD error = GetLastError();
		postReply(error, 0, new std::string("Failed to terminate process"));
	} else {
		postReply(0, 0);
	}

}

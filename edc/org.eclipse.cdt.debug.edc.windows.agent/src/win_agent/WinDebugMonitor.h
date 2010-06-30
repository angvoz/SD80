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

#include <string>
#include <vector>
#include "stdafx.h"
#include "DebugMonitor.h"
#include "TCFChannel.h" 
#include <queue>

extern "C" {
#include "channel.h"
#include "events.h"
}

class AgentAction;
class AgentActionParams;
class WinProcess;

/*
 * Windows implementation of DebugMonitor.
 */
class WinDebugMonitor: public DebugMonitor {
public:
	WinDebugMonitor(const LaunchProcessParams& params);

	WinDebugMonitor(const AttachToProcessParams& params);

	virtual ~WinDebugMonitor(void);

	void StartProcessForDebug();
	void EventLoop();
	void StartMonitor();

	void CaptureMonitorThread();
	void SetProcess(WinProcess* process);
	void Suspend();
	void Resume();

	void PostAction(AgentAction* action);

	virtual void Attach(unsigned long pid, ContextAttachCallBack * done,
			void * data, int selfattach);

	void StartDebug();

	/*
	 * Launch a process and monitor it.
	 */
	static void LaunchProcess(const LaunchProcessParams& params) throw (AgentException);

	/*
	 * Attach to a process and monitor it.
	 * processID: the Windows process ID.
	 */
	static void AttachToProcess(const AttachToProcessParams& params) throw (AgentException);

	/**
	 * Tell whether the exception is a first-chance exception where we want
	 * to immediately suspend and debug.  Usually, we allow a user-written
	 * __try/__except handler take these, but in some cases (e.g. DLL laod failure)
	 * the process will just crash.
	 */
	bool ShouldDebugFirstChance(const DEBUG_EVENT& debugEvent);
	static std::string GetDebugExceptionDescription(const EXCEPTION_DEBUG_INFO& exceptionInfo);

private:
	void AttachToProcessForDebug();

	void HandleDebugEvent(DEBUG_EVENT& debugEvent);
	void HandleNoDebugEvent();

	void HandleExceptionEvent(DEBUG_EVENT& debugEvent);
	void HandleProcessCreatedEvent(DEBUG_EVENT& debugEvent);
	void HandleThreadCreatedEvent(DEBUG_EVENT& debugEvent);
	void HandleProcessExitedEvent(DEBUG_EVENT& debugEvent);
	void HandleThreadExitedEvent(DEBUG_EVENT& debugEvent);
	void HandleDLLLoadedEvent(DEBUG_EVENT& debugEvent);
	void HandleDLLUnloadedEvent(DEBUG_EVENT& debugEvent);
	void HandleDebugStringEvent(DEBUG_EVENT& debugEvent);
	void HandleSystemDebugErrorEvent(DEBUG_EVENT& debugEvent);
	void HandleUnknwonDebugEvent(DEBUG_EVENT& debugEvent);

	void HandleException(DEBUG_EVENT& debugEvent);

private:

	DWORD wfdeWait;
	bool waitForDebugEvents;
	bool handledFirstException_;
	DWORD processID;
	bool isAttach;

	PROCESS_INFORMATION processInfo;
	HANDLE monitorThread_;
	WinProcess* process_;
	std::queue<AgentAction*> actions_;

};


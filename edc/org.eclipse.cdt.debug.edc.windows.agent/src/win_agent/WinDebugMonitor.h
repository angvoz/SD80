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
#include <queue>
#include "channel.h"

class AgentAction;
class WinProcess;

/*
 * Windows implementation of DebugMonitor.
 */
class WinDebugMonitor: public DebugMonitor {
public:
	WinDebugMonitor(std::string& executable, std::string& directory,
			std::string& args, std::vector<std::string>& environment, bool debug_children,
			std::string& token, Channel *c);

	WinDebugMonitor(DWORD processID, bool debug_children, std::string& token, Channel *c);

	~WinDebugMonitor(void);

	void StartProcessForDebug();
	void EventLoop();
	void StartMonitor();
	void WriteError(unsigned long errNum, const char* message);

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
	static void LaunchProcess(std::string& executable, std::string& directory,
			std::string& args, std::vector<std::string>& environment, bool debug_children,
			std::string& token, Channel *c) throw (AgentException);

	/*
	 * Attach to a process and monitor it.
	 * processID: the Windows process ID.
	 */
	static void AttachToProcess(DWORD processID, bool debug_children,
			std::string& token, Channel *c) throw (AgentException);

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

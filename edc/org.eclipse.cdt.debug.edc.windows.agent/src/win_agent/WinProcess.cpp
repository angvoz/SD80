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
#include "StdAfx.h"
#include "WinProcess.h"
#include "WinThread.h"
#include "EventClientNotifier.h"
#include "AgentUtils.h"
#include "Psapi.h"
#include "assert.h"
#include "WinDebugMonitor.h"
#include "TerminateProcessAction.h"
#include "ProtocolConstants.h"
#include "RunControlService.h"
#include "ContextManager.h"

std::map<int, WinProcess*> WinProcess::processIDMap;

WinProcess::WinProcess(WinDebugMonitor* monitor, DEBUG_EVENT& debugEvent) :
	ProcessContext(debugEvent.dwProcessId, ROOT_CONTEXT_ID, CreateInternalID(debugEvent.dwProcessId)),
	processHandle_(debugEvent.u.CreateProcessInfo.hProcess),
	monitor_(monitor)
{
	isRoot_ = true;
	processIDMap[debugEvent.dwProcessId] = this;

	// Get the name for the new process
	std::string moduleFileName = "unknown";
	int bufferSize = 32768;
	{
		LPTSTR processNameBuffer = new TCHAR[bufferSize];
		int nameLength = GetProcessImageFileName((HMODULE) processHandle_,
				processNameBuffer, bufferSize);
		if (nameLength > 0) {
			moduleFileName = AgentUtils::makeString(processNameBuffer);
		}
		delete[] processNameBuffer;
	}
	int lastSlash = moduleFileName.find_last_of("\\");
	if (lastSlash > 0)
		moduleFileName = moduleFileName.substr(lastSlash + 1);
	processName_ = moduleFileName;

	initialize();
}

WinProcess::WinProcess(DWORD procID, std::string procName) :
	ProcessContext(procID, ROOT_CONTEXT_ID, CreateInternalID(procID)),
	processHandle_(NULL),
	monitor_(NULL)
{
	processName_ = procName;

	initialize();
}

// Initialize process specific properties.
void WinProcess::initialize()
{
	SetProperty(PROP_NAME, new PropertyValue(processName_));

	// do not support process stepping yet
	int supportedResumeModes = (1 << RM_RESUME);
	SetProperty(PROP_CAN_RESUME, new PropertyValue(supportedResumeModes));

	SetProperty(PROP_CAN_TERMINATE, new PropertyValue(true));
	SetProperty(PROP_CAN_SUSPEND, new PropertyValue(true));
}

WinProcess::~WinProcess(void) {
	// This makes a copy.
	std::list<Context *> remainingKids = GetChildren();

	// delete children
	for (std::list<Context *>::iterator iter = remainingKids.begin();
		iter != remainingKids.end(); iter++) {
		Context* kid = *iter;
		delete ContextManager::removeContext(kid->GetID());
	}

	processIDMap.erase(GetOSID());
}

HANDLE WinProcess::GetProcessHandle() {
	return processHandle_;
}

WinProcess* WinProcess::GetProcessByID(int processID) {
	std::map<int, WinProcess*>::iterator iter = processIDMap.find(processID);
	if (iter == processIDMap.end())
		return NULL;
	else
		return iter->second;
}

int WinProcess::ReadMemory(const ReadWriteMemoryParams& params) throw (AgentException) {
	// to do: handle breakpoints and watchpoints
	int result = 0;

	boolean success = ReadProcessMemory(processHandle_, (LPCVOID) params.address,
			params.memBuffer, params.size, params.sizeTransferred);
	if (!success)
		result = GetLastError();

	return result;
}

int WinProcess::WriteMemory(const ReadWriteMemoryParams& params) throw (AgentException) {
	// to do: handle breakpoints and watchpoints
	int result = 0;

	boolean success = WriteProcessMemory(processHandle_, (LPVOID) params.address,
			params.memBuffer, params.size, params.sizeTransferred);
	if (!success)
		result = GetLastError();

	return result;
}

void WinProcess::Terminate(const AgentActionParams& params) throw (AgentException) {
	if (monitor_)
		monitor_->PostAction(new TerminateProcessAction(params, GetOSID()));
}

// TODO: if we report an error, DSF gets confused...
// just report success even though it's not implemented
void WinProcess::SingleStep(const AgentActionParams& params) throw (AgentException) {
	AgentActionReply::postReply(params.channel, params.token, 0);
	//AgentActionReply::postReply(params.channel, params.token, ERR_UNSUPPORTED);
};


WinDebugMonitor* WinProcess::GetMonitor() {
	return monitor_;
}

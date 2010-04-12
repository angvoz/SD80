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

std::map<int, WinProcess*> WinProcess::processIDMap;

WinProcess::WinProcess(WinDebugMonitor* monitor, DEBUG_EVENT& debugEvent) :
	RunControlContext(debugEvent.dwProcessId, ROOT_CONTEXT_ID, CreateInternalID(debugEvent.dwProcessId)),
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
	RunControlContext(procID, ROOT_CONTEXT_ID, CreateInternalID(procID)),
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

	// Not support process resume yet.
	int supportedResumeModes = 0; // (1 << RM_RESUME) | (1 << RM_STEP_INTO);
	SetProperty(PROP_CAN_RESUME, new PropertyValue(supportedResumeModes));

	SetProperty(PROP_CAN_TERMINATE, new PropertyValue(true));
	SetProperty(PROP_CAN_SUSPEND, new PropertyValue(true));
}

WinProcess::~WinProcess(void) {
	// This makes a copy.
	std::list<Context *> remainingKids = GetChildren();

	for (std::list<Context *>::iterator iter = remainingKids.begin();
		iter != remainingKids.end(); iter++)
		delete *iter;
}

ContextID WinProcess::CreateInternalID(ContextOSID osID) {
	// return:  pnnn
	// No parent for a process
	std::string ret = "p";	// a prefix
	ret += AgentUtils::IntToString(osID);

	return ret;
}

HANDLE WinProcess::GetProcessHandle() {
	return processHandle_;
}

WinProcess* WinProcess::GetProcessByID(int processID) {
	return processIDMap[processID];
}

int WinProcess::ReadMemory(unsigned long address, unsigned long size,
		char* memBuffer, unsigned long bufferSize, unsigned long& sizeRead) {
	// to do: handle breakpoints and watchpoints
	int result = 0;

	SIZE_T memRead = 0;
	boolean success = ReadProcessMemory(processHandle_, (LPCVOID) address,
			memBuffer, size, &memRead);
	if (success) {
	} else
		result = GetLastError();

	return result;
}

void WinProcess::Terminate() throw (AgentException) {
	monitor_->PostAction(new TerminateProcessAction(GetOSID()));
}

void WinProcess::Resume() throw (AgentException) {
	std::list<Context*> kids = GetChildren();

	std::list<Context *>::iterator iter;
	for (iter = kids.begin(); iter != kids.end(); iter++)
	{
		WinThread* thread = dynamic_cast<WinThread*>(*iter);
		if (thread != NULL) {
			thread->Resume();
		}
	}
}

int WinProcess::WriteMemory(unsigned long address, unsigned long size,
		char* memBuffer, unsigned long bufferSize, unsigned long& sizeWritten) {
	// to do: handle breakpoints and watchpoints
	int result = 0;

	SIZE_T memRead = 0;
	boolean success = WriteProcessMemory(processHandle_, (LPVOID) address,
			memBuffer, size, &memRead);
	sizeWritten = memRead;
	if (success) {
	} else
		result = GetLastError();

	return result;
}

WinDebugMonitor* WinProcess::GetMonitor() {
	return monitor_;
}


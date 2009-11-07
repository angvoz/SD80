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

std::map<int, WinProcess*> WinProcess::processIDMap;

WinProcess::WinProcess(WinDebugMonitor* monitor, DEBUG_EVENT& debugEvent) :
	Context(debugEvent.dwProcessId, ROOT_CONTEXT_ID, CreateInternalID(debugEvent.dwProcessId)),
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

	SetProperty(PROP_NAME, processName_);
}

WinProcess::WinProcess(DWORD procID, std::string procName) :
	Context(procID, ROOT_CONTEXT_ID, CreateInternalID(procID)),
	processHandle_(NULL),
	monitor_(NULL),
	processName_(procName)
{
	SetProperty(PROP_NAME, processName_);
}

WinProcess::~WinProcess(void) {
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

ContextAddress WinProcess::GetPCAddress() {
	// not needed for WinProcess
	assert(false);
	return 0;
}

std::string WinProcess::GetSuspendReason() {
	// not needed.
	assert(false);
	return "";
}

WinProcess* WinProcess::GetProcessByID(int processID) {
	return processIDMap[processID];
}

std::vector<std::string> WinProcess::GetRegisterValues(
		std::vector<std::string> registerIDs) {
	// not needed.
	assert(false);
	std::vector<std::string> empty;
	return empty;
}

void WinProcess::AttachSelf() throw (AgentException) {
}

void WinProcess::SetRegisterValues(std::vector<std::string> registerIDs,
		std::vector<std::string> registerValues) {
	// not needed.
	assert(false);
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

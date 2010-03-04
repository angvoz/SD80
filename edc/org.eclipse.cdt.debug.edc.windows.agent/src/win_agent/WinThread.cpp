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
#include <stdio.h>
#include <assert.h>

#include "stdafx.h"
#include "WinThread.h"
#include "WinProcess.h"
#include "AgentUtils.h"
#include "EventClientNotifier.h"
#include "Logger.h"
#include "WinDebugMonitor.h"
#include "ResumeContextAction.h"
#include "ProtocolConstants.h"
// TODO: remove this
#include "TCFHeaders.h"

std::map<std::pair<int, int>, WinThread*> WinThread::threadIDMap_;

WinThread::WinThread(WinProcess& process, DEBUG_EVENT& debugEvent) :
	RunControlContext(debugEvent.dwThreadId, process.GetID(), CreateInternalID(debugEvent.dwThreadId, process.GetID())),
	parentProcess_(process)
{
	process.AddChild(this);

	std::pair<int, int> ptPair(debugEvent.dwProcessId, debugEvent.dwThreadId);
	threadIDMap_[ptPair] = this;
	threadContextValid_ = false;
	if (debugEvent.dwDebugEventCode == CREATE_PROCESS_DEBUG_EVENT) {
		handle_ = debugEvent.u.CreateProcessInfo.hThread;
		startAddress_
				= (unsigned long) debugEvent.u.CreateProcessInfo.lpStartAddress;
		localBase_ = debugEvent.u.CreateProcessInfo.lpThreadLocalBase;
	} else if (debugEvent.dwDebugEventCode == CREATE_THREAD_DEBUG_EVENT) {
		handle_ = debugEvent.u.CreateThread.hThread;
		startAddress_
				= (unsigned long) debugEvent.u.CreateThread.lpStartAddress;
		localBase_ = debugEvent.u.CreateThread.lpThreadLocalBase;
	}
	isSuspended_ = false;
	isTerminating_ = false;
	isUserSuspended_ = false;

	initialize();
}

void WinThread::initialize()
{
	RunControlContext::initialize();

	SetProperty(PROP_OS_ID, new PropertyValue(AgentUtils::IntToString(GetOSID())) );

	char buf[32];
	_snprintf(buf, sizeof(buf), "0x%08x", startAddress_);
	SetProperty(PROP_NAME, new PropertyValue(buf));

	int supportedResumeModes = (1 << RM_RESUME) | (1 << RM_STEP_INTO);
	SetProperty(PROP_CAN_RESUME, new PropertyValue(supportedResumeModes));

	SetProperty(PROP_CAN_TERMINATE, new PropertyValue(true));
	SetProperty(PROP_CAN_SUSPEND, new PropertyValue(true));
}

int WinThread::GetThreadID() {
	return GetOSID();
}

WinThread::~WinThread(void) {
	parentProcess_.RemoveChild(this);
}

ContextID WinThread::CreateInternalID(ContextOSID osID, ContextID parentID) {
	// return:  parentID.Ttid
	ContextID ret = parentID;
	ret += ".t";	// a prefix
	ret += AgentUtils::IntToString(osID);

	return ret;
}

ContextAddress WinThread::GetPCAddress() {
	// The following is actually the address of the instruction that causes
	// the exception, not the actual PC register value which is usually 
	// pointing to the byte after the exception instruction.
	// But what we need here is PC value.
	//
	// exceptionInfo_.ExceptionRecord.ExceptionAddress;

	assert(threadContextValid_);
	return threadContextInfo_.Eip;
}

std::string WinThread::GetSuspendReason() {
	std::string reason = "Exception";

	switch (exceptionInfo_.ExceptionRecord.ExceptionCode) {
	case USER_SUSPEND_THREAD:
		return "Suspended";
	case EXCEPTION_SINGLE_STEP:
		return "Step";
	case EXCEPTION_BREAKPOINT:
		return "Breakpoint";
	}

	return reason;
}

void WinThread::MarkSuspended() {
	isSuspended_ = true;
	threadContextValid_ = false;
}

void WinThread::HandleException(DEBUG_EVENT& debugEvent) {
	MarkSuspended();
	exceptionInfo_ = debugEvent.u.Exception;
	EnsureValidContextInfo();
	EventClientNotifier::SendContextSuspended(this);
}

void WinThread::HandleExecutableEvent(bool isLoaded, std::string exePath,
		unsigned long baseAddress, unsigned long codeSize) {
	MarkSuspended();
	EnsureValidContextInfo();
	EventClientNotifier::SendExecutableEvent(this, isLoaded,
			threadContextInfo_.Eip, exePath, baseAddress, codeSize);
}

bool WinThread::isSuspended() {
	return isSuspended_;
}

#ifndef CONTEXT_ALL
#define CONTEXT_ALL             (CONTEXT_CONTROL | CONTEXT_INTEGER | CONTEXT_SEGMENTS | \
	CONTEXT_FLOATING_POINT | CONTEXT_DEBUG_REGISTERS | \
	CONTEXT_EXTENDED_REGISTERS)
#endif

void WinThread::EnsureValidContextInfo() {
	if (!threadContextValid_ && isSuspended()) {
		threadContextInfo_.ContextFlags = CONTEXT_ALL;
		if (GetThreadContext(handle_, &threadContextInfo_) != 0) {
			registerValueCache_.clear();
			// Cache general registers
			registerValueCache_["EAX"] = AgentUtils::IntToHexString(
					threadContextInfo_.Eax);
			registerValueCache_["ECX"] = AgentUtils::IntToHexString(
					threadContextInfo_.Ecx);
			registerValueCache_["EDX"] = AgentUtils::IntToHexString(
					threadContextInfo_.Edx);
			registerValueCache_["EBX"] = AgentUtils::IntToHexString(
					threadContextInfo_.Ebx);
			registerValueCache_["ESP"] = AgentUtils::IntToHexString(
					threadContextInfo_.Esp);
			registerValueCache_["EBP"] = AgentUtils::IntToHexString(
					threadContextInfo_.Ebp);
			registerValueCache_["ESI"] = AgentUtils::IntToHexString(
					threadContextInfo_.Esi);
			registerValueCache_["EDI"] = AgentUtils::IntToHexString(
					threadContextInfo_.Edi);
			registerValueCache_["EIP"] = AgentUtils::IntToHexString(
					threadContextInfo_.Eip);
			registerValueCache_["GS"] = AgentUtils::IntToHexString(
					threadContextInfo_.SegGs);
			registerValueCache_["FS"] = AgentUtils::IntToHexString(
					threadContextInfo_.SegFs);
			registerValueCache_["ES"] = AgentUtils::IntToHexString(
					threadContextInfo_.SegEs);
			registerValueCache_["DS"] = AgentUtils::IntToHexString(
					threadContextInfo_.SegDs);
			registerValueCache_["CS"] = AgentUtils::IntToHexString(
					threadContextInfo_.SegCs);
			registerValueCache_["EFL"] = AgentUtils::IntToHexString(
					threadContextInfo_.EFlags);
			registerValueCache_["SS"] = AgentUtils::IntToHexString(
					threadContextInfo_.SegSs);

			threadContextValid_ = true;
		}
	}
}

void WinThread::SetContextInfo() {
	if (isSuspended()) {
		threadContextInfo_.ContextFlags = CONTEXT_ALL;
		// Set general registers values
		threadContextInfo_.Eax = AgentUtils::HexStringToInt(
				registerValueCache_["EAX"]);
		threadContextInfo_.Ecx = AgentUtils::HexStringToInt(
				registerValueCache_["ECX"]);
		threadContextInfo_.Edx = AgentUtils::HexStringToInt(
				registerValueCache_["EDX"]);
		threadContextInfo_.Ebx = AgentUtils::HexStringToInt(
				registerValueCache_["EBX"]);
		threadContextInfo_.Esp = AgentUtils::HexStringToInt(
				registerValueCache_["ESP"]);
		threadContextInfo_.Ebp = AgentUtils::HexStringToInt(
				registerValueCache_["EBP"]);
		threadContextInfo_.Esi = AgentUtils::HexStringToInt(
				registerValueCache_["ESI"]);
		threadContextInfo_.Edi = AgentUtils::HexStringToInt(
				registerValueCache_["EDI"]);
		threadContextInfo_.Eip = AgentUtils::HexStringToInt(
				registerValueCache_["EIP"]);
		threadContextInfo_.SegGs = AgentUtils::HexStringToInt(
				registerValueCache_["GS"]);
		threadContextInfo_.SegFs = AgentUtils::HexStringToInt(
				registerValueCache_["FS"]);
		threadContextInfo_.SegEs = AgentUtils::HexStringToInt(
				registerValueCache_["ES"]);
		threadContextInfo_.SegDs = AgentUtils::HexStringToInt(
				registerValueCache_["DS"]);
		threadContextInfo_.SegCs = AgentUtils::HexStringToInt(
				registerValueCache_["CS"]);
		threadContextInfo_.EFlags = AgentUtils::HexStringToInt(
				registerValueCache_["EFL"]);
		threadContextInfo_.SegSs = AgentUtils::HexStringToInt(
				registerValueCache_["SS"]);
		SetThreadContext(handle_, &threadContextInfo_);
	}
}

WinThread* WinThread::GetThreadByID(int processID, int threadID) {
	std::pair<int, int> ptPair(processID, threadID);
	return threadIDMap_[ptPair];
}

std::vector<std::string> WinThread::GetRegisterValues(
		std::vector<std::string> registerIDs) {
	std::vector<std::string> registerValues;

	if (isSuspended()) {
		EnsureValidContextInfo();

		std::vector<std::string>::iterator itVectorData;
		for (itVectorData = registerIDs.begin(); itVectorData
				!= registerIDs.end(); itVectorData++) {
			std::string registerID = *itVectorData;
			std::string registerValue = registerValueCache_[registerID];
			registerValues.push_back(registerValue);
		}
	}

	return registerValues;
}

void WinThread::SetRegisterValues(std::vector<std::string> registerIDs,
		std::vector<std::string> registerValues) {
	if (isSuspended()) {
		std::vector<std::string>::reverse_iterator itVectorData;
		for (itVectorData = registerIDs.rbegin(); itVectorData
				!= registerIDs.rend(); itVectorData++) {
			std::string registerID = *itVectorData;
			registerValueCache_[registerID] = registerValues.back();
			registerValues.pop_back();
		}

		SetContextInfo();
	}
}

int WinThread::ReadMemory(unsigned long address, unsigned long size,
		char* memBuffer, unsigned long bufferSize, unsigned long& sizeRead) {
	return parentProcess_.ReadMemory(address, size, memBuffer, bufferSize,
			sizeRead);
}

int WinThread::WriteMemory(unsigned long address, unsigned long size,
		char* memBuffer, unsigned long bufferSize, unsigned long& sizeWritten) {
	return parentProcess_.WriteMemory(address, size, memBuffer, bufferSize,
			sizeWritten);
}

void WinThread::Terminate() throw (AgentException) {
	parentProcess_.Terminate();
}

void WinThread::Suspend() throw (AgentException) {
	DWORD suspendCount = SuspendThread(handle_);
	MarkSuspended();
	EnsureValidContextInfo();
	exceptionInfo_.ExceptionRecord.ExceptionCode = USER_SUSPEND_THREAD; // "Suspended"
	isUserSuspended_ = true;
	EventClientNotifier::SendContextSuspended(this);
	Logger::getLogger().Log(Logger::LOG_NORMAL, "WinThread::Suspend",
			"suspendCount: %d", suspendCount);
}

void WinThread::Resume() throw (AgentException) {
	if (isUserSuspended_){
		ResumeThread(handle_);
		isUserSuspended_ = false;
	}
	else {
		parentProcess_.GetMonitor()->PostAction(new ResumeContextAction(
			parentProcess_.GetOSID(), GetOSID()));
	}
}

void WinThread::SingleStep() throw (AgentException) {
	//if (exceptionInfo_.ExceptionRecord.ExceptionCode == USER_SUSPEND_THREAD){
	//	ResumeThread(handle_);
	//}
#define FLAG_TRACE_BIT 0x100
	threadContextInfo_.EFlags |= FLAG_TRACE_BIT;
	SetThreadContext(handle_, &threadContextInfo_);

	parentProcess_.GetMonitor()->PostAction(new ResumeContextAction(
			parentProcess_.GetOSID(), GetOSID()));
}

void WinThread::PrepareForTermination() throw (AgentException) {
	isTerminating_ = true;
	if (isSuspended()) {
		Suspend();
		ContinueDebugEvent(parentProcess_.GetOSID(), GetOSID(), DBG_CONTINUE);
	}
}

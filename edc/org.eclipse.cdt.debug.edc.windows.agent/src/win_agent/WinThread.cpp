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
#include "RunControlService.h"

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

// Initialize thread specific properties.
void WinThread::initialize()
{
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

/*
 * Get pointer to register value cache for a given register.
 * Return NULL if the register is not found.
 */
void* WinThread::getRegisterValueBuffer(const std::string& regName) {
	void* v = NULL;

	if (regName == "EAX")
		v = (void*)&threadContextInfo_.Eax;
	else if (regName == "EBX")
		v = (void*)&threadContextInfo_.Ebx;
	else if (regName == "ECX")
		v = (void*)&threadContextInfo_.Ecx;
	else if (regName == "EDX")
		v = (void*)&threadContextInfo_.Edx;
	else if (regName == "ESP")
		v = (void*)&threadContextInfo_.Esp;
	else if (regName == "EBP")
		v = (void*)&threadContextInfo_.Ebp;
	else if (regName == "ESI")
		v = (void*)&threadContextInfo_.Esi;
	else if (regName == "EDI")
		v = (void*)&threadContextInfo_.Edi;
	else if (regName == "EIP")
		v = (void*)&threadContextInfo_.Eip;
	else if (regName == "EFL")
		v = (void*)&threadContextInfo_.EFlags;
	else if (regName == "GS")
		v = (void*)&threadContextInfo_.SegGs;
	else if (regName == "FS")
		v = (void*)&threadContextInfo_.SegFs;
	else if (regName == "ES")
		v = (void*)&threadContextInfo_.SegEs;
	else if (regName == "DS")
		v = (void*)&threadContextInfo_.SegDs;
	else if (regName == "CS")
		v = (void*)&threadContextInfo_.SegCs;
	else if (regName == "SS")
		v = (void*)&threadContextInfo_.SegSs;
	else {
		assert(false);
	}

	return v;
}

/*
 * Read one register.
 * Return binary data buffer, which caller should free by calling delete[].
 */
char* WinThread::GetRegisterValue(std::string regName, int regSize) {

	char* ret = NULL;

	if (isSuspended()) {
		EnsureValidContextInfo();

		ret = new char[regSize];

		void* v = getRegisterValueBuffer(regName);
		assert(v != NULL);

		memcpy((void*)ret, v, regSize);
	}

	return ret;
}

bool WinThread::SetRegisterValue(std::string regName, int regSize, char* val) {

	if (! isSuspended())
		return false;

	void* v = getRegisterValueBuffer(regName);
	assert(v != NULL);

	memcpy(v, (void*)val, regSize);
	return SetThreadContext(handle_, &threadContextInfo_);
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
	if (! isTerminating_)	// don't send Suspend event if we are terminating.
		EventClientNotifier::SendContextSuspended(this);
	Logger::getLogger().Log(Logger::LOG_NORMAL, "WinThread::Suspend",
			"suspendCount: %d", suspendCount);
}

void WinThread::Resume() throw (AgentException) {
	if (! isSuspended())
		return;

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

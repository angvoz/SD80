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
	ThreadContext(debugEvent.dwThreadId, process.GetID(), CreateInternalID(debugEvent.dwThreadId, process.GetID())),
	threadLookupPair_(debugEvent.dwProcessId, debugEvent.dwThreadId),
	parentProcess_(process)
{
	process.AddChild(this);

	threadIDMap_[threadLookupPair_] = this;

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
	threadIDMap_.erase(threadLookupPair_);
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

const char* WinThread::GetSuspendReason() {
	const char* reason = REASON_EXCEPTION;

	switch (exceptionInfo_.ExceptionRecord.ExceptionCode) {
	case USER_SUSPEND_THREAD:
		return REASON_USER_REQUEST;
	case EXCEPTION_SINGLE_STEP:
		return REASON_STEP;
	case EXCEPTION_BREAKPOINT:
		return REASON_BREAKPOINT;
	}

	return reason;
}


std::string WinThread::GetExceptionMessage() {
	if (exceptionInfo_.ExceptionRecord.ExceptionCode == EXCEPTION_SINGLE_STEP
			|| exceptionInfo_.ExceptionRecord.ExceptionCode == EXCEPTION_BREAKPOINT
			|| exceptionInfo_.ExceptionRecord.ExceptionCode == USER_SUSPEND_THREAD)
		return "";

	return WinDebugMonitor::GetDebugExceptionDescription(exceptionInfo_);
}

void WinThread::MarkSuspended() {
	isSuspended_ = true;
	threadContextValid_ = false;
}

void WinThread::HandleException(DEBUG_EVENT& debugEvent) {
	MarkSuspended();
	exceptionInfo_ = debugEvent.u.Exception;
	EnsureValidContextInfo();
	EventClientNotifier::SendContextSuspended(this,
			GetPCAddress(), GetSuspendReason(), GetExceptionMessage());

}

void WinThread::HandleExecutableEvent(bool isLoaded, const std::string& exePath,
		unsigned long baseAddress, unsigned long codeSize) {
	MarkSuspended();
	EnsureValidContextInfo();
	
	Properties props;
	props[PROP_FILE] = new PropertyValue(exePath);
	props[PROP_NAME] = new PropertyValue(AgentUtils::GetFileNameFromPath(exePath));
	props[PROP_MODULE_LOADED] = new PropertyValue(isLoaded);
	props[PROP_IMAGE_BASE_ADDRESS] = new PropertyValue((int) baseAddress);
	props[PROP_CODE_SIZE] = new PropertyValue((int) codeSize);
	EventClientNotifier::SendExecutableEvent(this, 
			threadContextInfo_.Eip, props);
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
	std::map<std::pair<int, int>, WinThread*>::iterator iter = threadIDMap_.find(ptPair);
	if (iter == threadIDMap_.end())
		return NULL;
	else
		return iter->second;
}

std::vector<std::string> WinThread::GetRegisterValues(
		const std::vector<std::string>& registerIDs) {
	std::vector<std::string> registerValues;

	if (isSuspended()) {
		EnsureValidContextInfo();

		std::vector<std::string>::const_iterator itVectorData;
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
char* WinThread::GetRegisterValue(const std::string& regName, int regSize) {

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

bool WinThread::SetRegisterValue(const std::string& regName, int regSize, char* val) {

	if (! isSuspended())
		return false;

	void* v = getRegisterValueBuffer(regName);
	assert(v != NULL);

	memcpy(v, (void*)val, regSize);
	return SetThreadContext(handle_, &threadContextInfo_);
}

void WinThread::SetRegisterValues(const std::vector<std::string>& registerIDs,
		const std::vector<std::string>& registerValues) {
	if (isSuspended()) {
		std::vector<std::string>::const_reverse_iterator itVectorData;
		int idx = registerValues.size();
		for (itVectorData = registerIDs.rbegin(); itVectorData
				!= registerIDs.rend(); itVectorData++) {
			std::string registerID = *itVectorData;
			registerValueCache_[registerID] = registerValues[--idx];
		}

		SetContextInfo();
	}
}

int WinThread::ReadMemory(const ReadWriteMemoryParams& params) throw (AgentException) {
	return parentProcess_.ReadMemory(params);
}

int WinThread::WriteMemory(const ReadWriteMemoryParams& params) throw (AgentException) {
	return parentProcess_.WriteMemory(params);
}

void WinThread::Terminate(const AgentActionParams& params) throw (AgentException) {
	parentProcess_.Terminate(params);
}

void WinThread::Suspend(const AgentActionParams& params) throw (AgentException) {
	DWORD suspendCount = SuspendThread(handle_);
	MarkSuspended();
	EnsureValidContextInfo();
	exceptionInfo_.ExceptionRecord.ExceptionCode = USER_SUSPEND_THREAD; // "Suspended"
	isUserSuspended_ = true;
	if (! isTerminating_)	// don't send Suspend event if we are terminating.
		EventClientNotifier::SendContextSuspended(this,
				GetPCAddress(), GetSuspendReason(), GetExceptionMessage());
	Logger::getLogger().Log(Logger::LOG_NORMAL, "WinThread::Suspend",
			"suspendCount: %d", suspendCount);

	params.reportSuccessForAction();
}

void WinThread::Resume(const AgentActionParams& params) throw (AgentException) {
	if (! isSuspended()) {
		params.reportSuccessForAction();
		return;
	}

	if (isUserSuspended_){
		ResumeThread(handle_);
		isUserSuspended_ = false;
		params.reportSuccessForAction();
	}
	else {
		parentProcess_.GetMonitor()->PostAction(new ResumeContextAction(
			params, parentProcess_.GetOSID(), GetOSID()));
	}
}

void WinThread::SingleStep(const AgentActionParams& params) throw (AgentException) {
	//if (exceptionInfo_.ExceptionRecord.ExceptionCode == USER_SUSPEND_THREAD){
	//	ResumeThread(handle_);
	//}
#define FLAG_TRACE_BIT 0x100
	threadContextInfo_.EFlags |= FLAG_TRACE_BIT;
	SetThreadContext(handle_, &threadContextInfo_);

	parentProcess_.GetMonitor()->PostAction(new ResumeContextAction(
			params, parentProcess_.GetOSID(), GetOSID()));
}

void WinThread::PrepareForTermination(const AgentActionParams& params) throw (AgentException) {
	isTerminating_ = true;

	if (isSuspended()) {
		Suspend(params);
		ContinueDebugEvent(parentProcess_.GetOSID(), GetOSID(), DBG_CONTINUE);
	}
}

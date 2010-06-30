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

#include "stdafx.h"
#include "ThreadContext.h"

#define USER_SUSPEND_THREAD 0

class WinProcess;

class WinThread : public ThreadContext {
public:
	WinThread(WinProcess& process, DEBUG_EVENT& debugEvent);
	virtual ~WinThread(void);

	int GetThreadID();

	static WinThread* GetThreadByID(int processID, int threadID);

	virtual std::vector<std::string> GetRegisterValues(
			const std::vector<std::string>& registerIDs);

	virtual void SetRegisterValues(const std::vector<std::string>& registerIDs,
			const std::vector<std::string>& registerValues);

	char*	GetRegisterValue(const std::string& regName, int regSize);
	bool 	SetRegisterValue(const std::string& regName, int regSize, char* val);

	//
	// overrides of RunControlContext
	//
	virtual int ReadMemory(const ReadWriteMemoryParams& params) throw (AgentException);
	virtual int WriteMemory(const ReadWriteMemoryParams& params) throw (AgentException);

	virtual void Resume(const AgentActionParams& params) throw (AgentException);

	virtual void Suspend(const AgentActionParams& params) throw (AgentException);

	virtual void Terminate(const AgentActionParams& params) throw (AgentException);

	virtual void SingleStep(const AgentActionParams& params) throw (AgentException);
	//
	//	end overrides
	
	void PrepareForTermination(const AgentActionParams& params) throw (AgentException);

	void HandleException(DEBUG_EVENT& debugEvent);
	void HandleExecutableEvent(bool isLoaded, const std::string& exePath,
			unsigned long baseAddress, unsigned long codeSize);

private:
	void initialize();

	void EnsureValidContextInfo();
	void SetContextInfo();
	bool isSuspended();
	void MarkSuspended();

	/** Address where suspend is reported */
	ContextAddress GetPCAddress();
	/** REASON_xx code for suspend */
	const char* GetSuspendReason();
	/** Description for suspend */
	std::string GetExceptionMessage();

private:
	void* getRegisterValueBuffer(const std::string& regName);

	std::pair<int, int> threadLookupPair_;

	bool threadContextValid_;
	bool isSuspended_;
	bool isTerminating_;
	bool isUserSuspended_;
	CONTEXT threadContextInfo_;
	EXCEPTION_DEBUG_INFO exceptionInfo_;
	std::map<std::string, std::string> registerValueCache_;

	WinProcess& parentProcess_;

	unsigned long startAddress_;
	HANDLE handle_;
	void* localBase_;

	static std::map<std::pair<int, int>, WinThread*> threadIDMap_;

};

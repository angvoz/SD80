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
#include "RunControlContext.h"

#define USER_SUSPEND_THREAD 0

class WinProcess;

class WinThread: public RunControlContext {
public:
	WinThread(WinProcess& process, DEBUG_EVENT& debugEvent);
	~WinThread(void);

	int GetThreadID();

	static WinThread* GetThreadByID(int processID, int threadID);

	virtual ContextAddress GetPCAddress();
	virtual std::string GetSuspendReason();

	virtual std::vector<std::string> GetRegisterValues(
			std::vector<std::string> registerIDs);

	virtual void SetRegisterValues(std::vector<std::string> registerIDs,
			std::vector<std::string> registerValues);

	virtual int ReadMemory(unsigned long address, unsigned long size,
			char* memBuffer, unsigned long bufferSize, unsigned long& sizeRead);
	virtual int WriteMemory(unsigned long address, unsigned long size,
			char* memBuffer, unsigned long bufferSize,
			unsigned long& sizeWritten);

	virtual void Resume() throw (AgentException);

	virtual void Suspend() throw (AgentException);

	virtual void Terminate() throw (AgentException);

	void PrepareForTermination() throw (AgentException);

	virtual void SingleStep() throw (AgentException);

	void HandleException(DEBUG_EVENT& debugEvent);
	void HandleExecutableEvent(bool isLoaded, std::string exePath,
			unsigned long baseAddress, unsigned long codeSize);

	static ContextID CreateInternalID(ContextOSID osID, ContextID parentID);


protected:
	virtual void initialize();

private:
	void EnsureValidContextInfo();
	void SetContextInfo();
	bool isSuspended();
	void MarkSuspended();

private:

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

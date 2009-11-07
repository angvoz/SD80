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

#include <vector>
#include "stdafx.h"
#include "Context.h"

class WinThread;
class WinDebugMonitor;

class WinProcess: public Context {
public:
	WinProcess(WinDebugMonitor* monitor, DEBUG_EVENT& debugEvent);
	WinProcess(DWORD procID, std::string procName);

	~WinProcess(void);

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

	virtual void AttachSelf() throw (AgentException);

	virtual void Resume() throw (AgentException);

	virtual void Suspend() throw (AgentException) {/* TODO */
	}
	;

	virtual void Terminate() throw (AgentException);

	virtual void SingleStep() throw (AgentException) { /* TODO */
	}
	;

	HANDLE GetProcessHandle();

	WinDebugMonitor* GetMonitor();

	static WinProcess* GetProcessByID(int processID);

	static ContextID CreateInternalID(ContextOSID osID);

private:

	bool isRoot_;
	HANDLE processHandle_;
	std::string processName_;
	WinDebugMonitor* monitor_;

	static std::map<int, WinProcess*> processIDMap;
};

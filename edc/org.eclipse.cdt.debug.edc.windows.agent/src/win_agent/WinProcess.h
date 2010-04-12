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
#include "RunControlContext.h"

class WinThread;
class WinDebugMonitor;

class WinProcess: public RunControlContext {
public:
	WinProcess(WinDebugMonitor* monitor, DEBUG_EVENT& debugEvent);
	WinProcess(DWORD procID, std::string procName);

	virtual ~WinProcess(void);

	virtual int ReadMemory(unsigned long address, unsigned long size,
			char* memBuffer, unsigned long bufferSize, unsigned long& sizeRead);
	virtual int WriteMemory(unsigned long address, unsigned long size,
			char* memBuffer, unsigned long bufferSize,
			unsigned long& sizeWritten);

	virtual void Resume() throw (AgentException);

	virtual void Suspend() throw (AgentException) {/* TODO */};

	virtual void Terminate() throw (AgentException);

	virtual void SingleStep() throw (AgentException) { /* TODO */};

	HANDLE GetProcessHandle();

	WinDebugMonitor* GetMonitor();

	static WinProcess* GetProcessByID(int processID);

	static ContextID CreateInternalID(ContextOSID osID);

private:
	void initialize();

	bool isRoot_;
	HANDLE processHandle_;
	std::string processName_;
	WinDebugMonitor* monitor_;

	static std::map<int, WinProcess*> processIDMap;
};

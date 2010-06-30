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
#include "ProcessContext.h"
#include "WinDebugMonitor.h"

class WinThread;
class WinDebugMonitor;

class WinProcess : public ProcessContext {
public:
	WinProcess(WinDebugMonitor* monitor, DEBUG_EVENT& debugEvent);
	WinProcess(DWORD procID, std::string procName);

	virtual ~WinProcess(void);

	//
	// Overrides of RunControlContext
	//
	virtual int ReadMemory(const ReadWriteMemoryParams& params) throw (AgentException);
	virtual int WriteMemory(const ReadWriteMemoryParams& params) throw (AgentException);
	virtual void Terminate(const AgentActionParams& params) throw (AgentException);
	virtual void SingleStep(const AgentActionParams& params) throw (AgentException);
	//
	//	end overrides

	HANDLE GetProcessHandle();

	WinDebugMonitor* GetMonitor();

	static WinProcess* GetProcessByID(int processID);

private:
	void initialize();

	bool isRoot_;
	HANDLE processHandle_;
	std::string processName_;
	WinDebugMonitor* monitor_;

	static std::map<int, WinProcess*> processIDMap;
};

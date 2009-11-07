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
#include "TerminateProcessAction.h"
#include "WinProcess.h"
#include "WinThread.h"

TerminateProcessAction::TerminateProcessAction(ContextOSID processID) :
	processID_(processID) {
}

TerminateProcessAction::~TerminateProcessAction(void) {
}

void TerminateProcessAction::Run() {
	WinProcess* process = WinProcess::GetProcessByID(processID_);

	std::list<Context*> threads = process->GetChildren();

	std::list<Context*>::iterator itr;
	for (itr = threads.begin(); itr != threads.end(); itr++) {
		((WinThread*) *itr)->PrepareForTermination();
	}

	if (!TerminateProcess(process->GetProcessHandle(), 0)) {
		DWORD error = GetLastError();
	}
}

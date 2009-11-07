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
#include "ResumeContextAction.h"
#include "Context.h"
#include "WinProcess.h"
#include "WinThread.h"

ResumeContextAction::ResumeContextAction(ContextOSID processid,
		ContextOSID threadid) {
	processid_ = processid;
	threadid_ = threadid;
}

ResumeContextAction::~ResumeContextAction(void) {
}

void ResumeContextAction::Run() {
	ContinueDebugEvent(processid_, threadid_, DBG_CONTINUE);
}

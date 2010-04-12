/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Nokia - Initial API and implementation
 *******************************************************************************/
#include "DetachProcessAction.h"
#include "TCFChannel.h"

#ifdef __cplusplus
extern "C" {
#endif
	#include "errors.h"
#ifdef __cplusplus
}
#endif


DetachProcessAction::DetachProcessAction(ContextOSID processID, char* token, Channel* channel) :
	processID(processID) {
	tcfToken = token;
	this->channel = channel;
}

DetachProcessAction::~DetachProcessAction(void) {
}

void DetachProcessAction::Run() {
	TCFChannel tcfChannel(channel);

	tcfChannel.writeReplyHeader((char*)tcfToken.c_str());

	if (! DebugActiveProcessStop(processID)) {
		DWORD err = GetLastError();

		tcfChannel.writeError(set_win32_errno(err));
	}
	else
		tcfChannel.writeError(0);

	tcfChannel.writeComplete();
}

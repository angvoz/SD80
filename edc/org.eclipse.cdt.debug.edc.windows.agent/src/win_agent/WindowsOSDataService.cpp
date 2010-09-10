/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
#include "WindowsOSDataService.h"

#include <algorithm>
#include <string>
#include <set>
#include <Tlhelp32.h>

#include "TCFHeaders.h"
#include "TCFChannel.h"
#include "WinDebugMonitor.h"
#include "WinProcess.h"
#include "AgentUtils.h"
#include "ContextManager.h"
#include "EventClientNotifier.h"
#include "Logger.h"
#include "DetachProcessAction.h"

static const char * sServiceName = "WindowsOSData";

WindowsOSDataService::WindowsOSDataService(Protocol * proto) :
	TCFService(proto) {
	AddCommand("getThreads", command_get_threads);
}

WindowsOSDataService::~WindowsOSDataService(void) {
}

const char* WindowsOSDataService::GetName() {
	return sServiceName;
}

void WindowsOSDataService::command_get_threads(const char * token, Channel * c) {
	LogTrace("WindowsOSDataService::command_get_threads", "token: %s", token);
	TCFChannel channel(c);
	channel.readComplete();

    DWORD err = 0;
    HANDLE snapshot;
    THREADENTRY32 thread32;

    snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD, 0);
    if (snapshot == INVALID_HANDLE_VALUE) err = set_win32_errno(GetLastError());
    memset(&thread32, 0, sizeof(thread32));
    thread32.dwSize = sizeof(THREADENTRY32);
    if (!err && !Thread32First(snapshot, &thread32)) {
        err = set_win32_errno(GetLastError());
        CloseHandle(snapshot);
    }
	channel.writeReplyHeader(token);
	channel.writeError(err);
    if (err) {
    	channel.writeStringZ("null");
    }
    else {

        int cnt = 0;
    	channel.writeCharacter('[');

        do {
            if (cnt > 0) channel.writeCharacter(',');

            channel.writeCharacter('{');

        	channel.writeString("OSID");
    		channel.writeCharacter(':');
    		channel.writeLong(thread32.th32ThreadID);
    		channel.writeCharacter(',');
        	channel.writeString("p_os_id");
    		channel.writeCharacter(':');
    		channel.writeLong(thread32.th32OwnerProcessID);
    		channel.writeCharacter(',');
        	channel.writeString("pri");
    		channel.writeCharacter(':');
    		channel.writeLong(thread32.tpBasePri);
   		channel.writeCharacter('}');

            cnt++;
        } while (Thread32Next(snapshot, &thread32));

    	channel.writeCharacter(']');
    	channel.writeZero(); // end of context
    }
    if (snapshot != INVALID_HANDLE_VALUE)
    	CloseHandle(snapshot);

	channel.writeComplete();

}

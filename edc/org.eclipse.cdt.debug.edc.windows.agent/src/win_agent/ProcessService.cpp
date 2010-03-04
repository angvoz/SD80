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
#include "ProcessService.h"

#include <string>
#include <Tlhelp32.h>

#include "TCFHeaders.h"
#include "TCFChannel.h"
#include "WinDebugMonitor.h"
#include "WinProcess.h"
#include "AgentUtils.h"
#include "ContextManager.h"
#include "EventClientNotifier.h"
#include "Logger.h"

static const char * sServiceName = "Processes";

static std::string quoteIfNeeded(const char* source);
static void initializeDebugSession();

ProcessService::ProcessService(Protocol * proto) :
	TCFService(proto) {
	AddCommand("getContext", command_get_context);
	AddCommand("getChildren", command_get_children);
	AddCommand("attach", command_attach);
	AddCommand("detach", command_detach);
	AddCommand("terminate", command_terminate);
	AddCommand("signal", command_signal);
	AddCommand("getEnvironment", command_get_environment);
	AddCommand("start", command_start);
}

ProcessService::~ProcessService(void) {
}

const char* ProcessService::GetName() {
	return sServiceName;
}

void ProcessService::command_get_context(char * token, Channel * c) {
	LogTrace("ProcessService::command_get_context", "token: %s", token);
	TCFChannel channel(c);

	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	Context* context = ContextManager::FindRunningContext(id);

	channel.writeReplyHeader(token);

	if (context == NULL) {
		// Return an invalid context ID error.
		channel.writeError(ERR_INV_CONTEXT);
		channel.writeString("null");
	}
	else {
		channel.writeError(0);
		EventClientNotifier::WriteContext(*context, channel);
		channel.writeZero();
	}

	channel.writeComplete();
}

void ProcessService::command_get_children(char * token, Channel * c) {
    char id[256];
    int attached_only;

    json_read_string(&c->inp, id, sizeof(id));
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    attached_only = json_read_boolean(&c->inp);
    if (read_stream(&c->inp) != 0) exception(ERR_JSON_SYNTAX);
    if (read_stream(&c->inp) != MARKER_EOM) exception(ERR_JSON_SYNTAX);

    write_stringz(&c->out, "R");
    write_stringz(&c->out, token);

    if (id[0] != 0) {
        write_errno(&c->out, 0);
        write_stringz(&c->out, "null");
    }
    else {
        DWORD err = 0;
        HANDLE snapshot;
        PROCESSENTRY32 pe32;

        snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
        if (snapshot == INVALID_HANDLE_VALUE) err = set_win32_errno(GetLastError());
        memset(&pe32, 0, sizeof(pe32));
        pe32.dwSize = sizeof(PROCESSENTRY32);
        if (!err && !Process32First(snapshot, &pe32)) {
            err = set_win32_errno(GetLastError());
            CloseHandle(snapshot);
        }
        write_errno(&c->out, err);
        if (err) {
            write_stringz(&c->out, "null");
        }
        else {
        	// Get rid of stale cache.
        	ContextManager::ClearRunningContextCache();

            int cnt = 0;
            write_stream(&c->out, '[');

            do {
                if (!attached_only || WinProcess::GetProcessByID(pe32.th32ProcessID) != NULL) {
                    if (cnt > 0) write_stream(&c->out, ',');
                    // We just pass the OS process ID.

					WinProcess* newProc = new WinProcess(pe32.th32ProcessID, pe32.szExeFile);

					ContextManager::AddRunningContext(newProc);

					// Tell host the unique internal ID.
					json_write_string(&c->out, newProc->GetID().c_str());

                    cnt++;
                }
            } while (Process32Next(snapshot, &pe32));

            write_stream(&c->out, ']');
            write_stream(&c->out, 0);
        }
        if (snapshot != INVALID_HANDLE_VALUE)
        	CloseHandle(snapshot);
    }

    write_stream(&c->out, MARKER_EOM);
}

void ProcessService::command_attach(char * token, Channel * c) {
	LogTrace("ProcessService::command_attach", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	RunControlContext* context = dynamic_cast<RunControlContext*>(ContextManager::FindRunningContext(id));

	channel.writeReplyHeader(token);

	if (context == NULL) {
		// Return an invalid context ID error.
		channel.writeError(ERR_INV_CONTEXT);
		channel.writeComplete();
	}
	else {
		std::string str_tok = token;
		// This function will report result to host debugger.
		WinDebugMonitor::AttachToProcess(context->GetOSID(), true, str_tok, c);
	}

}

void ProcessService::command_detach(char * token, Channel * c) {
}

void ProcessService::command_terminate(char * token, Channel * c) {
}

void ProcessService::command_signal(char * token, Channel * c) {
}

void ProcessService::command_get_environment(char * token, Channel * c) {
	char ** p = environ;

	TCFChannel channel(c);
	channel.readComplete();

	channel.writeReplyHeader(token);
	channel.writeError(0);

	channel.writeCharacter('[');
	if (p != NULL) {
		while (*p != NULL) {
			if (p != environ)
				channel.writeCharacter(',');
			json_write_string(&c->out, *p++);
		}
	}
	channel.writeCharacter(']');
	channel.writeZero();
	channel.writeComplete();
}

std::wstring json_read_string(Channel * c) {
	char stringbuffer[4096];
	json_read_string(&c->inp, stringbuffer, sizeof(stringbuffer));
	if (c->inp.read(&c->inp) != 0)
		exception(ERR_JSON_SYNTAX);

	int wideSize = MultiByteToWideChar(CP_UTF8, 0, stringbuffer, strlen(
			stringbuffer) + 1, NULL, 0);
	wchar_t* wideChars = new wchar_t[wideSize];
	MultiByteToWideChar(CP_UTF8, 0, stringbuffer, strlen(stringbuffer) + 1,
			wideChars, wideSize);
	std::wstring result = wideChars;
	delete[] wideChars;
	return result;
}

void ProcessService::command_start(char * token, Channel * c) {
	TCFChannel channel(c);
	std::string tokenStr = token;
	std::string directory = channel.readString();
	channel.readZero();
	std::string executable = channel.readString();
	channel.readZero();

	initializeDebugSession();

	char ** args = NULL;
	char ** envp = NULL;
	int args_len = 0;
	int envp_len = 0;

	args = json_read_alloc_string_array(&c->inp, &args_len);
	if (c->inp.read(&c->inp) != 0)
		exception(ERR_JSON_SYNTAX);
	envp = json_read_alloc_string_array(&c->inp, &envp_len);
	if (c->inp.read(&c->inp) != 0)
		exception(ERR_JSON_SYNTAX);

	std::vector<std::string> environment;
	for (int i = 0; i < envp_len; i++)
	{
		environment.push_back(envp[i]);
	}

	json_read_boolean(&c->inp); // attach

	if (read_stream(&c->inp) != 0)
		exception(ERR_JSON_SYNTAX);
	channel.readComplete();

	std::string wargs;

	wargs += quoteIfNeeded(executable.c_str());
	for (int i = 0; i < args_len; i++) {
		if (i > 0)
			wargs += ' ';
		wargs += quoteIfNeeded(args[i]);
	}

	loc_free(args);
	loc_free(envp);

	WinDebugMonitor::LaunchProcess(executable, directory, wargs, environment, true,
			tokenStr, c);

}


/**
 * Escape a string for the Win32 command line.
 *
 * Adapted from Win32ProcessEx.c in spawner:
 *
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Wind River Systems, Inc.
 *******************************************************************************/

static std::string quoteIfNeeded(const char* source) {
	std::string target;
	int cpyLength = strlen(source);

	BOOL bSlash = FALSE;
	int i = 0;

#define QUOTATION_DO   0
#define QUOTATION_DONE 1
#define QUOTATION_NONE 2
#undef _T
#define _T(x) x

	int nQuotationMode = 0;

	if((_T('\"') == *source) && (_T('\"') == *(source + cpyLength - 1)))
		{
		nQuotationMode = QUOTATION_DONE;
		}
	else
	if(strchr(source, _T(' ')) == NULL)
		{
		// No reason to quotate term becase it doesn't have embedded spaces
		nQuotationMode = QUOTATION_NONE;
		}
	else
		{
		// Needs to be quotated
		nQuotationMode = QUOTATION_DO;
		target += _T('\"');
		}


	for(; i < cpyLength; ++i)
		{
		if(source[i] == _T('\\'))
			bSlash = TRUE;
		else
			{
			// Don't quote embracing quotation marks
			if((source[i] == _T('\"')) && !((nQuotationMode == QUOTATION_DONE) && ((i == 0) || (i == (cpyLength - 1))) ) )
				{
				if(!bSlash) // If still not escaped
					{
					target += _T('\\');
					}
				}
			bSlash = FALSE;
			}

		target += source[i];
		}

	if(nQuotationMode == QUOTATION_DO)
		{
		target += _T('\"');
		}

	return target;
}

static void initializeDebugSession() {
	// Clear stale data (and free memory).
	// It's not easy to know when the debug session ends in agent. So we
	// do this at beginning of a debug session.
	ContextManager::ClearContextCache();
}


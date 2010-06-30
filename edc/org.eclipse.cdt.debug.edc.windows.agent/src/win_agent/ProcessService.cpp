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

static const char * sServiceName = "Processes";

static std::string quoteIfNeeded(const char* source);

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

/*
 * This will return a context, running or debugged one.
 */
void ProcessService::command_get_context(char * token, Channel * c) {
	LogTrace("ProcessService::command_get_context", "token: %s", token);
	TCFChannel channel(c);

	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	Context* context = ContextManager::findContext(id);
	if (context == NULL) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 1);
		return;
	}
	
	channel.writeReplyHeader(token);
	channel.writeError(0);
	EventClientNotifier::WriteContext(*context, channel);
	channel.writeZero();
	channel.writeComplete();
}

/** Filter to get choose Contexts which are the top-level RunControlContext that were from the
 * previous Processes::getChildren call.  Ignore those that are being debugged. */
static bool IsNotRunningProcessContext(const ContextID& contextID) {
	Context* context = ContextManager::findContext(contextID);
	ProcessContext* rcContext = dynamic_cast<ProcessContext*>(context);
	trace(LOG_ALWAYS, "Context %s: isRC=%d, isDebugging=%d", contextID.c_str(), rcContext!=0, rcContext && rcContext->IsDebugging());
	return !rcContext || rcContext->IsDebugging();
}
/** Remove and delete a context by value. */
static void RemoveAndDeleteContext(const ContextID& contextID) {
	trace(LOG_ALWAYS, "Removing context %s", contextID.c_str());
	delete ContextManager::removeContext(contextID);
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
        	// Get all the known contexts
        	std::list<ContextID> runContexts = ContextManager::getContexts();

        	// Shuffle to the end the ones we want to remove
        	std::list<ContextID>::iterator removeEnd = std::remove_if(runContexts.begin(), runContexts.end(),
        			IsNotRunningProcessContext);

        	// Remove these entries from the manager
        	std::for_each(runContexts.begin(), removeEnd, RemoveAndDeleteContext);

            int cnt = 0;
            write_stream(&c->out, '[');

            do {
                if (!attached_only || WinProcess::GetProcessByID(pe32.th32ProcessID) != NULL) {
                    if (cnt > 0) write_stream(&c->out, ',');
                    // We just pass the OS process ID.

                    // If context still exists, it is being debugged, so leave it be.
					WinProcess* process = dynamic_cast<WinProcess*>(ContextManager::findContext(
							WinProcess::CreateInternalID(pe32.th32ProcessID)));

					if (!process) {
						process = new WinProcess(pe32.th32ProcessID, pe32.szExeFile);
						ContextManager::addContext(process);
					}

					// Tell host the unique internal ID.
					json_write_string(&c->out, process->GetID().c_str());

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

	RunControlContext* context = dynamic_cast<RunControlContext*>(ContextManager::findContext(id));

	// TODO: or not running
	if (context == NULL) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT);
		return;
	}
	
	// This function will report result to host debugger.

	AttachToProcessParams params(token, c, context->GetOSID(), true);
	WinDebugMonitor::AttachToProcess(params);

}

void ProcessService::command_detach(char * token, Channel * c) {
	LogTrace("ProcessService::command_detach", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	WinProcess* context = dynamic_cast<WinProcess*>(ContextManager::findContext(id));

	if (context == NULL || !context->IsDebugging()) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT);
		return;
	}

	context->GetMonitor()->PostAction(new DetachProcessAction(
			AgentActionParams(token, c), context->GetOSID()));
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
	std::string directory = channel.readString();
	channel.readZero();
	std::string executable = channel.readString();
	channel.readZero();

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
		wargs += ' ';
		wargs += quoteIfNeeded(args[i]);
	}

	loc_free(args);
	loc_free(envp);

	LaunchProcessParams params(token, c, executable, directory, wargs, environment, true);
	WinDebugMonitor::LaunchProcess(params);

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


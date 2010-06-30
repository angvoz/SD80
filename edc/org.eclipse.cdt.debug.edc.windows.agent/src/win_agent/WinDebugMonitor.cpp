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

#include <iostream>
#include "TCFHeaders.h"
#include "WinDebugMonitor.h"
#include "Logger.h"
#include "WinProcess.h"
#include "WinThread.h"
#include "EventClientNotifier.h"
#include "AgentUtils.h"
#include "psapi.h"
#include "AgentAction.h"
#include "ContextManager.h"
#include "LoggingService.h"

// These aren't defined in any Windows system headers
#define	EXCEPTION_DLL_NOT_FOUND				((unsigned long) 0xC0000135L)
#define	EXCEPTION_ENTRY_NOT_FOUND			((unsigned long) 0xC0000139L)
#define	EXCEPTION_DLL_INIT_FAIL				((unsigned long) 0xC0000142L)
#define	EXCEPTION_MS_CPLUS					((unsigned long) 0xE06D7363L)
#define	EXCEPTION_VDM_EVENT					((unsigned long) 0x40000005L)

#define BUFSIZE 512


std::string GetExecutableInfo(HANDLE hFile, unsigned long& baseOfCode, unsigned long& codeSize)
{
	codeSize = 0;
	BOOL bSuccess = FALSE;
	TCHAR pszFilename[MAX_PATH+1];
	HANDLE hFileMap;
	std::wstring path;

	// Get the file size.
	DWORD dwFileSizeHi = 0;
	DWORD dwFileSizeLo = GetFileSize(hFile, &dwFileSizeHi);

	if( dwFileSizeLo == 0 && dwFileSizeHi == 0 )
	{
		printf("Cannot map a file with a length of zero.\n");
		return FALSE;
	}

	// Create a file mapping object.
	hFileMap = CreateFileMapping(hFile,
		NULL,
		PAGE_READONLY,
		0,
		1,
		NULL);

	if (hFileMap)
	{
		// Create a file mapping to get the file name.
		void* pMem = MapViewOfFile(hFileMap, FILE_MAP_READ, 0, 0, 1);

		if (pMem)
		{

			PIMAGE_DOS_HEADER dosHeader=(PIMAGE_DOS_HEADER )pMem;
			PIMAGE_NT_HEADERS pNTHeader;

			pNTHeader = (PIMAGE_NT_HEADERS) ((DWORD)dosHeader + dosHeader->e_lfanew);

			if ( pNTHeader->Signature == IMAGE_NT_SIGNATURE )
			{

				PIMAGE_OPTIONAL_HEADER OptionalHeader = (PIMAGE_OPTIONAL_HEADER)&pNTHeader->OptionalHeader;
				codeSize = OptionalHeader->SizeOfCode;
				baseOfCode = OptionalHeader->BaseOfCode;
			}


			if (GetMappedFileName (GetCurrentProcess(),
				pMem,
				pszFilename,
				MAX_PATH))
			{

				// Translate path with device name to drive letters.
				TCHAR szTemp[BUFSIZE];
				szTemp[0] = '\0';

				if (GetLogicalDriveStrings(BUFSIZE-1, szTemp))
				{
					TCHAR szName[MAX_PATH];
					TCHAR szDrive[3] = TEXT(" :");
					BOOL bFound = FALSE;
					TCHAR* p = szTemp;

					do
					{
						// Copy the drive letter to the template string
						*szDrive = *p;

						// Look up each device name
						if (QueryDosDevice(szDrive, szName, MAX_PATH))
						{
							UINT uNameLen = _tcslen(szName);

							if (uNameLen < MAX_PATH)
							{
								bFound = _tcsnicmp(pszFilename, szName,
									uNameLen) == 0;

								if (bFound)
								{
									// Reconstruct pszFilename using szTempFile
									// Replace device path with DOS path
									TCHAR szTempFile[MAX_PATH];

									snprintf(szTempFile, sizeof(szTempFile),
										TEXT("%s%s"),
										szDrive,
										pszFilename+uNameLen);
									strncpy(pszFilename, szTempFile, _tcslen(szTempFile));
									pszFilename[_tcslen(szTempFile)] = 0;

								}
							}
						}

						// Go to the next NULL character.
						while (*p++);
					} while (!bFound && *p); // end of string
				}
			}
			bSuccess = TRUE;
			UnmapViewOfFile(pMem);
		}

		CloseHandle(hFileMap);
	}
	return AgentUtils::makeString(pszFilename);
}

WinDebugMonitor::WinDebugMonitor(const LaunchProcessParams& params) :
		DebugMonitor(params)
{
	memset(&processInfo, 0, sizeof(processInfo));

	handledFirstException_ = false;
	waitForDebugEvents = true;
	wfdeWait = 50;
	monitorThread_ = NULL;
	isAttach = false;
}

WinDebugMonitor::WinDebugMonitor(const AttachToProcessParams& params) :
	DebugMonitor(params)
{
	memset(&processInfo, 0, sizeof(processInfo));

	handledFirstException_ = false;
	waitForDebugEvents = true;
	wfdeWait = 50;
	monitorThread_ = NULL;
	this->processID = (DWORD) params.processID;
	isAttach = true;
}

WinDebugMonitor::~WinDebugMonitor(void)
{
}

void WinDebugMonitor::LaunchProcess(const LaunchProcessParams& params) throw (AgentException)
{
	(new WinDebugMonitor(params))->StartMonitor();
}

/*
 * Static method. Entry for attaching.
 */
void WinDebugMonitor::AttachToProcess(const AttachToProcessParams& params) throw (AgentException)
{
	(new WinDebugMonitor(params))->StartMonitor();
}

void WinDebugMonitor::StartDebug() {
	if (! isAttach)
		StartProcessForDebug();
	else
		AttachToProcessForDebug();
}


void WinDebugMonitor::AttachToProcessForDebug()
{
	// Note this is supposed to reply to TCF request ProcessService::Command_Attach().

	if (!DebugActiveProcess(processID))
	{
		DWORD err = GetLastError();

		AgentActionReply::postReply(channel, token, set_win32_errno(err));
	} else {
		// Allow detach without kill.
		DebugSetProcessKillOnExit(false);

		// OK
		AgentActionReply::postReply(channel, token, 0);
	}
}

void WinDebugMonitor::StartProcessForDebug()
{
	STARTUPINFO			si;
	memset(&si, 0, sizeof(si));
	si.dwFlags	       	= STARTF_FORCEONFEEDBACK | STARTF_USESHOWWINDOW;
	si.wShowWindow     	= SW_SHOWNORMAL;

	TCHAR* argsBuffer = new TCHAR[args.size() + sizeof(TCHAR)];

	strcpy(argsBuffer, args.c_str());
	std::string exeName = executable;

	LPTSTR workingDirectory = NULL;
	if (directory.length() > 0)
	{
		workingDirectory = (LPTSTR)directory.c_str();
	}

	char* envBuffer = NULL;
	std::string envString;
	if (environment.size() > 0)
	{
		std::vector<std::string>::iterator itEnvData;
		for (itEnvData = environment.begin(); itEnvData
				!= environment.end(); itEnvData++)
		{
			std::string value = *itEnvData;
			envString += value;
			envString += char(0);
		}
		envString += char(0);
		envBuffer = new char[envString.length()];
		memcpy(envBuffer, envString.c_str(), envString.length());
	}

	if (!CreateProcess(exeName.c_str(), argsBuffer,
		(LPSECURITY_ATTRIBUTES)NULL,
		(LPSECURITY_ATTRIBUTES)NULL,
		FALSE,
		(GetDebugChildren() ? DEBUG_PROCESS : DEBUG_ONLY_THIS_PROCESS)  | CREATE_NEW_CONSOLE,
		envBuffer,
		workingDirectory,				//NULL,
		(LPSTARTUPINFO)&si,
		(LPPROCESS_INFORMATION)&processInfo))
	{
		DWORD err = GetLastError();
		std::string msg = "Failed to start process ";
		msg += '\"';
		msg += AgentUtils::makeUTF8String(exeName);
		msg += "\"";
		err = set_win32_errno(err);

		AgentActionReply::postReply(channel, token, err, 1, new std::string(msg));
	} else {
		// AOK	
		AgentActionReply::postReply(channel, token, 0, 1);
	}

	delete[] envBuffer;
	delete[] argsBuffer;
}

void WinDebugMonitor::CaptureMonitorThread()
{
	DuplicateHandle(GetCurrentProcess(),GetCurrentThread(),
		GetCurrentProcess(),&monitorThread_,
		0,FALSE,DUPLICATE_SAME_ACCESS);
}

DWORD WINAPI debuggerMonitorThread(LPVOID param)
{
	WinDebugMonitor * dpm = (WinDebugMonitor*)param;
	
	try 
	{
		dpm->CaptureMonitorThread();
		dpm->StartDebug();
		dpm->EventLoop();
	}
	catch (const AgentException& e) 
	{
		DWORD error = GetLastError();
		trace(LOG_ALWAYS, "Agent Exception: code=%x: %s", error, e.what());
	}
	
	return 0;
}

void WinDebugMonitor::Suspend()
{
	SuspendThread(monitorThread_);
}

void WinDebugMonitor::Resume()
{
	ResumeThread(monitorThread_);
}

void WinDebugMonitor::StartMonitor()
{
	DWORD threadID = 0;
	HANDLE startThread = CreateThread(
		NULL,                   // default security attributes
		0,                      // use default stack size
		debuggerMonitorThread,  // thread function name
		this,   				// argument to thread function
		0,                      // use default creation flags
		&threadID);   // returns the thread identifier

	CloseHandle(startThread);
}

void WinDebugMonitor::EventLoop()
{
	DEBUG_EVENT debugEvent;

	while (waitForDebugEvents)
	{
		if (WaitForDebugEvent(&debugEvent, wfdeWait))
			HandleDebugEvent(debugEvent);
		else {
			DWORD err = GetLastError();
			if (err == ERROR_SEM_TIMEOUT || err == 0)
				HandleNoDebugEvent();
			else {
				trace(LOG_ALWAYS, "WinDebugMonitor::EventLoop: error %d", err);
				waitForDebugEvents = false;
			}
		}
	}
}

void WinDebugMonitor::Attach(unsigned long pid, ContextAttachCallBack * done, void * data, int selfattach) {
	// TODO: implement
}

static char * win32_debug_event_name(int event) {
	switch (event) {
	case CREATE_PROCESS_DEBUG_EVENT:
		return "CREATE_PROCESS_DEBUG_EVENT";
	case CREATE_THREAD_DEBUG_EVENT:
		return "CREATE_THREAD_DEBUG_EVENT";
	case EXCEPTION_DEBUG_EVENT:
		return "EXCEPTION_DEBUG_EVENT";
	case EXIT_PROCESS_DEBUG_EVENT:
		return "EXIT_PROCESS_DEBUG_EVENT";
	case EXIT_THREAD_DEBUG_EVENT:
		return "EXIT_THREAD_DEBUG_EVENT";
	case LOAD_DLL_DEBUG_EVENT:
		return "LOAD_DLL_DEBUG_EVENT";
	case OUTPUT_DEBUG_STRING_EVENT:
		return "OUTPUT_DEBUG_STRING_EVENT";
	case UNLOAD_DLL_DEBUG_EVENT:
		return "UNLOAD_DLL_DEBUG_EVENT";
	}
	return "Unknown";
}

void WinDebugMonitor::HandleDebugEvent(DEBUG_EVENT& debugEvent)
{
	LogTrace("DebugProcessMonitor::HandleDebugEvent", "event code: %s", win32_debug_event_name(debugEvent.dwDebugEventCode));
	switch (debugEvent.dwDebugEventCode)
	{
	case EXCEPTION_DEBUG_EVENT:
		HandleExceptionEvent(debugEvent);
		break;

	case CREATE_PROCESS_DEBUG_EVENT:
		HandleProcessCreatedEvent(debugEvent);
		break;

	case CREATE_THREAD_DEBUG_EVENT:
		HandleThreadCreatedEvent(debugEvent);
		break;

	case EXIT_PROCESS_DEBUG_EVENT:
		HandleProcessExitedEvent(debugEvent);
		return;

	case EXIT_THREAD_DEBUG_EVENT:
		HandleThreadExitedEvent(debugEvent);
		break;

	case LOAD_DLL_DEBUG_EVENT:
		HandleDLLLoadedEvent(debugEvent);
		break;

	case UNLOAD_DLL_DEBUG_EVENT:
		HandleDLLUnloadedEvent(debugEvent);
		break;

	case OUTPUT_DEBUG_STRING_EVENT:
		HandleDebugStringEvent(debugEvent);
		break;

	case RIP_EVENT:
		HandleSystemDebugErrorEvent(debugEvent);
		break;

	default:
		HandleUnknwonDebugEvent(debugEvent);
		break;
	}
}

void WinDebugMonitor::HandleNoDebugEvent()
{
	while (!actions_.empty())
	{
		AgentAction* action = actions_.front();
		actions_.pop();
		action->Run();
		delete action;
	}
}

std::string WinDebugMonitor::GetDebugExceptionDescription(const EXCEPTION_DEBUG_INFO& exceptionInfo) {
	DWORD code = exceptionInfo.ExceptionRecord.ExceptionCode;

	const char* base = "Exception";
	std::string detail;

	switch (code) {
	case EXCEPTION_SINGLE_STEP:
		base = "Step";
		break;
	case EXCEPTION_BREAKPOINT:
		base = "Breakpoint";
		break;

	case EXCEPTION_ACCESS_VIOLATION:
		base = "Access violation";
		detail = " at 0x" + AgentUtils::IntToHexString(exceptionInfo.ExceptionRecord.ExceptionInformation[1]);
		break;
	case DBG_CONTROL_C:
		base = "Control-C";
		break;
	case DBG_CONTROL_BREAK:
		base = "Control-Break";
		break;
	case STATUS_DATATYPE_MISALIGNMENT:
		base = "Datatype misalignment";
		break;
	case STATUS_IN_PAGE_ERROR:
		base = "Virtual memory paging error";
		break;
	case STATUS_NO_MEMORY:
		base = "Out of memory";
		break;
	case STATUS_ILLEGAL_INSTRUCTION:
		base = "Illegal instruction";
		break;
	case STATUS_NONCONTINUABLE_EXCEPTION:
		base =  "Noncontinuable exception";
		break;
	case STATUS_INVALID_DISPOSITION:
		base =  "Invalid disposition";
		break;
	case STATUS_ARRAY_BOUNDS_EXCEEDED:
		base = "Array bounds exceeded";
		break;
	case STATUS_FLOAT_DENORMAL_OPERAND:
		base = "Floating point denormal operand";
		break;
	case STATUS_FLOAT_DIVIDE_BY_ZERO:
		base =  "Floating point divide by zero";
		break;
	case STATUS_FLOAT_INEXACT_RESULT:
		base =  "Floating point inexact result";
		break;
	case STATUS_FLOAT_INVALID_OPERATION:
		base =  "Floating point invalid operation";
		break;
	case STATUS_FLOAT_STACK_CHECK:
		base = "Floating point stack check";
		break;
	case STATUS_FLOAT_OVERFLOW:
		base = "Floating point overflow";
		break;
	case STATUS_FLOAT_UNDERFLOW:
		base = "Floating point underflow";
		break;
	case STATUS_INTEGER_DIVIDE_BY_ZERO:
		base = "Integer divide by zero";
		break;
	case STATUS_INTEGER_OVERFLOW:
		base = "Integer overflow";
		break;
	case STATUS_PRIVILEGED_INSTRUCTION:
		base = "Privileged instruction";
		break;
	case STATUS_STACK_OVERFLOW:
		base = "Stack overflow";
		break;
	case EXCEPTION_DLL_NOT_FOUND:
		base = "DLL not found";
		// TODO: find out how to determine which DLL it was...
		break;
	case EXCEPTION_DLL_INIT_FAIL:
		base = "DLL initialization failed";
		break;
	case EXCEPTION_ENTRY_NOT_FOUND:
		base = "Entry point not found";
		break;
	case EXCEPTION_MS_CPLUS:
		base = "C++ exception";
		break;

	case RPC_S_UNKNOWN_IF:
		base = "RPC unknown interface";
		break;
	case RPC_S_SERVER_UNAVAILABLE:
		base = "RPC server unavailable";
		break;
	case EXCEPTION_VDM_EVENT:
		base = "VDM event";
		break;
	}

	if (detail.size() > 0) {
		return std::string(base) + detail;
	}
	return base;
}

void WinDebugMonitor::HandleExceptionEvent(DEBUG_EVENT& debugEvent)
{
	LogTrace("DebugProcessMonitor::HandleExceptionEvent", "event code: %s",
			GetDebugExceptionDescription(debugEvent.u.Exception).c_str());

	HandleException(debugEvent);

}

void WinDebugMonitor::HandleProcessCreatedEvent(DEBUG_EVENT& debugEvent)
{
	WinProcess* process = new WinProcess(this, debugEvent);
	WinThread* thread = new WinThread(*process, debugEvent);

	process->SetDebugging(true);
	thread->SetDebugging(true);

	// record in our cache
	ContextManager::addContext(process);
	ContextManager::addContext(thread);

	// Notify host
	EventClientNotifier::SendContextAdded(process);
	EventClientNotifier::SendContextAdded(thread);

	unsigned long codeSize = 0;
	unsigned long baseOfCode = 0;
	std::string imageName = GetExecutableInfo(debugEvent.u.CreateProcessInfo.hFile, baseOfCode, codeSize);
	thread->HandleExecutableEvent(true, imageName, (unsigned long)debugEvent.u.CreateProcessInfo.lpBaseOfImage, codeSize + baseOfCode);
	CloseHandle(debugEvent.u.CreateProcessInfo.hFile);
}

void WinDebugMonitor::HandleThreadCreatedEvent(DEBUG_EVENT& debugEvent)
{
	WinProcess* process = WinProcess::GetProcessByID(debugEvent.dwProcessId);
	if (process) {
		WinThread* thread = new WinThread(*process, debugEvent);
		thread->SetDebugging(true);
		ContextManager::addContext(thread);
		EventClientNotifier::SendContextAdded(thread);
	} else {
		assert(false);
	}

	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
}

void WinDebugMonitor::HandleProcessExitedEvent(DEBUG_EVENT& debugEvent)
{
	WinProcess* process = WinProcess::GetProcessByID(debugEvent.dwProcessId);
	if (process) {
		ContextManager::removeContext(process->GetID());
		EventClientNotifier::SendContextRemoved(process, true);
	} else {
		assert(false);
	}
	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
}

void WinDebugMonitor::HandleThreadExitedEvent(DEBUG_EVENT& debugEvent)
{
	WinThread* thread = WinThread::GetThreadByID(debugEvent.dwProcessId, debugEvent.dwThreadId);
	if (thread) {
		ContextManager::removeContext(thread->GetID());
		EventClientNotifier::SendContextRemoved(thread, true);
	} else {
		assert(false);
	}
	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
}

bool WinDebugMonitor::ShouldDebugFirstChance(const DEBUG_EVENT& debugEvent) {
	if (debugEvent.dwDebugEventCode == EXCEPTION_DEBUG_EVENT) {
		const EXCEPTION_DEBUG_INFO& info = debugEvent.u.Exception;
		if (!info.dwFirstChance)
			return false;
		return info.ExceptionRecord.ExceptionCode == EXCEPTION_DLL_NOT_FOUND;
	}
	return false;
}

void WinDebugMonitor::HandleException(DEBUG_EVENT& debugEvent)
{
	WinThread* thread = WinThread::GetThreadByID(debugEvent.dwProcessId, debugEvent.dwThreadId);
	if (!thread)
		assert(false);
	if (thread && (handledFirstException_ || isAttach || ShouldDebugFirstChance(debugEvent)))
		thread->HandleException(debugEvent);
	else
		ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
	handledFirstException_ = true;
}

void WinDebugMonitor::HandleDLLLoadedEvent(DEBUG_EVENT& debugEvent)
{
	unsigned long codeSize = 0;
	unsigned long baseOfCode = 0;
	std::string moduleName = GetExecutableInfo(debugEvent.u.LoadDll.hFile, baseOfCode, codeSize);

	LogTrace("DebugProcessMonitor::HandleDLLLoadedEvent", "Base address: %8.8x %s", debugEvent.u.LoadDll.lpBaseOfDll, moduleName.c_str());
	WinThread* thread = WinThread::GetThreadByID(debugEvent.dwProcessId, debugEvent.dwThreadId);
	if (thread) {
		thread->HandleExecutableEvent(true, moduleName, (unsigned long)debugEvent.u.LoadDll.lpBaseOfDll, codeSize);
	}
}

void WinDebugMonitor::HandleDLLUnloadedEvent(DEBUG_EVENT& debugEvent)
{
	WinThread* thread = WinThread::GetThreadByID(debugEvent.dwProcessId, debugEvent.dwThreadId);
	if (thread) {
		thread->HandleExecutableEvent(false, "", (unsigned long)debugEvent.u.CreateProcessInfo.lpBaseOfImage, 0);
	}
	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
}

void WinDebugMonitor::HandleDebugStringEvent(DEBUG_EVENT& debugEvent)
{
	WinProcess* process = WinProcess::GetProcessByID(debugEvent.dwProcessId);
	char debugStringBuffer[2048];
	ReadProcessMemory(process->GetProcessHandle(), debugEvent.u.DebugString.lpDebugStringData, debugStringBuffer,
		sizeof(debugStringBuffer),NULL);

	// write console data, if console
	LoggingService::WriteLoggingMessage(channel, debugStringBuffer, LoggingService::GetWindowsConsoleID());

	LogTrace("DebugProcessMonitor::HandleDebugStringEvent", "%s", debugStringBuffer);

	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
}

void WinDebugMonitor::HandleSystemDebugErrorEvent(DEBUG_EVENT& debugEvent)
{
	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
}

void WinDebugMonitor::HandleUnknwonDebugEvent(DEBUG_EVENT& debugEvent)
{
	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
}

void WinDebugMonitor::SetProcess(WinProcess* process)
{
	process_ = process;
}

void WinDebugMonitor::PostAction(AgentAction* action)
{
	actions_.push(action);
}


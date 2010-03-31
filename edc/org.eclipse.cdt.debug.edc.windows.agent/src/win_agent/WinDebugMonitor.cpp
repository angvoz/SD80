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
#include "TCFOutputStream.h"
#include "WinDebugMonitor.h"
#include "Logger.h"
#include "WinProcess.h"
#include "WinThread.h"
#include "EventClientNotifier.h"
#include "AgentUtils.h"
#include "psapi.h"
#include "AgentAction.h"
#include "TCFChannel.h"
#include "ContextManager.h"
#include "LoggingService.h"

// These aren't defined in any Windows system headers
#define	EXCEPTION_DLL_NOT_FOUND				((unsigned long) 0xC0000135L)
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

									sprintf(szTempFile,
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

WinDebugMonitor::WinDebugMonitor(std::string& executable, std::string& directory, std::string& args, std::vector<std::string>& environment, bool debug_children, std::string& token, Channel *c) :
DebugMonitor(executable, directory, args, environment, debug_children, token, c)
{
	memset(&processInfo, 0, sizeof(processInfo));

	handledFirstException_ = false;
	waitForDebugEvents = true;
	wfdeWait = 50;
	monitorThread_ = NULL;
	isAttach = false;
}

WinDebugMonitor::WinDebugMonitor(DWORD processID, bool debug_children, std::string& token, Channel *c) :
	DebugMonitor(debug_children, token, c)
{
	memset(&processInfo, 0, sizeof(processInfo));

	handledFirstException_ = false;
	waitForDebugEvents = true;
	wfdeWait = 50;
	monitorThread_ = NULL;
	this->processID = processID;
	isAttach = true;
}

WinDebugMonitor::~WinDebugMonitor(void)
{
}

void WinDebugMonitor::LaunchProcess(std::string& executable, std::string& directory, std::string& args, std::vector<std::string>& environment, bool debug_children, std::string& token, Channel *c) throw (AgentException)
{
	(new WinDebugMonitor(executable, directory, args, environment, debug_children, token, c))->StartMonitor();
}

/*
 * Static method. Entry for attaching.
 */
void WinDebugMonitor::AttachToProcess(DWORD processID, bool debug_children, std::string& token, Channel *c) throw (AgentException)
{
	(new WinDebugMonitor(processID, debug_children, token, c))->StartMonitor();
}

void WinDebugMonitor::StartDebug() {
	if (! isAttach)
		StartProcessForDebug();
	else
		AttachToProcessForDebug();
}

void WinDebugMonitor::AttachToProcessForDebug()
{
	TCFChannel tcf(channel);

	// Note this is supposed to reply to TCF request ProcessService::Command_Attach().

	if (!DebugActiveProcess(processID))
	{
		DWORD err = GetLastError();
		/*
		std::string msg = "Failed to attach to process ";
		msg += '\"';
		msg += AgentUtils::IntToString(processID);
		msg += "\". Win32 error code: ";
		msg += AgentUtils::IntToString(err);

		// Don't throw exception, just report the error to host.
		// throw AgentException(msg);
		set_exception_errno(err, (char*)msg.c_str());
		tcf.writeError(ERR_EXCEPTION);
		*/
		tcf.writeError(set_win32_errno(err));

		tcf.writeComplete();
	} else {
		// OK
		tcf.writeError(0);
		tcf.writeComplete();
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
		msg += "\". Win32 error code: ";
		msg += AgentUtils::IntToString(err);

		throw AgentException(msg);
	} else {
		// AOK	
		TCFChannel tcf(channel);
		write_stringz(&channel->out, "R");
		write_stringz(&channel->out, token.c_str());
		tcf.writeError(0);
		write_stringz(&channel->out, "null");
		tcf.writeComplete();
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
	catch (AgentException e) 
	{
		std::cout << e.what() << std::endl;
		dpm->WriteError(GetLastError(), e.what());
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

void WinDebugMonitor::WriteError(unsigned long errNum, const char* message) {
	TCFOutputStream tcf(&channel->out);
	
	write_stringz(&channel->out, "R");
	write_stringz(&channel->out, token.c_str());
	tcf.writeError(errNum);
	write_stringz(&channel->out, "null");
	tcf.writeComplete();
}

void WinDebugMonitor::EventLoop()
{
	DEBUG_EVENT debugEvent;

	while (waitForDebugEvents)
	{
		if (WaitForDebugEvent(&debugEvent, wfdeWait))
			HandleDebugEvent(debugEvent);
		else
			HandleNoDebugEvent();
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

static char * win32_debug_exception_name(int event) {
	switch (event) {
	case EXCEPTION_BREAKPOINT:
		return "EXCEPTION_BREAKPOINT";
		break;

	case EXCEPTION_SINGLE_STEP:
		return "EXCEPTION_SINGLE_STEP";
		break;

	case EXCEPTION_ACCESS_VIOLATION:
		return "EXCEPTION_ACCESS_VIOLATION";
		break;
	case DBG_CONTROL_C:
		return "DBG_CONTROL_C";
		break;
	case DBG_CONTROL_BREAK:
		return "DBG_CONTROL_BREAK";
		break;
	case STATUS_DATATYPE_MISALIGNMENT:
		return "STATUS_DATATYPE_MISALIGNMENT";
		break;
	case STATUS_IN_PAGE_ERROR:
		return "STATUS_IN_PAGE_ERROR";
		break;
	case STATUS_NO_MEMORY:
		return "STATUS_NO_MEMORY";
		break;
	case STATUS_ILLEGAL_INSTRUCTION:
		return "STATUS_ILLEGAL_INSTRUCTION";
		break;
	case STATUS_NONCONTINUABLE_EXCEPTION:
		return "STATUS_NONCONTINUABLE_EXCEPTION";
		break;
	case STATUS_INVALID_DISPOSITION:
		return "STATUS_INVALID_DISPOSITION";
		break;
	case STATUS_ARRAY_BOUNDS_EXCEEDED:
		return "STATUS_ARRAY_BOUNDS_EXCEEDED";
		break;
	case STATUS_FLOAT_DENORMAL_OPERAND:
		return "STATUS_FLOAT_DENORMAL_OPERAND";
		break;
	case STATUS_FLOAT_DIVIDE_BY_ZERO:
		return "STATUS_FLOAT_DIVIDE_BY_ZERO";
		break;
	case STATUS_FLOAT_INEXACT_RESULT:
		return "STATUS_FLOAT_INEXACT_RESULT";
		break;
	case STATUS_FLOAT_INVALID_OPERATION:
		return "STATUS_FLOAT_INVALID_OPERATION";
		break;
	case STATUS_FLOAT_STACK_CHECK:
		return "STATUS_FLOAT_STACK_CHECK";
		break;
	case STATUS_FLOAT_OVERFLOW:
		return "STATUS_FLOAT_OVERFLOW";
		break;
	case STATUS_FLOAT_UNDERFLOW:
		return "STATUS_FLOAT_UNDERFLOW";
		break;
	case STATUS_INTEGER_DIVIDE_BY_ZERO:
		return "STATUS_INTEGER_DIVIDE_BY_ZERO";
		break;
	case STATUS_INTEGER_OVERFLOW:
		return "STATUS_INTEGER_OVERFLOW";
		break;
	case STATUS_PRIVILEGED_INSTRUCTION:
		return "STATUS_PRIVILEGED_INSTRUCTION";
		break;
	case STATUS_STACK_OVERFLOW:
		return "STATUS_STACK_OVERFLOW";
		break;
	case EXCEPTION_DLL_NOT_FOUND:
		return "EXCEPTION_DLL_NOT_FOUND";
		break;
	case EXCEPTION_DLL_INIT_FAIL:
		return "EXCEPTION_DLL_INIT_FAIL";
		break;
	case EXCEPTION_MS_CPLUS:
		return "EXCEPTION_MS_CPLUS";
		break;

	case DBG_TERMINATE_PROCESS:
		return "DBG_TERMINATE_PROCESS";
		break;
	case RPC_S_UNKNOWN_IF:
		return "RPC_S_UNKNOWN_IF";
		break;
	case RPC_S_SERVER_UNAVAILABLE:
		return "RPC_S_SERVER_UNAVAILABLE";
		break;
	case EXCEPTION_VDM_EVENT:
		return "EXCEPTION_VDM_EVENT";
		break;
	}
	return "Unknown";
}

void WinDebugMonitor::HandleExceptionEvent(DEBUG_EVENT& debugEvent)
{
	LogTrace("DebugProcessMonitor::HandleExceptionEvent", "event code: %s", win32_debug_exception_name(debugEvent.u.Exception.ExceptionRecord.ExceptionCode));

	HandleException(debugEvent);

}

void WinDebugMonitor::HandleProcessCreatedEvent(DEBUG_EVENT& debugEvent)
{
	WinProcess* process = new WinProcess(this, debugEvent);
	WinThread* thread = new WinThread(*process, debugEvent);

	// record in our cache
	ContextManager::addDebuggedContext(process);
	ContextManager::addDebuggedContext(thread);

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
	WinThread* thread = new WinThread(*process, debugEvent);
	ContextManager::addDebuggedContext(thread);

	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);
	EventClientNotifier::SendContextAdded(thread);
}

void WinDebugMonitor::HandleProcessExitedEvent(DEBUG_EVENT& debugEvent)
{
	WinProcess* process = WinProcess::GetProcessByID(debugEvent.dwProcessId);
	EventClientNotifier::SendContextRemoved(process);
	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);

	delete process;
}

void WinDebugMonitor::HandleThreadExitedEvent(DEBUG_EVENT& debugEvent)
{
	WinThread* thread = WinThread::GetThreadByID(debugEvent.dwProcessId, debugEvent.dwThreadId);
	EventClientNotifier::SendContextRemoved(thread);
	ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, DBG_CONTINUE);

	delete thread;
}

void WinDebugMonitor::HandleException(DEBUG_EVENT& debugEvent)
{
	WinThread* thread = WinThread::GetThreadByID(debugEvent.dwProcessId, debugEvent.dwThreadId);
	if (handledFirstException_ || isAttach)
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
	thread->HandleExecutableEvent(true, moduleName, (unsigned long)debugEvent.u.LoadDll.lpBaseOfDll, codeSize);
}

void WinDebugMonitor::HandleDLLUnloadedEvent(DEBUG_EVENT& debugEvent)
{
	WinThread* thread = WinThread::GetThreadByID(debugEvent.dwProcessId, debugEvent.dwThreadId);
	thread->HandleExecutableEvent(false, "", (unsigned long)debugEvent.u.CreateProcessInfo.lpBaseOfImage, 0);
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


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

#include <string>
#include <vector>

#include "LoggingService.h"
#include "DebugMonitor.h"
#include "ContextManager.h"
#include "TCFChannel.h"

static const char * sServiceName = "Logging";

// ID of the console to write debug process output to
static const char * sWindowsConsoleID = "org.eclipse.cdt.debug.edc.ui.ProgramOutputConsoleLogger";
// Number of listeners to the service utilizing 'sWindowsConsoleID'.
int LoggingService::numConsoleListeners = 0;

/**
* LoggingService: Communicates with logging hosts and allows the agent to send back messages
* to be printed to a console or otherwise redirected.
*/
LoggingService::LoggingService(Protocol * proto) :
	TCFService(proto) {
	AddCommand("addListener", command_addListener);
	AddCommand("removeListener", command_removeListener);
}

LoggingService::~LoggingService(void) {
}

const char* LoggingService::GetName() {
	return sServiceName;
}

const char * LoggingService::GetWindowsConsoleID() {
	return sWindowsConsoleID;
}

void LoggingService::command_addListener(char * token, Channel * c) {

	TCFChannel tcf(c);
	std::string id = tcf.readString();
	tcf.readZero();
	tcf.readComplete();
	
	if ( id.compare(sWindowsConsoleID) == 0 )
		numConsoleListeners++;
	
	sendOK(token, c);
}


void LoggingService::command_removeListener(char * token, Channel * c) {
	TCFChannel tcf(c);
	std::string id = tcf.readString();
	tcf.readZero();

	if ( id.compare(sWindowsConsoleID) == 0 )
		numConsoleListeners--;

	sendOK(token, c);
}

void LoggingService::sendOK(char * token, Channel * c)
{
	// Send OK message
	TCFChannel tcf(c);
	tcf.writeReplyHeader(token);
	tcf.writeError(0);
	tcf.writeComplete();
}

// Currently on sends "write" event. Another method could be send to add a newline with the "writeln" event.
void LoggingService::WriteLoggingMessage(Channel *channel, std::string str, const char *consoleID)
{
	const char *windowsConsoleID = GetWindowsConsoleID();
	// only send messages to the proper console service and when there are more than one listener
	if ( numConsoleListeners > 0 && strcmp(windowsConsoleID, consoleID) == 0 )
	{
		TCFChannel tcf(channel);

		// write to the console
		tcf.writeStringZ("E");
		tcf.writeStringZ(sServiceName);
		tcf.writeStringZ("write");

		/* <array of context data> */
		tcf.writeString(consoleID);
		tcf.writeZero();
		tcf.writeString(str);
		tcf.writeZero();
		tcf.writeComplete();
	}
}
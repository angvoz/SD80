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
static const char * sWindowsConsoleID = "ProgramOutputConsoleLogger";
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

void LoggingService::command_addListener(const char * token, Channel * c) {

	TCFChannel tcf(c);
	std::string id = tcf.readString();
	tcf.readZero();
	tcf.readComplete();
	
	if ( id.compare(sWindowsConsoleID) == 0 )
		numConsoleListeners++;
	
	sendOK(token, c);
}


void LoggingService::command_removeListener(const char * token, Channel * c) {
	TCFChannel tcf(c);
	std::string id = tcf.readString();
	tcf.readZero();

	if ( id.compare(sWindowsConsoleID) == 0 )
		numConsoleListeners--;

	sendOK(token, c);
}

void LoggingService::sendOK(const char * token, Channel * c)
{
	// Send OK message
	TCFChannel tcf(c);
	tcf.writeCompleteReply(token, 0);
}


struct LoggingMessage {
	Channel* channel;
	std::string str;
	const char* consoleID;
	
	LoggingMessage(Channel* channel_, const std::string& str_, const char* consoleID_) 
		: channel(channel_), str(str_), consoleID(consoleID_) 
	{ }
};

struct DialogMessage {
	Channel* channel;
	IStatus::Severity severity;
	std::string summary;
	std::string details;

	DialogMessage(Channel* channel_, const IStatus::Severity severity,
					const std::string& summary_, const std::string& details_)
	  : channel(channel_), severity(severity), summary(summary_), details(details_)
	{ }
};

static void emit_logging_message(void *data) {
	LoggingMessage* m = (LoggingMessage*) data;
	
	TCFChannel tcf(m->channel);

	// write to the console
	tcf.writeStringZ("E");
	tcf.writeStringZ(sServiceName);
	tcf.writeStringZ("write");

	/* <array of context data> */
	tcf.writeString(m->consoleID);
	tcf.writeZero();
	tcf.writeString(m->str);
	tcf.writeZero();
	tcf.writeComplete();
	
	delete m;
}


static void emit_logging_message_with_newline(void *data) {
	LoggingMessage* m = (LoggingMessage*) data;

	TCFChannel tcf(m->channel);

	// write to the console
	tcf.writeStringZ("E");
	tcf.writeStringZ(sServiceName);
	tcf.writeStringZ("writeln");

	/* <array of context data> */
	tcf.writeString(m->consoleID);
	tcf.writeZero();
	tcf.writeString(m->str);
	tcf.writeZero();
	tcf.writeComplete();

	delete m;
}


static void emit_dialog_message(void *data) {
	DialogMessage* m = (DialogMessage*) data;

	TCFChannel tcf(m->channel);

	// write to the console
	tcf.writeStringZ("E");
	tcf.writeStringZ(sServiceName);
	tcf.writeStringZ("dialog");

	/* <array of context data> */
	tcf.writeString(LoggingService::GetWindowsConsoleID());
	tcf.writeZero();
	tcf.writeLong((long)m->severity);
	tcf.writeZero();
	tcf.writeString(m->summary);
	tcf.writeZero();
	tcf.writeString(m->details);
	tcf.writeZero();
	tcf.writeComplete();

	delete m;
}


void LoggingService::WriteLoggingMessage(Channel *channel, const std::string& str, const char *consoleID)
{
	const char *windowsConsoleID = GetWindowsConsoleID();
	// only send messages to the proper console service and when there are more than one listener
	if ( numConsoleListeners > 0 && strcmp(windowsConsoleID, consoleID) == 0 )
	{
		LoggingMessage* message = new LoggingMessage(channel, str, consoleID);
		post_event(emit_logging_message, message);
	}
}

/** since 2.0 */
void LoggingService::WriteLnLoggingMessage(Channel *channel, const std::string& str, const char *consoleID)
{
	const char *windowsConsoleID = GetWindowsConsoleID();
	// only send messages to the proper console service and when there are more than one listener
	if ( numConsoleListeners > 0 && strcmp(windowsConsoleID, consoleID) == 0 )
	{
		LoggingMessage* message = new LoggingMessage(channel, str, consoleID);
		post_event(emit_logging_message_with_newline, message);
	}
}

/** since 2.0 */
void LoggingService::Dialog(Channel *channel, IStatus::Severity severity, const std::string& summary, const std::string& details)
{
	DialogMessage* message = new DialogMessage(channel, severity, summary, details);
	post_event(emit_dialog_message, message);
}

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

#include "Logger.h"
#include "TCFHeaders.h"

Logger* Logger::defaultLogger = NULL;

Logger::Logger(const char* name) {
	loggerName = name;
	// main agent driver should call this if it wants logging
	// open_log_file("C:\\EDC_Win_Agent_Log.txt");
}

Logger::~Logger(void) {
}

Logger& Logger::getLogger() {
	if (defaultLogger == NULL)
		defaultLogger = new Logger("default");
	;
	return *defaultLogger;
}

Logger& Logger::getLogger(const char* name) {
	if (defaultLogger == NULL)
		defaultLogger = new Logger("default");
	;
	return *defaultLogger;
}

void Logger::Log(int level, const char* msg) {
	Log(level, "%s", msg);
}

void Logger::Log(int level, char * fmt, ...) {
	char tmpbuf[1000];
	va_list ap;
	va_start(ap, fmt);
	vsnprintf(tmpbuf, sizeof(tmpbuf), fmt, ap);
	va_end(ap);
	trace(LOG_ALWAYS, "%s", tmpbuf);
}

LogTrace::LogTrace(const char* traceName) {
	this->traceName = traceName;
	std::string tracemsg(traceName);
	tracemsg += "\tentry\t";
	Logger::getLogger().Log(Logger::LOG_NORMAL, tracemsg.c_str());
}

LogTrace::LogTrace(const char* traceName, char * fmt, ...) {
	char tmpbuf[1000];
	this->traceName = traceName;
	std::string format(traceName);
	format += "\tentry\t";
	format += fmt;
	va_list ap;
	va_start(ap, fmt);
	vsnprintf(tmpbuf, sizeof(tmpbuf), (char*) format.c_str(), ap);
	va_end(ap);
	Logger::getLogger().Log(Logger::LOG_NORMAL, tmpbuf, ap);
}

LogTrace::~LogTrace() {
	this->traceName = traceName;
	std::string tracemsg(traceName);
	tracemsg += "\texit";
	Logger::getLogger().Log(Logger::LOG_NORMAL, tracemsg.c_str());
}

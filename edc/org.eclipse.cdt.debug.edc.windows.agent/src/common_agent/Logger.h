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
#pragma once

#include <string>

class LogTrace {
public:

	LogTrace(const char* traceName);
	LogTrace(const char* traceName, const char * fmt, ...);
	~LogTrace();

private:
	std::string traceName;

};

class Logger {
public:

	static const int LOG_NORMAL = 50;

	Logger(const char* name);
	~Logger(void);

	static Logger& getLogger();

	static Logger& getLogger(const char* name);

	void Log(int level, const std::string& msg);

	void Log(int level, const char * fmt, ...);

private:

	std::string loggerName;

	static Logger* defaultLogger;

};

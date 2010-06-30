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
#ifndef LOGGINGSERVICE_H_
#define LOGGINGSERVICE_H_


#include "TCFService.h"
#include "TCFHeaders.h"

class LoggingService: public TCFService {
public:
    LoggingService(Protocol * proto);
	~LoggingService(void);

	const char* GetName();

	static void command_addListener(char * token, Channel * c);
	static void command_removeListener(char * token, Channel * c);

	static void sendOK(char * token, Channel * c);
	static void WriteLoggingMessage(Channel *channel, std::string str, const char *consoleID);
	static const char * GetWindowsConsoleID();

private:
	static int numConsoleListeners;

};


#endif /* #define LOGGINGSERVICE_H_ */

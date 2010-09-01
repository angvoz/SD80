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

namespace IStatus { 	/** from IStatus.java */
	enum Severity {

// IStatus.OK not currently accepted by ILogging.Dialog()
//		/** Status severity constant (value 0) indicating this status represents the nominal case.
//		 * This constant is also used as the status code representing the nominal case.
//		 */
//		OK = 0,

		/** Status type severity (bit mask, value 1) indicating this status is informational only.
		 */
		INFO = 0x01,

		/** Status type severity (bit mask, value 2) indicating this status represents a warning.
		 */
		WARNING = 0x02,

		/** Status type severity (bit mask, value 4) indicating this status represents an error.
		 */
		_ERROR = 0x04, /// ERROR without _ disallowed due to macro conflict

// IStatus.CANCEL not currently accepted by ILogging.Dialog()
//		/** Status type severity (bit mask, value 8) indicating this status represents a
//		 * cancelation
//		 * @since 3.0
//		 */
//		CANCEL = 0x08,
	};
};

class LoggingService: public TCFService {
public:

	LoggingService(Protocol * proto);
	~LoggingService(void);

	const char* GetName();

	static void command_addListener(const char * token, Channel * c);
	static void command_removeListener(const char * token, Channel * c);

	static void sendOK(const char * token, Channel * c);
	static void WriteLoggingMessage(Channel *channel, const std::string& str, const char *consoleID);

	/** since 2.0 */
	static void WriteLnLoggingMessage(Channel *channel, const std::string& str, const char *consoleID);
	/** since 2.0 */
	static void Dialog(Channel *channel, IStatus::Severity severity, const std::string& summary, const std::string& details);

	static const char * GetWindowsConsoleID();

private:
	static int numConsoleListeners;

};


#endif /* #define LOGGINGSERVICE_H_ */

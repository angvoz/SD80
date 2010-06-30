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

#ifndef AGENTEXCEPTION_H_
#define AGENTEXCEPTION_H_

#include <string>

/*
 * Exception that is used in the agent code.
 * This class allows cascading of error messages.
 */
class AgentException {
public:
	AgentException();
	AgentException(const std::string& msg);
	AgentException(const char* msg);
	AgentException(const char* msg, AgentException* cause);

	virtual ~AgentException() throw ();

	const char* what() const throw ();

private:
	std::string message;
	AgentException* cause;
};

#endif /* AGENTEXCEPTION_H_ */

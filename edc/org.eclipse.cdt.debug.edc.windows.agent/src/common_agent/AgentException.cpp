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
#include "AgentException.h"

AgentException::AgentException() {
}

AgentException::AgentException(const std::string& msg) {
	message = msg;
	cause = NULL;
}

AgentException::AgentException(const char* msg) {
	message = msg;
	cause = NULL;
}

AgentException::AgentException(const char* msg, AgentException* cause) {
	message = msg;
	this->cause = cause;
}

AgentException::~AgentException() throw () {
	if (cause != NULL)
		delete cause;
}

const char* AgentException::what() const throw () {
	if (cause != NULL) {
		std::string m = message;
		m += "\n\t";
		m += cause->what();
		return m.c_str();
	}

	return message.c_str();
}

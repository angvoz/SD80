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

#include "SimpleRegistersService.h"

#include "DebugMonitor.h"
#include "ContextManager.h"
#include "TCFChannel.h"
#include "WinThread.h"

static const char * sServiceName = "SimpleRegisters";

SimpleRegistersService::SimpleRegistersService(Protocol * proto) :
	TCFService(proto) {
	AddCommand("get", command_get);
	AddCommand("set", command_set);
}

SimpleRegistersService::~SimpleRegistersService(void) {
}

const char* SimpleRegistersService::GetName() {
	return sServiceName;
}

/*
 * register values are passed as hex-string in big-endian
 */
void SimpleRegistersService::command_get(char * token, Channel * c) {
	TCFChannel channel(c);
	std::vector<std::string> registerIDs;

	std::string exeContextID = channel.readString();
	channel.readZero();

	int ch = read_stream(&c->inp);
	if (ch == 'n') {
		if (read_stream(&c->inp) != 'u')
			exception(ERR_JSON_SYNTAX);
		if (read_stream(&c->inp) != 'l')
			exception(ERR_JSON_SYNTAX);
		if (read_stream(&c->inp) != 'l')
			exception(ERR_JSON_SYNTAX);
	} else {
		if (ch != '[')
			exception(ERR_PROTOCOL);
		if (peek_stream(&c->inp) == ']') {
			read_stream(&c->inp);
		} else {
			while (1) {
				int ch;
				std::string id = channel.readString();

				registerIDs.push_back(id);

				ch = read_stream(&c->inp);
				if (ch == ',')
					continue;
				if (ch == ']')
					break;
				exception(ERR_JSON_SYNTAX);
			}
		}
	}
	channel.readZero();
	channel.readComplete();

	WinThread* context = dynamic_cast<WinThread *>(ContextManager::findContext(exeContextID));

	if (context == NULL || !context->IsDebugging()) {
		// Return invalid-context-ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 1);
		return;
	}
	
	std::vector<std::string> registerValues = context->GetRegisterValues(
			registerIDs);

	if (registerValues.size() == 0) { 
		// no values got. Assuming target is running.
		// TODO: it's better the above context->GetRegisterValues() API return error code.
		channel.writeCompleteReply(token, ERR_IS_RUNNING, 1);
		return;
	}
	
	channel.writeError(0);
	channel.writeCharacter('[');

	std::vector<std::string>::iterator itVectorData;
	for (itVectorData = registerValues.begin(); itVectorData
			!= registerValues.end(); itVectorData++) 
	{
		if (itVectorData != registerValues.begin())
			write_stream(&c->out, ',');
		std::string value = *itVectorData;
		channel.writeString(value);
	}

	channel.writeCharacter(']');
	channel.writeZero();

	channel.writeComplete();
}

/*
 * register values are passed as hex-string in big-endian
 */
void SimpleRegistersService::command_set(char * token, Channel * c) {
	TCFChannel channel(c);
	std::vector<std::string> registerIDs;
	std::vector<std::string> registerValues;

	std::string exeContextID = channel.readString();
	channel.readZero();

	int ch = read_stream(&c->inp);
	if (ch == 'n') {
		if (read_stream(&c->inp) != 'u')
			exception(ERR_JSON_SYNTAX);
		if (read_stream(&c->inp) != 'l')
			exception(ERR_JSON_SYNTAX);
		if (read_stream(&c->inp) != 'l')
			exception(ERR_JSON_SYNTAX);
	} else {
		// read register IDs
		if (ch != '[')
			exception(ERR_PROTOCOL);
		if (peek_stream(&c->inp) == ']') {
			read_stream(&c->inp);
		} else {
			while (1) {
				int ch;
				std::string id = channel.readString();

				registerIDs.push_back(id);

				ch = read_stream(&c->inp);
				if (ch == ',')
					continue;
				if (ch == ']')
					break;
				exception(ERR_JSON_SYNTAX);
			}
		}
		channel.readZero();

		// read register values
		ch = read_stream(&c->inp);
		if (ch != '[')
			exception(ERR_PROTOCOL);
		if (peek_stream(&c->inp) == ']') {
			read_stream(&c->inp);
		} else {
			while (1) {
				int ch;
				std::string value = channel.readString();

				registerValues.push_back(value);

				ch = read_stream(&c->inp);
				if (ch == ',')
					continue;
				if (ch == ']')
					break;
				exception(ERR_JSON_SYNTAX);
			}
		}
	}
	channel.readZero();
	channel.readComplete();

	WinThread* context = dynamic_cast<WinThread *>(ContextManager::findContext(exeContextID));

	if (context == NULL || !context->IsDebugging()) {
		// Return invalid-context-ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 1);
		return;
	}

	context->SetRegisterValues(registerIDs, registerValues);

	channel.writeReplyHeader(token);
	channel.writeZero();
	channel.writeComplete();
}

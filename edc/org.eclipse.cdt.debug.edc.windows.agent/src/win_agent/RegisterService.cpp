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

#include "RegisterService.h"

#include "DebugMonitor.h"
#include "ContextManager.h"
#include "TCFChannel.h"

static const char * sServiceName = "SimpleRegisters";

RegisterService::RegisterService(Protocol * proto) :
	TCFService(proto) {
	AddCommand("get", command_get);
	AddCommand("set", command_set);
}

RegisterService::~RegisterService(void) {
}

const char* RegisterService::GetName() {
	return sServiceName;
}

/*
 * register values are passed as hex-string in big-endian
 */
void RegisterService::command_get(char * token, Channel * c) {
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

	write_stringz(&c->out, "R");
	write_stringz(&c->out, token);
	write_errno(&c->out, 0);

	write_stream(&c->out, '[');

	Context* context = ContextManager::FindDebuggedContext(exeContextID);

	if (context != NULL) {
		std::vector<std::string> registerValues = context->GetRegisterValues(
				registerIDs);

		std::vector<std::string>::iterator itVectorData;
		for (itVectorData = registerValues.begin(); itVectorData
				!= registerValues.end(); itVectorData++) {
			if (itVectorData != registerValues.begin())
				write_stream(&c->out, ',');
			std::string contextID = *itVectorData;
			json_write_string(&c->out, contextID.c_str());
		}
	}

	write_stream(&c->out, ']');
	write_stream(&c->out, 0);

	write_stream(&c->out, MARKER_EOM);

}

/*
 * register values are passed as hex-string in big-endian
 */
void RegisterService::command_set(char * token, Channel * c) {
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

	Context* context = ContextManager::FindDebuggedContext(exeContextID);

	if (context != NULL) {
		context->SetRegisterValues(registerIDs, registerValues);
	}

	channel.writeReplyHeader(token);
	channel.writeZero();
	channel.writeComplete();
}

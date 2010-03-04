/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Nokia - Initial API and implementation
 *******************************************************************************/

#include "RegistersService.h"

#include "DebugMonitor.h"
#include "ContextManager.h"
#include "EventClientNotifier.h"
#include "Logger.h"
#include "TCFChannel.h"
#include "WinThread.h"

static const char* sServiceName = "Registers";

RegistersService::RegistersService(Protocol * proto) : TCFService(proto)
{
	AddCommand("get", command_get);
	AddCommand("set", command_set);
}

RegistersService::~RegistersService() {
	// TODO Auto-generated destructor stub
}

const char* RegistersService::GetName() {
	return sServiceName;
}

void RegistersService::command_get_context(char * token, Channel * c) {
	LogTrace("RegistersService::command_get_context", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	Context* context = ContextManager::FindDebuggedContext(id);

	channel.writeReplyHeader(token);

	if (context == NULL) {
		// Return an invalid context ID error.
		channel.writeError(ERR_INV_CONTEXT);
		// channel.writeString("null");
		channel.writeZero();	// this puts a null object in the reply
	}
	else {
		channel.writeError(0);
		EventClientNotifier::WriteContext(*context, channel);
		channel.writeZero();
	}

	channel.writeComplete();
}

void RegistersService::command_get_children(char * token, Channel * c) {
	LogTrace("RunControl::command_get_children", "token: %s", token);
	TCFChannel channel(c);

	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	std::string parentID = id;
	if (parentID.length() == 0)
		parentID = "root";

	channel.writeReplyHeader(token);
	channel.writeError(0);

	channel.writeCharacter('[');

	std::list<Context*> chidren =
			ContextManager::FindDebuggedContext(parentID)->GetChildren();

	std::list<Context*>::iterator itr;
	for (itr = chidren.begin(); itr != chidren.end(); itr++) {
		if (itr != chidren.begin())
			channel.writeCharacter(',');
		std::string contextID = ((Context*) *itr)->GetID();
		channel.writeString(contextID);
	}

	channel.writeCharacter(']');

	channel.writeZero();
	channel.writeComplete();
}

/*
 */
void RegistersService::command_get(char * token, Channel * c) {
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

	WinThread* context = dynamic_cast<WinThread *>(ContextManager::FindDebuggedContext(exeContextID));

	channel.writeReplyHeader(token);

	if (context == NULL) {
		// Return invalid-context-ID error.
		channel.writeError(ERR_INV_CONTEXT);
		channel.writeZero();	// this puts a null object in the reply
	}
	else {
		std::vector<std::string> registerValues = context->GetRegisterValues(
				registerIDs);

		if (registerValues.size() == 0) {
			// no values got. Assuming target is running.
			// TODO: it's better the above context->GetRegisterValues() API return error code.
			channel.writeError(ERR_IS_RUNNING);
			channel.writeZero();	// this puts a null object in the reply
		}
		else {
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
		}
	}

	channel.writeComplete();
}

/*
 * register values are passed as hex-string in big-endian
 */
void RegistersService::command_set(char * token, Channel * c) {
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

	WinThread* context = dynamic_cast<WinThread *>(ContextManager::FindDebuggedContext(exeContextID));

	if (context != NULL) {
		context->SetRegisterValues(registerIDs, registerValues);
	}

	channel.writeReplyHeader(token);
	channel.writeZero();
	channel.writeComplete();
}

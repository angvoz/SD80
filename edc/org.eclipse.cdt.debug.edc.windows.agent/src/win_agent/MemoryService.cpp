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
#include "MemoryService.h"
#include "RunControlService.h"
#include "Logger.h"
#include "TCFChannel.h"
#include "TCFHeaders.h"
#include "ContextManager.h"
#include "RunControlContext.h"

static const char * sServiceName = "Memory";

MemoryService::MemoryService(Protocol * proto) :
	TCFService(proto) {
	AddCommand("getContext", RunControlService::command_get_context);
	AddCommand("getChildren", command_get_children);
	AddCommand("set", command_set);
	AddCommand("get", command_get);
	AddCommand("fill", command_fill);
}

MemoryService::~MemoryService(void) {
}

const char* MemoryService::GetName() {
	return sServiceName;
}

void MemoryService::command_get_children(char * token, Channel * c) {

}

void MemoryService::command_set(char * token, Channel * c) {
	LogTrace("Memory::command_set", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();

	RunControlContext* context = dynamic_cast<RunControlContext*>(ContextManager::findContext(id));

	unsigned long address = channel.readULong();
	channel.readZero();
	/*long wordSize =*/ channel.readLong();
	channel.readZero();
	long size = channel.readLong();
	channel.readZero();
	/*long mode =*/ channel.readLong();
	channel.readZero();

	char* memBuffer = channel.readBinaryData(size);

	channel.readZero();
	channel.readComplete();
	unsigned long bytesWritten = 0;
	int memBufferSize = size;

	if (context == NULL || !context->IsDebugging()) {
		// Return invalid-context-ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 1);
		delete[] memBuffer;
		return;
	}

	ReadWriteMemoryParams params(address, size, memBuffer, memBufferSize,
			&bytesWritten);

	try {
		int error = context->WriteMemory(params);
		channel.writeCompleteReply(token, error, 1);
	} catch (const AgentException& e) {
		channel.writeCompleteReply(token, ERR_OTHER, 1, e.what());
	}
	delete[] memBuffer;
}

void MemoryService::command_get(char * token, Channel * c) {
	LogTrace("Memory::command_get", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();
	unsigned long address = channel.readULong();
	channel.readZero();
	long wordSize = channel.readLong();
	channel.readZero();
	long size = channel.readLong();
	channel.readZero();
	long mode = channel.readLong();
	channel.readZero();
	channel.readComplete();

	unsigned long bytesRead;
	char* memBuffer = new char[size];
	int memBufferSize = size;

	RunControlContext* context = dynamic_cast<RunControlContext*>(ContextManager::findContext(id));

	if (context == NULL || !context->IsDebugging()) {
		channel.writeReplyHeader(token);
    	channel.writeZero();	// no data (comes BEFORE the error)
		channel.writeErrorReply(ERR_INV_CONTEXT, 1);
		return;
	}

	ReadWriteMemoryParams params(address, size, memBuffer, memBufferSize,
				&bytesRead);

	try {
		int error = context->ReadMemory(params);

		channel.writeReplyHeader(token);
		channel.writeBinaryData(memBuffer, memBufferSize);
		channel.writeError(error);

		channel.writeZero();
		channel.writeComplete();
	} catch (const AgentException& e) {
		channel.writeReplyHeader(token);
		channel.writeZero();	// data
		channel.writeErrorReply(ERR_OTHER, 0, e.what());
		channel.writeZero();
		channel.writeComplete();
	}
}

void MemoryService::command_fill(char * token, Channel * c) {
	LogTrace("Memory::command_fill", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();

	RunControlContext* context = dynamic_cast<RunControlContext*>(ContextManager::findContext(id));

	unsigned long address = channel.readULong();
	channel.readZero();
	/*long wordSize =*/ channel.readLong();
	channel.readZero();
	long size = channel.readLong();
	channel.readZero();
	/*long mode =*/ channel.readLong();
	channel.readZero();

	char* memBuffer = channel.readBinaryData(size);

	channel.readZero();
	channel.readComplete();

	if (context == NULL || !context->IsDebugging()) {
		// Return invalid-context-ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 2);
		return;
	}

	unsigned long bytesWritten = 0;
	int memBufferSize = size;

	ReadWriteMemoryParams params(address, size, memBuffer, memBufferSize,
			&bytesWritten);
	try {
		int error = context->WriteMemory(params);
		channel.writeCompleteReply(token, error, 2);
	} catch (const AgentException& e) {
		channel.writeCompleteReply(token, ERR_OTHER, 2, e.what());
	}
	delete[] memBuffer;
}

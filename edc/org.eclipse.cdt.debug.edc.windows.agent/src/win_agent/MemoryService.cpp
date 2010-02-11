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
	Context* context = ContextManager::FindDebuggedContext(id);
	unsigned long address = channel.readULong();
	channel.readZero();
	long wordSize = channel.readLong();
	channel.readZero();
	long size = channel.readLong();
	channel.readZero();
	long mode = channel.readLong();
	channel.readZero();

	char* memBuffer = channel.readBinaryData(size);

	channel.readZero();
	channel.readComplete();
	unsigned long bytesWritten = 0;
	int memBufferSize = size;

	int error = context->WriteMemory(address, size, memBuffer, memBufferSize,
			bytesWritten);

	delete[] memBuffer;

	channel.writeReplyHeader(token);
	channel.writeError(error);
	channel.writeString("null");

	channel.writeZero();
	channel.writeComplete();
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

	Context* context = ContextManager::FindDebuggedContext(id);

	if (context != NULL)
	{
		int error = context->ReadMemory(address, size, memBuffer, memBufferSize,
				bytesRead);
	}

	channel.writeReplyHeader(token);
	channel.writeBinaryData(memBuffer, memBufferSize);
	channel.writeError(0);
	channel.writeString("null");

	channel.writeZero();
	channel.writeComplete();

}

void MemoryService::command_fill(char * token, Channel * c) {
	LogTrace("Memory::command_fill", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();
	Context* context = ContextManager::FindDebuggedContext(id);
	unsigned long address = channel.readULong();
	channel.readZero();
	long wordSize = channel.readLong();
	channel.readZero();
	long size = channel.readLong();
	channel.readZero();
	long mode = channel.readLong();
	channel.readZero();

	char* memBuffer = channel.readBinaryData(size);

	channel.readZero();
	channel.readComplete();

	unsigned long bytesWritten = 0;
	int memBufferSize = size;

	int error = context->WriteMemory(address, size, memBuffer, memBufferSize,
			bytesWritten);

	delete[] memBuffer;

	channel.writeReplyHeader(token);
	channel.writeError(error);
	channel.writeString("null");

	channel.writeZero();
	channel.writeComplete();
}

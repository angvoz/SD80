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
#include "RegisterInfoX86.h"
#include "RegisterGroupInAgent.h"
#include "RegisterInAgent.h"
#include "AgentUtils.h"

static const char* sServiceName = "Registers";

/**
 * Add register group & register contexts for the give thread context.
 *
 * @param threadContextID
 * @return list of register group contexts.
 */
static void addRegisterContextsForThread(std::string threadContextID) {

	// Get static register info first.
	std::list<RegisterGroupInfo *>& rgInfoList = RegisterInfoX86::getRegisterGroupInfo();

	// Now add thread-specific register contexts.
	//
	std::list<RegisterGroupInfo *>::iterator it;
	for (it = rgInfoList.begin(); it != rgInfoList.end(); it++) {
		Properties& props = (*it)->getProperties();

		// This will be added as child context of the thread.
		RegisterGroupInAgent* rgContext = new RegisterGroupInAgent(
			props[PROP_NAME]->getStringValue(), threadContextID, props);

		ContextID rgContextID = rgContext->GetID();

		// Now add register contexts under the register group context
		//
		std::list<RegisterInfo*>& regs = (*it)->getRegisters();
		for (std::list<RegisterInfo*>::iterator it2 = regs.begin(); it2 != regs.end(); it2++) {
			Properties& regProps = (*it2)->getProperties();
			new RegisterInAgent(regProps[PROP_NAME]->getStringValue(), rgContextID, regProps);
		}
	}
}

RegistersService::RegistersService(Protocol * proto) :
	TCFService(proto)
{
	AddCommand("getContext", command_get_context);
	AddCommand("getChildren", command_get_children);
	AddCommand("get", command_get);
	AddCommand("set", command_set);
}

RegistersService::~RegistersService() {
	// TODO Auto-generated destructor stub
}

const char* RegistersService::GetName() {
	return sServiceName;
}

void RegistersService::command_get_context(const char * token, Channel * c) {
	LogTrace("RegistersService::command_get_context", "token: %s", token);
	TCFChannel channel(c);
	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	Context* context = ContextManager::findContext(id);

	if (context == NULL) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 1);
		return;
	}
	
	channel.writeReplyHeader(token);
	channel.writeError(0);
	EventClientNotifier::WriteContext(*context, channel);
	channel.writeZero(); // end of context
	channel.writeComplete();

}

void RegistersService::command_get_children(const char * token, Channel * c) {
	LogTrace("RunControl::command_get_children", "token: %s", token);
	TCFChannel channel(c);

	std::string id = channel.readString();
	channel.readZero();
	channel.readComplete();

	std::string parentID = id;
	if (parentID.length() == 0)
		parentID = "root";


	Context* parent = ContextManager::findContext(parentID);

	if (parent == NULL) {
		// Return an invalid context ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 1);
		return;
	}
	
	channel.writeReplyHeader(token);
	
	channel.writeError(0);

	channel.writeCharacter('[');

	std::list<Context*>& children = parent->GetChildren();

	if (NULL != dynamic_cast<WinThread*>(parent)) {
		// Currently it's assumed thread only has register group
		// contexts as children.
		// And we hook up the register children to a thread only
		// when requested. This way we don't bother adding registers
		// for a thread that user does not care about.
		//  ..................02/11/10
		if (children.size() == 0) {
			// Add register contexts for the thread when accessed.
			addRegisterContextsForThread(parentID);
			children = parent->GetChildren();
		}

	}
	std::list<Context*>::iterator itr;
	for (itr = children.begin(); itr != children.end(); itr++) {
		if (itr != children.begin())
			channel.writeCharacter(',');
		std::string contextID = ((Context*) *itr)->GetID();
		channel.writeString(contextID);
	}

	channel.writeCharacter(']');
	channel.writeZero(); // end of context

	channel.writeComplete();
}

/**
 * Find the owner thread context for a register or register group context.
 * Return NULL if not found.
 * @param regCxt register or register group context.
 */
WinThread* getThreadForRegisterContext(Context* regCxt) {
	RegisterGroupInAgent* regGroup;

	if (NULL != dynamic_cast<RegisterInAgent *>(regCxt))
		regGroup = dynamic_cast<RegisterGroupInAgent*>(ContextManager::findContext(regCxt->GetParentID()));
	else 
		regGroup = dynamic_cast<RegisterGroupInAgent *>(regCxt);

	if (regGroup == NULL)	 // invalid context
		return NULL;
	
	std::string threadID = regGroup->GetParentID();
	WinThread* thread = dynamic_cast<WinThread *>(ContextManager::findContext(threadID));

	return thread;
}

/*
 * register values are passed as hex-string in big-endian
 */
void RegistersService::command_get(const char * token, Channel * c) {
	TCFChannel channel(c);
	std::string regCxtID = channel.readString();
	channel.readZero();
	channel.readComplete();

	RegisterInAgent* regCxt = dynamic_cast<RegisterInAgent *>(ContextManager::findContext(regCxtID));
	WinThread* thread = getThreadForRegisterContext(regCxt);

	if (regCxt == NULL || thread == NULL || !thread->IsDebugging()) {
		// Return invalid-context-ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT, 1);
		return;
	}

	int regSize = regCxt->GetProperties()[PROP_SIZE]->getIntValue();

	char *valueBuff = thread->GetRegisterValue(
			regCxt->GetProperties()[PROP_NAME]->getStringValue(),
			regSize);

	if (valueBuff == NULL) {
		// no values got. Assuming target is running.
		channel.writeCompleteReply(token, ERR_IS_RUNNING, 1);
		return;
	}
	
	// Currently EDC host expects big-endian.
	AgentUtils::SwapBytes(valueBuff, regSize);

	channel.writeReplyHeader(token);
	channel.writeError(0);

	channel.writeBinaryData(valueBuff, regSize);
	delete[] valueBuff;

	channel.writeComplete();
}

/*
 */
void RegistersService::command_set(const char * token, Channel * c) {
	TCFChannel channel(c);

	std::string regCxtID = channel.readString();
	channel.readZero();

	RegisterInAgent* regCxt = dynamic_cast<RegisterInAgent *>(ContextManager::findContext(regCxtID));

	int regSize = 4;
	if (regCxt != NULL) {
		regSize = regCxt->GetProperties()[PROP_SIZE]->getIntValue();
	}
	char* val = channel.readBinaryData(regSize);
	channel.readZero();
	channel.readComplete();

	WinThread* thread = getThreadForRegisterContext(regCxt);

	if (regCxt == NULL || thread == NULL || !thread->IsDebugging()) {
		// Return invalid-context-ID error.
		channel.writeCompleteReply(token, ERR_INV_CONTEXT);
		return;
	}

	// Currently EDC host sends big-endian data.
	AgentUtils::SwapBytes(val, regSize);

	bool ok = thread->SetRegisterValue(
			regCxt->GetProperties()[PROP_NAME]->getStringValue(),
			regSize, val);

	delete[] val;

	if (!ok) {
		// Assuming target is running.
		channel.writeCompleteReply(token, ERR_IS_RUNNING);
	}
	else {
		channel.writeCompleteReply(token, 0);
	}
}

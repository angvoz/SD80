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
#include "AgentAction.h"
#include "TCFChannel.h"

AgentAction::AgentAction(const AgentActionParams& params_) : params(params_) {
}

AgentAction::~AgentAction(void) {
}

void AgentActionParams::reportSuccessForAction(int addNullObjects) const {
	if (isSubAction)
		return;
	AgentActionReply::postReply(channel, token, 0);
}

static void send_reply_message(void *data) {
	AgentActionReply *info = (AgentActionReply*) data;

	TCFOutputStream out(&info->channel->out);

	out.writeCompleteReply(info->token.c_str(), info->error, info->addNullObjects,
			info->message != 0 ? info->message->c_str() : 0, info->serviceName);

	delete info;
}

void AgentActionReply::postReply(Channel* channel, const std::string& token,
		int error, int addNullObjects, std::string* message, const char* serviceName) {
	AgentActionReply* reply = new AgentActionReply(channel, token, error, message, addNullObjects, serviceName);
	post_event(send_reply_message, reply);
}

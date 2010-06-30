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

#ifndef AGENT_ACTION_H
#define AGENT_ACTION_H

#include "TCFContext.h"
extern "C" {
#include "errors.h"
}

struct Channel;

/**
 * These are the basic parameters that initiated a given action, for use in
 * sending a reply later.  In case an action is invoked recursively,
 * 'isSubAction' is true, indicating the one action is calling
 * another, and the callee should throw an exception or return and let
 * the caller handle the reply.
 */
struct AgentActionParams {
	std::string token;
	Channel* channel;
	bool isSubAction;

	AgentActionParams(const std::string& token_, Channel* channel_, bool isSubAction_ = false) :
		token(token_), channel(channel_), isSubAction(isSubAction_) { }

	AgentActionParams subParams() const {
		return AgentActionParams(token, channel, true);
	}

	/** Report a successful status, if this is not a subaction. */
	void reportSuccessForAction(int addNullObjects = 0) const;
};


/**
 * This helper class allows asynchronous actions to conveniently send TCF replies.
 * Such replies must be sent from the dispatch thread, so this class wraps up
 * reply parameters and posts an event to send the reply.
 *
 */
struct AgentActionReply {
	Channel* channel;
	std::string token;
	std::string* message;
	int error;
	int addNullObjects;
	const char* serviceName;

	~AgentActionReply() {
		delete message;
	}

	/**
	 * Post an error reply, indicating an error and optionally emitting null objects
	 * in place of the contents that would be expected in a successful case.
	 * @channel the channel on which to send
	 * @token the token for the original command
	 * @error a system or TCF error code
	 * @addNullObjects number of null objects to emit after the error code
	 * @message if not NULL, the custom format (text) for the message (this class deletes)
	 * @serviceName if not NULL, an additional specifier for the service that caused the error
	 */
	static void postReply(Channel* channel, const std::string& token,
			int error, int addNullObjects = 0, std::string* message = 0, const char* serviceName = 0);

private:
	AgentActionReply(Channel* channel_, const std::string& token_, int error_,
			std::string* message_, int nullObjects_ = 0, const char* serviceName_ = 0)
		: channel(channel_), token(token_), message(message_), error(error_), addNullObjects(nullObjects_),
		  serviceName(serviceName_)
	{ }
};


class AgentAction {
public:
	AgentAction(const AgentActionParams& params);
	virtual ~AgentAction(void);

	/** 
	 * Run an action later.  If you use the TCF channel to send a reply, 
	 * use postReply() or explicitly wrap the calls in post_event().
	 */
	virtual void Run() = 0;

	/**
	 * Post an reply, indicating an error and optionally emitting null objects
	 * in place of the contents that would be expected in a successful case.
	 * @params the action params containing the channel and token
	 * @error a system or TCF error code
	 * @addNullObjects number of null objects to emit after the error code
	 * @message if not NULL, the custom format (text) for the message
	 * @serviceName if not NULL, an additional specifier for the service that caused the error
	 */
	void postReply(int error, int addNullObjects = 0, std::string* message = 0, const char* serviceName = 0) {
		AgentActionReply::postReply(params.channel, params.token, error, addNullObjects, message, serviceName);
	}

	/**
	 * Post an reply, indicating an error and optionally emitting null objects
	 * in place of the contents that would be expected in a successful case.
	 * @params the action params containing the channel and token
	 * @error a system or TCF error code
	 * @exception an exception to report as the message
	 * @addNullObjects number of null objects to emit after the error code
	 * @serviceName if not NULL, an additional specifier for the service that caused the error
	 */
	void postReply(const AgentException* exception, int addNullObjects = 0, const char* serviceName = 0) {
		AgentActionReply::postReply(params.channel, params.token, ERR_OTHER,
				addNullObjects, new std::string(exception->what()), serviceName);
	}

protected:
	AgentActionParams params;
};
#endif

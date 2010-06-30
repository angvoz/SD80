/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
#include "TCFHeaders.h"
#include "TCFChannel.h"

extern "C" {
#include "protocol.h"
};

TCFChannel::TCFChannel(Channel * c) :
	TCFOutputStream(&c->out), TCFInputStream(&c->inp) {
}

TCFChannel::~TCFChannel(void) {
}


std::map<Channel*, TCFChannelAdapter*> TCFChannelAdapter::adapterMap;

TCFChannelAdapter* TCFChannelAdapter::findChannel(Channel* channel) {
	std::map<Channel*, TCFChannelAdapter*>::iterator iter = adapterMap.find(channel);
	if (iter != adapterMap.end()) {
	    return adapterMap[channel];
	} else {
		// should not get here
		check_error(ERR_CHANNEL_CLOSED);
		exit(1);
		return 0; // quell warning
	}
}

TCFChannelAdapter::TCFChannelAdapter() {
	theChannel_ = new Channel();
	cb_ = 0;
	
	// clear out 
	memset(theChannel_, 0, sizeof(Channel));
	
	// populate implementation callbacks
	theChannel_->start_comm = TCFChannelAdapter::start_comm_impl;
	theChannel_->check_pending = TCFChannelAdapter::check_pending_impl;
	theChannel_->message_count = TCFChannelAdapter::message_count_impl;
	theChannel_->lock = TCFChannelAdapter::lock_impl;
	theChannel_->unlock = TCFChannelAdapter::unlock_impl;
	theChannel_->is_closed = TCFChannelAdapter::is_closed_impl;
	theChannel_->close = TCFChannelAdapter::close_impl;
	
	theChannel_->out.cur = obuf;
	theChannel_->out.end = obuf + sizeof(obuf);
	// populate callback handlers
	theChannel_->connecting = TCFChannelAdapter::connecting_cb_impl;
	theChannel_->connected = TCFChannelAdapter::connected_cb_impl;
	theChannel_->receive = TCFChannelAdapter::receive_cb_impl;
	theChannel_->disconnected = TCFChannelAdapter::disconnected_cb_impl;
	
	// use default callback handler initially
	setCallbacks(0);
	
	adapterMap[theChannel_] = this;
	
	theChannel_->state = ChannelStateStartWait;
}

TCFChannelAdapter::~TCFChannelAdapter() {
	adapterMap.erase(theChannel_);
	delete theChannel_;
}

void TCFChannelAdapter::init() {
	createStreamAdapters(&theChannel_->inp, &theChannel_->out);
	
	assert(inputStream_ && outputStream_);
}

void TCFChannelAdapter::setCallbacks(TCFChannelCallbackAdapter* cb) {
	if (!cb) {
		cb = &defaultCb_;
	}
	cb_ = cb;
}

/*
 * Channel implementations.  These forward to the C++ TCFChannelAdapter 
 * implementation.
 */

void TCFChannelAdapter::start_comm_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->start_comm();
}
void TCFChannelAdapter::check_pending_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->check_pending();
}
int TCFChannelAdapter::message_count_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	return tcfChannel->message_count();
}
void TCFChannelAdapter::lock_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->lock();
}
void TCFChannelAdapter::unlock_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->unlock();
}
int TCFChannelAdapter::is_closed_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	return tcfChannel->is_closed();
}
void TCFChannelAdapter::close_impl(Channel* channel, int code) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->close(code);
}

/*
 * Channel callback implementations.  These forward to the C++ 
 * TCFChannelAdapter implementation, which calls through to
 * TCFChannelCallbackAdapter.
 */

void TCFChannelAdapter::connecting_cb_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->cb_->connecting(tcfChannel);
}

void TCFChannelAdapter::connected_cb_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->cb_->connected(tcfChannel);
}

void TCFChannelAdapter::receive_cb_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->cb_->receive(tcfChannel);
}

void TCFChannelAdapter::disconnected_cb_impl(Channel* channel) {
	TCFChannelAdapter* tcfChannel = findChannel(channel);
	tcfChannel->cb_->disconnected(tcfChannel);
}

/*
 * Default callback implementations.  These use the same code
 * as in protocol.c.
 */

void TCFChannelCallbackAdapter::connecting(TCFChannelAdapter* channel) {
	trace(LOG_PROTOCOL, "channel server connecting");
	send_hello_message(channel->getChannel());
}

void TCFChannelCallbackAdapter::connected(TCFChannelAdapter* channel) {
	int i;
	Channel* c = channel->getChannel();
	trace(LOG_PROTOCOL, "channel server connected, remote services:");
	for (i = 0; i < c->peer_service_cnt; i++) {
		trace(LOG_PROTOCOL, "  %s", c->peer_service_list[i]);
	}
}

void TCFChannelCallbackAdapter::receive(TCFChannelAdapter* channel) {
	Channel* c = channel->getChannel();
	handle_protocol_message(c);
}

void TCFChannelCallbackAdapter::disconnected(TCFChannelAdapter* channel) {
	Channel* c = channel->getChannel();
	trace(LOG_PROTOCOL, "channel %#lx disconnected", channel);
	if (c->protocol)
		protocol_release(c->protocol);
	c->protocol = 0;
}

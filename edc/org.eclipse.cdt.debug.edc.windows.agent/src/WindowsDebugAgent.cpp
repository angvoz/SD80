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
#include <stdio.h>
#include <stdlib.h>

#include "ProcessService.h"
#include "WindowsOSDataService.h"
#include "RunControlService.h"
#include "SimpleRegistersService.h"
#include "RegistersService.h"
#include "MemoryService.h"
#include "LoggingService.h"

#include "EventClientNotifier.h"
#include "Logger.h"

#include "TCFHeaders.h"

static Protocol * proto;
static ChannelServer * serv;
static TCFBroadcastGroup * bcg;

static long channelCount = 0;

static void channel_server_connecting(Channel * c) {
	trace(LOG_PROTOCOL, "channel server connecting");

	send_hello_message(c);
	flush_stream(&c->out);
}

static void channel_server_connected(Channel * c) {
	channelCount++;
	int i;

	trace(LOG_PROTOCOL, "channel server connected, peer services:");
	for (i = 0; i < c->peer_service_cnt; i++) {
		trace(LOG_PROTOCOL, "  %s", c->peer_service_list[i]);
	}
}

static void channel_server_receive(Channel * c) {
	handle_protocol_message(c);
}

static void channel_server_disconnected(Channel * c) {
	trace(LOG_PROTOCOL, "channel server disconnected");
    protocol_release(c->protocol);
	channelCount--;
}

static void channel_new_connection(ChannelServer * serv, Channel * c) {
	protocol_reference(proto);
    c->protocol = proto;
	c->connecting = channel_server_connecting;
	c->connected = channel_server_connected;
	c->receive = channel_server_receive;
	c->disconnected = channel_server_disconnected;
	channel_set_broadcast_group(c, bcg);
	channel_start(c);
}

static void check_for_shutdown(void *x) {

	if (channelCount <= 0) {
		trace(LOG_PROTOCOL, "check_for_shutdown");
		cancel_event_loop();
	}

	post_event_with_delay(check_for_shutdown, NULL, 120 * 1000000);
}

// override this so we can actually break on it
_CRTIMP void __cdecl __MINGW_NOTHROW _assert (const char* error, const char* file, int line)
{
	char message[256];
	snprintf(message, sizeof(message),
			"Fatal error: assertion failed at file=%s, line=%d: %s\n",
			file ,line, error);
	fputs(message, stderr);
	trace(LOG_ALWAYS, message);
	exit(3);
}

int main(int argc, char* argv[]) {
#ifdef _DEBUG
	static const char* log_name = "C:\\WindowsDebugAgentLog.txt";
    log_mode = LOG_EVENTS | LOG_CHILD | LOG_WAITPID | LOG_CONTEXT | LOG_PROTOCOL | LOG_ASYNCREQ;
    open_log_file(log_name);
#endif

	try {
	static const char * url = "TCP:";
	PeerServer * ps = NULL;
	ini_mdep();
	ini_trace();
	ini_asyncreq();

	ini_events_queue();

	bcg = broadcast_group_alloc();
	proto = protocol_alloc();

	LogTrace("Starting up");
	EventClientNotifier::broadcastGroup = bcg;
	
	new ProcessService(proto);
	new RunControlService(proto);
	new WindowsOSDataService(proto);
	new RegistersService(proto);
	new MemoryService(proto);
	new LoggingService(proto);
	
	ps = channel_peer_from_url(url);

	peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("Win32 Debug Agent"));
	peer_server_addprop(ps, loc_strdup("DebugSupport"), loc_strdup(
			"Win32 Debug API"));

	if (ps == NULL) {
			LogTrace("invalid server URL");
		exit(1);
	}
	serv = channel_server(ps);
	if (serv == NULL) {
			LogTrace("cannot create TCF server\n");
		exit(1);
	}
	serv->new_conn = channel_new_connection;

	discovery_start();

#ifdef _DEBUG
#else
	post_event_with_delay(check_for_shutdown, NULL, 30 * 1000000);
#endif

	run_event_loop();
	} catch (...) {
		LogTrace("Exception thrown, caught at main");
	}
	return 0;
}

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
#include "RunControlService.h"
#include "RegisterService.h"
#include "MemoryService.h"
#include "LoggingService.h"

#include "EventClientNotifier.h"
#include "Logger.h"

#include "TCFHeaders.h"

static char * progname;
static Protocol * proto;
static ChannelServer * serv;
static ChannelServer * serv2;
static TCFBroadcastGroup * bcg;
static TCFSuspendGroup * spg;

static long channelCount = 0;

static void channel_server_connecting(Channel * c) {
	trace(LOG_PROTOCOL, "channel server connecting");

	send_hello_message((Protocol*) c->client_data, c);
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
	handle_protocol_message((Protocol*) c->client_data, c);
}

static void channel_server_disconnected(Channel * c) {
	trace(LOG_PROTOCOL, "channel server disconnected");
	channelCount--;
}

static void channel_new_connection(ChannelServer * serv, Channel * c) {
	protocol_reference(proto);
	c->client_data = proto;
	c->connecting = channel_server_connecting;
	c->connected = channel_server_connected;
	c->receive = channel_server_receive;
	c->disconnected = channel_server_disconnected;
	channel_set_suspend_group(c, spg);
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

int main(int argc, char* argv[]) {

	//TODO: need to add a main try/catch block

	int interactive = 0;
	char * log_name = 0;
	char * url = "TCP:";
	PeerServer * ps = NULL;
	ini_mdep();
	ini_trace();
	ini_asyncreq();

	open_log_file(log_name);

	ini_events_queue();

	bcg = broadcast_group_alloc();
	spg = suspend_group_alloc();
	proto = protocol_alloc();

	LogTrace("Starting up");
	EventClientNotifier::broadcastGroup = bcg;
	
	new ProcessService(proto);
	new RunControlService(proto);
	new RegisterService(proto);
	new MemoryService(proto);
	new LoggingService(proto);
	
	ps = channel_peer_from_url(url);

	peer_server_addprop(ps, loc_strdup("Name"), loc_strdup("Win32 Debug Agent"));
	peer_server_addprop(ps, loc_strdup("DebugSupport"), loc_strdup(
			"Win32 Debug API"));

	if (ps == NULL) {
		fprintf(stderr, "invalid server URL (-s option value): %s\n", url);
		exit(1);
	}
	serv = channel_server(ps);
	if (serv == NULL) {
		fprintf(stderr, "cannot create TCF server\n");
		exit(1);
	}
	serv->new_conn = channel_new_connection;

	discovery_start();

#ifdef _DEBUG
#else
	post_event_with_delay(check_for_shutdown, NULL, 30 * 1000000);
#endif

	/* Process events - must run on the initial thread since ptrace()
	 * returns ECHILD otherwise, thinking we are not the owner. */
	run_event_loop();
	return 0;
}

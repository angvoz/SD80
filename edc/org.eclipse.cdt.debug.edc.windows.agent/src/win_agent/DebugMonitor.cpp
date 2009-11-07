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

#include "DebugMonitor.h"
#include "Logger.h"

#include "AgentUtils.h"

using namespace std;

list<IContextEventListener *> DebugMonitor::gEventListeners;

DebugMonitor::DebugMonitor(string& executable, string& directory, string& args,
		string& environment, bool debug_children, std::string& token,
		Channel *c) {
	this->executable = executable;
	this->directory = directory;
	this->args = args;
	this->environment = environment;
	this->debug_children = debug_children;
	this->channel = c;
	this->token = token;
}

DebugMonitor::DebugMonitor(bool debug_children, std::string& token,	Channel *c) {
	this->executable = "";
	this->directory = "";
	this->args = "";
	this->environment = "";
	this->debug_children = debug_children;
	this->channel = c;
	this->token = token;
}

DebugMonitor::~DebugMonitor(void) {
}

void DebugMonitor::AddEventListener(IContextEventListener* listener) {
	gEventListeners.push_back(listener);
}

void DebugMonitor::RemoveEventListener(IContextEventListener* listener) {
	gEventListeners.remove(listener);
}

bool DebugMonitor::GetDebugChildren() {
	return debug_children;
}

void DebugMonitor::NotifyContextCreated(Context * ctx) {
	list<IContextEventListener *>::iterator itr;
	for (itr = gEventListeners.begin(); itr != gEventListeners.end(); itr++) {
		(*itr)->ContextCreated(ctx);
	}
}

void DebugMonitor::NotifyContextExited(Context * ctx) {
	list<IContextEventListener *>::iterator itr;
	for (itr = gEventListeners.begin(); itr != gEventListeners.end(); itr++) {
		(*itr)->ContextExited(ctx);
	}
}

void DebugMonitor::NotifyContextStopped(Context * ctx) {
	list<IContextEventListener *>::iterator itr;
	for (itr = gEventListeners.begin(); itr != gEventListeners.end(); itr++) {
		(*itr)->ContextStopped(ctx);
	}
}

void DebugMonitor::NotifyContextStarted(Context * ctx) {
	list<IContextEventListener *>::iterator itr;
	for (itr = gEventListeners.begin(); itr != gEventListeners.end(); itr++) {
		(*itr)->ContextStarted(ctx);
	}
}

void DebugMonitor::NotifyContextChanged(Context * ctx) {
	list<IContextEventListener *>::iterator itr;
	for (itr = gEventListeners.begin(); itr != gEventListeners.end(); itr++) {
		(*itr)->ContextChanged(ctx);
	}
}


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
#include "Context.h"
#include "ContextManager.h"
#include "AgentUtils.h"

Context::Context(ContextOSID osid, ContextID parentID, ContextID internalID) {
	// No, we need to make sure the internalID remains the same for the same context
	// (e.g. a process) in OS when we create "context" object.
	// this->internalID = ContextManager::GenerateInternalID();
	this->internalID = internalID;
	this->parentID = parentID;

	pid = osid;
	can_suspend = true;
	can_resume = true;
	can_terminate = true;

//	ContextManager::AddContext(internalID, this);
}

Context::~Context() {
	ContextManager::RemoveDebuggedContext(internalID);
}

ContextID Context::GetID() {
	return internalID;
}

ContextID Context::GetParentID() {
	return parentID;
}

ContextOSID Context::GetOSID() {
	return pid;
}

std::map<std::string, std::string> Context::GetProperties() {
	return properties;
}

void Context::AddChild(Context* child) {
	children_.push_back(child);
}

void Context::RemoveChild(Context* child) {
	children_.remove(child);
}

std::list<Context*> Context::GetChildren() {
	return children_;
}

bool Context::CanSuspend() {
	return can_suspend;
}

void Context::SetCanSuspend(bool yes) {
	can_suspend = yes;
}

bool Context::CanResume() {
	return can_resume;
}

void Context::SetCanResume(bool yes) {
	can_resume = yes;
}

bool Context::CanTerminate() {
	return can_terminate;
}

void Context::SetCanTerminate(bool yes) {
	can_terminate = yes;
}

std::string Context::GetProperty(std::string key) {
	return properties[key];
}

void Context::SetProperty(std::string key, std::string value) {
	properties[key] = value;
}

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

/*
 * Create a new context.
 */
Context::Context(ContextID parentID, ContextID internalID) {
	this->internalID = internalID;
	this->parentID = parentID;

	// Don't add the context to any context cache here as there are different
	// caches for different purposes.
	// See ContextManager for more.
}

Context::~Context() {
	for (Properties::iterator iter = properties.begin(); iter != properties.end(); iter++)
		delete iter->second;

	// remove the context from any context cache.
	// Note it does not hurt even if the context is not in the cache.
	ContextManager::RemoveDebuggedContext(GetID());
}

void Context::initialize()
{
	SetProperty(PROP_ID, new PropertyValue(internalID));
	SetProperty(PROP_PARENT_ID, new PropertyValue(parentID));
}

ContextID Context::GetID() {
	return internalID;
}

ContextID Context::GetParentID() {
	return parentID;
}

Properties Context::GetProperties() {
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

PropertyValue* Context::GetProperty(std::string key) {
	return properties[key];
}

void Context::SetProperty(std::string key, PropertyValue* value) {
	properties[key] = value;
}

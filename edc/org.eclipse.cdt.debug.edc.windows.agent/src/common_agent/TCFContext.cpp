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
#include "TCFContext.h"
#include "ContextManager.h"
#include "AgentUtils.h"

#undef remove

/*
 * Create a new context.
 */
Context::Context(const ContextID& parentID, const ContextID& internalID) {
	this->internalID = internalID;
	this->parentID = parentID;

	initialize();

	// Don't add the context to any context cache here as there are different
	// caches for different purposes.
	// See ContextManager for more.
}

Context::Context(const ContextID& parentID, const ContextID& internalID, Properties& props) {
	this->internalID = internalID;
	this->parentID = parentID;

	// Copy the "props" to internal member. We need deep copy.
	//	properties.insert(props.begin(), props.end());
	for (Properties::iterator it = props.begin(); it != props.end(); it++) {
		properties[it->first] = new PropertyValue(*(it->second));
	}

	initialize();
}

Context::~Context() {
	// clear properties, which is a hint to clients who may still
	// incorrectly hold on to a deallocated context.
	for (Properties::iterator iter = properties.begin(); iter != properties.end(); iter++)
		delete iter->second;
	properties.clear();
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

Properties& Context::GetProperties() {
	return properties;
}

void Context::AddChild(Context* child) {
	children_.push_back(child);
}

void Context::RemoveChild(Context* child) {
	children_.remove(child);
}

std::list<Context*>& Context::GetChildren() {
	return children_;
}

PropertyValue* Context::GetProperty(const std::string& key) {
	return properties[key];
}

void Context::SetProperty(const std::string& key, PropertyValue* value) {
	properties[key] = value;
}

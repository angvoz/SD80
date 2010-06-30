/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation  Feb, 2010
 *******************************************************************************/

#include "RegisterInAgent.h"
#include "ContextManager.h"
#include "assert.h"

/**
 * Construct a register context. The internal ID of the context will be
 * auto-generated. <br>
 * The constructed context will be added in debugged context cache. And it
 * will be added as child of the parent context.
 *
 * @param name
 *            name of the register group.
 * @param parentID
 *            internal ID of the parent (usually a register group).
 * @param props
 *            initial properties, cannot be null but can be empty. An
 *            internal copy of it will be made in this object.
 */
RegisterInAgent::RegisterInAgent(const std::string& name, const ContextID& parentID, Properties& props):
	Context(parentID, createInternalID(name, parentID), props)
{
	SetProperty(PROP_NAME, new PropertyValue(name));

	// We only need to add register as debugged context.
	ContextManager::addContext(this);
	Context* parent = ContextManager::findContext(parentID);

	if (parent != NULL)
		parent->AddChild(this);
	else
		// parent is not cached, should not happen.
		assert (false);
}

std::string& RegisterInAgent::createInternalID(const std::string& name, const std::string& parentID) {
	std::string* ret = new std::string(parentID);
	ret->append(".r");
	ret->append(name);
	return *ret;
}

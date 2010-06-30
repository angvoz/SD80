/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Nokia - Initial API and implementation
 *******************************************************************************/
#include "RunControlContext.h"
#include "AgentUtils.h"
#include "ProtocolConstants.h"

/*
 * Create a new context. Note the "internalID" should remain the same for the same context
 * (e.g. a process) in OS even when we create "context" object at different times.
 */
RunControlContext::RunControlContext(ContextOSID osid, const ContextID& parentID, const ContextID& internalID) :
	Context(parentID, internalID),
	isDebugging(false)
{
	osID = osid;

	initialize();
}


void RunControlContext::initialize()
{
	const std::string& str = AgentUtils::IntToString(GetOSID());
	SetProperty(PROP_OS_ID, new PropertyValue(str));
}

ContextOSID RunControlContext::GetOSID() {
	return osID;
}



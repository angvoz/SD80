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

#include "ThreadContext.h"
#include "AgentUtils.h"

ContextID ThreadContext::CreateInternalID(ContextOSID osID, const ContextID& parentID) {
	// return:  parentID.Ttid
	ContextID ret = parentID;
	ret += ".t";	// a prefix
	ret += AgentUtils::IntToString(osID);

	return ret;
}

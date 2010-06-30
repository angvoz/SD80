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
#pragma once
#include "AgentAction.h"
#include "TCFContext.h"

#ifdef __cplusplus
extern "C" {
#endif
	#include "channel.h"
#ifdef __cplusplus
}
#endif

class DetachProcessAction : public AgentAction {
public:
	DetachProcessAction(const AgentActionParams& params, ContextOSID processID);
	virtual ~DetachProcessAction(void);

	void Run();

private:
	ContextOSID processID;
};

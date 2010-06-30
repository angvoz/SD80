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
#pragma once
#include "AgentAction.h"
#include "TCFContext.h"

class ResumeContextAction: public AgentAction {
public:
	ResumeContextAction(const AgentActionParams& params, ContextOSID processid, ContextOSID threadid);
	virtual ~ResumeContextAction(void);

	void Run();

private:

	ContextOSID processid_;
	ContextOSID threadid_;

};

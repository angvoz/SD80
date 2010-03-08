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
#ifndef RUNCONTROLCONTEXT_H_
#define RUNCONTROLCONTEXT_H_

#include "Context.h"

/*
 * Context in TCF agent that represents process and thread.
 */
class RunControlContext : public Context {
public:
	RunControlContext(ContextOSID osid, ContextID parentID, ContextID internalID);

	/*
	 * Get OS ID of the process or thread.
	 */
	ContextOSID GetOSID();

	virtual int	ReadMemory(unsigned long address, unsigned long size,
					char* memBuffer, unsigned long bufferSize,
					unsigned long& sizeRead) = 0;

	virtual int WriteMemory(unsigned long address, unsigned long size,
			char* memBuffer, unsigned long bufferSize,
			unsigned long& sizeWritten) = 0;

	virtual void Resume() throw (AgentException) = 0;

	virtual void Suspend() throw (AgentException) = 0;

	virtual void Terminate() throw (AgentException) = 0;

	// Single-instruction step.
	virtual void SingleStep() throw (AgentException) = 0;

private:
	// initialize specific properties.
	void initialize();

	ContextOSID osID; 	/* process or thread identifier */
};

#endif /* RUNCONTROLCONTEXT_H_ */

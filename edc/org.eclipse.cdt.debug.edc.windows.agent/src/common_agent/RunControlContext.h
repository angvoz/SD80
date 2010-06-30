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

#include "TCFContext.h"
#include "AgentAction.h"

/**
 * Parameters for a memory transfer operation.
 */
struct ReadWriteMemoryParams {
	ReadWriteMemoryParams(unsigned long address_, unsigned long size_,
			char* memBuffer_, unsigned long bufferSize_,
			unsigned long* sizeTransferred_) :
		address(address_), size(size_), 
		memBuffer(memBuffer_), 
		bufferSize(bufferSize_),
		sizeTransferred(sizeTransferred_)
	{ }
	
	unsigned long address;
	unsigned long size;
	char* memBuffer;
	unsigned long bufferSize;
	
	unsigned long* sizeTransferred;
};

/*
 * Context in TCF agent that represents process and thread.
 *
 * The virtual methods below may be synchronous or asynchronous.
 *
 * When synchronous, they return a TCF error code, which the caller
 * will report as the TCF reply.
 *
 * When asynchronous, they all receive AgentActionParams or a subclass,
 * whose token and channel should be used for reporting a TCF reply.
 *
 * For either type, the methods may throw an AgentException, if they can
 * immediately detect an error condition for the command, and the caller
 * will report a TCF error reply for the exception.
 */
class RunControlContext : public Context {
public:
	RunControlContext(ContextOSID osid, const ContextID& parentID, const ContextID& internalID);

	/*
	 * Get OS ID of the process or thread.
	 */
	ContextOSID GetOSID();

	/**
	 * If true, the context is under control of the debugger.
	 * If not, it is just known to be running as of some recent update
	 * (usually the Processes::getChildren command).
	 */
	bool IsDebugging() { return isDebugging; }
	/**
	 * Toggle the status of the context being under control of the debugger.
	 *
	 * NOTE: this may be set on a process but not its threads, or vice versa -- it's
	 * up to the implementation to decide.
	 */
	void SetDebugging(bool debugging) { isDebugging = debugging; }

	/** Read memory synchronously, returning TCF error code or throwing exception. */
	virtual int	ReadMemory(const ReadWriteMemoryParams& params) throw (AgentException) = 0;

	/** Write memory synchronously, returning TCF error code or throwing exception. */
	virtual int WriteMemory(const ReadWriteMemoryParams& params) throw (AgentException) = 0;

	/** Resume execution asynchronously. */
	virtual void Resume(const AgentActionParams& params) throw (AgentException) = 0;

	/** Suspend execution asynchronously. */
	virtual void Suspend(const AgentActionParams& params) throw (AgentException) = 0;

	/** Terminate execution asynchronously. */
	virtual void Terminate(const AgentActionParams& params) throw (AgentException) = 0;

	/** Invoke single step and suspend asynchronously.  */
	virtual void SingleStep(const AgentActionParams& params) throw (AgentException) = 0;

private:
	// initialize specific properties.
	void initialize();

	ContextOSID osID; 	/* process or thread identifier */

	bool isDebugging;
};

#endif /* RUNCONTROLCONTEXT_H_ */

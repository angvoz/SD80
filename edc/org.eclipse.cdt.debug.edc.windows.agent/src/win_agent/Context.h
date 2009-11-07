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
#ifndef CONTEXT_H_
#define CONTEXT_H_

#include <map>
#include <vector>
#include <list>
#include <string>

#include "AgentException.h"


#define INVALID_CONTEXT_ID	""
#define ROOT_CONTEXT_ID	"root"

/*
 * Properties of context.
 */
/* From: TCF IRunControl.java.
 */
#define PROP_ID 			"ID"
#define PROP_PARENT_ID		"ParentID"
#define PROP_PROCESS_ID		"ProcessID"
#define PROP_IS_CONTAINER	"IsContainer"
#define PROP_HAS_STATE		"HasState"
#define PROP_CAN_RESUME		"CanResume"
#define PROP_CAN_COUNT		"CanCount"
#define PROP_CAN_SUSPEND	"CanSuspend"
#define PROP_CAN_TERMINATE	"CanTerminate"

/* From: TCF IProcesses.java
 */
/** Is the context attached */
#define PROP_ATTACHED		"Attached"
/** Process name. Client UI can show this name to a user */
#define PROP_NAME 			"Name"


typedef unsigned long ContextOSID; // ID in the OS
typedef std::string ContextID; // ID in debugger
typedef unsigned long ContextAddress; /* Type to represent byted address inside context memory */

class Context {
public:
	Context(ContextOSID osid, ContextID parentID, ContextID internalID);

	virtual ~Context();

	/*
	 * Get unique ID for the instance. This is internal ID, not process ID
	 * or thread ID in the OS.
	 */
	ContextID GetID();

	/* Get internal ID of the process if the context is a thread.
	 * Return invalid id if the context is a process.
	 */
	ContextID GetParentID();

	/*
	 * Get OS ID of the process or thread.
	 */
	ContextOSID GetOSID();

	virtual std::map<std::string, std::string> GetProperties();
	virtual std::list<Context*> GetChildren();
	void AddChild(Context *);
	void RemoveChild(Context *);

	virtual ContextAddress GetPCAddress() = 0;

	virtual std::string GetSuspendReason() = 0;

	virtual std::vector<std::string> GetRegisterValues(
			std::vector<std::string> registerIDs) = 0;

	virtual void SetRegisterValues(std::vector<std::string> registerIDs,
			std::vector<std::string> registerValues) = 0;

	virtual int
			ReadMemory(unsigned long address, unsigned long size,
					char* memBuffer, unsigned long bufferSize,
					unsigned long& sizeRead) = 0;

	virtual int WriteMemory(unsigned long address, unsigned long size,
			char* memBuffer, unsigned long bufferSize,
			unsigned long& sizeWritten) = 0;

	// Put the context under monitor of debugger.
	// TODO: Peculiar to Linux ?
	virtual void AttachSelf() throw (AgentException) = 0;

	virtual void Resume() throw (AgentException) = 0;

	virtual void Suspend() throw (AgentException) = 0;

	virtual void Terminate() throw (AgentException) = 0;

	// Single-instruction step.
	virtual void SingleStep() throw (AgentException) = 0;

	virtual bool CanSuspend();
	virtual void SetCanSuspend(bool yes);
	virtual bool CanResume();
	virtual void SetCanResume(bool yes);
	virtual bool CanTerminate();
	virtual void SetCanTerminate(bool yes);

	std::string GetProperty(std::string key);
	void SetProperty(std::string key, std::string value);

private:
	ContextID internalID;
	ContextID parentID; /* if this is not main thread in a process, parent points to main thread */
	// TODO: Do we want to do reference count ?
	//    unsigned int        ref_count;          /* reference count, see context_lock() and context_unlock() */
	ContextOSID pid; /* process or thread identifier */
	unsigned long mem; /* context memory space identifier */
	int stopped; /* OS kernel has stopped this context */
	int stopped_by_bp; /* stopped by breakpoint */
	void * stepping_over_bp; /* if not NULL context is stepping over a breakpoint */
	int exiting; /* context is about to exit */
	int exited; /* context exited */
	int intercepted; /* context is reported to a host as suspended */
	int pending_step; /* context is executing single instruction step */
	int pending_intercept; /* host is waiting for this context to be suspended */
	int pending_safe_event; /* safe events are waiting for this context to be stopped */
	unsigned long pending_signals; /* bitset of signals that were received, but not handled yet */
	unsigned long sig_dont_stop; /* bitset of signals that should not be intercepted by the debugger */
	unsigned long sig_dont_pass; /* bitset of signals that should not be delivered to the context */
	int signal; /* signal that stopped this context */
	int regs_error; /* if not 0, 'regs' is invalid */
	int regs_dirty; /* if not 0, 'regs' is modified and needs to be saved before context is continued */
	void * stack_trace;

	std::list<Context *> children_;
	std::map<std::string, std::string> properties;
	bool can_resume;
	bool can_suspend;
	bool can_terminate;
};

typedef void ContextAttachCallBack(int, Context *, void *);

#endif /* CONTEXT_H_ */

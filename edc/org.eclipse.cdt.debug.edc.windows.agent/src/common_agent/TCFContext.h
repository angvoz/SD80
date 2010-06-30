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
#ifndef TCFCONTEXT_H_
#define TCFCONTEXT_H_

#include <map>
#include <vector>
#include <list>
#include <string>

#include "AgentException.h"
#include "PropertyValue.h"

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
#define PROP_CAN_RESUME		"CanResume"	// value: int/long
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

/*
 * Context in TCF agent.
 * The context can be a process, thread, register group, register, etc.
 */
class Context {
public:
	Context(const ContextID& parentID, const ContextID& internalID);

	Context(const ContextID& parentID, const ContextID& internalID, Properties& props);

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

	virtual std::list<Context*>& GetChildren();
	void AddChild(Context *);
	void RemoveChild(Context *);

	Properties& GetProperties();
	PropertyValue* GetProperty(const std::string& key);
	void SetProperty(const std::string& key, PropertyValue* value);

private:
	void initialize();

	ContextID internalID;
	ContextID parentID;

	std::list<Context *> children_;
	Properties properties;
};

typedef void ContextAttachCallBack(int, Context *, void *);

#endif /* CONTEXT_H_ */

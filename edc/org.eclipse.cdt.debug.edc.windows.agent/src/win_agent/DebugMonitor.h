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
#ifndef DEBUGMONITOR_H_
#define DEBUGMONITOR_H_

#include <string>
#include <list>
#include <vector>
#include "TCFContext.h"
#include "AgentAction.h"
#include "AgentException.h"
#include "IContextEventListener.h"
#include "channel.h"


struct LaunchProcessParams : public AgentActionParams {
	/*
	 * Parameters:
	 *  debugChildren -- whether to monitor/debug child processes
	 */
	LaunchProcessParams(const std::string& token, Channel *c,
			const std::string& executable_, const std::string& directory_,
			const std::string& args_, std::vector<std::string>& environment_, bool debug_children_) :
				AgentActionParams(token, c), executable(executable_),
				directory(directory_), args(args_), environment(environment_),
				debug_children(debug_children_)
	{};
	std::string executable;
	std::string directory;
	std::string args;
	std::vector<std::string>& environment;
	bool debug_children;
};


struct AttachToProcessParams : public AgentActionParams {
	/*
	 * Parameters:
	 *  debugChildren -- whether to monitor/debug child processes
	 */
	AttachToProcessParams(const std::string& token, Channel *c,
			unsigned long processID_, bool debug_children_) :
		AgentActionParams(token, c), processID(processID_),
		debug_children(debug_children_)
	{};
	unsigned long processID;
	bool debug_children;
};

/*
 * Monitor and dispatch debug events.
 * Each instance of the class is in charge of one family of process (a process and/or
 * all its child processes).
 *
 * The pattern for client should be like this:
 * 		DebugMonitor dm = new DebugMonitor(...);
 * 		dm.StartMonitor();
 */
class DebugMonitor {
public:
	DebugMonitor(const LaunchProcessParams& params);

	DebugMonitor(const AttachToProcessParams& params);

	virtual ~DebugMonitor(void);

	/*
	 * Start the process and put it under debug.
	 *
	 */
	virtual void StartProcessForDebug() = 0;

	/*
	 * Start the loop of monitoring and handling debug events.
	 */
	virtual void EventLoop() = 0;

	/*
	 * Start the monitor in a thread.
	 */
	virtual void StartMonitor() = 0;

	bool GetDebugChildren();

	/*
	 * Attach debugger to a given process.
	 * The implementation is platform specific.
	 * TODO: In which class should we put this API ?
	 */
	virtual void Attach(unsigned long pid, ContextAttachCallBack * done,
			void * data, int selfattach) = 0;

	/*
	 * These are for event dispatching.
	 */

	static void AddEventListener(IContextEventListener* listener);
	static void RemoveEventListener(IContextEventListener* listener);

	static void NotifyContextCreated(Context * ctx);
	static void NotifyContextExited(Context * ctx);
	static void NotifyContextStopped(Context * ctx);
	static void NotifyContextStarted(Context * ctx);
	static void NotifyContextChanged(Context * ctx);

protected:

	std::string executable;
	std::string directory;
	std::string args;
	std::vector<std::string> environment;
	bool debug_children;

	/** the channel used to start the debug session */
	Channel *channel;
	/** the token used to start the debug session; must use only if initial launch/attach fails */
	std::string token;

private:
	static std::list<IContextEventListener *> gEventListeners;
};

#endif  /* #ifndef DEBUGMONITOR_H_ */

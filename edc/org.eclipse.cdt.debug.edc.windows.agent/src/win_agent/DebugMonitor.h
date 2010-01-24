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
#include "Context.h"
#include "AgentException.h"
#include "IContextEventListener.h"
#include "channel.h"

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
	/*
	 * Parameters:
	 *  debugChildren -- whether to monitor/debug child processes
	 */
	DebugMonitor(std::string& executable, std::string& directory,
			std::string& args, std::vector<std::string>& environment, bool debug_children,
			std::string& token, Channel *c);

	DebugMonitor(bool debug_children, std::string& token, Channel *c);

	~DebugMonitor(void);

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

	/* Write a error over the Channel */
	virtual void WriteError(unsigned long errNum, const char* message) = 0;

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
	Channel *channel;
	std::string token;

private:
	static std::list<IContextEventListener *> gEventListeners;
};

#endif  /* #ifndef DEBUGMONITOR_H_ */

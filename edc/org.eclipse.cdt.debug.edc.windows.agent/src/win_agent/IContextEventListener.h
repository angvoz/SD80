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
#ifndef ICONTEXTEVENTLISTENER_H_
#define ICONTEXTEVENTLISTENER_H_

class Context;

/*
 * Any class that's interested in getting debug events should implement
 * this interface. And call DebugMonitor::AddEventListener() to register.
 */
class IContextEventListener {

public:
	virtual void ContextCreated(Context * ctx) = 0;
	virtual void ContextExited(Context * ctx) = 0;
	virtual void ContextStopped(Context * ctx) = 0;
	virtual void ContextStarted(Context * ctx) = 0;
	virtual void ContextChanged(Context * ctx) = 0;
};

#endif /* CONTEXTEVENTLISTENER_H_ */

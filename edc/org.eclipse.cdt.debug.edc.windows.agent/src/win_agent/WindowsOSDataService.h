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

#include <map>
#include <string>
#include "TCFService.h"
#include "RunControlContext.h"

struct Protocol;
struct Channel;

class WindowsOSDataService: public TCFService {
public:
	WindowsOSDataService(Protocol * proto);
	~WindowsOSDataService(void);

	const char* GetName();

	static void command_get_threads(const char *, Channel *);
};

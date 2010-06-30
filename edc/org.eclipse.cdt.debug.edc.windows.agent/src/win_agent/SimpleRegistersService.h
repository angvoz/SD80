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
#ifndef SIMPLEREGISTERSSERVICE_H_
#define SIMPLEREGISTERSSERVICE_H_

#include "TCFService.h"
#include "TCFHeaders.h"

class SimpleRegistersService: public TCFService {
public:
	SimpleRegistersService(Protocol * proto);
	~SimpleRegistersService(void);

	const char* GetName();

	static void command_get(char * token, Channel * c);
	static void command_set(char * token, Channel * c);
};

#endif /* #define SIMPLEREGISTERSERVICE_H_ */

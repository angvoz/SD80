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

#ifndef REGISTERSSERVICE_H_
#define REGISTERSSERVICE_H_

#include "TCFService.h"

class RegistersService: public TCFService {
public:
	RegistersService(Protocol * proto);
	virtual ~RegistersService();

	const char* GetName();

	static void command_get_context(const char * token, Channel * c);
	static void command_get_children(const char * token, Channel * c);
	static void command_get(const char * token, Channel * c);
	static void command_set(const char * token, Channel * c);

	/* Commands not supported/needed for now.
	static void command_getm(const char * token, Channel * c);
	static void command_setm(const char * token, Channel * c);
	static void command_search(const char * token, Channel * c);
	*/
};

#endif /* REGISTERSSERVICE_H_ */

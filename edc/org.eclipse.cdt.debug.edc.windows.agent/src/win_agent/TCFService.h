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

#ifndef TCFSERVICE_H_
#define TCFSERVICE_H_

#include <string>
#include "TCFHeaders.h"

/*
 * Abstract of TCF service in agent.
 */
class TCFService {
public:
	TCFService(Protocol * proto);
	virtual ~TCFService();

	Protocol* GetProtocol();

	void AddCommand(const char * name, ProtocolCommandHandler handler);

	/*
	 * get service name.
	 */
	virtual const char* GetName() = 0;

private:
	Protocol * protocol;
};

#endif /* TCFSERVICE_H_ */

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
#include "TCFService.h"

TCFService::TCFService(Protocol * proto) {
	protocol = proto;
}

TCFService::~TCFService() {
}

Protocol* TCFService::GetProtocol() {
	return protocol;
}

void TCFService::AddCommand(const char * name, ProtocolCommandHandler handler) {
	add_command_handler(GetProtocol(), GetName(), name, handler);
}

/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation  Feb, 2010
 *******************************************************************************/

#ifndef REGISTERGROUPINAGENT_H_
#define REGISTERGROUPINAGENT_H_

#include "TCFContext.h"

/**
 * Register group context in a TCF agent.
 *
 */
class RegisterGroupInAgent : public Context {

public:
	RegisterGroupInAgent(const std::string& name, const ContextID& parentID, Properties& props);

	static std::string& createInternalID(const std::string& name, const std::string& parentID);
};

#endif // REGISTERGROUPINAGENT_H_

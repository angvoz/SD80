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
#include "TCFHeaders.h"
#include "TCFChannel.h"

TCFChannel::TCFChannel(Channel * c) :
	TCFOutputStream(&c->out), TCFInputStream(&c->inp) {
}

TCFChannel::~TCFChannel(void) {
}
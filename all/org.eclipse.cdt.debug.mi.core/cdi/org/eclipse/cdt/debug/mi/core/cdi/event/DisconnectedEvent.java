/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.event;

import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.mi.core.cdi.Session;

/**
 */
public class DisconnectedEvent implements ICDIDisconnectedEvent {

	ICDIObject source;

	public DisconnectedEvent(Session session) {
		source = session.getCurrentTarget();
	}

	/**
	 * @see org.eclipse.cdt.debug.core..ICDIEvent#getSource()
	 */
	public ICDIObject getSource() {
		return source;
	}

}

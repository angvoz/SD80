/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.dsf.debug.service.ISignals;
import org.eclipse.cdt.dsf.service.DsfSession;

public class Signals extends AbstractEDCService implements ISignals {

	public Signals(DsfSession session) {
		super(session, new String[] { ISignals.class.getName(), Signals.class.getName() });
		// TODO Auto-generated constructor stub
	}

}

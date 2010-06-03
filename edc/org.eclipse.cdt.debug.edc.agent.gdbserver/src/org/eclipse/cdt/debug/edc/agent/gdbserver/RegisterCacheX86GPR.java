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

package org.eclipse.cdt.debug.edc.agent.gdbserver;

import org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.Packet;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;

/**
 * Cache for x86 general purpose registers.
 */
public class RegisterCacheX86GPR extends RegisterCache {

	private Register[] fCache;

	public RegisterCacheX86GPR() {
		super();
	}

	@Override
	protected Register[] getCacheDefinition() {
		if (fCache == null) {
			// The order below (and the index of each register) is defined by
			// gdbserver.
			// The names are defined by host debugger.
			//
			fCache = new Register[] {
			/* name size (value valid) */
			new Register("EAX", 4), new Register("ECX", 4), new Register("EDX", 4), new Register("EBX", 4),
					new Register("ESP", 4), new Register("EBP", 4), new Register("ESI", 4), new Register("EDI", 4),
					new Register("EIP", 4), new Register("EFL", 4), new Register("CS", 4), new Register("SS", 4),
					new Register("DS", 4), new Register("ES", 4), new Register("FS", 4), new Register("GS", 4), };
		}

		return fCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.edc.agent.gdbserver.RegisterCache#updateGPRCache
	 * (org.eclipse.cdt.debug.edc.agent.gdbserver.GdbRemoteProtocol.Packet)
	 */
	@Override
	public void updateCache(Packet p) throws AgentException {
		/*
		 * note the register values in the packet data is in little-endian.
		 */
		for (int i = 0; i < fCache.length; i++)
			cacheRegister(i, p.getData().substring(i * 8, i * 8 + 8));
	}
}

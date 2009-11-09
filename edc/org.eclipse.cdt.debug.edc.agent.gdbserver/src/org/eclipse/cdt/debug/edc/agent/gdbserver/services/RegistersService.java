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

package org.eclipse.cdt.debug.edc.agent.gdbserver.services;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IRegisters;

/**
 * @author LWang
 * 
 */
public class RegistersService implements IRegisters {

	private final IChannel fChannel;

	private class CommandServer implements IChannel.ICommandServer {

		public void command(IToken token, String name, byte[] data) {
			try {
				command(token, name, JSON.parseSequence(data));
			} catch (Throwable x) {
				fChannel.terminate(x);
			}
		}

		private void command(IToken token, String name, Object[] args) throws Exception {
			System.out.println("RegistersService Command:" + name);

			fChannel.rejectCommand(token);
		}

	}

	public RegistersService(IChannel channel) {
		this.fChannel = channel;
		channel.addCommandServer(this, new CommandServer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IRegisters#addListener(org.eclipse.tm.tcf
	 * .services.IRegisters.RegistersListener)
	 */
	public void addListener(RegistersListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IRegisters#getChildren(java.lang.String,
	 * org.eclipse.tm.tcf.services.IRegisters.DoneGetChildren)
	 */
	public IToken getChildren(String parentContextId, DoneGetChildren done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.services.IRegisters#getContext(java.lang.String,
	 * org.eclipse.tm.tcf.services.IRegisters.DoneGetContext)
	 */
	public IToken getContext(String id, DoneGetContext done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IRegisters#getm(org.eclipse.tm.tcf.services
	 * .IRegisters.Location[], org.eclipse.tm.tcf.services.IRegisters.DoneGet)
	 */
	public IToken getm(Location[] locs, DoneGet done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IRegisters#removeListener(org.eclipse.tm.
	 * tcf.services.IRegisters.RegistersListener)
	 */
	public void removeListener(RegistersListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.tm.tcf.services.IRegisters#setm(org.eclipse.tm.tcf.services
	 * .IRegisters.Location[], byte[],
	 * org.eclipse.tm.tcf.services.IRegisters.DoneSet)
	 */
	public IToken setm(Location[] locs, byte[] value, DoneSet done) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	public String getName() {
		return IRegisters.NAME;
	}

}

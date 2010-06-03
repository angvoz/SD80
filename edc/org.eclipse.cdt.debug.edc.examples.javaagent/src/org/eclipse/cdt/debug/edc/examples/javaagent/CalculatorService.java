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

package org.eclipse.cdt.debug.edc.examples.javaagent;

import org.eclipse.cdt.debug.edc.examples.javaagent.remote.ICalculator;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;

/**
 * @author LWang
 * 
 */
public class CalculatorService implements ICalculator {

	private final IChannel channel;

	private class CommandServer implements IChannel.ICommandServer {

		public void command(IToken token, String name, byte[] data) {
			try {
				command(token, name, JSON.parseSequence(data));
			} catch (Throwable x) {
				channel.terminate(x);
			}
		}

		private void command(IToken token, String name, Object[] args) throws Exception {
			if (name.equals("increment")) {
				if (args.length != 1)
					throw new Exception("Invalid number of arguments");
				Integer s = (Integer) args[0];
				s = s + 1;
				channel.sendResult(token, JSON.toJSONSequence(new Object[] { s }));
			} else {
				channel.rejectCommand(token);
			}
		}
	}

	public CalculatorService(IChannel channel) {
		this.channel = channel;
		channel.addCommandServer(this, new CommandServer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.edc.agent.protoinplugin.ICalculator#Increment(java
	 * .lang.String,
	 * org.eclipse.cdt.debug.edc.agent.protoinplugin.ICalculator.DoneIncrement)
	 */
	public IToken increment(int s, DoneIncrement done) {
		// TODO need to do anything ?
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.tm.tcf.protocol.IService#getName()
	 */
	public String getName() {
		return NAME;
	}

}

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

package org.eclipse.cdt.debug.edc.examples.javaagent.remote;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;

public class CalculatorServiceProxy implements ICalculator {

	private final IChannel channel;

	static {
		/*
		 * TODO: use extension point "org.eclipse.tm.tcf.startup" to load this
		 * class at TCF startup time, so proxy factory is properly activated and
		 * clients do not need to explicitly call this
		 * "registerProxyForClient()".
		 */
		registerProxyForClient();
	}

	CalculatorServiceProxy(IChannel channel) {
		this.channel = channel;
	}

	/**
	 * Register/configure the service on the Eclipse client side. This should be
	 * called only once. And it is usually called when the TCF framework starts
	 * (see where it's called for more).
	 */
	static public void registerProxyForClient() {

		Protocol.addServiceProvider(new IServiceProvider() {
			/*
			 * Tell the framework that for remote service named
			 * "ICalculator.NAME", we offer the proxy for it.
			 */
			public IService getServiceProxy(IChannel channel, String serviceName) {
				if (serviceName.equals(ICalculator.NAME))
					return new CalculatorServiceProxy(channel);
				return null;
			}

			public IService[] getLocalService(IChannel channel) {
				return null;
			}
		});

		/*
		 * Make the service proxy available to all potential clients by creating
		 * the proxy object every time a TCF communication channel is opened.
		 * 
		 * No. This would fail the request of
		 * "Channel.getRemoteService(servicename)" (see the unit test
		 * TestTCFAgent for more). A better solution is to use the above
		 * Protocol.addServiceProvider(). .................... 04/08/09
		 */
		/*
		 * Protocol.addChannelOpenListener(new Protocol.ChannelOpenListener() {
		 * public void onChannelOpen(IChannel channel) { // Check if remote
		 * server provides Daytime service if
		 * (channel.getRemoteService(ICalculator.NAME) == null) return; //
		 * Create service proxy channel.setServiceProxy(ICalculator.class, new
		 * CalculatorServiceProxy(channel)); } });
		 */
	}

	/**
	 * Return service name, as it appears on the wire - a TCF name of the
	 * service.
	 */
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.debug.edc.agent.protoinplugin.ICalculator#Increment(java
	 * .lang.String,
	 * org.eclipse.cdt.debug.edc.agent.protoinplugin.ICalculator.DoneIncrement)
	 */
	public IToken increment(int i, final DoneIncrement done) {
		return new Command(channel, this, "increment", new Object[] { new Integer(i) }) {
			@Override
			public void done(Exception error, Object[] args) {
				int ret = 0;
				if (error == null) {
					assert args.length == 1;
					ret = ((Integer) args[0]).intValue();
				}
				done.done(token, error, ret);
			}
		}.token;
	}
}

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
package org.eclipse.cdt.debug.edc.tcf.extension.services;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;

public class SimpleRegistersProxy implements ISimpleRegisters {

	private final IChannel channel;

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
				if (serviceName.equals(ISimpleRegisters.NAME))
					return new SimpleRegistersProxy(channel);
				return null;
			}

			public IService[] getLocalService(IChannel channel) {
				return null;
			}
		});
	}

	public SimpleRegistersProxy(IChannel channel) {
		this.channel = channel;

	}

	public String getName() {
		return NAME;
	}

	public IToken get(String executionContextID, String[] registerIDs, final DoneGet done) {
		return new Command(channel, SimpleRegistersProxy.this, "get", new Object[] { executionContextID, registerIDs }) {
			@Override
			public void done(Exception error, Object[] args) {
				String[] val = null;

				if (error == null) {
					assert args.length == 2;
					error = toError(args[0]);
					val = toStringArray(args[1]);
				}
				done.doneGet(token, error, val);
			}
		}.token;
	}

	public IToken set(String executionContextID, String[] registerIDs, String[] registerValues, final DoneSet done) {
		return new Command(channel, SimpleRegistersProxy.this, "set", new Object[] { executionContextID, registerIDs,
				registerValues }) {
			@Override
			public void done(Exception error, Object[] args) {
				done.doneSet(token, error, null);
			}
		}.token;
	}

	@SuppressWarnings("unchecked")
	private String[] toStringArray(Object o) {
		if (o != null && o instanceof Collection<?>) {
			Collection<Object> objs = (Collection<Object>) o;
			Collection<String> strings = new ArrayList<String>();
			for (Object object : objs) {
				strings.add(object.toString());
			}
			return strings.toArray(new String[strings.size()]);
		}
		return null;
	}

}

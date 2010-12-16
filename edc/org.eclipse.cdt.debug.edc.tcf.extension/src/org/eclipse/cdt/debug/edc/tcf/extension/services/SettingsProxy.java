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

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;

public class SettingsProxy implements ISettings {

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
				if (serviceName.equals(ISettings.NAME))
					return new SettingsProxy(channel);
				return null;
			}

			public IService[] getLocalService(IChannel channel) {
				return null;
			}
		});
	}

	public SettingsProxy(IChannel channel) {
		this.channel = channel;

	}

	public String getName() {
		return NAME;
	}

	/**
	 * @since 2.0
	 */
	public IToken getIds(final DoneGetSettingIds done) {
		return new Command(channel, SettingsProxy.this, "getIds", new Object[] {}) {
			@SuppressWarnings("unchecked")
			@Override
			public void done(Exception error, Object[] args) {
				Collection<String> idStrings;
				if (args != null && args[1] != null)
					idStrings = (Collection<String>) args[1];
				else
					idStrings = Collections.emptyList();
				done.doneGetSettingIds(token, error, idStrings.toArray(new String[idStrings.size()]));
			}
		}.token;
	}
	
	/**
	 * @since 2.0
	 */
	public IToken setValues(String context, String[] ids, Object[] values, final DoneSetSettingValues done) {
		return new Command(channel, SettingsProxy.this, "set", new Object[] { context, ids, values }) {
			@Override
			public void done(Exception error, Object[] args) {
				 if (error == null) {
					 assert args.length == 1;
					 if (args[0] != null)
						 error = new IOException(args[0].toString());
					 else
						 error = null;
                }
				done.doneSetSettingValues(token, error);
			}
		}.token;
	}
}

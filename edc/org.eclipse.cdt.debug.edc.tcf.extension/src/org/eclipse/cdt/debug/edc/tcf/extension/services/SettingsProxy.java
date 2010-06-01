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
package org.eclipse.cdt.debug.edc.tcf.extension.services;

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

	public IToken getSupportedSettings(final DoneGetSettingValues done) {
		return new Command(channel, SettingsProxy.this, "get", new Object[] {}) {
			@SuppressWarnings("unchecked")
			@Override
			public void done(Exception error, Object[] args) {
				Collection<String> idStrings;
				if (args[1] != null)
					idStrings = (Collection<String>) args[1];
				else
					idStrings = Collections.emptyList();
				done.doneGetSettingValues(token, error, idStrings.toArray(new String[idStrings.size()]));
			}
		}.token;
	}

	public IToken setValues(String context, String[] ids, Object[] values) {
		return new Command(channel, SettingsProxy.this, "set", new Object[] { context, ids, values }) {
			@Override
			public void done(Exception error, Object[] args) {
			}
		}.token;
	}

	// Unused. Remove? [11-17-09]
//	@SuppressWarnings("unchecked")
//	private String[] toStringArray(Object o) {
//		if (o != null && o instanceof Collection) {
//			Collection<Object> objs = (Collection<Object>) o;
//			Collection<String> strings = new ArrayList<String>();
//			for (Object object : objs) {
//				strings.add(object.toString());
//			}
//			return strings.toArray(new String[strings.size()]);
//		}
//		return null;
//	}

}

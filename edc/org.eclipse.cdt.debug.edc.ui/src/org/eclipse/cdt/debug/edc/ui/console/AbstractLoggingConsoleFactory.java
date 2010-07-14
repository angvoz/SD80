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

package org.eclipse.cdt.debug.edc.ui.console;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.MessageConsole;

public abstract class AbstractLoggingConsoleFactory implements IConsoleFactory {

	private static Map<String, IChannel> channelMappings = new HashMap<String, IChannel>();

	protected abstract String getConsoleType();

	protected abstract String getLogId();

	protected abstract String getConsoleTitle();

	/**
	 * Call this during launch sequence to initialize channel for factory -
	 * allows users to open a console as needed
	 * 
	 * @param consoleType
	 *            String
	 * @param logId
	 *            String
	 * @param channel
	 *            IChannel
	 */
	public static void setChannel(String consoleType, String logId, IChannel channel) {
		String key = createKey(consoleType, logId);
		IChannel existing = channelMappings.get(key);
		if (existing != null) {
			ConsoleLogManager.removeManagersForChannel(existing);
		}
		channelMappings.put(key, channel);
	}

	/**
	 * Call this during launch sequence to initialize channel for factory -
	 * opens a console
	 * 
	 * @param consoleType
	 *            String
	 * @param logId
	 *            String
	 * @param channel
	 *            IChannel
	 * @param clearConsole
	 *            boolean
	 */
	public static void openConsole(String consoleType, String consoleTitle, String logId, IChannel channel,
			boolean clearConsole) {
		if (channel != null)
			setChannel(consoleType, logId, channel);
		// create the manager
		ConsoleLogManager manager = null;
		if (channel != null) {
			manager = ConsoleLogManager.findExisting(consoleType, logId, channel);
			if (manager == null) {
				manager = ConsoleLogManager.create(consoleType, logId, channel);
			}
		}

		MessageConsole console = findConsole(consoleType);
		if (console == null) {
			// create a new console
			console = new MessageConsole(consoleTitle, consoleType, null, true);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { console });
		} else if (manager != null) {
			if (clearConsole)
				console.clearConsole();
			// add existing console
			manager.addConsole(console);
		}

		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(console);
	}

	public void openConsole() {
		openConsole(getConsoleType(), getConsoleTitle(), getLogId(), getChannel(createKey()), false);
	}

	private String createKey() {
		return createKey(getConsoleType(), getLogId());
	}

	private static String createKey(String consoleType, String logId) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(consoleType);
		stringBuilder.append("|");
		stringBuilder.append(logId);
		return stringBuilder.toString();
	}

	private static IChannel getChannel(String key) {
		return channelMappings.get(key);
	}

	private static MessageConsole findConsole(String consoleType) {
		IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
		for (IConsole c : consoles) {
			if (consoleType.equals(c.getType())) {
				return (MessageConsole) c;
			}
		}
		return null;
	}

}
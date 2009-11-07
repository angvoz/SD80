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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.tcf.extension.services.ILogging;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ILogging.DoneAddListener;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ILogging.DoneRemoveListener;
import org.eclipse.cdt.debug.edc.tcf.extension.services.ILogging.LogListener;
import org.eclipse.tm.tcf.core.AbstractChannel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleListener;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * A class that manages connecting an ILogging with an IConsole
 */
public class ConsoleLogManager implements LogListener {

	/**
	 * List of created managers
	 */
	private static List<ConsoleLogManager> managers;

	/**
	 * The IChannel
	 */
	private IChannel channel;

	/**
	 * The managed consoles
	 */
	private Map<MessageConsole, MessageConsoleStream> consoleStreamMappings;

	/**
	 * The identifier of the log
	 */
	private final String logId;

	/**
	 * The type id of the managed consoles
	 */
	private final String consoleType;

	/**
	 * Create a new manager to manager a console type with a specific ILogging
	 * service log id and channel
	 * 
	 * @param consoleType
	 *            String
	 * @param logId
	 *            String
	 * @param channel
	 *            IChannel
	 */
	public static ConsoleLogManager create(String consoleType, String logId, IChannel channel) {
		if (managers == null)
			managers = new ArrayList<ConsoleLogManager>();

		ConsoleLogManager consoleStreamManager = new ConsoleLogManager(consoleType, logId, channel);
		managers.add(consoleStreamManager);
		return consoleStreamManager;
	}

	public static ConsoleLogManager findExisting(String consoleType, String logId, IChannel channel) {
		if (managers != null) {
			for (ConsoleLogManager consoleLogManager : managers) {
				if (consoleLogManager.consoleType.equals(consoleType) && consoleLogManager.logId.equals(logId)
						&& consoleLogManager.channel.equals(channel))
					return consoleLogManager;
			}
		}
		return null;
	}

	private ConsoleLogManager(final String consoleType, String logId, IChannel channel) {
		this.consoleType = consoleType;
		this.logId = logId;
		this.channel = channel;
		hookChannel(channel);

		consoleStreamMappings = new HashMap<MessageConsole, MessageConsoleStream>();

		ConsolePlugin.getDefault().getConsoleManager().addConsoleListener(new IConsoleListener() {
			public void consolesRemoved(IConsole[] consoles) {
				for (IConsole console : consoles) {
					if (console instanceof MessageConsole) {
						removeConsole((MessageConsole) console);
					}
				}
			}

			public void consolesAdded(IConsole[] consoles) {
				for (IConsole console : consoles) {
					String type = console.getType();
					if (type != null && type.equals(consoleType) && console instanceof MessageConsole) {
						addConsole((MessageConsole) console);
					}
				}
			}
		});
	}

	private void hookChannel(final IChannel channel) {
		Protocol.invokeLater(new Runnable() {
			public void run() {
				channel.addChannelListener(new IChannelListener() {

					public void onChannelOpened() {
					}

					public void onChannelClosed(Throwable error) {
						managers.remove(ConsoleLogManager.this);
					}

					public void congestionLevel(int level) {
					}
				});
			}
		});
	}

	public void removeConsole(MessageConsole console) {
		if (consoleStreamMappings.remove(console) != null && consoleStreamMappings.isEmpty()) {
			if (isChannelOpen()) {
				TCFTask<Object> task = new TCFTask<Object>() {
					public void run() {
						ILogging logging = ConsoleLogManager.this.channel.getRemoteService(ILogging.class);
						assert logging != null;
						logging.removeListener(logId, ConsoleLogManager.this, new DoneRemoveListener() {
							public void doneRemoveListener(IToken token, Exception error) {
								if (error == null)
									done(this);
								else
									error(error);
							}
						});
					}
				};
				task.getE();
			}
		}
	}

	public void addConsole(MessageConsole console) {
		if (consoleStreamMappings.isEmpty()) {
			if (isChannelOpen()) {
				TCFTask<Object> task = new TCFTask<Object>() {
					public void run() {
						ILogging logging = ConsoleLogManager.this.channel.getRemoteService(ILogging.class);
						assert logging != null;
						logging.addListener(logId, ConsoleLogManager.this, new DoneAddListener() {
							public void doneAddListener(IToken token, Exception error) {
								if (error == null)
									done(this);
								else
									error(error);
							}
						});
					}
				};
				task.getE();
			}
		}
		consoleStreamMappings.put(console, console.newMessageStream());
	}

	private boolean isChannelOpen() {
		return (((AbstractChannel) channel).getState() == IChannel.STATE_OPEN);
	}

	public void appendText(final MessageConsole console, final String text, boolean eol) {
		MessageConsoleStream stream = consoleStreamMappings.get(console);
		if (stream.isClosed()) {
			stream = console.newMessageStream();
			consoleStreamMappings.put(console, stream);
		}
		if (eol)
			stream.println(text);
		else
			stream.print(text);
	}

	public void write(String msg) {
		for (MessageConsole console : consoleStreamMappings.keySet()) {
			appendText(console, msg, false);
		}
	}

	public void writeln(String msg) {
		for (MessageConsole console : consoleStreamMappings.keySet()) {
			appendText(console, msg, true);
		}
	}
}

/**
* Copyright (c) 2009 Nokia Corporation and/or its subsidiary(-ies).
* All rights reserved.
* This component and the accompanying materials are made available
* under the terms of the License "Eclipse Public License v1.0"
* which accompanies this distribution, and is available
* at the URL "http://www.eclipse.org/legal/epl-v10.html".
*
* Initial Contributors:
* Nokia Corporation - initial contribution.
*
* Contributors:
*
* Description: 
*
*/


package org.eclipse.cdt.debug.edc.tcf.extension.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tm.tcf.core.Command;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.IChannel.IEventListener;

public class LoggingProxy implements ILogging {
	
    private final class ChannelEventListener implements IEventListener {
        public void event(String name, byte[] data) {
            try {
                Object[] args = JSON.parseSequence(data);
                if (name.equals("write")) {
                    assert args.length == 2;
					write((String)args[0], (String)args[1]);
                }
                else if (name.equals("writeln")) {
                    assert args.length == 2;
					writeln((String)args[0], (String)args[1]);
				}
				else if (name.equals("dialog")) {
					assert args.length == 4;
					dialog((String)args[0], (Integer)args[1], (String)args[2], (String)args[3]);
				}
                else {
                    throw new IOException("Logging service: unknown event: " + name);
                }
            }
            catch (Throwable x) {
                channel.terminate(x);
            }
        }
	}

	private final IChannel channel;
	private Map<String, List<LogListener>> mapIdToListeners;
	private IEventListener eventListener;

	public LoggingProxy(IChannel channel) {
		this.channel = channel;
	}

	public IToken addListener(final String id, final LogListener listener, final DoneAddListener done) {
        return new Command(channel, this, "addListener", new Object[]{ id }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                if (error == null) {
                	addListener(id, listener);
                }
                done.doneAddListener(token, error);
            }
        }.token;
	}

	public IToken removeListener(final String id, final LogListener listener, final DoneRemoveListener done) {
        return new Command(channel, this, "removeListener", new Object[]{ id }) {
            @Override
            public void done(Exception error, Object[] args) {
                if (error == null) {
                    assert args.length == 1;
                    error = toError(args[0]);
                }
                if (error == null) {
                	removeListener(id, listener);
                }
                done.doneRemoveListener(token, error);
            }
        }.token;
	}
	
	private void addListener(String id, LogListener listener) {
		if (mapIdToListeners == null)
			mapIdToListeners = new HashMap<String, List<LogListener>>();
		
		List<LogListener> listeners = mapIdToListeners.get(id);
		if (listeners == null) {
			listeners = new ArrayList<LogListener>();
			mapIdToListeners.put(id, listeners);
		}
		
		if (!listeners.contains(listener))
			listeners.add(listener);
		
		if (eventListener == null) {
			eventListener = new ChannelEventListener();
			channel.addEventListener(this, eventListener);
		}
	}
	
	private void removeListener(String id, LogListener listener) {
		if (mapIdToListeners != null) {
			List<LogListener> listeners = mapIdToListeners.get(id);
			if (listeners != null) {
				listeners.remove(listener);
				if (listeners.isEmpty()) {
					mapIdToListeners.remove(id);
					if (mapIdToListeners.isEmpty()) {
						mapIdToListeners = null;
					}
				}
			}
		}
		if (mapIdToListeners == null && eventListener != null) {
			channel.removeEventListener(this, eventListener);
			eventListener = null;
		}
	}
	
	private Collection<LogListener> getListeners(String id) {
		if (mapIdToListeners != null) {
			List<LogListener> listeners = mapIdToListeners.get(id);
			if (listeners != null)
				return listeners;
		}
		
		return Collections.emptyList();
	}

	
	private LogListener getFirstListener(String id) {
		if (mapIdToListeners != null)
		{
			List<LogListener> listeners = mapIdToListeners.get(id);
			if (listeners != null) {
				return listeners.get(0);
			}
		}

		return null;
	}

	public void write(String id, String msg) {
		for (LogListener listener : getListeners(id)) {
			listener.write(msg);
		}
	}

	public void writeln(String id, String msg) {
		for (LogListener listener : getListeners(id)) {
			listener.writeln(msg);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.tcf.extension.services.ILogging#dialog(String, int, String, String)
	 */
	/** @since 2.0 */
	public void dialog(String id, int severity, String summary, String details) {
		getFirstListener(id).dialog(severity, summary, details);
	}

	public String getName() {
		return NAME;
	}

}

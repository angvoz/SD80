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
package org.eclipse.cdt.debug.edc.tcf.extension;

import java.util.LinkedList;

import org.eclipse.tm.tcf.protocol.IEventQueue;

/**
 * This is supposed to be used in a stand-alone TCF agent written in Java.
 * 
 */
public class SimpleEventQueue extends Thread implements IEventQueue {

	private final LinkedList<Runnable> queue = new LinkedList<Runnable>();

	public SimpleEventQueue() {
		setName("TCF Event Dispatch");
		start();
	}

	@Override
	public void run() {
		try {
			while (true) {
				Runnable r = null;
				synchronized (this) {
					while (queue.isEmpty())
						wait();
					r = queue.removeFirst();
				}
				try {
					r.run();
				} catch (Throwable x) {
					System.err.println("Error dispatching TCF event:");
					x.printStackTrace();
				}
			}
		} catch (InterruptedException x) {
			System.exit(1);
		} catch (Throwable x) {
			x.printStackTrace();
			System.exit(1);
		}
	}

	public synchronized int getCongestion() {
		int n = queue.size() - 100;
		if (n > 100)
			n = 100;
		return n;
	}

	public synchronized void invokeLater(Runnable runnable) {
		queue.add(runnable);
		notify();
	}

	public boolean isDispatchThread() {
		return Thread.currentThread() == this;
	}
}

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
package org.eclipse.cdt.debug.edc.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WaitForResult<V> implements Future<V> {

	public static final long DEFAULT_WAIT_TIMEOUT_SECONDS = 10;
	public static final long WAIT_INTERVAL_MILLIS = 50;
	private boolean running;
	private boolean canceled;
	private boolean done;
	private V data;
	private Throwable exception;

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#cancel(boolean)
	 */
	public boolean cancel(boolean mayInterruptIfRunning) {
		if (!running)
			return false;
		else {
			canceled = true;
			return true;
		}
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#get()
	 */
	public V get() throws InterruptedException, ExecutionException {
		try {
			return get(DEFAULT_WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			throw new ExecutionException(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
	 */
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

		long limitMillis = System.currentTimeMillis() + unit.toMillis(timeout);
		running = true;
		while (!canceled && (exception == null) && !hasResult()) {
			Thread.sleep(WAIT_INTERVAL_MILLIS);
			if (System.currentTimeMillis() > limitMillis)
				throw new TimeoutException();
		}
		done = true;
		running = false;

		if (exception != null)
			throw new ExecutionException(exception);

		return data;
	}

	public void setData(V data) {
		this.data = data;
	}

	public V getData() {
		return data;
	}

	public boolean hasResult() {
		return getData() != null;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#isCancelled()
	 */
	public boolean isCancelled() {
		return canceled;
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Future#isDone()
	 */
	public boolean isDone() {
		return done;
	}

	public void handleException(Throwable e) {
		this.exception = e;
	}
}

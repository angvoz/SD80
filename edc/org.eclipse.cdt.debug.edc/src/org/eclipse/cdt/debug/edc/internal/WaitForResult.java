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
package org.eclipse.cdt.debug.edc.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WaitForResult<V> implements Future<V> {

	public static final long DEFAULT_WAIT_TIMEOUT = 10; // seconds
	public static final long DEFAULT_WAIT_INTERVAL = 2; // seconds
	private boolean running;
	private boolean canceled;
	private boolean done;
	private V data;
	private Throwable exception;

	public boolean cancel(boolean arg0) {
		if (!running)
			return false;
		else {
			canceled = true;
			return true;
		}
	}

	public V get() throws InterruptedException, ExecutionException {
		try {
			return get(DEFAULT_WAIT_TIMEOUT, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			throw new ExecutionException(e);
		}
	}

	public V get(long arg0, TimeUnit arg1) throws InterruptedException, ExecutionException, TimeoutException {

		long limit = System.currentTimeMillis() + arg1.toMillis(arg0);
		running = true;
		while (!canceled && (exception == null) && !hasResult()) {
			Thread.sleep(DEFAULT_WAIT_INTERVAL * 1000);
			if (System.currentTimeMillis() > limit)
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

	public boolean isCancelled() {
		return canceled;
	}

	public boolean isDone() {
		return done;
	}

	public void handleException(Throwable e) {
		this.exception = e;
	}

}

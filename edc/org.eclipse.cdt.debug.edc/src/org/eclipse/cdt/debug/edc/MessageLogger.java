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
package org.eclipse.cdt.debug.edc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * This class performs error logging. The error can be logged, shown or both. It
 * is important to log errors to clearly identify the plug-in as the source of
 * problems.
 * <p>
 * The goals of the logging and the showing are different:
 * <p>
 * A logged message should retain enough information so it's not useless, while
 * not having empty top-level messages or unnecessary levels of nesting.
 * <p>
 * A reported message, which is meant to be more user-friendly, does not show a
 * stack trace or exception names, so it should not expose the StatusDialog's
 * weaknesses of reporting empty messages, empty details, or details which are
 * the same as the main message.
 */
public abstract class MessageLogger {

	public static final String MISSING_MESSAGE_PLACEHOLDER = "Internal error";
	public static final String MULTIPLE_MESSAGE_PLACEHOLDER = "Multiple problems have occurred";

	public interface Listener {
		void statusLogged(IStatus status);
	}

	private static List<Listener> listeners = new ArrayList<Listener>();

	public MessageLogger() {
	}

	/**
	 * Add listener to logged errors for all instances of ErrorLogger.
	 * 
	 * @param listener
	 *            listener, duplicates ignored
	 */
	public static synchronized void addListener(Listener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	/**
	 * Remove listener for logged errors for all instances of ErrorLogger.
	 * 
	 * @param listener
	 *            listener,missing ignored
	 */
	public static synchronized void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	protected static void fireListener(IStatus status) {
		Listener[] array;
		synchronized (listeners) {
			array = listeners.toArray(new Listener[listeners.size()]);
		}
		for (Listener listener : array) {
			listener.statusLogged(status);
		}
	}

	/**
	 * Log a finalized status message.
	 * 
	 * @param status
	 */
	protected void doLog(IStatus status) {
		if (getPlugin() != null) {
			getPlugin().getLog().log(status);
		} else {
			System.err.println(status.toString());
			if (status.getException() != null)
				status.getException().printStackTrace(System.err);
		}

		fireListener(status);
	}

	/**
	 * Create a status with as much information as possible at the top level
	 * (IStatus#getMessage), reading the message from the exception or a nested
	 * exception if possible. Favor a CoreException's IStatus if the mainMessage
	 * is null.
	 * 
	 * @param severity
	 * @param mainMessage
	 * @param exception
	 * @return new or existing IStatus
	 * @since 2.0
	 */
	public IStatus createStatus(int severity, String mainMessage, Throwable exception) {
		String pluginID = getPluginID();
		IStatus status;

		// use an available status if possible
		if (isEmpty(mainMessage) && exception instanceof CoreException) {
			status = ((CoreException) exception).getStatus();
			exception = null;

			// the CoreException message is just the status message
			if (!isEmpty(status.getMessage())) {
				if (!isRealMultiStatus(status))
					return status;
				mainMessage = MULTIPLE_MESSAGE_PLACEHOLDER;
			}

			if (status.getException() != null && status.getException().getMessage() != null) {
				// make a new status -- the current one isn't very useful
				mainMessage = status.getException().getMessage();
				exception = status.getException();
			} else {
				return status;
			}
		}

		// peek to see if the exception is a simple wrapper
		if (isEmpty(mainMessage)) {
			Throwable base = exception;
			while (base != null && isEmpty(base.getMessage()))
				base = base.getCause();
			if (base != null && !isEmpty(base.getMessage()))
				mainMessage = base.getMessage();
			else
				mainMessage = MISSING_MESSAGE_PLACEHOLDER;
		}

		status = new Status(severity, pluginID, mainMessage, exception);
		return status;
	}

	private static boolean isEmpty(String text) {
		return text == null || text.length() == 0;
	}

	/**
	 * Log a status message.
	 * 
	 * @param status
	 */
	public void log(IStatus status) {
		if (status.getSeverity() == IStatus.CANCEL)
			return;
		if (isEmpty(status.getMessage()) && !isRealMultiStatus(status))
			status = createStatus(status.getSeverity(), null, status.getException());
		doLog(status);
	}

	/**
	 * @param status
	 * @return
	 */
	private boolean isRealMultiStatus(IStatus status) {
		return status.isMultiStatus() && ((MultiStatus) status).getChildren().length > 0;
	}

	/**
	 * Log a message
	 * 
	 * @param severity
	 *            one of {@value IStatus#ERROR} {@value IStatus#WARNING}
	 *            {@value IStatus#INFO}
	 * @param mainMessage
	 *            the error message (may be <code>null</code> to use the
	 *            exception message)
	 * @param exception
	 *            the exception caused by the error (may be <code>null</code>)
	 */
	public void log(int severity, String mainMessage, Throwable exception) {
		IStatus status = createStatus(severity, mainMessage, exception);
		log(status);
	}

	/**
	 * Log an error.
	 * 
	 * @param mainMessage
	 *            the error message (may be <code>null</code> to use the
	 *            exception message)
	 * @param exception
	 *            the exception caused by the error (may be <code>null</code>)
	 */
	public void logError(String mainMessage, Throwable t) {
		log(IStatus.ERROR, mainMessage, t);
	}

	public abstract Plugin getPlugin();

	public abstract String getPluginID();

}

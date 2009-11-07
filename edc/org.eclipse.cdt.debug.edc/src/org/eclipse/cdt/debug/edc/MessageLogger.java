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
package org.eclipse.cdt.debug.edc;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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
	 */
	private IStatus createStatus(int severity, String mainMessage, Throwable exception) {
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

	/**
	 * Show a status message.
	 * 
	 * @param mainMessage
	 *            the main message
	 * @param status_
	 *            the status with more detail
	 */
	protected void doShow(String mainMessage_, IStatus status_) {
		if (status_ == null && mainMessage_ == null)
			return;

		// the ErrorDialog will just repeat the status
		// fill in a main message if one won't otherwise show up
		if (isEmpty(mainMessage_)) {
			if (isEmpty(status_.getMessage())) {
				if (!status_.isMultiStatus()) {
					Throwable t = status_.getException();
					while (t != null && isEmpty(t.getMessage())) {
						t = t.getCause();
					}
					if (t != null && !isEmpty(t.getMessage())) {
						mainMessage_ = t.getMessage();
						// since we've descended into the status, and the dialog
						// doesn't show anything
						// useful about exception traces, restart here
						if (t.getCause() == null)
							status_ = null;
						else
							status_ = new Status(IStatus.ERROR, getPluginID(), null, t.getCause());
					}
				} else {
					mainMessage_ = MULTIPLE_MESSAGE_PLACEHOLDER;
				}
			}

			// ignore status if it just repeats the main message
			if (status_ != null && mainMessage_ != null && mainMessage_.equals(status_.getMessage())
					&& !status_.isMultiStatus()) {
				status_ = null;
			}
		}

		Shell shell_;
		try {
			shell_ = getSafeShell();
		} catch (SWTException e) {
			shell_ = null;
		}
		final Shell shell = shell_;
		final String mainMessage = mainMessage_;
		final IStatus status = status_;

		Runnable runnable = new Runnable() {
			public void run() {
				String title = "Error";
				if (!status.matches(IStatus.ERROR))
					title = "Information";
				ErrorDialog.openError(shell, title, mainMessage, status, -1);
			}
		};

		if (Display.getCurrent() != null) {
			runnable.run();
		} else {
			Display.getDefault().syncExec(runnable);
		}

	}

	/**
	 * Get a shell that is preferably the active workbench window, but pick some
	 * other workbench shell or even a random SWT shell if none is active. Fail
	 * if no shells are available.
	 * 
	 * @return shell, never null
	 * @throws IllegalStateException
	 *             if no shells visible
	 */
	public static Shell getSafeShell() {
		Shell shell = getActiveShell();
		if (shell != null)
			return shell;

		try {
			IWorkbench workbench = PlatformUI.getWorkbench();
			IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
			if (activeWorkbenchWindow != null) {
				shell = activeWorkbenchWindow.getShell();
			}
			if (shell == null) {
				for (IWorkbenchWindow window : workbench.getWorkbenchWindows()) {
					shell = window.getShell();
					if (shell != null)
						return shell;
				}
			}
		} catch (IllegalStateException e) {
			// platform not running
		}

		// resort to SWT
		final Shell[] shellEntry = { null };

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Shell[] shells = PlatformUI.getWorkbench().getDisplay().getShells();
				if (shells.length > 0) {
					shellEntry[0] = shells[0];
				}
			}
		});
		shell = shellEntry[0];

		return shell;
	}

	/**
	 * Get the active workbench shell. This can return null if the shell is in
	 * the background!
	 * 
	 * @return current foreground workbench window shell
	 */
	public static Shell getActiveShell() {
		if (!Platform.isRunning())
			return null;

		final Shell[] shell = { null };

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				final IWorkbench workbench;
				try {
					workbench = PlatformUI.getWorkbench();
				} catch (IllegalStateException e) {
					return;
				}

				final IWorkbenchWindow[] activeWorkbenchWindow = { null };
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						activeWorkbenchWindow[0] = workbench.getActiveWorkbenchWindow();
					}
				});
				if (activeWorkbenchWindow[0] == null)
					return;

				shell[0] = activeWorkbenchWindow[0].getShell();

			}
		});

		return shell[0];
	}

	/**
	 * Show a status message.
	 * 
	 * @param status
	 *            the status
	 */
	public void show(IStatus status) {
		// the status dialog will not provide any useful information if we just
		// wrap an exception in an IStatus, so go down a level if possible.
		String mainMessage;
		Throwable exception;
		if (isRealMultiStatus(status)) {
			showMessage(status.getSeverity(), MULTIPLE_MESSAGE_PLACEHOLDER, new CoreException(status));
		} else {
			if (!isEmpty(status.getMessage())) {
				mainMessage = status.getMessage();
				exception = status.getException();
			} else {
				mainMessage = status.getMessage();
				exception = status.getException();
			}

			showMessage(status.getSeverity(), mainMessage, exception);
		}
	}

	/**
	 * Show a message.
	 * 
	 * @param mainMessage
	 *            the message.
	 * @param exception
	 *            the exception caused by the error.
	 */
	public void showMessage(int severity, String mainMessage, Throwable exception) {
		IStatus status = null;

		// the status dialog will not provide any useful information if we just
		// wrap an exception in an IStatus, so go down a level if possible.
		if (isEmpty(mainMessage) && exception != null) {
			if (exception instanceof CoreException) {
				status = ((CoreException) exception).getStatus();
				if (!status.isMultiStatus()) {
					if (!isEmpty(status.getMessage())) {
						mainMessage = status.getMessage();
						exception = status.getException();
						status = null;
					} else {
						if (status.getException() != null) {
							exception = status.getException().getCause();
							mainMessage = status.getException().getMessage();
							status = null;
						}
					}
				}
			} else {
				if (!isEmpty(exception.getMessage())) {
					mainMessage = exception.getMessage();
					exception = exception.getCause();
					status = null;
				}
			}
		} else {
			// flatten a status reporting just an exception
			if (exception instanceof CoreException) {
				status = ((CoreException) exception).getStatus();
				if (!isRealMultiStatus(status)) {
					if (isEmpty(status.getMessage())) {
						if (status.getException() != null) {
							exception = status.getException();
							status = null;
						}
					}
				}
			}
		}

		if (status == null)
			status = createStatus(severity, mainMessage, exception);

		doShow(null, status);
	}

	/**
	 * Show an error.
	 * 
	 * @param mainMessage
	 *            the error message.
	 * @param exception
	 *            the exception caused by the error.
	 */
	public void showError(String mainMessage, Throwable exception) {
		showMessage(IStatus.ERROR, mainMessage, exception);
	}

	/**
	 * Convenience method for log and show a status message.
	 * 
	 * @param status
	 *            status to report
	 */
	public void logAndShow(IStatus status) {
		if (status.getSeverity() == IStatus.CANCEL)
			return;

		this.log(status);
		this.show(status);
	}

	/**
	 * Convenience method for log and show an error.
	 * 
	 * @param mainMessage
	 *            the error message (may be <code>null</code> to use the
	 *            exception message)
	 * @param exception
	 *            the exception caused by the error (may be <code>null</code>)
	 */
	public void logAndShowError(String mainMessage, Throwable exception) {
		this.logError(mainMessage, exception);
		this.showError(mainMessage, exception);
	}

	public abstract Plugin getPlugin();

	public abstract String getPluginID();

}

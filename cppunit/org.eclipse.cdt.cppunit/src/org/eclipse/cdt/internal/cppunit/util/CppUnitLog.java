
package org.eclipse.cdt.internal.cppunit.util;

import org.eclipse.cdt.internal.cppunit.ui.CppUnitPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

public class CppUnitLog {
	private static ILog pluginLog = null;
	private static final String CPPUNIT_PREFIX="CppUnit:"; //NON-NLS-1$
	
	private static String deprecated(String message) {
		return message;
	}
	private static String format(int code, String message, int severity) {
		StringBuffer buf = new StringBuffer(10 + message.length());
		buf.append(CPPUNIT_PREFIX);
		buf.append(code / 1000);
		code = code % 1000;
		buf.append(code / 100);
		code = code % 100;
		buf.append(code / 10);
		code = code % 10;
		buf.append(code);
		switch(severity) {
			case IStatus.ERROR: buf.append('S'); break;
			case IStatus.WARNING: buf.append('W'); break;
			default: buf.append('I'); break;
		}
		buf.append(' ');
		buf.append(message);
		return buf.toString();
	}
	
	/**
	 * Log an error.
	 * @param plugin The plug-in that logs the error.
	 * @param message The error message.
	 * @deprecated
	 */
	public static void error(String message) {
		logStatus(-1, deprecated(message), null, IStatus.ERROR);
	}
	
	/**
	 * Log an error.
	 * @param plugin The plug-in that logs the error.
	 * @param message The error message.
	 */
	public static void error(int code, String message) {
		logStatus(code, message, null, IStatus.ERROR);
	}
	
	/**
	 * Log an error caused by an exception.
	 * @param plugin The plug-in that logs the error.
	 * @param message The error message.
	 * @param exception The exception that caused the error.
	 */
	public static void error(String message, Throwable exception) {
		logStatus(-1, deprecated(message), exception, IStatus.ERROR);
	}
		
	/**
	 * Log an error caused by an exception.
	 * @param plugin The plug-in that logs the error.
	 * @param message The error message.
	 * @param exception The exception that caused the error.
	 */
	public static void error(int code, String message, Throwable exception) {
		logStatus(code, message, exception, IStatus.ERROR);
	}
	
	/**
	 * Log a warning.
	 * @param plugin The plug-in that logs the warning.
	 * @param message The warning message.
	 * @deprecated
	 */
	public static void warning(String message) {
		logStatus(-1, deprecated(message), null, IStatus.WARNING);
	}
	
	/**
	 * Log a warning.
	 * @param plugin The plug-in that logs the warning.
	 * @param message The warning message.
	 */
	public static void warning(int code, String message) {
		logStatus(code, message, null, IStatus.WARNING);
	}
	
	/**
	 * Log a warning caused by an exception.
	 * @param plugin The plug-in that logs the warning.
	 * @param message The warning message.
	 * @param exception The exception that caused the warning.
	 * @deprecated
	 */
	public static void warning(String message, Throwable exception) {
		logStatus(-1, deprecated(message), exception, IStatus.WARNING);
	}
	
	/**
	 * Log a warning caused by an exception.
	 * @param plugin The plug-in that logs the warning.
	 * @param message The warning message.
	 * @param exception The exception that caused the warning.
	 */
	public static void warning(int code, String message, Throwable exception) {
		logStatus(code, message, exception, IStatus.WARNING);
	}
	
	/**
	 * Log a information message.
	 * @param plugin The plug-in that logs the information message.
	 * @param message The information message.
	 * @deprecated
	 */
	public static void info(String message) {
		logStatus(-1, deprecated(message), null, IStatus.INFO);
	}
	
	/**
	 * Log a information message.
	 * @param plugin The plug-in that logs the information message.
	 * @param message The information message.
	 */
	public static void info(int code, String message) {
		logStatus(code, message, null, IStatus.INFO);
	}
	
	/**
	 * Log a information message caused by an exception.
	 * @param plugin The plug-in that logs the information message.
	 * @param message The information message.
	 * @deprecated
	 */
	public static void info(String message, Throwable exception) {
		logStatus(-1, deprecated(message), exception, IStatus.INFO);
	}
	
	/**
	 * Log a information message caused by an exception.
	 * @param plugin The plug-in that logs the information message.
	 * @param message The information message.
	 */
	public static void info(int code, String message, Throwable exception) {
		logStatus(code, message, exception, IStatus.INFO);
	}
	
	/**
	 * Throw a CoreException with the specified details.
	 * @param plugin The plugin originating the exception.
	 * @param message The description of the problem.
	 * @throws CoreException
	 */
	public static void throwException(Plugin plugin, String message) throws CoreException {
		IStatus status = new Status(
			IStatus.ERROR,
			plugin.getBundle().getSymbolicName(),
			IStatus.OK,
			message != null ? message : "(no message)", //$NON-NLS-1$
			null);
		throw new CoreException(status);
	}
	
	/**
	 * Throw a CoreException with the specified details.
	 * @param plugin The plugin originating the exception.
	 * @param message The description of the problem.
	 * @param exception The nested exception that caused the problem.
	 * @throws CoreException
	 */
	public static void throwException(Plugin plugin, String message, Throwable exception) throws CoreException {
		IStatus status = new Status(
			IStatus.ERROR,
			plugin.getBundle().getSymbolicName(),
			IStatus.OK,
			message != null ? message : "(no message)", //$NON-NLS-1$
			exception);
		throw new CoreException(status);
	}
	
	/**
	 * Throw a CoreException with the specified status.
	 * @param status The status describing the problem. 
	 * @throws CoreException
	 */
	public static void throwException(IStatus status) throws CoreException {
		throw new CoreException(status);
	}
	
	private static void logStatus(int code, String message, Throwable exception, int severity) {
		String formattedMessage = format(code, message, severity);
		IStatus status = new Status(severity, CppUnitPlugin.getDefault().getBundle().getSymbolicName(), code, formattedMessage, exception);
		logStatus(status);
	}
	
	/**
	 * Log a status.
	 * 
	 * @param plugin
	 *            The plugin that is logging the status.
	 * @param status
	 *            The status. Note that the status may refer to another plug-in
	 *            than the plug-in that invokes this method.
	 */
	public static void logStatus(IStatus status) {
		if (pluginLog == null) {
			pluginLog = CppUnitPlugin.getDefault().getLog();
		}
		pluginLog.log(status);
	}	
}

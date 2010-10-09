package org.eclipse.cdt.internal.core.language.settings.providers;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class LanguageSettingsLogger {

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logInfo(String msg) {
		Exception e = new Exception(msg);
		IStatus status = new Status(IStatus.INFO, CCorePlugin.PLUGIN_ID, msg, e);
		CCorePlugin.log(status);
	}

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logWarning(String msg) {
		Exception e = new Exception(msg);
		IStatus status = new Status(IStatus.WARNING, CCorePlugin.PLUGIN_ID, msg, e);
		CCorePlugin.log(status);
	}

	// AG FIXME
	/**
	 * @param msg
	 * @noreference This method is not intended to be referenced by clients.
	 */
	@Deprecated
	public static void logError(String msg) {
		Exception e = new Exception(msg);
		IStatus status = new Status(IStatus.ERROR, CCorePlugin.PLUGIN_ID, msg, e);
		CCorePlugin.log(status);
	}
}
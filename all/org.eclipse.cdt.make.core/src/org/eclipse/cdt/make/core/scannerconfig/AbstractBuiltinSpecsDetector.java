/*******************************************************************************
 * Copyright (c) 2009, 2009 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.core.scannerconfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCMarkerGenerator;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;

public abstract class AbstractBuiltinSpecsDetector extends LanguageSettingsSerializable implements
		ILanguageSettingsOutputScanner {
	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String PLUGIN_CDT_MAKE_UI_ID = "org.eclipse.cdt.make.ui"; //$NON-NLS-1$
	private static final String GMAKE_ERROR_PARSER_ID = "org.eclipse.cdt.core.GmakeErrorParser"; //$NON-NLS-1$
	private static final String PATH_ENV = "PATH"; //$NON-NLS-1$

	// temporaries which are reassigned before running
	private ICConfigurationDescription currentCfgDescription = null;
	private IProject currentProject = null;
	private String currentLanguageId = null;
	private String currentCommandResolved = null;
	protected List<ICLanguageSettingEntry> detectedSettingEntries = null;

	private boolean runOnce = true;
	private boolean isConsoleEnabled = false;


	@Override
	public void configureProvider(String id, String name, List<String> languages, List<ICLanguageSettingEntry> entries, String customParameter) {
		super.configureProvider(id, name, languages, entries, customParameter);

		runOnce = true;
	}


	protected void setResolvedCommand(String command) {
		this.currentCommandResolved = command;
	}

	protected String getResolvedCommand() {
		return currentCommandResolved;
	}

	public void setRunOnce(boolean once) {
		runOnce = once;
	}

	public boolean isRunOnce() {
		return runOnce;
	}
	
	public void setConsoleEnabled(boolean enable) {
		isConsoleEnabled = enable;
	}

	public boolean isConsoleEnabled() {
		return isConsoleEnabled;
	}
	
	public void startup(ICConfigurationDescription cfgDescription, String languageId) throws CoreException {
		currentCfgDescription = cfgDescription;
		currentLanguageId = languageId;
		currentProject = cfgDescription != null ? cfgDescription.getProjectDescription().getProject() : null;
		detectedSettingEntries = new ArrayList<ICLanguageSettingEntry>();
		currentCommandResolved = customParameter;

		if (!runOnce) {
			setSettingEntries(cfgDescription, currentProject, currentLanguageId, null);
		}
	}

	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		startup(cfgDescription, null);
	}

	public ICConfigurationDescription getConfigurationDescription() {
		return currentCfgDescription;
	}

	public IProject getProject() {
		return currentProject;
	}

	public String getLanguage() {
		return currentLanguageId;
	}

	/**
	 * This method is expected to populate {@link #detectedSettingEntries} with specific values
	 * parsed from supplied lines.
	 */
	public abstract boolean processLine(String line);

	public void shutdown() {
		if (detectedSettingEntries!=null && detectedSettingEntries.size()>0) {
			setSettingEntries(currentCfgDescription, currentProject, currentLanguageId, detectedSettingEntries);
		}
		detectedSettingEntries = null;
	}

	/**
	 * TODO: test case for this function
	 */
	public void run(IPath workingDirectory, String[] env, IProgressMonitor monitor)
			throws CoreException, IOException {

		String command = getResolvedCommand();
		if (command==null || command.trim().length()==0) {
			return;
		}

		if (runOnce && !isEmpty()) {
			return;
		}
		IConsole console;
		if (isConsoleEnabled) {
			String consoleId = MakeCorePlugin.PLUGIN_ID + '.' + getId()/* + '.' + getLanguage()*/;
//			console = CCorePlugin.getDefault().getBuildConsole(consoleId, getName(), getIconURL());
			URL defaultIcon = getIconURL();
			console = CCorePlugin.getDefault().getConsole("org.eclipse.cdt.make.internal.ui.scannerconfig.ScannerDiscoveryConsole", getId(), getName(), defaultIcon);
		} else {
			// that looks in extension points registry and won't find the id
			console = CCorePlugin.getDefault().getConsole(MakeCorePlugin.PLUGIN_ID + ".console.hidden"); //$NON-NLS-1$
		}
		console.start(currentProject);
		OutputStream cos = console.getOutputStream();

		ErrorParserManager epm = new ErrorParserManager(currentProject, new SCMarkerGenerator(), new String[] {GMAKE_ERROR_PARSER_ID});
		epm.setOutputStream(cos);
		StreamMonitor streamMon = new StreamMonitor(new SubProgressMonitor(monitor, 70), epm, 100);
		OutputStream stdout = streamMon;
		OutputStream stderr = streamMon;

		String msg = "Running scanner discovery: " + getName();
		monitor.subTask(msg);
		printLine(stdout, "**** " + msg + " ****" + NEWLINE);

		ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(stdout, stderr, new IConsoleParser[] { this });
		OutputStream consoleOut = sniffer.getOutputStream();
		OutputStream consoleErr = sniffer.getErrorStream();


		String errMsg = null;
		ICommandLauncher launcher = new CommandLauncher();

		launcher.setProject(currentProject);

		// Print the command for visual interaction.
		launcher.showCommand(true);

		String[] cmdArray = CommandLineUtil.argumentsToArray(command);
		IPath program = new Path(cmdArray[0]);
		String[] args = new String[0];
		if (cmdArray.length>1) {
			args = new String[cmdArray.length-1];
			System.arraycopy(cmdArray, 1, args, 0, args.length);
		}

		Process p = launcher.execute(program, args, env, workingDirectory, monitor);

		if (p != null) {
			// Before launching give visual cues via the monitor
			monitor.subTask("Invoking command " + command);
			if (launcher.waitAndRead(consoleOut, consoleErr, new SubProgressMonitor(monitor, 0))
					!= ICommandLauncher.OK) {
				errMsg = launcher.getErrorMessage();
			}
		} else {
			errMsg = launcher.getErrorMessage();
		}
		if (errMsg!=null) {
			String errorPrefix = MakeMessages.getString("ExternalScannerInfoProvider.Error_Prefix"); //$NON-NLS-1$

			msg = MakeMessages.getFormattedString("ExternalScannerInfoProvider.Provider_Error", command);
			printLine(consoleErr, errorPrefix + msg + NEWLINE);

			// Launching failed, trying to figure out possible cause
			String envPath = getEnvVar(env, PATH_ENV);
			if (!program.isAbsolute() && PathUtil.findProgramLocation(program.toString(), envPath) == null) {
				printLine(consoleErr, errMsg);
				msg = MakeMessages.getFormattedString("ExternalScannerInfoProvider.Working_Directory", workingDirectory); //$NON-NLS-1$
				msg = MakeMessages.getFormattedString("ExternalScannerInfoProvider.Program_Not_In_Path", program); //$NON-NLS-1$
				printLine(consoleErr, errorPrefix + msg + NEWLINE);
				printLine(consoleErr, PATH_ENV + "=[" + envPath + "]" + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				printLine(consoleErr, errorPrefix + errMsg);
				msg = MakeMessages.getFormattedString("ExternalScannerInfoProvider.Working_Directory", workingDirectory); //$NON-NLS-1$
				printLine(consoleErr, PATH_ENV + "=[" + envPath + "]" + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}


	private String getEnvVar(String[] envStrings, String envVar) {
		String envPath = null;
		if (envStrings!=null) {
			String varPrefix = envVar+'=';
			for (String envStr : envStrings) {
				if (envStr.startsWith(varPrefix)) {
					envPath = envStr.substring(varPrefix.length());
					break;
				}
			}
		} else {
			envPath = System.getenv(envVar);
		}
		return envPath;
	}

	protected URL getIconURL() {
		return Platform.getBundle(PLUGIN_CDT_MAKE_UI_ID).getEntry("icons/obj16/inspect_system.gif"); //$NON-NLS-1$
	}

	private void printLine(OutputStream stream, String msg) throws IOException {
		stream.write((msg + NEWLINE).getBytes());
		stream.flush();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (runOnce ? 1231 : 1237);
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof AbstractBuiltinSpecsDetector))
			return false;
		AbstractBuiltinSpecsDetector other = (AbstractBuiltinSpecsDetector) obj;
		if (runOnce != other.runOnce)
			return false;
		return true;
	}


}

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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

public abstract class AbstractBuiltinSpecsDetector extends LanguageSettingsSerializable implements
		ILanguageSettingsOutputScanner {
	private static final String EOL = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$

	// temporaries which are reassigned before running
	private ICConfigurationDescription currentCfgDescription = null;
	private IProject currentProject = null;
	private String currentLanguageId = null;
	private String currentCommandResolved = null;
	protected List<ICLanguageSettingEntry> detectedSettingEntries = null;

	private boolean runOnce = true;


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
	 * This method is expected to populate this.settingEntries with specific values
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
	public void run(IPath workingDirectory, String[] env, IConsole console, IProgressMonitor monitor)
			throws CoreException, IOException {

		String command = getResolvedCommand();
		if (command==null || command.trim().length()==0) {
			return;
		}

		if (runOnce && !isEmpty()) {
			return;
		}

		OutputStream stdout = console.getOutputStream();
		OutputStream stderr = console.getErrorStream();

		String msg = "Running scanner discovery: " + getName();
		monitor.subTask(msg);
		stdout.write(("**** "+msg+" ****"+EOL+EOL).getBytes());
		stdout.flush();

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
			monitor.subTask("Invoking command "+program);
			if (launcher.waitAndRead(consoleOut, consoleErr, new SubProgressMonitor(monitor, 0))
					!= ICommandLauncher.OK) {
				errMsg = launcher.getErrorMessage();
			}
		} else {
			errMsg = launcher.getErrorMessage();
		}
		if (errMsg!=null) {
			stdout.write((errMsg+EOL+EOL).getBytes());
			stdout.flush();
		}
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

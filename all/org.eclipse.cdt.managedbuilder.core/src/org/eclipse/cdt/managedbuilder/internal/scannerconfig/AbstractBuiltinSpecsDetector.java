/*******************************************************************************
 * Copyright (c) 2009, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.internal.core.XmlUtil;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.AbstractLanguageSettingsOutputScanner;
import org.eclipse.cdt.make.core.scannerconfig.ILanguageSettingsBuiltinSpecsDetector;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCMarkerGenerator;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.w3c.dom.Element;

public abstract class AbstractBuiltinSpecsDetector extends AbstractLanguageSettingsOutputScanner implements ILanguageSettingsBuiltinSpecsDetector {

	private boolean isEnabled = true;
	
	
	private static final String NEWLINE = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
	private static final String PLUGIN_CDT_MAKE_UI_ID = "org.eclipse.cdt.make.ui"; //$NON-NLS-1$
	private static final String GMAKE_ERROR_PARSER_ID = "org.eclipse.cdt.core.GmakeErrorParser"; //$NON-NLS-1$
	private static final String PATH_ENV = "PATH"; //$NON-NLS-1$
	private static final String ATTR_RUN_ONCE = "run-once"; //$NON-NLS-1$
	private static final String ATTR_CONSOLE = "console"; //$NON-NLS-1$

	protected static final String COMPILER_MACRO = "${COMMAND}";
	protected static final String SPEC_FILE_MACRO = "${INPUTS}";
	protected static final String SPEC_EXT_MACRO = "${EXT}";
	protected static final String SPEC_FILE_BASE = "spec.";

	// temporaries which are reassigned before running
	private String currentLanguageId = null;
	private String currentCommandResolved = null;
	protected List<ICLanguageSettingEntry> detectedSettingEntries = null;

	private boolean runOnce = true;
	private boolean isConsoleEnabled = false;
	protected java.io.File specFile = null;
	protected boolean preserveSpecFile = false;

	public AbstractBuiltinSpecsDetector() {
		isForProject = true;
	}

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

		specFile = null;

		if (!runOnce) {
			setSettingEntries(cfgDescription, currentProject, currentLanguageId, null);
		}

		isEnabled = true;
		String cmd = getCustomParameter();

		if (cmd!=null && (cmd.contains(COMPILER_MACRO) || cmd.contains(SPEC_FILE_MACRO) || cmd.contains(SPEC_EXT_MACRO))) {
			ITool tool = getTool(getToolchainId(), languageId);
			isEnabled = tool!=null;
			
			if (tool!=null) {
				if (cmd.contains(COMPILER_MACRO)) {
					String compiler = getCompilerCommand(tool);
					cmd = cmd.replace(COMPILER_MACRO, compiler);
				}
				if (cmd.contains(SPEC_FILE_MACRO)) {
					String specFile = getSpecFile(languageId, tool);
					cmd = cmd.replace(SPEC_FILE_MACRO, specFile);
				}
				if (cmd.contains(SPEC_EXT_MACRO)) {
					String specFile = getSpecExt(languageId, tool);
					cmd = cmd.replace(SPEC_EXT_MACRO, specFile);
				}
				setResolvedCommand(cmd);
			}
		}
	}

	@Override
	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		startup(cfgDescription, null);
	}

	public String getLanguage() {
		return currentLanguageId;
	}

	/**
	 * TODO
	 */
	@Override
	protected String parseResourceName(String line) {
		// For the project name
		return "/";
	}

	@Override
	public void shutdown() {
		if (specFile!=null && !preserveSpecFile) {
			specFile.delete();
			specFile = null;
		}

		setResolvedCommand(null);

		if (detectedSettingEntries==null) {
			detectedSettingEntries = new ArrayList<ICLanguageSettingEntry>();
		}

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

		boolean isEmpty = getSettingEntries(currentCfgDescription, currentProject, currentLanguageId)==null;
		if (runOnce && !isEmpty) {
			return;
		}
		IConsole console;
		if (isConsoleEnabled) {
			console = startProviderConsole();
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

	protected IConsole startProviderConsole() {
		String languageId = getLanguage();
		ILanguageDescriptor ld = LanguageManager.getInstance().getLanguageDescriptor(languageId);
		
		String consoleId = MakeCorePlugin.PLUGIN_ID + '.' + getId() + '.' + languageId;
		String consoleName = getName() + ", " + ld.getName();
		URL defaultIcon = Platform.getBundle(PLUGIN_CDT_MAKE_UI_ID).getEntry("icons/obj16/inspect_system.gif");
		
		IConsole console = CCorePlugin.getDefault().getConsole("org.eclipse.cdt.make.internal.ui.scannerconfig.ScannerDiscoveryConsole", consoleId, consoleName, defaultIcon);
		return console;
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

	private void printLine(OutputStream stream, String msg) throws IOException {
		stream.write((msg + NEWLINE).getBytes());
		stream.flush();
	}

	@Override
	public Element serialize(Element parentElement) {
		Element elementProvider = super.serialize(parentElement);
		elementProvider.setAttribute(ATTR_RUN_ONCE, Boolean.toString(runOnce));
		elementProvider.setAttribute(ATTR_CONSOLE, Boolean.toString(isConsoleEnabled));
		return elementProvider;
	}
	
	@Override
	public void load(Element providerNode) {
		super.load(providerNode);
		
		String runOnceValue = XmlUtil.determineAttributeValue(providerNode, ATTR_RUN_ONCE);
		if (runOnceValue!=null)
			runOnce = Boolean.parseBoolean(runOnceValue);

		String consoleValue = XmlUtil.determineAttributeValue(providerNode, ATTR_CONSOLE);
		if (consoleValue!=null)
			isConsoleEnabled = Boolean.parseBoolean(consoleValue);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (runOnce ? 1231 : 1237);
		result = prime * result + (isConsoleEnabled ? 1231 : 1237);
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
		if (isConsoleEnabled != other.isConsoleEnabled)
			return false;
		return true;
	}


	@Override
	protected void setSettingEntries(List<ICLanguageSettingEntry> entries, IResource rc) {
		// Builtin specs detectors collect entries not per line but for the whole output
		if (entries!=null)
			detectedSettingEntries.addAll(entries);
	}


	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean processLine(String line, ErrorParserManager epm) {
		if (isEnabled) {
			return super.processLine(line, epm);
		}
		return false;
	}
	
	@Override
	public boolean processLine(String line) {
		if (isEnabled) {
			return super.processLine(line);
		}
		return false;
	}
	
	/**
	 * TODO
	 */
	protected abstract String getToolchainId();

	private ITool getTool(String toolchainId, String languageId) {
		IToolChain toolchain = ManagedBuildManager.getExtensionToolChain(toolchainId);
		if (toolchain != null) {
			ITool[] tools = toolchain.getTools();
			for (ITool tool : tools) {
				IInputType[] inputTypes = tool.getInputTypes();
				for (IInputType inType : inputTypes) {
					String lang = inType.getLanguageId(tool);
					if (languageId.equals(lang))
						return tool;
				}
			}
		}
		ManagedBuilderCorePlugin.error("Unable to find tool in toolchain="+toolchainId+" for language="+languageId);
		return null;
	}

	private String getCompilerCommand(ITool tool) {
		String compiler = tool.getToolCommand();
		if (compiler.length()==0) {
			String msg = "Unable to find compiler command in toolchain="+getToolchainId();
			ManagedBuilderCorePlugin.error(msg);
		}
		return compiler;
	}

	private String getSpecFile(String languageId, ITool tool) {
		String ext = getSpecExt(languageId, tool);
		
		String specFileName = SPEC_FILE_BASE + ext;
		IPath workingLocation = MakeCorePlugin.getWorkingDirectory();
		IPath fileLocation = workingLocation.append(specFileName);

		specFile = new java.io.File(fileLocation.toOSString());
		// will preserve spec file if it was already there otherwise will delete upon finishing
		preserveSpecFile = specFile.exists();
		if (!preserveSpecFile) {
			try {
				specFile.createNewFile();
			} catch (IOException e) {
				ManagedBuilderCorePlugin.log(e);
			}
		}

		return fileLocation.toString();
	}

	private String getSpecExt(String languageId, ITool tool) {
		String ext = "";
		String[] srcFileExtensions = tool.getAllInputExtensions();
		if (srcFileExtensions!=null && srcFileExtensions.length>0) {
			ext = srcFileExtensions[0];
		}
		if (ext.length()==0) {
			ManagedBuilderCorePlugin.error("Unable to find file extension for language "+languageId);
		}
		return ext;
	}


}

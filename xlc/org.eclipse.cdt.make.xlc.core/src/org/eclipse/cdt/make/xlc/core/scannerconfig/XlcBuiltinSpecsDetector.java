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

package org.eclipse.cdt.make.xlc.core.scannerconfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.make.core.scannerconfig.GCCBuildCommandParser;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Class to detect built-in compiler settings. Note that currently this class is hardwired
 * to GCC toolchain {@code cdt.managedbuild.toolchain.gnu.base}.
 *
 */
public class XlcBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector implements ILanguageSettingsEditableProvider {
	// must match the toolchain definition in org.eclipse.cdt.managedbuilder.core.buildDefinitions extension point
	private static final String GCC_TOOLCHAIN_ID = "cdt.managedbuild.toolchain.gnu.base";  //$NON-NLS-1$
	
	private static final String COMPILER_MACRO = "${COMMAND}"; //$NON-NLS-1$
	private static final String SPEC_FILE_MACRO = "${INPUTS}"; //$NON-NLS-1$
	private static final String SPEC_FILE_BASE = "spec."; //$NON-NLS-1$

	private static final Pattern MACRO_PATTERN = Pattern.compile("#define (\\S*) *(.*)"); //$NON-NLS-1$
	private static final Pattern MACRO_WITH_ARGS_PATTERN = Pattern.compile("#define (\\S*\\(.*?\\)) *(.*)"); //$NON-NLS-1$
	
	private boolean expectingIncludes = false;
	private int includeFlag = 0;
	private java.io.File specFile = null;
	private boolean preserveSpecFile = false;

	protected List<CIncludePathEntry> detectedIncludes = null;
	protected List<CMacroEntry> detectedDefines = null;

	private GCCBuildCommandParser gccParser = new GCCBuildCommandParser();
	
	@Override
	public void startup(ICConfigurationDescription cfgDescription, String languageId) throws CoreException {
		Assert.isNotNull(languageId);
		
		super.startup(cfgDescription, languageId);
		detectedIncludes = new ArrayList<CIncludePathEntry>();
		detectedDefines = new ArrayList<CMacroEntry>();

		includeFlag = 0;
		expectingIncludes = false;
		specFile = null;

		String cmd = getCustomParameter();

		if (cmd!=null && (cmd.contains(COMPILER_MACRO) || cmd.contains(SPEC_FILE_MACRO))) {
			ITool tool = getTool(GCC_TOOLCHAIN_ID, languageId);
			
			if (tool!=null) {
				if (cmd.contains(COMPILER_MACRO)) {
					String compiler = getCompilerCommand(tool);
					cmd = cmd.replace(COMPILER_MACRO, compiler);
				}
				if (cmd.contains(SPEC_FILE_MACRO)) {
					String specFile = getSpecFile(languageId, tool);
					cmd = cmd.replace(SPEC_FILE_MACRO, specFile);
				}
				setResolvedCommand(cmd);
			}
		}
		
		gccParser.startup(cfgDescription);
	}

	@Override
	public boolean processLine(String line) {
		line = line.trim();

		// contribution of -dD option
		Matcher matcher = MACRO_WITH_ARGS_PATTERN.matcher(line);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			detectedDefines.add(new CMacroEntry(name, value, ICSettingEntry.BUILTIN|ICSettingEntry.READONLY));
			return true;
		}
		matcher = MACRO_PATTERN.matcher(line);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			detectedDefines.add(new CMacroEntry(name, value, ICSettingEntry.BUILTIN|ICSettingEntry.READONLY));
			return true;
		}

		// contribution of includes
		if (line.equals("#include \"...\" search starts here:")) {
			expectingIncludes = true;
			includeFlag = ICSettingEntry.BUILTIN | ICSettingEntry.READONLY | ICSettingEntry.LOCAL;
		} else if (line.equals("#include <...> search starts here:")) {
			expectingIncludes = true;
			includeFlag = ICSettingEntry.BUILTIN | ICSettingEntry.READONLY;
		} else if (line.startsWith("End of search list.")) {
			expectingIncludes = false;
			includeFlag = 0;
		} else if (expectingIncludes) {
			line.trim();
			IPath path = new Path(line);
			java.io.File file = new java.io.File(line);
			if (file.exists() && file.isDirectory()) {
				// get rid of relative portions "../"
				try {
					path = new Path(file.getCanonicalPath());
				} catch (IOException e) {
					ManagedBuilderCorePlugin.log(e);
				}
			}
			// TODO - Redo Cygwin and do remote scenario
			if (!file.exists()) {
				try {
					path = new Path(cygwinToWindowsPath(line));
				} catch (Exception e) {
				}
			}
			detectedIncludes.add(new CIncludePathEntry(path, includeFlag));
		}

		return gccParser.processLine(line);
		
//		return true;
	}

	@Override
	public void shutdown() {
		
		gccParser.shutdown();
		
//		gccParser.
		
		
		
		
		includeFlag = 0;
		expectingIncludes = false;

		if (specFile!=null && !preserveSpecFile) {
			specFile.delete();
			specFile = null;
		}

		setResolvedCommand(null);

		if (detectedSettingEntries==null) {
			detectedSettingEntries = new ArrayList<ICLanguageSettingEntry>();
		}
		// That places Includes first, then Defines, then other if any
		if (detectedDefines!=null) {
			detectedSettingEntries.addAll(0, detectedDefines);
		}
		detectedDefines = null;

		if (detectedIncludes!=null) {
			detectedSettingEntries.addAll(0, detectedIncludes);
		}
		detectedIncludes = null;

		super.shutdown();
	}

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
			String msg = "Unable to find compiler command in toolchain="+GCC_TOOLCHAIN_ID;
			ManagedBuilderCorePlugin.error(msg);
		}
		return compiler;
	}

	private String getSpecFile(String languageId, ITool tool) {
		String ext = "";
		String[] srcFileExtensions = tool.getAllInputExtensions();
		if (srcFileExtensions!=null && srcFileExtensions.length>0) {
			ext = srcFileExtensions[0];
		}
		if (ext.length()==0) {
			ManagedBuilderCorePlugin.error("Unable to find file extension for language "+languageId);
		}
		
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

	// FIXME
	/**
	 * See ResourceHelper#cygwinToWindowsPath(String).
	 * 
	 * Conversion from Cygwin path to Windows path.
	 *
	 * @param cygwinPath - Cygwin path.
	 * @return Windows style converted path.
	 * @throws UnsupportedOperationException if Cygwin is unavailable.
	 * @throws IOException on IO problem.
	 */
	private static String cygwinToWindowsPath(String cygwinPath) throws IOException, UnsupportedOperationException {
		if (!Platform.getOS().equals(Platform.OS_WIN32)) {
			// Don't run this on non-windows platforms
			throw new UnsupportedOperationException("Not a Windows system, Cygwin is unavailable.");
		}
		String[] args = {"cygpath", "-w", cygwinPath};
		Process cygpath;
		try {
			cygpath = Runtime.getRuntime().exec(args);
		} catch (IOException ioe) {
			throw new UnsupportedOperationException("Cygwin utility cygpath is not in the system search path.");
		}
		BufferedReader stdout = new BufferedReader(new InputStreamReader(cygpath.getInputStream()));

		String windowsPath = stdout.readLine();
		if (windowsPath == null) {
			throw new UnsupportedOperationException("Cygwin utility cygpath is not available.");
		}
		return windowsPath.trim();
	}
	
	@Override
	public XlcBuiltinSpecsDetector cloneShallow() throws CloneNotSupportedException {
		return (XlcBuiltinSpecsDetector) super.cloneShallow();
	}

	@Override
	public XlcBuiltinSpecsDetector clone() throws CloneNotSupportedException {
		return (XlcBuiltinSpecsDetector) super.clone();
	}
	
}

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

package org.eclipse.cdt.make.internal.core.scannerconfig.gnu;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

public class GCCBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
	private static final Pattern MACRO_PATTERN = Pattern.compile("#define (\\S*) *(.*)"); //$NON-NLS-1$
	private static final Pattern MACRO_WITH_ARGS_PATTERN = Pattern.compile("#define (\\S*\\(.*?\\)) *(.*)"); //$NON-NLS-1$

	
	private static final String SPEC_FILE_NAME = "spec";
	
	//	private static final String GCC_SCANNER_INFO_COMMAND = "gcc -E -P -v -dD ${plugin_state_location}/${specs_file}"; //$NON-NLS-1$
	private static final String GCC_SCANNER_INFO_COMMAND = "gcc -E -P -v -dD "; //$NON-NLS-1$
	
	private static final String LANGUAGE_ID_ASSEMBLER = "org.eclipse.cdt.core.assembly";
	private static final String LANGUAGE_ID_C = "org.eclipse.cdt.core.gcc";
	private static final String LANGUAGE_ID_CPLUSPLUS = "org.eclipse.cdt.core.g++";

	private String specFileName;
	private String command;
	
	private boolean expectingIncludes = false;
	private int includeFlag = 0;
	private java.io.File specFile;
	
	@Override
	public String getCommand() {
		
		return command;
	}

	@Override
	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		super.startup(cfgDescription);
		includeFlag = 0;
		expectingIncludes = false;
		specFileName = null;
		command = null;

		// TODO: figure out file extension from language id
//		ILanguageDescriptor ld = LanguageManager.getInstance().getLanguageDescriptor(getCurrentLanguage());
		if (LANGUAGE_ID_CPLUSPLUS.equals(getCurrentLanguage())) {
			specFileName = SPEC_FILE_NAME + ".cpp";
		} else if (LANGUAGE_ID_C.equals(getCurrentLanguage())) {
			specFileName = SPEC_FILE_NAME + ".c";
		} else if (LANGUAGE_ID_ASSEMBLER.equals(getCurrentLanguage())) {
			specFileName = SPEC_FILE_NAME + ".s";
		}
		if (specFileName!=null) {
			IProject project = cfgDescription.getProjectDescription().getProject();
			IPath workingLocation = project.getWorkingLocation(MakeCorePlugin.PLUGIN_ID);
			
			try {
				specFile = workingLocation.append(specFileName).toFile();
				specFile.createNewFile();
			} catch (IOException e) {
				Status status = new Status(IStatus.ERROR, MakeCorePlugin.PLUGIN_ID, IStatus.ERROR,
						"Error creating specs file ["+specFile+"]", e);
				throw new CoreException(status);
			}
			command = GCC_SCANNER_INFO_COMMAND + specFile;
		}
		
		
	}
	
	@Override
	public boolean processLine(String line) {
		TraceUtil.outputTrace("GCCBuiltinSpecsDetector parsing line: [", line, "]"); //$NON-NLS-1$ //$NON-NLS-2$
		line = line.trim();

		// contribution of -dD option
		Matcher matcher = MACRO_WITH_ARGS_PATTERN.matcher(line);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			detectedSettingEntries.add(new CMacroEntry(name, value, ICSettingEntry.BUILTIN|ICSettingEntry.READONLY));
			return true;
		}
		matcher = MACRO_PATTERN.matcher(line);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			detectedSettingEntries.add(new CMacroEntry(name, value, ICSettingEntry.BUILTIN|ICSettingEntry.READONLY));
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
			java.io.File file = new java.io.File(line);
			if (file.exists() && file.isDirectory()) {
				// get rid of relative portions "../"
				try {
					IPath path = new Path(file.getCanonicalPath());
					detectedSettingEntries.add(new CIncludePathEntry(path, includeFlag));
				} catch (IOException e) {
					MakeCorePlugin.log(e);
				}
			}
		}

		return true;
	}

	@Override
	public void shutdown() {
		super.shutdown();
		
		includeFlag = 0;
		expectingIncludes = false;
		specFileName = null;
		command = null;
		
		if (specFile!=null) {
			specFile.delete();
			specFile = null;
		}
	}

}

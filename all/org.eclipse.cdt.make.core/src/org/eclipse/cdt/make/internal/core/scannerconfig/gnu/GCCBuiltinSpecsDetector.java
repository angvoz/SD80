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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class GCCBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
	private static final Pattern MACRO_PATTERN = Pattern.compile("#define (\\S*) *(.*)"); //$NON-NLS-1$
	private static final Pattern MACRO_WITH_ARGS_PATTERN = Pattern.compile("#define (\\S*\\(.*?\\)) *(.*)"); //$NON-NLS-1$

	private static final String SPEC_FILE_MACRO = "${spec_file}"; //$NON-NLS-1$
	private static final String SPEC_FILE_BASE = "spec"; //$NON-NLS-1$

	private static final String LANGUAGE_ID_ASSEMBLER = "org.eclipse.cdt.core.assembly";
	private static final String LANGUAGE_ID_C = "org.eclipse.cdt.core.gcc";
	private static final String LANGUAGE_ID_CPLUSPLUS = "org.eclipse.cdt.core.g++";


	private boolean expectingIncludes = false;
	private int includeFlag = 0;
	private java.io.File specFile = null;
	private boolean isSpecFileAlreadyThere = false;

	protected List<CIncludePathEntry> detectedIncludes = null;
	protected List<CMacroEntry> detectedDefines = null;


	public String getSpecFileName(String languageId) {
		String specFileName=null;
		// TODO: figure out file extension from language id
//			ILanguageDescriptor ld = LanguageManager.getInstance().getLanguageDescriptor(getCurrentLanguage());
//		CContentTypes.getContentType(project, filename);
//		final IContentTypeManager ctm = Platform.getContentTypeManager();
//		final IContentType ctbin = ctm.getContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE);
//		final IContentType[] cts= ctm.findContentTypesFor(baseFileName.toString());
//		language = LanguageManager.getInstance().getLanguageForFile(filePath, project, configuration);
//		CDataUtil.getExtensionsFromContentTypes()
		
		if (LANGUAGE_ID_CPLUSPLUS.equals(languageId)) {
			specFileName = SPEC_FILE_BASE + ".cpp";
		} else if (LANGUAGE_ID_C.equals(languageId)) {
			specFileName = SPEC_FILE_BASE + ".c";
		} else if (LANGUAGE_ID_ASSEMBLER.equals(languageId)) {
			specFileName = SPEC_FILE_BASE + ".s";
		}
		return specFileName;
	}

	@Override
	public void startup(ICConfigurationDescription cfgDescription, String languageId) throws CoreException {
		super.startup(cfgDescription, languageId);
		detectedIncludes = new ArrayList<CIncludePathEntry>();
		detectedDefines = new ArrayList<CMacroEntry>();

		includeFlag = 0;
		expectingIncludes = false;
		specFile = null;

		String cmd = getCustomParameter();

		String specFileName = getSpecFileName(languageId);
		if (specFileName!=null) {
			IPath workingLocation = MakeCorePlugin.getWorkingDirectory();
			IPath specFileLocation = workingLocation.append(specFileName);
			cmd = cmd.replace(SPEC_FILE_MACRO, specFileLocation.toString());

			specFile = new java.io.File(specFileLocation.toOSString());
			isSpecFileAlreadyThere = specFile.exists();
			try {
				specFile.createNewFile();
			} catch (IOException e) {
				MakeCorePlugin.log(e);
			}
		}
		setCommand(cmd);
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
			// TODO - Cygwin and remote scenario
			java.io.File file = new java.io.File(line);
			if (file.exists() && file.isDirectory()) {
				// get rid of relative portions "../"
				try {
					path = new Path(file.getCanonicalPath());
				} catch (IOException e) {
					MakeCorePlugin.log(e);
				}
			}
			detectedIncludes.add(new CIncludePathEntry(path, includeFlag));
		}

		return true;
	}

	@Override
	public void shutdown() {
		includeFlag = 0;
		expectingIncludes = false;

		if (specFile!=null && !isSpecFileAlreadyThere) {
			specFile.delete();
			specFile = null;
		}

		setCommand(null);

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

}

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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;

public class GCCBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector {
	private static final Pattern MACRO_PATTERN = Pattern.compile("#define (\\S*) *(.*)"); //$NON-NLS-1$
	private static final Pattern MACRO_WITH_ARGS_PATTERN = Pattern.compile("#define (\\S*\\(.*?\\)) *(.*)"); //$NON-NLS-1$
	private static final String GCC_SCANNER_INFO_COMMAND = "gcc -E -P -v -dD specs.cpp"; //$NON-NLS-1$

	private boolean expectingIncludes = false;
	private int includeFlag = 0;
	
	public GCCBuiltinSpecsDetector() {
		setCommand(GCC_SCANNER_INFO_COMMAND);
	}
	
	@Override
	public void startup(ICConfigurationDescription cfgDescription) {
		super.startup(cfgDescription);
		includeFlag = 0;
		expectingIncludes = false;
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
			settingEntries.add(new CMacroEntry(name, value, ICSettingEntry.BUILTIN));
			return true;
		}
		matcher = MACRO_PATTERN.matcher(line);
		if (matcher.matches()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			settingEntries.add(new CMacroEntry(name, value, ICSettingEntry.BUILTIN));
			return true;
		}
		
		// contribution of includes
		if (line.equals("#include \"...\" search starts here:")) {
			expectingIncludes = true;
			includeFlag = ICSettingEntry.BUILTIN | ICSettingEntry.LOCAL;
		} else if (line.equals("#include <...> search starts here:")) {
			expectingIncludes = true;
			includeFlag = ICSettingEntry.BUILTIN;
		} else if (line.startsWith("End of search list.")) {
			expectingIncludes = false;
			includeFlag = 0;
		} else if (expectingIncludes) {
			settingEntries.add(new CIncludePathEntry(line, includeFlag));
		}

		return true;
	}

}

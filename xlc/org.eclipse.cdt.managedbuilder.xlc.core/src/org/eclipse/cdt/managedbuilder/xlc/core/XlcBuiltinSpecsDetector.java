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

package org.eclipse.cdt.managedbuilder.xlc.core;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.AbstractBuiltinSpecsDetector;


/**
 * 

> xlC -E -V -P -w ~/tmp/spec.C
export XL_CONFIG=/etc/vac.cfg:xlC 
/usr/vac/exe/xlCcpp /home/me/tmp/spec.C - -qc++=/usr/vacpp/include -D_AIX -D_AIX32 -D_AIX41 -D_AIX43 -D_AIX50 -D_AIX51 -D_AIX52 -D_IBMR2 -D_POWER -E -P -w -qlanglvl=ansi -qansialias 
rm /tmp/xlcW0lt4Jia
rm /tmp/xlcW1lt4Jib
rm /tmp/xlcW2lt4Jic

 */

/**
 * Class to detect built-in compiler settings. Note that currently this class is hardwired
 * to GCC toolchain {@code cdt.managedbuild.toolchain.gnu.base}.
 *
 */
public class XlcBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector implements ILanguageSettingsEditableProvider {
	// must match the toolchain definition in org.eclipse.cdt.managedbuilder.core.buildDefinitions extension point
	// FIXME - ill defined XLC toolchain
//	private static final String XLC_TOOLCHAIN_ID = "cdt.managedbuild.toolchain.xlc.exe.debug";  //$NON-NLS-1$
	private static final String GCC_TOOLCHAIN_ID = "cdt.managedbuild.toolchain.gnu.base";  //$NON-NLS-1$
	
	private static final Pattern PATTERN_OPTIONS = Pattern.compile("-[^\\s\"']*(\\s*((\".*?\")|('.*?')|([^-\\s][^\\s]+)))?"); //$NON-NLS-1$
	private static final int PATTERN_OPTION_GROUP = 0;

	private static final int BUILTIN_SPECS_FLAG = ICSettingEntry.BUILTIN | ICSettingEntry.READONLY;
	@SuppressWarnings("nls")
	private static final AbstractOptionParser[] optionParsers = {
			new IncludePathOptionParser("-I\\s*([\"'])(.*)\\1", "$2", BUILTIN_SPECS_FLAG | ICSettingEntry.LOCAL),
			new IncludePathOptionParser("-I\\s*([^\\s\"']*)", "$1", BUILTIN_SPECS_FLAG),
			new IncludePathOptionParser("-qc\\+\\+=\\s*([^\\s\"']*)", "$1", BUILTIN_SPECS_FLAG),
			new MacroOptionParser("-D\\s*([\"'])([^=]*)(=(.*))?\\1", "$2", "$4", BUILTIN_SPECS_FLAG),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=(\\\\([\"']))(.*?)\\2", "$1", "$3$4$3", BUILTIN_SPECS_FLAG),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)=([\"'])(.*?)\\2", "$1", "$3", BUILTIN_SPECS_FLAG),
			new MacroOptionParser("-D\\s*([^\\s=\"']*)(=([^\\s\"']*))?", "$1", "$3", BUILTIN_SPECS_FLAG),
	};

	@Override
	protected String getToolchainId() {
//		return XLC_TOOLCHAIN_ID;
		return GCC_TOOLCHAIN_ID;
	}

	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}

	@Override
	protected List<String> parseOptions(String line) {
		List<String> options = new ArrayList<String>();
		Matcher optionMatcher = PATTERN_OPTIONS.matcher(line);
		while (optionMatcher.find()) {
			String option = optionMatcher.group(PATTERN_OPTION_GROUP);
			if (option!=null) {
				options.add(option);
			}
		}
		return options;
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

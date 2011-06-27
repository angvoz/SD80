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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.core.runtime.CoreException;

/**
 * Class to detect built-in compiler settings. Note that currently this class is hardwired
 * to GCC toolchain {@code cdt.managedbuild.toolchain.gnu.base}.
 *
 */
public class GCCBuiltinSpecsDetector extends AbstractBuiltinSpecsDetector implements ILanguageSettingsEditableProvider {
	// must match the toolchain definition in org.eclipse.cdt.managedbuilder.core.buildDefinitions extension point
	private static final String GCC_TOOLCHAIN_ID = "cdt.managedbuild.toolchain.gnu.base";  //$NON-NLS-1$
	
	private enum State {NONE, EXPECTING_LOCAL_INCLUDE, EXPECTING_SYSTEM_INCLUDE}
	State state = State.NONE;
	
	private static final int BUILTIN_SPECS_FLAG = ICSettingEntry.BUILTIN | ICSettingEntry.READONLY;
	@SuppressWarnings("nls")
	static final AbstractOptionParser[] optionParsers = {
			new IncludePathOptionParser("#include <(\\S.*)>", "$1", BUILTIN_SPECS_FLAG),
			new IncludePathOptionParser("#include \"(\\S.*)\"", "$1", BUILTIN_SPECS_FLAG | ICSettingEntry.LOCAL),
			new MacroOptionParser("#define (\\S*\\(.*?\\)) *(.*)", "$1", "$2", BUILTIN_SPECS_FLAG),
			new MacroOptionParser("#define (\\S*) *(.*)", "$1", "$2", BUILTIN_SPECS_FLAG),
	};

	@Override
	protected String getToolchainId() {
		return GCC_TOOLCHAIN_ID;
	}

	@Override
	protected AbstractOptionParser[] getOptionParsers() {
		return optionParsers;
	}

	private List<String> makeList(final String line) {
		return new ArrayList<String>() {{ add(line); }};
	}
	
	@Override
	protected List<String> parseOptions(String line) {
		line = line.trim();

		// contribution of -dD option
		if (line.startsWith("#define")) {
			return makeList(line);
		}

		// contribution of includes
		if (line.equals("#include \"...\" search starts here:")) {
			state = State.EXPECTING_LOCAL_INCLUDE;
		} else if (line.equals("#include <...> search starts here:")) {
			state = State.EXPECTING_SYSTEM_INCLUDE;
		} else if (line.startsWith("End of search list.")) {
			state = State.NONE;
		} else if (state==State.EXPECTING_LOCAL_INCLUDE) {
			// making that up for the parser to figure out
			line = "#include \""+line+"\"";
			return makeList(line);
		} else if (state==State.EXPECTING_SYSTEM_INCLUDE) {
			// making that up for the parser to figure out
			line = "#include <"+line+">";
			return makeList(line);
		}

		return null;
	}

	@Override
	public void startup(ICConfigurationDescription cfgDescription) throws CoreException {
		super.startup(cfgDescription);
		
		state = State.NONE;
	}
	
	@Override
	public void shutdown() {
		state = State.NONE;

		super.shutdown();
	}

	@Override
	public GCCBuiltinSpecsDetector cloneShallow() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetector) super.cloneShallow();
	}

	@Override
	public GCCBuiltinSpecsDetector clone() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetector) super.clone();
	}

	
}

/*******************************************************************************
 * Copyright (c) 2010, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.scannerdiscovery;

import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;

public class MockBuiltinSettingsDetector extends AbstractBuiltinSpecsDetector implements ILanguageSettingsEditableProvider {

	@Override
	public boolean processLine(String line) {
		if (detectedSettingEntries.size()==0) {
			detectedSettingEntries.add(new CMacroEntry("TEST_MACRO", "TestValue", ICSettingEntry.BUILTIN|ICSettingEntry.READONLY));
			detectedSettingEntries.add(new CIncludePathEntry("/test/path/", ICSettingEntry.BUILTIN|ICSettingEntry.READONLY));
		}
		return false;
	}

	@Override
	public MockBuiltinSettingsDetector cloneShallow() throws CloneNotSupportedException {
		return (MockBuiltinSettingsDetector) super.cloneShallow();
	}

	@Override
	public MockBuiltinSettingsDetector clone() throws CloneNotSupportedException {
		return (MockBuiltinSettingsDetector) super.clone();
	}
	
}

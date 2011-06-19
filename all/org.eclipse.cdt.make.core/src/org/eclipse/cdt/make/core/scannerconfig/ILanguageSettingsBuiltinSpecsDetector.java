/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.core.scannerconfig;

import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ILanguageSettingsBuiltinSpecsDetector extends ILanguageSettingsOutputScanner {
	public List<String> getLanguageScope();
	public void startup(ICConfigurationDescription cfgDescription, String languageId) throws CoreException;
	public void run(IPath workingDirectory, String[] env, IProgressMonitor monitor) throws CoreException, IOException;
}

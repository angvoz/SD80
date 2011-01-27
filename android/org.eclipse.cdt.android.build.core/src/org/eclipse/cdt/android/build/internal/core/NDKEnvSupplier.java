/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.android.build.internal.core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.android.build.core.NDKManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.Platform;

public class NDKEnvSupplier implements IConfigurationEnvironmentVariableSupplier {

	private static Map<String, IBuildEnvironmentVariable> envVars;
	
	private static class NDKPathEnvironmentVariable implements IBuildEnvironmentVariable {
		@Override
		public String getName() {
			return "PATH";
		}
		
		@Override
		public String getValue() {
			String path = NDKManager.getNDKLocation();
			
			if (Platform.getOS().equals(Platform.OS_WIN32)) {
				// Add in the path to the shell
				String shellPath = findShellPath();
				if (shellPath != null)
					path = shellPath + ";" + path;
			}
				
			return path;
		}
		
		private String findShellPath() {
			// I'm giving MSYS precedence over Cygwin. I'm biased that way :)
			// TODO using the default paths for now, need smarter ways to get at them
			// Alternatively the user can add the bin to their path themselves.
			File bin = new File("C:\\MinGW\\msys\\1.0\\bin");
			if (bin.isDirectory()) {
				return bin.getAbsolutePath();
			}
			
			bin = new File("C:\\cygwin\\bin");
			if (bin.isDirectory())
				return bin.getAbsolutePath();
			
			return null;
		}
		
		@Override
		public int getOperation() {
			return ENVVAR_PREPEND;
		}
		
		@Override
		public String getDelimiter() {
			return Platform.getOS().equals(Platform.OS_WIN32) ? ";" : ":";
		}
	}
	
	
	private synchronized void init() {
		if (envVars != null)
			return;
		
		envVars = new HashMap<String, IBuildEnvironmentVariable>();
		
		IBuildEnvironmentVariable path = new NDKPathEnvironmentVariable();
		envVars.put(path.getName(), path);
		
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			IBuildEnvironmentVariable cygwin = new IBuildEnvironmentVariable() {
				@Override
				public String getName() {
					return "CYGWIN";
				}
				
				@Override
				public String getValue() {
					return "nodosfilewarning";
				}
				
				@Override
				public int getOperation() {
					return IBuildEnvironmentVariable.ENVVAR_REPLACE;
				}
				
				@Override
				public String getDelimiter() {
					return null;
				}
			};
			
			envVars.put(cygwin.getName(), cygwin);
		}
	}
	
	@Override
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		init();
		return envVars.get(variableName);
	}

	@Override
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		init();
		return envVars.values().toArray(new IBuildEnvironmentVariable[envVars.size()]);
	}

}

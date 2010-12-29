package org.eclipse.cdt.android.build.internal.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.android.build.core.NDKManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class NDKEnvSupplier implements IConfigurationEnvironmentVariableSupplier {

	private static Map<String, IBuildEnvironmentVariable> envVars;
	
	private static class MyBuildEnvironmentVariable implements IBuildEnvironmentVariable {
		private final String name;
		private final String value;
		private final int operation;
		private final String delimiter;
		
		public MyBuildEnvironmentVariable(String name, String value, int operation, String delimiter) {
			this.name = name;
			this.value = value;
			this.operation = operation;
			this.delimiter = delimiter;
			envVars.put(getName(), this);
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public String getValue() {
			return value;
		}
		
		@Override
		public int getOperation() {
			return operation;
		}
		
		@Override
		public String getDelimiter() {
			return delimiter;
		}
	}
	
	private String findShellPath() {
		// I'm giving MSYS precedence over Cygwin
		File bin = new File("C:\\MinGW\\msys\\1.0\\bin");
		if (bin.isDirectory()) {
			// for now, need location of cygpath added to the path
			String path = bin.getAbsolutePath();
			try {
				URL url = Activator.find(new Path("cygpath"));
				if (url != null) {
					File cygpathFile = new File(FileLocator.toFileURL(url).toURI());
					path += ";" + cygpathFile.getParentFile().getAbsolutePath();
				}
			} catch (IOException e) {
				Activator.log(e);
			} catch (URISyntaxException e) {
				Activator.log(e);
			}
			return path;
		}
		
		// Add in the shell environment
		bin = new File("C:\\cygwin\\bin");
		if (bin.isDirectory())
			return bin.getAbsolutePath();
		
		return null;
	}
	
	private String getPath() {
		String path = NDKManager.getNDKLocation();
		
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			String shellPath = findShellPath();
			if (shellPath != null)
				path = shellPath + ";" + path;
		}
			
		return path;
	}
	
	private synchronized void init() {
		if (envVars != null)
			return;
		
		envVars = new HashMap<String, IBuildEnvironmentVariable>();
		
		new MyBuildEnvironmentVariable(
				"PATH",
				getPath(),
				IBuildEnvironmentVariable.ENVVAR_PREPEND,
				Platform.getOS().equals(Platform.OS_WIN32) ? ";" : ":");
		new MyBuildEnvironmentVariable(
				"CYGWIN",
				"nodosfilewarning",
				IBuildEnvironmentVariable.ENVVAR_REPLACE,
				null);
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

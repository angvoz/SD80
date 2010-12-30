package org.eclipse.cdt.android.build.internal.core.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.android.build.internal.core.Activator;
import org.eclipse.cdt.android.build.internal.core.NDKCommandLauncher;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.envvar.IEnvironmentVariableManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class NDKDiscoveryUpdater {

	private final NDKDiscoveredPathInfo pathInfo;
	private final IProject project;
	
	private boolean cplusplus = false;
	private String command;
	private List<String> arguments = new ArrayList<String>();
	
	public NDKDiscoveryUpdater(NDKDiscoveredPathInfo pathInfo) {
		this.pathInfo = pathInfo;
		this.project = pathInfo.getProject();
	}
	
	public void runUpdate(IProgressMonitor monitor) throws CoreException {
		try {
			// Run ndk-build -nB to get the list of commands
			IPath commandPath = new Path("ndk-build");
			String[] args = { "-nB" };
			String[] env = calcEnvironment();
			File projectDir = new File(project.getLocationURI());
			IPath changeToDirectory = new Path(projectDir.getAbsolutePath());
			Process proc = new NDKCommandLauncher().execute(commandPath, args, env, changeToDirectory, monitor);
			BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			String line = reader.readLine();
			while (line != null) {
				checkLine(line);
				line = reader.readLine();
			}
			
			// Run the unique commands with special gcc options to extract the symbols and paths
			// -E -P -v -dD
			arguments.add("-E");
			arguments.add("-P");
			arguments.add("-v");
			arguments.add("-dD");
			
			URL url = Activator.find(new Path(cplusplus ? "test.cpp" : "test.c"));
			File testFile = new File(FileLocator.toFileURL(url).toURI());
			String testFileName = testFile.getAbsolutePath().replace('\\', '/');
			arguments.add(testFileName);
			
			args = arguments.toArray(new String[arguments.size()]);
			proc = new NDKCommandLauncher().execute(new Path(command), args, env, changeToDirectory, monitor);
			// Error stream has the includes
			// Input stream has the defines
			final InputStream errStream = proc.getErrorStream();
			new Thread() {
				public void run() {
					try {
						BufferedReader reader = new BufferedReader(new InputStreamReader(errStream));
						String line = reader.readLine();
						while (line != null) {
							System.out.println(line);
							line = reader.readLine();
						}
					} catch (IOException e) { }
				};
			}.start();
			reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			
		} catch (IOException e) {
			throw new CoreException(Activator.newStatus(e));
		} catch (URISyntaxException e) {
			throw new CoreException(Activator.newStatus(e));
		}
	}

	private String[] calcEnvironment() throws CoreException {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		IBuilder builder = info.getDefaultConfiguration().getBuilder();
		HashMap<String, String> envMap = new HashMap<String, String>();
		if (builder.appendEnvironment()) {
			ICConfigurationDescription cfgDes = ManagedBuildManager.getDescriptionForConfiguration(builder.getParent().getParent());
			IEnvironmentVariableManager mngr = CCorePlugin.getDefault().getBuildEnvironmentManager();
			IEnvironmentVariable[] vars = mngr.getVariables(cfgDes, true);
			for (IEnvironmentVariable var : vars) {
				envMap.put(var.getName(), var.getValue());
			}
		}
		// Add variables from build info
		@SuppressWarnings("unchecked")
		Map<String, String> builderEnv = builder.getExpandedEnvironment();
		if(builderEnv != null)
			envMap.putAll(builderEnv);
		List<String> strings= new ArrayList<String>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet()) {
			StringBuffer buffer= new StringBuffer(entry.getKey());
			buffer.append('=').append(entry.getValue());
			strings.add(buffer.toString());
		}
		return strings.toArray(new String[strings.size()]);
	}

	private void checkLine(String text) {
		Line line = new Line(text);
		String cmd = line.getToken();
		if (cmd == null) {
			return;
		} else if (cmd.endsWith("g++")) {
			if (command == null || !cplusplus) {
				command = cmd;
				cplusplus = true;
			}
			gatherOptions(line);
		} else if (cmd.endsWith("gcc")) {
			if (command == null)
				command = cmd;
			gatherOptions(line);
		}
	}
	
	private static class Line {
		private final String line;
		private int pos;
		
		public Line(String line) {
			this.line = line;
		}

		public String getToken() {
			skipWhiteSpace();
			if (pos == line.length())
				return null;
			
			int start = pos;
			boolean inQuote = false;
			
			while (true) {
				char c = line.charAt(pos);
				if (c == ' ') {
					if (!inQuote)
						return line.substring(start, pos);
				} else if (c == '"') {
					inQuote = !inQuote;
				}
				
				if (++pos == line.length())
					return null;
			}

		}
		
		private void skipWhiteSpace() {
			if (pos == line.length())
				return;
			
			while (true) {
				char c = line.charAt(pos);
				if (c == ' ')
					pos++;
				else
					return;
			}
		}
	}
	
	private void addArg(String arg) {
		if (!arguments.contains(arg))
			arguments.add(arg);
	}
	
	private void gatherOptions(Line line) {
		for (String option = line.getToken(); option != null; option = line.getToken()) {
			if (option.startsWith("-")) {
				// only look at options
				if (option.equals("-I")) {
					String dir = line.getToken();
					if (dir != null)
						addArg(option + dir);
				} else if (option.startsWith("-I")) {
					addArg(option);
				} else if (option.equals("-D")) {
					String def = line.getToken();
					if (def != null)
						addArg(option + def);
				} else if (option.startsWith("-D")) {
					addArg(option);
				} else if (option.startsWith("-f")) {
					addArg(option);
				} else if (option.startsWith("-m")) {
					addArg(option);
				} else if (option.startsWith("--sysroot")) {
					addArg(option);
				}
			}
		}
	}
	
}

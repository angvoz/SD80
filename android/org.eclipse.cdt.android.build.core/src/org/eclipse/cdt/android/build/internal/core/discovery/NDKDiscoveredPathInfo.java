package org.eclipse.cdt.android.build.internal.core.discovery;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.internal.jobs.OrderedLock;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class NDKDiscoveredPathInfo implements IDiscoveredPathInfo {

	private final IProject project;
	private long lastUpdate = IFile.NULL_STAMP;
	
	public NDKDiscoveredPathInfo(IProject project) {
		this.project = project;
	}
	
	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public IPath[] getIncludePaths() {
		return new IPath[] { new Path("C:\\Android") };
	}

	@Override
	public Map<String, String> getSymbols() {
		return new HashMap<String, String>();
	}

	@Override
	public IDiscoveredScannerInfoSerializable getSerializable() {
		return null;
	}

	public void update(IProgressMonitor monitor) throws CoreException {
		// TODO Only check if Android.mk is newer
		if (!needsUpdating())
			return;
		clearInfo();
		
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
			// The general strategy is to create the discovered path info only if
			// the Android.mk file is newer than the last time we looked
			// To update run ndk-build -nvh (or something) to fake the build and extract the commands
			// Then run them through a specs parser thing
			
			recordUpdate();
		} catch (IOException e) {
			throw new CoreException(Activator.newStatus(e));
		}
	}

	private boolean needsUpdating() {
		if (lastUpdate == IFile.NULL_STAMP)
			return true;
		return project.getFile(new Path("jni/Android.mk")).getLocalTimeStamp() > lastUpdate;
	}
	
	private void recordUpdate() {
		lastUpdate = project.getFile(new Path("jni/Android.mk")).getLocalTimeStamp();
	}
	
	public void delete() {
		lastUpdate = IFile.NULL_STAMP;
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

	private void clearInfo() {
		
	}
	
	private void checkLine(String line) {
		int pos = scanToken(line, 0);
		String cmd = line.substring(0, pos);
		if (cmd.endsWith("g++")) {
			String gpp = cmd;
		}
			
	}
	
	private int skipWhiteSpace(String line, int pos) {
		if (pos == line.length())
			return pos;
		
		while (true) {
			char c = line.charAt(pos);
			if (c == ' ')
				pos++;
			else
				return pos;
		}
	}
	
	private int scanToken(String line, int pos) {
		pos = skipWhiteSpace(line, pos);
		if (pos == line.length())
			return -1;
		
		boolean inQuote = false;
		
		while (true) {
			char c = line.charAt(pos);
			if (c == ' ') {
				if (!inQuote)
					return pos;
			} else if (c == '"') {
				inQuote = !inQuote;
			}
			pos++;
		}
	}
}

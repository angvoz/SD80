package org.eclipse.cdt.android.build.internal.core;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class NDKCommandLauncher extends CommandLauncher {

	@Override
	public Process execute(IPath commandPath, String[] args, String[] env,
			IPath changeToDirectory, IProgressMonitor monitor)
			throws CoreException {
		if (Platform.getOS().equals(Platform.OS_WIN32) && commandPath.toString().equals("ndk-build")) {
			commandPath = new Path("sh");
			String[] newArgs = new String[2];
			newArgs[0] = "-c";
			String command = "ndk-build";
			for (String arg : args)
				command += " " + arg;
			newArgs[1] = command;
			args = newArgs;
		}
		return super.execute(commandPath, args, env, changeToDirectory, monitor);
	}
}

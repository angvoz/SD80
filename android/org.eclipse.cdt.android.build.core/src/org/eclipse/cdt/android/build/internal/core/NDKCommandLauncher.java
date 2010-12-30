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
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			String command = commandPath.toString();
			for (String arg : args)
				// TODO check for spaces in args
				command += " " + arg;
			commandPath = new Path("sh");
			args = new String[] { "-c", command };
		}
		return super.execute(commandPath, args, env, changeToDirectory, monitor);
	}
}

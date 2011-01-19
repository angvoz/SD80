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

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public class NDKCommandLauncher extends CommandLauncher {

	@Override
	public Process execute(IPath commandPath, String[] args, String[] env, IPath changeToDirectory, IProgressMonitor monitor)
			throws CoreException {
		if (Platform.getOS().equals(Platform.OS_WIN32) || Platform.getOS().equals(Platform.OS_MACOSX)) {
			// Neither Mac or Windows support calling the ndk-build script directly.
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

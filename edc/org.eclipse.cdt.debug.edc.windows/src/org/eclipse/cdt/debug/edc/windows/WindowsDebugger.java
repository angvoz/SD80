/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.windows;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.edc.MessageLogger;
import org.eclipse.cdt.debug.edc.launch.ChooseProcessItem;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.launch.LaunchUtils;
import org.eclipse.cdt.debug.edc.ui.ChooseProcessDialog;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class WindowsDebugger extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.edc.windows";

	// The shared instance
	private static WindowsDebugger plugin;

	/**
	 * The constructor
	 */
	public WindowsDebugger() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static WindowsDebugger getDefault() {
		return plugin;
	}

	public static BundleContext getBundleContext() {
		return getDefault().getBundle().getBundleContext();
	}

	public static MessageLogger getMessageLogger() {
		return new MessageLogger() {
			@Override
			public String getPluginID() {
				return PLUGIN_ID;
			}

			@Override
			public Plugin getPlugin() {
				return plugin;
			}
		};
	}

	public void launchProcess(final ILaunch launch, final IProcesses ps, final RequestMonitor requestMonitor) {
		try {
			ILaunchConfiguration cfg = ((EDCLaunch) launch).getLaunchConfiguration();

			// Get absolute program path.
			ICProject cproject = LaunchUtils.getCProject(cfg);
			// This works even if cproject is null.
			IPath program = LaunchUtils.verifyProgramPath(cfg, cproject); 
			final String file = program.toOSString();

			final String workingDirectory = LaunchUtils.getWorkingDirectoryPath(cfg);
			final String[] args = LaunchUtils.getProgramArgumentsArray(cfg);
			final Map<String, String> env = LaunchUtils.getEnvironmentVariables(cfg);
			final boolean append = cfg.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
			final boolean attach = true;

			final IProcesses.DoneGetEnvironment done_env = new IProcesses.DoneGetEnvironment() {
				public void doneGetEnvironment(IToken token, Exception error, Map<String, String> def) {
					if (error != null) {
						requestMonitor.setStatus(new Status(IStatus.ERROR, PLUGIN_ID, error.getLocalizedMessage(),
								error));
						requestMonitor.done();
						return;
					}
					Map<String, String> vars = new HashMap<String, String>();
					if (append)
						vars.putAll(def);
					if (env != null)
						vars.putAll(env);
					ps.start(workingDirectory, file, args, vars, attach, new IProcesses.DoneStart() {

						public void doneStart(IToken token, Exception error, ProcessContext process) {
							if (error != null) {
								requestMonitor.setStatus(new Status(IStatus.ERROR, PLUGIN_ID, error
										.getLocalizedMessage(), error));
								requestMonitor.done();
								return;
							}

							requestMonitor.done();
						}
					});
				}
			};

			if (append) {
				Protocol.invokeLater(new Runnable() {
					public void run() {
						ps.getEnvironment(done_env);
					}
				});
			} else {
				done_env.doneGetEnvironment(null, null, null);
			}
		} catch (Exception x) {
			requestMonitor.setStatus(new Status(IStatus.ERROR, PLUGIN_ID, x.getLocalizedMessage(), x));
			requestMonitor.done();
		}
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	static public ChooseProcessItem chooseProcess(final ChooseProcessItem[] processes) throws CoreException {
		final ChooseProcessItem selectedProcessItem[] = { null };
		final boolean chooseProcessCanceled[] = { false };

		Display.getDefault().syncExec(new Runnable() {

			public void run() {
				ChooseProcessDialog dialog = new ChooseProcessDialog(processes, "", Display.getDefault()
						.getActiveShell());
				int dialogResult = dialog.open();

				if (dialogResult == Window.OK) {
					selectedProcessItem[0] = dialog.getSelectedProcess();
				} else {
					chooseProcessCanceled[0] = true;
				}
			}

		});

		if (chooseProcessCanceled[0]) {
			String msg = "user canceled selection of process";
			IStatus status = new Status(IStatus.CANCEL, WindowsDebugger.PLUGIN_ID, msg);
			throw new CoreException(status);
		}
		return selectedProcessItem[0];
	}
}

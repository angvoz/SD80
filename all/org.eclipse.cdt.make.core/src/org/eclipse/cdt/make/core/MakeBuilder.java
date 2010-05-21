/*******************************************************************************
 * Copyright (c) 2000, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Tianchao Li (tianchao.li@gmail.com) - arbitrary build directory (bug #136136)
 *     Dmitry Kozlov (CodeSourcery) - Build error highlighting and navigation
 *                                    Save build output  (bug 294106)
 *     Andrew Gvozdev (Quoin Inc)   - Saving build output implemented in different way (bug 306222)
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.ICommandLauncher;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class MakeBuilder extends ACBuilder {

	public final static String BUILDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".makeBuilder"; //$NON-NLS-1$

	public MakeBuilder() {
	}

	/**
	 * @see IncrementalProjectBuilder#build
	 */
	@Override
	protected IProject[] build(int kind, @SuppressWarnings("rawtypes") Map args0, IProgressMonitor monitor) throws CoreException {
		@SuppressWarnings("unchecked")
		Map<String, String> args = args0;
		if (DEBUG_EVENTS)
			printEvent(kind, args);

		boolean bPerformBuild = true;
		IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(args, MakeBuilder.BUILDER_ID);
		if (!shouldBuild(kind, info)) {
			return new IProject[0];
		}
		if (kind == IncrementalProjectBuilder.AUTO_BUILD) {
			IResourceDelta delta = getDelta(getProject());
			if (delta != null) {
				IResource res = delta.getResource();
				if (res != null) {
					bPerformBuild = res.getProject().equals(getProject());
				}
			} else {
				bPerformBuild = false;
			}
		}
		if (bPerformBuild) {
			boolean isClean = invokeMake(kind, info, monitor);
			if (isClean) {
				forgetLastBuiltState();
			}
		}
		checkCancel(monitor);
		return getProject().getReferencedProjects();
	}


	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		if (DEBUG_EVENTS)
			printEvent(IncrementalProjectBuilder.CLEAN_BUILD, null);

		final IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(getProject(), BUILDER_ID);
		if (shouldBuild(CLEAN_BUILD, info)) {
			IResourceRuleFactory ruleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
			final ISchedulingRule rule = ruleFactory.modifyRule(getProject());
			Job backgroundJob = new Job("Standard Make Builder"){  //$NON-NLS-1$
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

							public void run(IProgressMonitor monitor) {
								invokeMake(CLEAN_BUILD, info, monitor);
							}
						}, rule, IWorkspace.AVOID_UPDATE, monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
					IStatus returnStatus = Status.OK_STATUS;
					return returnStatus;
				}


			};

			backgroundJob.setRule(rule);
			backgroundJob.schedule();
		}
	}

	protected boolean invokeMake(int kind, IMakeBuilderInfo info, IProgressMonitor monitor) {
		boolean isClean = false;
		IProject currProject = getProject();

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask(MakeMessages.getString("MakeBuilder.Invoking_Make_Builder") + currProject.getName(), 100); //$NON-NLS-1$

		try {
			IPath buildCommand = info.getBuildCommand();
			if (buildCommand != null) {
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(currProject);

				OutputStream cos = console.getOutputStream();

				// remove all markers for this project
				removeAllMarkers(currProject);

				IPath workingDirectory = MakeBuilderUtil.getBuildDirectory(currProject, info);
				URI workingDirectoryURI = MakeBuilderUtil.getBuildDirectoryURI(currProject, info);

				String[] targets = getTargets(kind, info);
				if (targets.length != 0 && targets[targets.length - 1].equals(info.getCleanBuildTarget()))
					isClean = true;

				String errMsg = null;
				ICommandLauncher launcher = new CommandLauncher();
				launcher.setProject(currProject);
				// Print the command for visual interaction.
				launcher.showCommand(true);

				// Set the environment
				HashMap<String, String> envMap = new HashMap<String, String>();
				if (info.appendEnvironment()) {
					@SuppressWarnings({"unchecked", "rawtypes"})
					Map<String, String> env = (Map)launcher.getEnvironment();
					envMap.putAll(env);
				}
				// Add variables from build info
				envMap.putAll(info.getExpandedEnvironment());
				List<String> strings= new ArrayList<String>(envMap.size());
				Set<Entry<String, String>> entrySet = envMap.entrySet();
				for (Entry<String, String> entry : entrySet) {
					StringBuffer buffer= new StringBuffer(entry.getKey());
					buffer.append('=').append(entry.getValue());
					strings.add(buffer.toString());
				}
				String[] env = strings.toArray(new String[strings.size()]);
				String[] buildArguments = targets;
				if (info.isDefaultBuildCmd()) {
					if (!info.isStopOnError()) {
						buildArguments = new String[targets.length + 1];
						buildArguments[0] = "-k"; //$NON-NLS-1$
						System.arraycopy(targets, 0, buildArguments, 1, targets.length);
					}
				} else {
					String args = info.getBuildArguments();
					if (args != null && !args.equals("")) { //$NON-NLS-1$
						String[] newArgs = makeArray(args);
						buildArguments = new String[targets.length + newArgs.length];
						System.arraycopy(newArgs, 0, buildArguments, 0, newArgs.length);
						System.arraycopy(targets, 0, buildArguments, newArgs.length, targets.length);
					}
				}
//					MakeRecon recon = new MakeRecon(buildCommand, buildArguments, env, workingDirectory, makeMonitor, cos);
//					recon.invokeMakeRecon();
//					cos = recon;
				QualifiedName qName = new QualifiedName(MakeCorePlugin.getUniqueIdentifier(), "progressMonitor"); //$NON-NLS-1$
				Integer last = (Integer)getProject().getSessionProperty(qName);
				if (last == null) {
					last = new Integer(100);
				}
				ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectoryURI, this, info.getErrorParsers());
				epm.setOutputStream(cos);
				StreamMonitor streamMon = new StreamMonitor(new SubProgressMonitor(monitor, 100), epm, last.intValue());
				OutputStream stdout = streamMon;
				OutputStream stderr = streamMon;
				// Sniff console output for scanner info
				ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.getMakeBuilderOutputSniffer(
						stdout, stderr, getProject(), workingDirectory, null, this, null);
				OutputStream consoleOut = (sniffer == null ? stdout : sniffer.getOutputStream());
				OutputStream consoleErr = (sniffer == null ? stderr : sniffer.getErrorStream());
				Process p = launcher.execute(buildCommand, buildArguments, env, workingDirectory, monitor);
				if (p != null) {
					try {
						// Close the input of the Process explicitly.
						// We will never write to it.
						p.getOutputStream().close();
					} catch (IOException e) {
					}
					// Before launching give visual cues via the monitor
					monitor.subTask(MakeMessages.getString("MakeBuilder.Invoking_Command") + launcher.getCommandLine()); //$NON-NLS-1$
					if (launcher.waitAndRead(consoleOut, consoleErr, new SubProgressMonitor(monitor, 0))
						!= ICommandLauncher.OK)
						errMsg = launcher.getErrorMessage();
					monitor.subTask(MakeMessages.getString("MakeBuilder.Updating_project")); //$NON-NLS-1$
					refreshProject(currProject);
				} else {
					errMsg = launcher.getErrorMessage();
				}
				getProject().setSessionProperty(qName, !monitor.isCanceled() && !isClean ? new Integer(streamMon.getWorkDone()) : null);

				if (errMsg != null) {
					StringBuffer buf = new StringBuffer(buildCommand.toString() + " "); //$NON-NLS-1$
					for (String buildArgument : buildArguments) {
						buf.append(buildArgument);
						buf.append(' ');
					}

					String errorDesc = MakeMessages.getFormattedString("MakeBuilder.buildError", buf.toString()); //$NON-NLS-1$
					buf = new StringBuffer(errorDesc);
					buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
					cos.write(buf.toString().getBytes());
					cos.flush();
				}

				stdout.close();
				stderr.close();

				monitor.subTask(MakeMessages.getString("MakeBuilder.Creating_Markers")); //$NON-NLS-1$
				consoleOut.close();
				consoleErr.close();
				cos.close();
			}
		} catch (Exception e) {
			MakeCorePlugin.log(e);
		} finally {
			monitor.done();
		}
		return (isClean);
	}

	/**
	 * Refresh project. Can be overridden to not call actual refresh or to do something else.
	 * Method is called after build is complete.
	 * 
	 * @since 6.0
	 */
	protected void refreshProject(IProject project) {
		try {
			// Do not allow the cancel of the refresh, since the builder is external
			// to Eclipse, files may have been created/modified and we will be out-of-sync.
			// The caveat is for huge projects, it may take sometimes at every build.
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			MakeCorePlugin.log(e);
		}
	}


	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel(IProgressMonitor monitor) {
		if (monitor != null && monitor.isCanceled())
			throw new OperationCanceledException();
	}

	protected boolean shouldBuild(int kind, IMakeBuilderInfo info) {
		switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				return info.isAutoBuildEnable();
			case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
			case IncrementalProjectBuilder.FULL_BUILD :
				return info.isFullBuildEnabled() | info.isIncrementalBuildEnabled() ;
			case IncrementalProjectBuilder.CLEAN_BUILD :
				return info.isCleanBuildEnabled();
		}
		return true;
	}

	protected String[] getTargets(int kind, IMakeBuilderInfo info) {
		String targets = ""; //$NON-NLS-1$
		switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				targets = info.getAutoBuildTarget();
				break;
			case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
			case IncrementalProjectBuilder.FULL_BUILD :
				targets = info.getIncrementalBuildTarget();
				break;
			case IncrementalProjectBuilder.CLEAN_BUILD :
				targets = info.getCleanBuildTarget();
				break;
		}
		return makeArray(targets);
	}

	// Turn the string into an array.
	private String[] makeArray(String string) {
		return CommandLineUtil.argumentsToArray(string);
	}

	private void removeAllMarkers(IProject currProject) throws CoreException {
		IWorkspace workspace = currProject.getWorkspace();

		// remove all markers
		IMarker[] markers = currProject.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null) {
			workspace.deleteMarkers(markers);
		}
	}
}

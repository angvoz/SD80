/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuildCommandParser;
import org.eclipse.cdt.make.core.scannerconfig.AbstractBuiltinSpecsDetector;
import org.eclipse.cdt.managedbuilder.buildmodel.BuildDescriptionManager;
import org.eclipse.cdt.managedbuilder.buildmodel.IBuildDescription;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildDescription;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildStateManager;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DescriptionBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IBuildModelBuilder;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IConfigurationBuildState;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.IProjectBuildState;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.ParallelBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * The build runner for the internal builder.
 *
 * @author dschaefer
 * @since 8.0
 */
public class InternalBuildRunner implements IBuildRunner {

	private static final String INTERNAL_BUILDER = "ManagedMakeBuilder.message.internal.builder";	//$NON-NLS-1$
	private static final String TYPE_INC = "ManagedMakeBuider.type.incremental";	//$NON-NLS-1$
	private static final String TYPE_REBUILD = "ManagedMakeBuider.type.rebuild";	//$NON-NLS-1$
	private static final String CONSOLE_HEADER = "ManagedMakeBuilder.message.console.header";	//$NON-NLS-1$
	private static final String INTERNAL_BUILDER_HEADER_NOTE = "ManagedMakeBuilder.message.internal.builder.header.note";	//$NON-NLS-1$
	private static final String WARNING_UNSUPPORTED_CONFIGURATION = "ManagedMakeBuilder.warning.unsupported.configuration";	//$NON-NLS-1$
	private static final String BUILD_FINISHED = "ManagedMakeBuilder.message.finished";	//$NON-NLS-1$
	private static final String BUILD_CANCELLED = "ManagedMakeBuilder.message.cancelled";	//$NON-NLS-1$
	private static final String BUILD_FINISHED_WITH_ERRS = "ManagedMakeBuilder.message.finished.with.errs";	//$NON-NLS-1$
	private static final String BUILD_STOPPED_ERR = "ManagedMakeBuilder.message.stopped.error";	//$NON-NLS-1$
	private static final String BUILD_FAILED_ERR = "ManagedMakeBuilder.message.internal.builder.error";	//$NON-NLS-1$
	private static final String MARKERS = "ManagedMakeBuilder.message.creating.markers";	//$NON-NLS-1$
	private static final String NOTHING_BUILT = "ManagedMakeBuilder.message.no.build";	//$NON-NLS-1$
	private static final String BUILD_ERROR = "ManagedMakeBuilder.message.error";	//$NON-NLS-1$


	// TODO: same function is present in CommandBuilder and BuildProcessManager
	private String[] mapToStringArray(Map<String, String> map){
		if(map == null)
			return null;

		List<String> list = new ArrayList<String>();

		for (Entry<String, String> entry : map.entrySet()) {
			list.add(entry.getKey() + '=' + entry.getValue());
		}

		return list.toArray(new String[list.size()]);
	}

	public boolean invokeBuild(int kind, IProject project, IConfiguration configuration,
			IBuilder builder, IConsole console, IMarkerGenerator markerGenerator,
			IncrementalProjectBuilder projectBuilder, IProgressMonitor monitor) throws CoreException {
		boolean isParallel = builder.isParallelBuildOn() && builder.getParallelizationNum() > 1;
//		boolean buildIncrementaly = true;
		boolean resumeOnErr = !builder.isStopOnError();

		// Get the project and make sure there's a monitor to cancel the build
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		String[] msgs = new String[2];
		msgs[0] = ManagedMakeMessages.getResourceString(INTERNAL_BUILDER);
		msgs[1] = project.getName();

		ConsoleOutputStream consoleOutStream = null;
		OutputStream epmOutputStream = null;
		try {
			int flags = 0;
			IResourceDelta delta = projectBuilder.getDelta(project);
			BuildStateManager bsMngr = BuildStateManager.getInstance();
			IProjectBuildState pBS = bsMngr.getProjectBuildState(project);
			IConfigurationBuildState cBS = pBS.getConfigurationBuildState(configuration.getId(), true);

//			if(delta != null){
				flags = BuildDescriptionManager.REBUILD | BuildDescriptionManager.REMOVED | BuildDescriptionManager.DEPS;
//				delta = getDelta(currentProject);
//			}

			boolean buildIncrementaly = delta != null;

			// Get a build console for the project
			StringBuffer buf = new StringBuffer();

			IBuildDescription des = BuildDescriptionManager.createBuildDescription(configuration, cBS, delta, flags);

			IPath workingDirectory = des.getDefaultBuildDirLocation();
			String[] env = null;
			if (des instanceof BuildDescription) {
				Map<String, String> envMap = ((BuildDescription)des).getEnvironment();
				env = mapToStringArray(envMap);
			}

			consoleOutStream = console.getOutputStream();
			String[] consoleHeader = new String[3];
			if(buildIncrementaly)
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_INC);
			else
				consoleHeader[0] = ManagedMakeMessages.getResourceString(TYPE_REBUILD);

			consoleHeader[1] = configuration.getName();
			consoleHeader[2] = project.getName();
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(ManagedMakeMessages.getFormattedString(CONSOLE_HEADER, consoleHeader));
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$

			buf.append(ManagedMakeMessages.getResourceString(INTERNAL_BUILDER_HEADER_NOTE));
			buf.append("\n"); //$NON-NLS-1$

			if(!configuration.isSupported()){
				buf.append(ManagedMakeMessages.getFormattedString(WARNING_UNSUPPORTED_CONFIGURATION,
						new String[] { configuration.getName(), configuration.getToolChain().getName()}));
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
				buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
			}

			if (kind!=IncrementalProjectBuilder.CLEAN_BUILD) {
				ICConfigurationDescription cfgDescription = ManagedBuildManager.getDescriptionForConfiguration(configuration);
				runBuiltinSpecsDetectors(cfgDescription, workingDirectory, env, console, monitor);

				List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
				for (ILanguageSettingsProvider provider : providers) {
					if (provider instanceof AbstractBuildCommandParser) {
						buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
						String msg = ManagedMakeMessages.getFormattedString("BOP Language Settings Provider [{0}] is not supported by Internal Builder.", provider.getName());
						buf.append("**** "+msg+" ****");
						buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$
						buf.append(System.getProperty("line.separator", "\n"));	//$NON-NLS-1$	//$NON-NLS-2$

						ManagedBuilderCorePlugin.error(msg);
					}
				}
			}

			consoleOutStream.write(buf.toString().getBytes());
			consoleOutStream.flush();

			DescriptionBuilder dBuilder = null;
			if (!isParallel)
				dBuilder = new DescriptionBuilder(des, buildIncrementaly, resumeOnErr, cBS);

			if(isParallel || dBuilder.getNumCommands() > 0) {
				// Remove all markers for this project
				IWorkspace workspace = project.getWorkspace();
				IMarker[] markers = project.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
				if (markers != null)
					workspace.deleteMarkers(markers);

				// Hook up an error parser manager
				String[] errorParsers = builder.getErrorParsers();
				ErrorParserManager epm = new ErrorParserManager(project, des.getDefaultBuildDirLocationURI(), markerGenerator, errorParsers);
				epm.setOutputStream(consoleOutStream);
				// This variable is necessary to ensure that the EPM stream stay open
				// until we explicitly close it. See bug#123302.
				epmOutputStream = epm.getOutputStream();

				int status = 0;

				long t1 = System.currentTimeMillis();
				if (isParallel)
					status = ParallelBuilder.build(des, null, null, epmOutputStream, epmOutputStream, monitor, resumeOnErr, buildIncrementaly);
				else
				    status = dBuilder.build(epmOutputStream, epmOutputStream, monitor);
				long t2 = System.currentTimeMillis();

				// Report either the success or failure of our mission
				buf = new StringBuffer();

				switch(status){
				case IBuildModelBuilder.STATUS_OK:
					buf.append(ManagedMakeMessages
							.getFormattedString(BUILD_FINISHED,
									project.getName()));
					break;
				case IBuildModelBuilder.STATUS_CANCELLED:
					buf.append(ManagedMakeMessages
							.getResourceString(BUILD_CANCELLED));
					break;
				case IBuildModelBuilder.STATUS_ERROR_BUILD:
					String msg = resumeOnErr ?
							ManagedMakeMessages.getResourceString(BUILD_FINISHED_WITH_ERRS) :
								ManagedMakeMessages.getResourceString(BUILD_STOPPED_ERR);
					buf.append(msg);
					break;
				case IBuildModelBuilder.STATUS_ERROR_LAUNCH:
				default:
					buf.append(ManagedMakeMessages
							.getResourceString(BUILD_FAILED_ERR));
					break;
				}
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$

				// Report time and number of threads used
				buf.append(ManagedMakeMessages.getFormattedString("CommonBuilder.6", Integer.toString((int)(t2 - t1)))); //$NON-NLS-1$
//				buf.append(t2 - t1);
//				buf.append(" ms.  ");
				if (isParallel) {
					buf.append(ManagedMakeMessages.getFormattedString("CommonBuilder.7", Integer.toString(ParallelBuilder.lastThreadsUsed))); //$NON-NLS-1$
//					buf.append(ParallelBuilder.lastThreadsUsed);
				}
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
				// Write message on the console
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
				epmOutputStream.close();
				epmOutputStream = null;
				if (kind!=IncrementalProjectBuilder.CLEAN_BUILD) {
					LanguageSettingsManager.serializeWorkspaceProviders();
				}

				// Generate any error markers that the build has discovered
				monitor.subTask(ManagedMakeMessages
						.getResourceString(MARKERS));

				bsMngr.setProjectBuildState(project, pBS);
			} else {
				buf = new StringBuffer();
				buf.append(ManagedMakeMessages.getFormattedString(NOTHING_BUILT, project.getName()));
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				consoleOutStream.write(buf.toString().getBytes());
				consoleOutStream.flush();
			}

		} catch (Exception e) {
			if(consoleOutStream != null){
				StringBuffer buf = new StringBuffer();
				String errorDesc = ManagedMakeMessages
							.getResourceString(BUILD_ERROR);
				buf.append(errorDesc);
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$
				buf.append(e.getLocalizedMessage());
				buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$//$NON-NLS-2$

				try {
					consoleOutStream.write(buf.toString().getBytes());
					consoleOutStream.flush();
				} catch (IOException e1) {
				}
			}
			projectBuilder.forgetLastBuiltState();
		} finally {
			if(epmOutputStream != null){
				try {
					epmOutputStream.close();
				} catch (IOException e) {
				}
			}
			if(consoleOutStream != null){
				try {
					consoleOutStream.close();
				} catch (IOException e) {
				}
			}
			monitor.done();
		}
		return false;
	}

	// TODO: same copy in ExternalBuildRunner
	private void runBuiltinSpecsDetectors(ICConfigurationDescription cfgDescription, IPath workingDirectory,
			String[] env, IConsole console, IProgressMonitor monitor) throws CoreException {
		ICFolderDescription rootFolderDescription = cfgDescription.getRootFolderDescription();
		List<String> languageIds = new ArrayList<String>();
		for (ICLanguageSetting languageSetting : rootFolderDescription.getLanguageSettings()) {
			String id = languageSetting.getLanguageId();
			if (id!=null) {
				languageIds.add(id);
			}
		}

		for (ILanguageSettingsProvider lsProvider : cfgDescription.getLanguageSettingProviders()) {
			if (lsProvider instanceof AbstractBuiltinSpecsDetector) {
				AbstractBuiltinSpecsDetector detector = (AbstractBuiltinSpecsDetector)lsProvider;
					for (String languageId : languageIds) {
					if (detector.getLanguageIds()==null || detector.getLanguageIds().contains(languageId)) {
						try {
							detector.startup(cfgDescription, languageId);
							detector.run(workingDirectory, env, console, monitor);
						} catch (Exception e) {
							ManagedBuilderCorePlugin.log(e);
						}
					}
				}
			}
		}

		// AG: FIXME
//		LanguageSettingsManager.serialize(cfgDescription);
		// AG: FIXME - rather send event that ls settings changed
		IProject project = cfgDescription.getProjectDescription().getProject();
		ICProject icProject = CoreModel.getDefault().create(project);
		ICElement[] tuSelection = new ICElement[] {icProject};
		CCorePlugin.getIndexManager().update(tuSelection, IIndexManager.UPDATE_ALL | IIndexManager.UPDATE_EXTERNAL_FILES_FOR_PROJECT);
	}

}

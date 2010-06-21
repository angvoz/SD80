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
package org.eclipse.cdt.debug.edc.launch;

import java.io.FileNotFoundException;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

public class LaunchUtils {

	/**
	 * Verify the following things about the project: - is a valid project name
	 * given - does the project exist - is the project open - is the project a
	 * C/C++ project
	 */
	public static ICProject verifyCProject(ILaunchConfiguration configuration) throws CoreException {
		String name = getProjectName(configuration);
		if (name == null) {
			abort(LaunchMessages.getString("LaunchUtils.C_Project_not_specified"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
			return null;
		}
		ICProject cproject = getCProject(configuration);
		if (cproject == null && name.length() > 0) {
			IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (!proj.exists()) {
				abort(LaunchMessages.getFormattedString("LaunchUtils.Project_NAME_does_not_exist", name), null, //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
			} else if (!proj.isOpen()) {
				abort(LaunchMessages.getFormattedString("LaunchUtils.Project_NAME_is_closed", name), null, //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
			}
			abort(LaunchMessages.getString("LaunchUtils.Not_a_C_CPP_project"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
		}
		return cproject;
	}

	/**
	 * Verify that program name of the configuration can be found as a file.
	 * 
	 * @return Absolute path of the program location
	 */
	public static IPath verifyProgramPath(ILaunchConfiguration configuration, ICProject cproject) throws CoreException {
		// Note this assumes CDT launch configuration main tab is used.
		String programName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,
				(String) null);
		if (programName == null) {
			abort(LaunchMessages.getString("LaunchUtils.Program_file_not_specified"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
		}

		IPath programPath = new Path(programName);
		if (programPath.isEmpty()) {
			abort(LaunchMessages.getString("LaunchUtils.Program_file_does_not_exist"), null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
		}

		if (!programPath.isAbsolute() && cproject != null) {
			// Find the specified program within the specified project
			IFile wsProgramPath = cproject.getProject().getFile(programPath);
			programPath = wsProgramPath.getLocation();
		}

		if (!programPath.toFile().exists()) {
			abort(LaunchMessages.getString("LaunchUtils.Program_file_does_not_exist"), //$NON-NLS-1$
					new FileNotFoundException(LaunchMessages.getFormattedString("LaunchUtils.PROGRAM_PATH_not_found", //$NON-NLS-1$ 
							programPath.toOSString())), ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}

		return programPath;
	}

	/**
	 * Throws a core exception with an error status object built from the given
	 * message, lower level exception, and error code.
	 * 
	 * @param message
	 *            the status message
	 * @param exception
	 *            lower level exception associated with the error, or
	 *            <code>null</code> if none
	 * @param code
	 *            error code
	 */
	private static void abort(String message, Throwable exception, int code) throws CoreException {
		MultiStatus status = new MultiStatus(EDCDebugger.getUniqueIdentifier(), code, message, exception);
		status.add(new Status(IStatus.ERROR, EDCDebugger.getUniqueIdentifier(), code,
				exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
				exception));
		throw new CoreException(status);
	}

	/**
	 * Returns an ICProject based on the project name provided in the
	 * configuration. First look for a project by name, and then confirm it is a
	 * C/C++ project.
	 */
	public static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				if (cProject != null && cProject.exists()) {
					return cProject;
				}
			}
		}
		return null;
	}

	private static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
	}
	
	/**
	 * Convenience method.
	 */
	public static IStringVariableManager getStringVariableManager() {
		return VariablesPlugin.getDefault().getStringVariableManager();
	}
	
	public static String getWorkingDirectoryPath(ILaunchConfiguration config) throws CoreException {
		String location = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, "");
		if (location != null) {
			String expandedLocation = LaunchUtils.getStringVariableManager().performStringSubstitution(location);
			if (expandedLocation.length() > 0) {
				return expandedLocation;
			}
		}
		return "";
	}

	/**
	 * @since 2.0
	 */
	public static String[] getProgramArgumentsArray(ILaunchConfiguration config) throws CoreException {
		return org.eclipse.cdt.launch.LaunchUtils.getProgramArgumentsArray(config);
	}
	
	/**
	 * @since 2.0
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> getEnvironmentVariables(ILaunchConfiguration config) throws CoreException {
		return config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map<?,?>) null);
	}

}

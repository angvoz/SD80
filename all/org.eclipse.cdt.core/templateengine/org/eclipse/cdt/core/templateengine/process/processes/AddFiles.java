/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process.processes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessHelper;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;


/**
 * Adds Files to the Project
 */
public class AddFiles extends ProcessRunner {
	
	/**
	 * This method Adds the list of Files to the corresponding Project.
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		ProcessArgument[][] files = args[1].getComplexArrayValue();
		for(int i=0; i<files.length; i++) {
			ProcessArgument[] file = files[i];
			String fileSourcePath = file[0].getSimpleValue();
			String fileTargetPath = file[1].getSimpleValue();
			boolean replaceable = file[2].getSimpleValue().equals("true"); //$NON-NLS-1$
	
			URL path;
			try {
				path = TemplateEngineHelper.getTemplateResourceURLRelativeToTemplate(template, fileSourcePath);
				if (path == null) {
					throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddFiles.1") + fileSourcePath)); //$NON-NLS-1$
				}
			} catch (IOException e1) {
				throw new ProcessFailureException(Messages.getString("AddFiles.2") + fileSourcePath); //$NON-NLS-1$
			}
	
			InputStream contents = null;
			if (replaceable) {
				String fileContents;
				try {
					fileContents = ProcessHelper.readFromFile(path);
				} catch (IOException e) {
					throw new ProcessFailureException(Messages.getString("AddFiles.3") + fileSourcePath); //$NON-NLS-1$
				}
				fileContents = ProcessHelper.getValueAfterExpandingMacros(fileContents, ProcessHelper.getReplaceKeys(fileContents), template.getValueStore());
				contents = new ByteArrayInputStream(fileContents.getBytes());
			} else {
				try {
					contents = path.openStream();
				} catch (IOException e) {
					throw new ProcessFailureException(getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddFiles.4") + fileSourcePath)); //$NON-NLS-1$
				}
			}
	
			try {
				IFile iFile = projectHandle.getFile(fileTargetPath);
				if (!iFile.getParent().exists()) {
					ProcessHelper.mkdirs(projectHandle, projectHandle.getFolder(iFile.getParent().getProjectRelativePath()));
				}
				
				if (iFile.exists()) {
					// honor the replaceable flag and replace the file contents if the file already exists.
					if (replaceable) {
						iFile.setContents(contents, true, true, null);
					} else {
						throw new ProcessFailureException(Messages.getString("AddFiles.5")); //$NON-NLS-1$
					}
					
				} else {
					iFile.create(contents, true, null);
					iFile.refreshLocal(IResource.DEPTH_ONE, null);					
				}
			} catch (CoreException e) {
				throw new ProcessFailureException(Messages.getString("AddFiles.6") + e.getMessage(), e); //$NON-NLS-1$
			}
		}
		try {
			projectHandle.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			throw new ProcessFailureException(Messages.getString("AddFiles.7") + e.getMessage(), e); //$NON-NLS-1$
		}
	}
}

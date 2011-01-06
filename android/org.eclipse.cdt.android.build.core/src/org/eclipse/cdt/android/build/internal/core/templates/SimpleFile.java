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
package org.eclipse.cdt.android.build.internal.core.templates;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.android.build.internal.core.Activator;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class SimpleFile extends ProcessRunner {

	private static final class FileOp {
		public String source;
		public String destination;
	}
	
	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {

		// Fetch the args
		String projectName = null;
		List<FileOp> fileOps = new ArrayList<FileOp>();
		
		for (ProcessArgument arg : args) {
			if (arg.getName().equals("projectName"))
				projectName = arg.getSimpleValue();
			else if (arg.getName().equals("files")) {
				ProcessArgument[][] files = arg.getComplexArrayValue();
				for (ProcessArgument[] file : files) {
					FileOp op = new FileOp();
					for (ProcessArgument fileArg : file) {
						if (fileArg.getName().equals("source"))
							op.source = fileArg.getSimpleValue();
						else if (fileArg.getName().equals("destination"))
							op.destination = fileArg.getSimpleValue();
					}
					if (op.source == null || op.destination == null)
						throw new ProcessFailureException("bad file op");
					fileOps.add(op);
				}
			}
		}
		
		if (projectName == null)
			throw new ProcessFailureException("no project name");
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists())
			throw new ProcessFailureException("project does not exist");
		
		// Find bundle to find source files
		Bundle bundle = Activator.getBundle(template.getTemplateInfo().getPluginId());
		if (bundle == null)
			throw new ProcessFailureException("bundle not found");
		
		try {
			for (FileOp op : fileOps) {
				IFile destFile = project.getFile(new Path(op.destination));
				if (destFile.exists())
					// don't overwrite files if they exist already
					continue;

				// Make sure parent folders are created
				mkDirs(project, destFile.getParent(), monitor);

				URL sourceURL = FileLocator.find(bundle, new Path(op.source), null);
				if (sourceURL == null)
					throw new ProcessFailureException("could not find source file: " + op.source);
				
				TemplatedInputStream in = new TemplatedInputStream(sourceURL.openStream(), template.getValueStore());
				destFile.create(in, true, monitor);
				in.close();
			}			
		} catch (IOException e) {
			throw new ProcessFailureException(e);
		} catch (CoreException e) {
			throw new ProcessFailureException(e);
		}

	}

	private void mkDirs(IProject project, IContainer container, IProgressMonitor monitor) throws CoreException {
		if (container.exists())
			return;
		mkDirs(project, container.getParent(), monitor);
		((IFolder)container).create(true, true, monitor);
	}

}

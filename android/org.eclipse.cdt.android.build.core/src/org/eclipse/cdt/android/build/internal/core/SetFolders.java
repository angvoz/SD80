package org.eclipse.cdt.android.build.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class SetFolders extends ProcessRunner {

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		String projectName = null;
		String[] sourceFolders = null;
		String[] outputFolders = null;
		
		for (ProcessArgument arg : args) {
			String argName = arg.getName();
			if (argName.equals("projectName")) {
				projectName = arg.getSimpleValue();
			} else if (argName.equals("sourceFolders")) {
				sourceFolders = arg.getSimpleArrayValue();
			} else if (argName.equals("outputFolders")) {
				outputFolders = arg.getSimpleArrayValue();
			}
		}
		
		// Get the project
		if (projectName == null)
			throw new ProcessFailureException("Missing project name");
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (!project.exists())
			throw new ProcessFailureException("Project does not exist");
		
		// Create the folders
		if (sourceFolders == null && outputFolders == null)
			throw new ProcessFailureException("No folders");
		
		try {
			// Add them in
			ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);
			IPathEntry[] pathEntries = cproject.getRawPathEntries();
			List<IPathEntry> newEntries = new ArrayList<IPathEntry>(pathEntries.length);
			for (IPathEntry pathEntry : pathEntries) {
				// remove the old source and output entries
				if (pathEntry.getEntryKind() != IPathEntry.CDT_SOURCE
						&& pathEntry.getEntryKind() != IPathEntry.CDT_OUTPUT) {
					newEntries.add(pathEntry);
				}
			}
			if (sourceFolders != null)
				for (String sourceFolder : sourceFolders) {
					IFolder folder = project.getFolder(new Path(sourceFolder));
					if (!folder.exists())
						folder.create(true, true, monitor);
					newEntries.add(CoreModel.newSourceEntry(folder.getFullPath()));
				}
			if (outputFolders != null)
				for (String outputFolder : outputFolders) {
					IFolder folder = project.getFolder(new Path(outputFolder));
					if (!folder.exists())
						folder.create(true, true, monitor);
					newEntries.add(CoreModel.newOutputEntry(folder.getFullPath()));
				}
			cproject.setRawPathEntries(newEntries.toArray(new IPathEntry[newEntries.size()]), monitor);
		} catch (CoreException e) {
			throw new ProcessFailureException(e);
		}
	}

}

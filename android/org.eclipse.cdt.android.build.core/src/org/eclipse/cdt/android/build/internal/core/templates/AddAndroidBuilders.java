package org.eclipse.cdt.android.build.internal.core.templates;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class AddAndroidBuilders extends ProcessRunner {

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		try {
			String projectName = template.getValueStore().get("projectName");
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			
			IProjectDescription projectDesc = project.getDescription();
			ICommand[] oldCommands = projectDesc.getBuildSpec();
			ICommand[] newCommands = new ICommand[oldCommands.length + 1];
			System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
			
			ICommand apkBuilder = projectDesc.newCommand();
			apkBuilder.setBuilderName("");
			newCommands[newCommands.length - 1] = apkBuilder;
			projectDesc.setBuildSpec(newCommands);
		} catch (CoreException e) {
			throw new ProcessFailureException(e);
		}
	}

}

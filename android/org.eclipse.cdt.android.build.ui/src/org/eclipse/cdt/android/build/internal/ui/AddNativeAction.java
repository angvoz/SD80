package org.eclipse.cdt.android.build.internal.ui;
import java.io.IOException;

import org.eclipse.cdt.android.build.core.NDKManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class AddNativeAction implements IObjectActionDelegate {

	private ISelection selection;

	@Override
	public void run(IAction action) {
		IProject project = null;
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if (ss.size() == 1) {
				Object obj = ss.getFirstElement();
				if (obj instanceof IProject) {
					project = (IProject)obj;
				} else if (obj instanceof PlatformObject) {
					project = (IProject)((PlatformObject)obj).getAdapter(IProject.class);
				}
			}
		}
		if (project != null) {
			final IProject proj = project;
			IWorkspaceRunnable op = new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					// Convert to CDT project
					CCorePlugin.getDefault().convertProjectToCC(proj, monitor, MakeCorePlugin.MAKE_PROJECT_ID);
					// Set up build information
					new NDKWizardHandler().convertProject(proj, monitor);
					// Set up the source and output folders
					try {
						NDKManager.addNativeSupport(proj, "jni", monitor);
					} catch (IOException e) {
						throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
					}
				}
			};

			// TODO run from a job
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			try {
				workspace.run(op, workspace.getRoot(), 0, new NullProgressMonitor());
			} catch (CoreException e) {
				Activator.getDefault().getLog().log(e.getStatus());
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection; 
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}

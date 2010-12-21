package org.eclipse.cdt.android.build.internal.ui;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.PlatformObject;
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
		if (project != null)
			try {
				// TODO see NewCProjectWizard.getRunnable() on how to set this up.
				IProgressMonitor monitor = new NullProgressMonitor();
				// Convert to CDT project
				CCorePlugin.getDefault().convertProjectToCC(project, monitor, MakeCorePlugin.MAKE_PROJECT_ID);
				// Set up build information
				new NDKWizardHandler().convertProject(project, monitor);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

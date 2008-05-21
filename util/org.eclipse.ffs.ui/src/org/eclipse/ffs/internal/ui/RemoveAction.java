package org.eclipse.ffs.internal.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.ffs.internal.core.FFSFileSystem;
import org.eclipse.ffs.internal.core.FFSProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionDelegate;

public class RemoveAction extends ActionDelegate implements
		IWorkbenchWindowActionDelegate {

	private ISelection selection;
	private IResource resource;

	public RemoveAction() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		boolean enabled = false;
		resource = null;
		this.selection = selection;
		if (selection instanceof IStructuredSelection) {
			Object sel = ((IStructuredSelection) selection).getFirstElement();
			if (sel instanceof IAdaptable) {
				IResource res = (IResource) ((IAdaptable) sel).getAdapter(IResource.class);
				this.resource = res;	
			}
		}
		action.setEnabled(resource != null);
	}

	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(IAction action) {
			
		if (resource != null)
		{
			try {
//				GroupNameDialog groupDialog = new GroupNameDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
//				if (groupDialog.open() == IDialogConstants.OK_ID)
				{
					FFSProject ffsProject = FFSFileSystem.getFFSFileSystem().getProject(resource);
					ffsProject.remove(resource);									
				}
				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		super.run(action);
	}

}

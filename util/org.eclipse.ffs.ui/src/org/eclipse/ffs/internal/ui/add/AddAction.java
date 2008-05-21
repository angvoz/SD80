package org.eclipse.ffs.internal.ui.add;

import org.eclipse.cdt.internal.ui.wizards.AbstractOpenWizardAction;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;

public class AddAction extends AbstractOpenWizardAction {

	private ISelection selection;
	private IResource resource;

/*	@Override
	public void run(IAction action) {
			
		if (resource != null)
		{
			FileSystemImportWizard importWizard = new FileSystemImportWizard();

			importWizard.init(getWorkbench(), resource);
			
			WizardDialog dialog= new WizardDialog(shell, wizard);
			PixelConverter converter= new PixelConverter(CUIPlugin.getActiveWorkbenchShell());
			
			dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(70), converter.convertHeightInCharsToPixels(20));
			dialog.create();
			dialog.open();

			FileDialog dialog = new FileDialog( PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() , SWT.NONE);
			String res = dialog.open();
			try {
				FFSProject ffsProject = FFSFileSystem.getFFSFileSystem().getProject(resource);
				Path filePath = new Path(res);
				ffsProject.addFile(filePath, resource);				
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		super.run(action);
	}
*/
	@Override
	protected Wizard createWizard() throws CoreException {
		return new FileSystemImportWizard();
	}

}

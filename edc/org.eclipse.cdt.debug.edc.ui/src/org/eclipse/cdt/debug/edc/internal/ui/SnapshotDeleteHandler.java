package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.snapshot.Snapshot;
import org.eclipse.cdt.debug.edc.internal.snapshot.SnapshotUtils;
import org.eclipse.cdt.debug.edc.internal.ui.views.SnapshotView;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our handler for the standard Delete command
 */
public class SnapshotDeleteHandler extends AbstractHandler {

	private boolean deleteAlbum(final IProject project, final IAlbum album, final Shell shell) throws CoreException {

		if (album == null){
			return false;
		}

		// delete any unzipped archive
		IPath extractedAlbum = album.getAlbumRootDirectory();
		if (extractedAlbum.toFile().exists()){
			SnapshotView.deleteDir(extractedAlbum.toFile());
		}
		
		// delete launch configuration
		ILaunchConfiguration lc = SnapshotUtils.findExistingLaunchForAlbum(album);
		if (lc != null){
			lc.delete();
		}
		
		final boolean[] success = { false };
		project.accept(new IResourceProxyVisitor() {
			public boolean visit(IResourceProxy proxy) throws CoreException {
		
				if (proxy.getType() == IResource.FILE) {
					IPath currentFile = proxy.requestResource()
							.getRawLocation();
		
					if (album.getLocation().toFile().getAbsolutePath().equals(
							currentFile.toFile().getAbsolutePath())) {
		
						boolean okToDelete = false;
						// Behavior will be different if resource is linked, so
						// alert the user and confirm.
						if (proxy.requestResource().isLinked()) {
							okToDelete = MessageDialog
									.openQuestion(
											shell,
											"Delete Album",
											"Are you sure you want to remove the linked file \""
													+ currentFile.toOSString()
													+ "\"?\n\nThis action will not delete the file from disk.");
						} else {
							okToDelete = MessageDialog
									.openQuestion(
											shell,
											"Delete Album",
											"Are you sure you want to delete album \""
													+ currentFile.toOSString()
													+ "\"from disk?\n\nThis action cannot be undone.");
						}
		
						if (okToDelete) {
							proxy.requestResource().delete(true, null);
						}
						success[0] = true;
						return false;
					}
				}
		
				return true;
			}
		}, IResource.NONE);
		
		return success[0];
	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow wbWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (!(part instanceof SnapshotView)) {
			assert false : "Delete handler invoked from unexpected view. Expecting Snapshot view";
			return null;
		}
		Object element = getSelectedItem(event.getApplicationContext());
		if (element != null) {
			if (element instanceof IAlbum) {
				IAlbum album = (IAlbum) element;
				try {
					if (deleteAlbum(SnapshotUtils.getSnapshotsProject(), album, wbWindow.getShell())) {
						((SnapshotView)part).refresh();
					}
				} catch (CoreException e) {
					MessageDialog.open(MessageDialog.ERROR,
							wbWindow.getShell(), part.getTitle(),
							"Error deleting item: " + e.getLocalizedMessage(),
							SWT.NONE);
				}
			} else if (element instanceof Snapshot) {
				Snapshot snap = (Snapshot) element;
				String descr = snap.getSnapshotDisplayName();
				if (MessageDialog.openQuestion(wbWindow.getShell(),
						"Delete Snapshot",
						"Are you sure you want to delete snapshot \""
								+ descr + "\"?")) {
					snap.getAlbum().deleteSnapshot(snap);
					((SnapshotView)part).refresh();
				}
			}
		}
		return null;
	}

	/**
	 * Return the Album or Snapshot that is selected (available in the command
	 * context)
	 * 
	 * @param evaluationContext
	 *            the command context
	 * @return either an Album or a Snapshot, or null if neither is selected
	 *         (won't happen in practice)
	 */
	private Object getSelectedItem(Object evaluationContext) {
	    if (evaluationContext instanceof IEvaluationContext) {
	    	// The command may have been invoked from the toolbar or the context menu 
	    	Object s = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
	        if (!(s instanceof IStructuredSelection)) {
	        	s = ((IEvaluationContext) evaluationContext).getVariable(ISources.ACTIVE_MENU_SELECTION_NAME);
	        }
	        if (s instanceof IStructuredSelection) {
	            IStructuredSelection ss = (IStructuredSelection)s;
	            if (ss.size() == 1) {
    	            TreeNode node = (TreeNode) ss.getFirstElement();
    	            Object element = node.getValue();
    	            if (element instanceof Album || element instanceof Snapshot) {
    	            	return element;
    	            }
	            }
	        }
	    }
	    return null;
	}

}

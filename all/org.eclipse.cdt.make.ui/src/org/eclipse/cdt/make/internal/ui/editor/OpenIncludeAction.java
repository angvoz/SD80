/*******************************************************************************
 * Copyright (c) 2002, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Violaine Batthish (IBM) - bug 270013
 *******************************************************************************/

package org.eclipse.cdt.make.internal.ui.editor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.make.core.makefile.IDirective;
import org.eclipse.cdt.make.core.makefile.gnu.IInclude;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;

/**
 * OpenIncludeAction
 */
public class OpenIncludeAction extends Action {

	ISelectionProvider fSelectionProvider;

	/**
	 * 
	 */
	public OpenIncludeAction(ISelectionProvider provider) {
		super(MakeUIPlugin.getResourceString("OpenIncludeAction.title")); //$NON-NLS-1$
		setDescription(MakeUIPlugin.getResourceString("OpenIncludeAction.description")); //$NON-NLS-1$
		setToolTipText(MakeUIPlugin.getResourceString("OpenIncludeAction.tooltip")); //$NON-NLS-1$
		fSelectionProvider= provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		IInclude[] includes= getIncludeDirective(fSelectionProvider.getSelection());
		if (includes != null) {
			for (int i = 0; i < includes.length; ++i) {
				IDirective[] directives = includes[i].getDirectives();
				for (int j = 0; j < directives.length; ++j) {
					try {
						openInEditor(directives[j]);
					} catch (PartInitException e) {
					}
				}
			}
		}
	}

	public static IEditorPart openInEditor(IDirective directive) throws PartInitException {
		try {
			URI uri = directive.getMakefile().getFileURI();
			IFileStore store = EFS.getStore(uri);

			IFile[] file = MakeUIPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
			if (file.length > 0 && file[0] != null) {
				IWorkbenchPage p = MakeUIPlugin.getActivePage();
				if (p != null) {
					IEditorPart editorPart = IDE.openEditor(p, file[0], true);
					if (editorPart instanceof MakefileEditor) {
						((MakefileEditor) editorPart).setSelection(directive, true);
					}
					return editorPart;
				}
			} else {
				// External file
				IEditorInput input = new FileStoreEditorInput(store);
				IWorkbenchPage p = MakeUIPlugin.getActivePage();
				if (p != null) {
					String editorID = "org.eclipse.cdt.make.editor"; //$NON-NLS-1$
					IEditorPart editorPart = IDE.openEditor(p, input, editorID, true);
					if (editorPart instanceof MakefileEditor) {
						((MakefileEditor) editorPart).setSelection(directive, true);
					}
					return editorPart;
				}
			}
		} catch (CoreException e) {
		}
		return null;
	}

	IInclude[] getIncludeDirective(ISelection sel) {
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			List list= ((IStructuredSelection)sel).toList();
			if (list.size() > 0) {
				List includes = new ArrayList(list.size());
				for (int i = 0; i < list.size(); ++i) {
					Object element= list.get(i);
					if (element instanceof IInclude) {
						includes.add(element);
					}
				}
				return (IInclude[]) includes.toArray(new IInclude[includes.size()]);
			}
		}
		return null;
	}

	public boolean canActionBeAdded(ISelection selection) {
        IInclude[] includes =  getIncludeDirective(selection);
        return includes != null && includes.length != 0;
	}
}

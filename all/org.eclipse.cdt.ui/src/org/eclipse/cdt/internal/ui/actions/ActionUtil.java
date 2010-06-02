/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/*
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=19104
 */
public class ActionUtil {
	
	private ActionUtil(){
	}

	//bug 31998	we will have to disable renaming of linked packages (and cus)
	public static boolean mustDisableCModelAction(Shell shell, Object element) {
	    return false;
//		if (!(element instanceof IPackageFragment) && !(element instanceof IPackageFragmentRoot))
//			return false;
//		
//		IResource resource= ResourceUtil.getResource(element);
//		if ((resource == null) || (! (resource instanceof IFolder)) || (! resource.isLinked()))
//			return false;
//			
//		MessageDialog.openInformation(shell, ActionMessages.ActionUtil.not_possible"), ActionMessages.ActionUtil.no_linked")); //$NON-NLS-1$ //$NON-NLS-2$
//		return true;
	}
	
	public static boolean isProcessable(Shell shell, CEditor editor) {
		if (editor == null)
			return true;
		ICElement input= SelectionConverter.getInput(editor);
		// if a Java editor doesn't have an input of type Java element
		// then it is for sure not on the build path
		if (input == null) {
			MessageDialog.openInformation(shell, 
					ActionMessages.ActionUtil_notOnBuildPath_title,
					ActionMessages.ActionUtil_notOnBuildPath_message);
			return false;
		}
		return isProcessable(shell, input);
	}
	
	public static boolean isProcessable(Shell shell, Object element) {
		if (!(element instanceof ICElement))
			return true;
			
		if (isOnBuildPath((ICElement)element))
			return true;
		MessageDialog.openInformation(shell, 
				ActionMessages.ActionUtil_notOnBuildPath_title,
				ActionMessages.ActionUtil_notOnBuildPath_message);
		return false;
	}

	public static boolean isOnBuildPath(ICElement element) {	
        //fix for bug http://dev.eclipse.org/bugs/show_bug.cgi?id=20051
        if (element.getElementType() == ICElement.C_PROJECT)
            return true;
//		ICProject project= element.getCProject();
//		if (!project.isOnSourceRoot(element.getResource()))
//			return false;
		return true;
	}
}


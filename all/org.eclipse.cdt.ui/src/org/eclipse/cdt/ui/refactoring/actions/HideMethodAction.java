/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.refactoring.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.window.IShellProvider;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.hidemethod.HideMethodRefactoringRunner;

/**
 * Launches a HideMethod refacoring
 * @author Guido Zgraggen IFS
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public class HideMethodAction extends RefactoringAction {
    
    public HideMethodAction() {
        super(Messages.HideMethodAction_label); 
    }
    
	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		if (elem instanceof ISourceReference) {
			new HideMethodRefactoringRunner(null, null, elem, shellProvider, elem.getCProject()).run();
		}
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s) {
		IResource res= wc.getResource();
		if (res instanceof IFile) {
			new HideMethodRefactoringRunner((IFile) res, 
					fEditor.getSelectionProvider().getSelection(), null, 
					fEditor.getSite().getWorkbenchWindow(), wc.getCProject()).run();
		}
	}

    @Override
	public void updateSelection(ICElement elem) {
    	super.updateSelection(elem);
    	if (elem instanceof IMethodDeclaration == false 
    			|| elem instanceof ISourceReference == false
    			|| ((ISourceReference) elem).getTranslationUnit().getResource() instanceof IFile == false) {
    		setEnabled(false);
    	}
    }
}

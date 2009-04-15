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
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.IWorkingCopy;

import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GenerateGettersAndSettersRefactoringRunner;

/**
 * Launches a getter and setter source code generation.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */          
public class GettersAndSettersAction extends RefactoringAction {
    
    public GettersAndSettersAction() {
        super(Messages.GettersAndSetters_label); 
    }
    
	/**
	 * @since 5.1
	 */
	public GettersAndSettersAction(IEditorPart editor) {
		super(Messages.GettersAndSetters_label);
		setEditor(editor);
	}

	@Override
	public void run(IShellProvider shellProvider, ICElement elem) {
		new GenerateGettersAndSettersRefactoringRunner(null, null, elem, shellProvider).run();
	}

	@Override
	public void run(IShellProvider shellProvider, IWorkingCopy wc, ITextSelection s) {
		IResource res= wc.getResource();
		if (res instanceof IFile) {
			new GenerateGettersAndSettersRefactoringRunner((IFile) res, s, null, shellProvider).run();
		}
	}

    @Override
	public void updateSelection(ICElement elem) {
    	super.updateSelection(elem);
    	if (elem instanceof IField == false 
    			|| elem instanceof ISourceReference == false
    			|| ((ISourceReference) elem).getTranslationUnit().getResource() instanceof IFile == false) {
    		setEnabled(false);
    	}
    }
}

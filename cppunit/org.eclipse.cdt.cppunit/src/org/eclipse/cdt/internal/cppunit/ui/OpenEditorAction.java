/*
 * (c) Copyright IBM Corp. 2003.
 * Created on 27 févr. 03
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 * IBM Software Group - Rational Software
 */

package org.eclipse.cdt.internal.cppunit.ui;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.CModelException;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * Abstract Action for opening a Java editor.
 */
public abstract class OpenEditorAction extends Action {
	protected String fFileName;
	protected TestRunnerViewPart fTestRunner;
	
	/**
	 * Constructor for OpenEditorAction.
	 */
	protected OpenEditorAction(TestRunnerViewPart testRunner, String name) {
		super(CppUnitMessages.getString("OpenEditorAction.action.label")); //$NON-NLS-1$
		fFileName= name;
		fTestRunner= testRunner;
	}

	/*
	 * @see IAction#run()
	 */
	public void run() {
		ITextEditor textEditor= null;
		try {
			ICElement element= findElement(fTestRunner.getLaunchedProject(), fFileName);
			if (element == null) {
				MessageDialog.openError(fTestRunner.getSite().getShell(), 
					CppUnitMessages.getString("OpenEditorAction.error.cannotopen.title"), CppUnitMessages.getString("OpenEditorAction.error.cannotopen.message")); //$NON-NLS-1$ //$NON-NLS-2$
				return;
			}
			// use of internal API for backward compatibility with 1.0
//Martin			textEditor= (ITextEditor)EditorUtility.openInEditor(element, false);			
			textEditor= (ITextEditor)EditorUtility.openInEditor(element);			
		} catch (CoreException e) {
			ErrorDialog.openError(fTestRunner.getSite().getShell(), CppUnitMessages.getString("OpenEditorAction.error.dialog.title"), CppUnitMessages.getString("OpenEditorAction.error.dialog.message"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		if (textEditor == null) {
			fTestRunner.postInfo(CppUnitMessages.getString("OpenEditorAction.message.cannotopen")); //$NON-NLS-1$
			return;
		}
		reveal(textEditor);
	}
	
	protected abstract ICElement findElement(ICProject project, String className) throws CModelException;
	
	protected abstract void reveal(ITextEditor editor);
}

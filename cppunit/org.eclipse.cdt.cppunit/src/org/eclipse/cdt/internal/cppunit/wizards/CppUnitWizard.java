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

package org.eclipse.cdt.internal.cppunit.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.internal.cppunit.ui.CppUnitPlugin;
import org.eclipse.cdt.internal.cppunit.util.ExceptionHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
/**
 * The wizard base class for CppUnit creation wizard.
 */
public abstract class CppUnitWizard extends BasicNewResourceWizard {

	protected static String DIALOG_SETTINGS_KEY= "CppUnitWizards"; //$NON-NLS-1$

	public CppUnitWizard() {
		setNeedsProgressMonitor(true);
	}
	
	/*
	 * @see IWizard#performFinish()
	 */
	public abstract boolean performFinish();

	/**
	 * Run a runnable
	 */	
	protected boolean finishPage(IRunnableWithProgress runnable) {
		IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(runnable);
		try {
			getContainer().run(false, true, op);
		} catch (InvocationTargetException e) {
			Shell shell= getShell();
			String title= WizardMessages.getString("NewCppUnitWizard.op_error.title"); //$NON-NLS-1$
			String message= WizardMessages.getString("NewCppUnitWizard.op_error.message"); //$NON-NLS-1$
			ExceptionHandler.handle(e, shell, title, message);
			return false;
		} catch  (InterruptedException e) {
			return false;
		}
		return true;
	}

	protected void openResource(final IResource resource) {
		if (resource.getType() == IResource.FILE) {
			final IWorkbenchPage activePage= CppUnitPlugin.getDefault().getActivePage();
			if (activePage != null) {
				final Display display= getShell().getDisplay();
				if (display != null) {
					display.asyncExec(new Runnable() {
						public void run() {
							try {
								IDE.openEditor(activePage,(IFile)resource,true);
//								activePage.openEditor((IFile)resource);
							} catch (PartInitException e) {
								CppUnitPlugin.log(e);
							}
						}
					});
				}
			}
		}
	}

	protected void initDialogSettings() {
		IDialogSettings pluginSettings= CppUnitPlugin.getDefault().getDialogSettings();
		IDialogSettings wizardSettings= pluginSettings.getSection(DIALOG_SETTINGS_KEY);
		if (wizardSettings == null) {
			wizardSettings= new DialogSettings(DIALOG_SETTINGS_KEY);
			pluginSettings.addSection(wizardSettings);
		}
		setDialogSettings(wizardSettings);
	}

}


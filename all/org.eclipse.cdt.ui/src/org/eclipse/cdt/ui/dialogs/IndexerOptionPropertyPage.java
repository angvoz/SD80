/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;

public class IndexerOptionPropertyPage extends PropertyPage {

	private IndexerBlock optionPage;
	private String oldIndexerID;

	public IndexerOptionPropertyPage(){
		super();
		optionPage = new IndexerBlock();
	}

	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new FillLayout());

		optionPage.createControl(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.PROJECT_INDEXER_PROPERTIES);	
		initialize();
		
		return composite;
	}

	protected void performDefaults() {
		ICProject tempProject = CoreModel.getDefault().create(getProject());
		optionPage.resetIndexerPageSettings(tempProject);
	}
	
	private void initialize(){
		ICProject project = CoreModel.getDefault().create(getProject());
		try {
			oldIndexerID = CCorePlugin.getPDOMManager().getIndexerId(project);
			optionPage.setIndexerID(oldIndexerID, project);
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
	public boolean performOk() {
		ICProject tempProject = CoreModel.getDefault().create(getProject());
		try {
			optionPage.persistIndexerSettings(tempProject, new NullProgressMonitor());
		} catch (CoreException e) {}
		
		return true;
	}
	
	public IProject getProject(){
		IAdaptable tempElement = getElement();
		IProject project;
		if (tempElement instanceof IProject) {
			project = (IProject) tempElement;
		} else {
			project = (IProject)tempElement.getAdapter(IProject.class);
		}
		return project;
	}

}

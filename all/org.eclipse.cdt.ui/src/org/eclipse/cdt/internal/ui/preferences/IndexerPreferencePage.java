/*******************************************************************************
 * Copyright (c) 2005, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import org.eclipse.cdt.ui.dialogs.CacheSizeBlock;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.IndexerBlock;

public class IndexerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, ICOptionContainer {

	private IndexerBlock fOptionBlock;
	private CacheSizeBlock fCacheBlock;
	private IndexerStrategyBlock fStrategyBlock;
	
	public IndexerPreferencePage(){
		fOptionBlock = new IndexerBlock();
		fStrategyBlock= new IndexerStrategyBlock(this);
		fCacheBlock= new CacheSizeBlock(this);
	}
	
	@Override
	protected Control createContents(Composite parent) {
		GridLayout gl;
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(gl= new GridLayout());
		composite.setLayoutData(new GridData());
		gl.verticalSpacing= 0;
	
		fOptionBlock.createControl(composite);
		fStrategyBlock.createControl(composite);
		fCacheBlock.createControl(composite);
		
		return composite;
	}

	public void init(IWorkbench workbench) {
	}

	public void updateContainer() {
		if (!fOptionBlock.isValid()) {
			setErrorMessage(fOptionBlock.getErrorMessage());
			setValid(false);
		}
		else if (!fStrategyBlock.isValid()) {
			setErrorMessage(fStrategyBlock.getErrorMessage());
			setValid(false);
		}
		else if (!fCacheBlock.isValid()) {
			setErrorMessage(fCacheBlock.getErrorMessage());
			setValid(false);
		}
		else {
			setErrorMessage(null);
			setValid(true);
		}
	}

	public IProject getProject() {
		return null;
	}

	public Preferences getPreferences() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean performOk() {
		try {
			fOptionBlock.performApply(new NullProgressMonitor());
			fStrategyBlock.performApply(new NullProgressMonitor());
			fCacheBlock.performApply(new NullProgressMonitor());
		} catch (CoreException e) {}
		return true;
	}
	
	@Override
	public void performDefaults() {
		fOptionBlock.performDefaults();
		fStrategyBlock.performDefaults();
		fCacheBlock.performDefaults();
		updateContainer();
	}
}

/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * DisassemblyView
 */
public class DisassemblyView extends DisassemblyPart implements IViewPart {

	private ISelectionListener fDebugViewListener;

	/**
	 * 
	 */
	public DisassemblyView() {
		super();
	}

	@Override
	protected IActionBars getActionBars() {
		return getViewSite().getActionBars();
	}

	/*
	 * @see org.eclipse.ui.IViewPart#getViewSite()
	 */
	public IViewSite getViewSite() {
		return (IViewSite)getSite();
	}

	/*
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		setSite(site);
	}

	/*
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		setSite(site);
		site.getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, fDebugViewListener= new ISelectionListener() {
			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
				updateDebugContext();
			}});
	}

	/*
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
	}

	@Override
	protected void contributeToActionBars(IActionBars bars) {
		super.contributeToActionBars(bars);
		fillLocalPullDown(bars.getMenuManager());
	}

	protected void fillLocalPullDown(IMenuManager manager) {
		manager.add(fActionGotoPC);
		manager.add(fActionGotoAddress);
		manager.add(fActionToggleSource);
		manager.add(new Separator());
	}

	@Override
	protected void closePart() {
		getViewSite().getPage().hideView(this);
	}

	@Override
	public void dispose() {
		getSite().getPage().removeSelectionListener(fDebugViewListener);
		super.dispose();
	}
}

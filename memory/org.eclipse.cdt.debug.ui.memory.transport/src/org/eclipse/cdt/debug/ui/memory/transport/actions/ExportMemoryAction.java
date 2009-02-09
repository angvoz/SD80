/*******************************************************************************
 * Copyright (c) 2006-2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.transport.actions;

import org.eclipse.cdt.debug.ui.memory.transport.ExportMemoryDialog;
import org.eclipse.cdt.debug.ui.memory.transport.MemoryTransportPlugin;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.internal.ui.views.memory.MemoryView;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action for exporting memory.
 */
public class ExportMemoryAction implements IViewActionDelegate {

	private MemoryView fView;

	public void init(IViewPart view) {
		if (view instanceof MemoryView)
			fView = (MemoryView) view;
	}

	private IMemoryBlock getMemoryBlock(ISelection selection)
	{
		IMemoryBlock memBlock = null;
		
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection strucSel = (IStructuredSelection) selection;

			// return if current selection is empty
			if (strucSel.isEmpty())
				return null;

			Object obj = strucSel.getFirstElement();

			if (obj == null)
				return null;

			if (obj instanceof IMemoryRendering) {
				memBlock = ((IMemoryRendering) obj).getMemoryBlock();
			} else if (obj instanceof IMemoryBlock) {
				memBlock = (IMemoryBlock) obj;
			}
		}
		return memBlock;
	}
	
	public void run(IAction action) {

		ISelection selection = fView.getSite().getSelectionProvider()
			.getSelection();
		IMemoryBlock memBlock = getMemoryBlock(selection);
		if(memBlock == null)
			return;
		ExportMemoryDialog dialog = new ExportMemoryDialog(MemoryTransportPlugin.getShell(), memBlock);
		dialog.open();
		
		dialog.getResult();
	}
	
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(getMemoryBlock(selection) != null);
	}

}

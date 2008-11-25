/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;


import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.util.CElementBaseLabels;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;

/**
 * Add the <code>StatusBarUpdater</code> to your ViewPart to have the statusbar
 * describing the selected elements.
 */
public class StatusBarUpdater implements ISelectionChangedListener {
	
	private final int LABEL_FLAGS= CElementBaseLabels.DEFAULT_QUALIFIED | CElementBaseLabels.ROOT_POST_QUALIFIED | CElementBaseLabels.APPEND_ROOT_PATH |
			CElementBaseLabels.M_PARAMETER_TYPES | CElementBaseLabels.M_APP_RETURNTYPE | CElementBaseLabels.M_EXCEPTIONS | 
			CElementBaseLabels.F_APP_TYPE_SIGNATURE;

	private IStatusLineManager fStatusLineManager;
	
	public StatusBarUpdater(IStatusLineManager statusLineManager) {
		fStatusLineManager= statusLineManager;
	}
		
	/*
	 * @see ISelectionChangedListener#selectionChanged
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		String statusBarMessage= formatMessage(event.getSelection());
		fStatusLineManager.setMessage(statusBarMessage);
	}
	
	
	protected String formatMessage(ISelection sel) {
		if (sel instanceof IStructuredSelection && !sel.isEmpty()) {
			IStructuredSelection selection= (IStructuredSelection) sel;
			
			int nElements= selection.size();
			if (nElements > 1) {
				return NLS.bind(CUIMessages.StatusBarUpdater_num_elements_selected, String.valueOf(nElements)); 
			} 
			Object elem= selection.getFirstElement();
			if (elem instanceof ICElement) {
				return formatCElementMessage((ICElement) elem);
			} else if (elem instanceof IResource) {
				return formatResourceMessage((IResource) elem);
			}
		}
		return "";  //$NON-NLS-1$
	}
		
	private String formatCElementMessage(ICElement element) {
		return CElementBaseLabels.getElementLabel(element, LABEL_FLAGS);
	}
		
	private String formatResourceMessage(IResource element) {
		IContainer parent= element.getParent();
		if (parent != null && parent.getType() != IResource.ROOT)
			return element.getName() + CElementBaseLabels.CONCAT_STRING + parent.getFullPath().makeRelative().toString();
		return element.getName();
	}	

}

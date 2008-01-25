/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.internal.ui.CBreakpointContext;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PropertyDialogAction;

/**
 * Presents a custom properties dialog to configure the attibutes of a C/C++ breakpoint.
 */
public class CBreakpointPropertiesAction implements IObjectActionDelegate {

	private IWorkbenchPart fPart;

	private ICBreakpoint fContext;

	/**
	 * Constructor for CBreakpointPropertiesAction.
	 */
	public CBreakpointPropertiesAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart ) {
		fPart = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		PropertyDialogAction propertyAction = new PropertyDialogAction( getActivePart().getSite(), new ISelectionProvider() {

			public void addSelectionChangedListener( ISelectionChangedListener listener ) {
			}

			public ISelection getSelection() {
				return new StructuredSelection( new CBreakpointContext(getBreakpoint(), getDebugContext()) );
			}

			public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
			}

			public void setSelection( ISelection selection ) {
			}
		} );
		propertyAction.run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( selection instanceof IStructuredSelection ) {
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.isEmpty() || ss.size() > 1 ) {
			    action.setEnabled(false);
				return;
			}
			Object element = ss.getFirstElement();
			if ( element instanceof ICBreakpoint ) {
			    action.setEnabled(true);
				setBreakpoint( (ICBreakpoint)element );
			}
		}
	}
	
	protected IWorkbenchPart getActivePart() {
		return fPart;
	}

	protected ICBreakpoint getBreakpoint() {
		return fContext;
	}

    private ISelection getDebugContext() {
        return DebugUITools.getDebugContextManager().getContextService(fPart.getSite().getWorkbenchWindow()).getActiveContext();
    }

	protected void setBreakpoint( ICBreakpoint breakpoint ) {
		fContext = breakpoint;
	}
}

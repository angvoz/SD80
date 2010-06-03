/*******************************************************************************
 * Copyright (c) 2004, 2007-7 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
*******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions; 

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.cdi.model.ICDIMemorySpaceManagement;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;
 
/**
 * A delegate for the "Add Watchpoint" action.
 */
public class AddWatchpointActionDelegate extends ActionDelegate implements IViewActionDelegate {

	private IViewPart fView;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view ) {
		setView( view );
	}

	private void setView(IViewPart view) {
		fView = view;
	}

	protected IViewPart getView() {
		return fView;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		AddWatchpointDialog dlg = new AddWatchpointDialog( CDebugUIPlugin.getActiveWorkbenchShell(), getMemorySpaceManagement() );
		if ( dlg.open() == Window.OK ) {
			addWatchpoint( dlg.getWriteAccess(), dlg.getReadAccess(), dlg.getExpression(), dlg.getMemorySpace(), dlg.getRange() );
		}
	}

	protected void addWatchpoint(boolean write, boolean read, String expression, String memorySpace, BigInteger range) {
		if ( getResource() == null )
			return;
		try {
			CDIDebugModel.createWatchpoint( getSourceHandle(), getResource(), write, read, expression, memorySpace, range, true, 0, "", true ); //$NON-NLS-1$
		}
		catch( CoreException ce ) {
			CDebugUIPlugin.errorDialog( ActionMessages.getString( "AddWatchpointActionDelegate1.0" ), ce ); //$NON-NLS-1$
		}
	}

	private IResource getResource() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private String getSourceHandle() {
		return ""; //$NON-NLS-1$
	}
	
	static ICDIMemorySpaceManagement getMemorySpaceManagement(){
		IAdaptable debugViewElement = DebugUITools.getDebugContext();
		ICDIMemorySpaceManagement memMgr = null;
		
		if ( debugViewElement != null ) {
			ICDebugTarget debugTarget = (ICDebugTarget)debugViewElement.getAdapter(ICDebugTarget.class);
			
			if ( debugTarget != null ){
				ICDITarget target = (ICDITarget)debugTarget.getAdapter(ICDITarget.class);
			
				if (target instanceof ICDIMemorySpaceManagement)
					memMgr = (ICDIMemorySpaceManagement)target;
			}
		}
		
		return memMgr;
	}
}

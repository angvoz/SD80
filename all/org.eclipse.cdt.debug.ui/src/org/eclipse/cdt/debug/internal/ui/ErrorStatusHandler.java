/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.dialogs.ErrorDialog;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 25, 2002
 */
public class ErrorStatusHandler implements IStatusHandler
{

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(IStatus, Object)
	 */
	public Object handleStatus( final IStatus status, Object source ) throws CoreException
	{
		if ( status != null && source != null && source instanceof IDebugTarget )
		{
			final String title = ((IDebugTarget)source).getName();
			CDebugUIPlugin.getStandardDisplay().asyncExec(
					new Runnable()
						{
							public void run()
							{
								ErrorDialog.openError( CDebugUIPlugin.getActiveWorkbenchShell(), title, null, status );
							}
						} );
		}
		return null;
	}
}

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

package org.eclipse.cdt.debug.internal.core;

import java.util.Observable;
import org.eclipse.cdt.debug.core.ICUpdateManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIManager;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.CDebugElement;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * Enter type comment.
 * 
 * @since Mar 31, 2003
 */
public abstract class CUpdateManager extends Observable implements ICUpdateManager, IAdaptable
{
	private CDebugTarget fDebugTarget = null;

	/**
	 * 
	 */
	public CUpdateManager( CDebugTarget target )
	{
		fDebugTarget = target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICUpdateManager#setAutoModeEnabled(boolean)
	 */
	public void setAutoModeEnabled( boolean enable )
	{
		if ( getCDIManager() != null )
		{
			getCDIManager().setAutoUpdate( enable );
			setChanged();
			notifyObservers();
			clearChanged();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICUpdateManager#getAutoModeEnabled()
	 */
	public boolean getAutoModeEnabled()
	{
		if ( getCDIManager() != null )
		{
			return getCDIManager().isAutoUpdate();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICUpdateManager#update()
	 */
	public void update() throws DebugException
	{
		if ( getCDIManager() != null )
		{
			try
			{
				getCDIManager().update();
			}
			catch( CDIException e )
			{
				CDebugElement.targetRequestFailed( e.getMessage(), null );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICUpdateManager#canUpdate()
	 */
	public boolean canUpdate()
	{
		if ( getDebugTarget() != null )
		{
			return getDebugTarget().isSuspended();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter( Class adapter )
	{
		if ( ICUpdateManager.class.equals( adapter ) )
			return this;
		if ( IDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( ICDebugTarget.class.equals( adapter ) )
			return getDebugTarget();
		if ( Observable.class.equals( adapter ) )
			return this;
		return null;
	}

	public CDebugTarget getDebugTarget()
	{
		return fDebugTarget;
	}

	public void dispose() {
		deleteObservers();
	}

	abstract protected ICDIManager getCDIManager();
}

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

package org.eclipse.cdt.debug.internal.core.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableObject;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IValueModification;

/**
 * 
 * Common functionality for variables that support value modification
 * 
 * @since Aug 9, 2002
 */
public class CModificationVariable extends CVariable
{
	/**
	 * Constructor for CModificationVariable.
	 * @param parent
	 * @param cdiVariable
	 */
	public CModificationVariable( CDebugElement parent, ICDIVariableObject cdiVariableObject )
	{
		super( parent, cdiVariableObject );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValueModification#supportsValueModification()
	 */
	public boolean supportsValueModification()
	{
		CDebugTarget target = (CDebugTarget)getDebugTarget().getAdapter( CDebugTarget.class );
		return ( target != null && CDebugElementState.SUSPENDED.equals( target.getState() ) && isEditable() );
	}

	/**
	 * @see IValueModification#verifyValue(String)
	 */
	public boolean verifyValue( String expression )
	{
		return true;
	}

	/**
	 * @see IValueModification#verifyValue(IValue)
	 */
	public boolean verifyValue( IValue value )
	{
		return value.getDebugTarget().equals( getDebugTarget() );
	}

	/**
	 * @see IValueModification#setValue(String)
	 */
	public final void setValue( String expression ) throws DebugException
	{
		String newExpression = processExpression( expression );
		ICDIVariable cdiVariable = null;
		try
		{
			cdiVariable = getCDIVariable();
			if ( cdiVariable != null )
				cdiVariable.setValue( newExpression );
			else
				requestFailed( CoreModelMessages.getString( "CModificationVariable.0" ), null ); //$NON-NLS-1$
			
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}
	}

	/**
	 * Set this variable's value to the given value
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.CModificationVariable#setValue(ICDIValue)
	 */
	protected void setValue( ICDIValue value ) throws DebugException
	{
		ICDIVariable cdiVariable = null;
		try
		{
			cdiVariable = getCDIVariable();
			if ( cdiVariable != null )
				cdiVariable.setValue( value );
			else
				requestFailed( CoreModelMessages.getString( "CModificationVariable.1" ), null ); //$NON-NLS-1$
		}
		catch( CDIException e )
		{
			targetRequestFailed( e.getMessage(), null );
		}			
	}
	
	private String processExpression( String oldExpression ) throws DebugException
	{
		return oldExpression;
	}
}

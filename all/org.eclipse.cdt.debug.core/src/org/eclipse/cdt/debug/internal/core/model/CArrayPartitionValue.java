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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.model.ICExpressionEvaluator;
import org.eclipse.cdt.debug.core.model.ICValue;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;

/**
 *
 * The value for an array partition.
 * 
 * @since Sep 9, 2002
 */
public class CArrayPartitionValue extends CDebugElement implements ICValue
{
	/**
	 * The underlying CDI variable.
	 */
	private ICDIVariable fCDIVariable;

	/**
	 * Parent variable.
	 */
	private CVariable fParent = null;

	/**
	 * List of child variables.
	 */
	private List fVariables = Collections.EMPTY_LIST;

	private int fStart;

	private int fEnd;

	/**
	 * Constructor for CArrayPartitionValue.
	 * @param target
	 */
	public CArrayPartitionValue( CVariable parent, ICDIVariable cdiVariable, int start, int end )
	{
		super( (CDebugTarget)parent.getDebugTarget() );
		fCDIVariable = cdiVariable;
		fParent = parent;
		fStart = start;
		fEnd = end;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() throws DebugException
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException
	{
		List list = getVariables0();
		return (IVariable[])list.toArray( new IVariable[list.size()] );
	}

	protected synchronized List getVariables0() throws DebugException 
	{
		if ( !isAllocated() || !hasVariables() )
			return Collections.EMPTY_LIST;
		if ( fVariables.size() == 0 )
		{
			fVariables = CArrayPartition.splitArray( this, getCDIVariable(), getStart(), getEnd() );
		}
		return fVariables;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() throws DebugException
	{
		return true;
	}
 
	protected int getStart()
	{
		return fStart;
	}

	protected int getEnd()
	{
		return fEnd;
	}

	public void setChanged( boolean changed ) throws DebugException
	{
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).setChanged( changed );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICValue#computeDetail()
	 */
	public String evaluateAsExpression()
	{
		ICExpressionEvaluator ee = (ICExpressionEvaluator)getDebugTarget().getAdapter( ICExpressionEvaluator.class );
		String valueString = null; 
		if ( ee != null && ee.canEvaluate() )
		{
			try
			{
				if ( getParentVariable() != null )
					valueString = ee.evaluateExpressionToString( getParentVariable().getQualifiedName() );
			}
			catch( DebugException e )
			{
				valueString = e.getMessage();
			}
		}
		return valueString;
	}

	public CVariable getParentVariable()
	{
		return fParent;
	}

	protected ICDIVariable getCDIVariable()
	{
		return fCDIVariable;
	}

	public void dispose()
	{
		Iterator it = fVariables.iterator();
		while( it.hasNext() )
		{
			((CVariable)it.next()).dispose();
		}
	}
}

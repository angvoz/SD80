/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.formatter;

import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;

/**
 * Abstract class implementing IVariableValueConverter with summary and detail variants
 */
public abstract class AbstractVariableConverter implements IVariableValueConverter {

	protected IType type;
	private boolean forDetails;

	public AbstractVariableConverter(IType type, boolean forDetails) {
		this.forDetails = forDetails;
		this.type = type;
	}
	
	protected abstract String getDetailsValue(IExpressionDMContext variable) throws CoreException;

	protected abstract String getSummaryValue(IExpressionDMContext variable) throws CoreException;
	
	public String getValue(IExpressionDMContext variable) throws CoreException {
		if (forDetails)
			return getDetailsValue(variable);
		return getSummaryValue(variable);
	}

	public boolean canEditValue() {
		return false; // read-only implementation
	}
	
	public String getEditableValue(IExpressionDMContext variable) throws CoreException {
		return getValue(variable); // default - use same value
	}
	
	public void setValue(IExpressionDMContext variable, String newValue) {
		// read-only implementation
	}
}

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

import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;


/**
 * Interface for converting displayed values and optionally editing converted value
 */
public interface IVariableValueConverter {
	/**
	 * Return the formatted value.
	 * @param variable IExpressionDMContext
	 * @return String
	 * @throws CoreException any error on getting the value.
	 */
	String getValue(IExpressionDMContext variable) throws CoreException;
	
	/**
	 * Whether the value is editable. 
	 * If false, {@link #setValue(String)} and {@link #getEditableValue(IExpressionDMContext)} may fail.
	 * @return boolean
	 */
	boolean canEditValue();
	
	/**
	 * Return the formatted value for editing.
	 * @param variable IExpressionDMContext
	 * @return String
	 * @throws CoreException any error on getting the value.
	 */
	String getEditableValue(IExpressionDMContext variable) throws CoreException;
	
	/**
	 * The value entered by the user to change the variable. 
	 * @param variable IExpressionDMContext
	 * @param newValue String
	 * @throws CoreException any error on setting the value.
	 */
	void setValue(IExpressionDMContext variable, String newValue) throws CoreException;
}

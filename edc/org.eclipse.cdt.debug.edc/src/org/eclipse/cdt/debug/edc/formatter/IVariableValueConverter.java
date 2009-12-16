/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
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


/**
 * Interface for converting displayed values and optionally editing converted value
 */
public interface IVariableValueConverter {
	/**
	 * Return a value, null to indicate no change, or empty string for no value.
	 * @param variable IExpressionDMContext
	 * @return String
	 */
	String getValue(IExpressionDMContext variable);
	
	/**
	 * Whether the value is editable. 
	 * If false, {@link #setValue(String)} will fail.
	 * @return boolean
	 */
	boolean canEditValue();
	
	/**
	 * The value entered by the user to change the variable. 
	 * @param variable IExpressionDMContext
	 * @param newValue
	 */
	void setValue(IExpressionDMContext variable, String newValue);
}

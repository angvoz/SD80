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

/**
 * Interface for an extension providing custom formatting for variables
 */
public interface IVariableFormatProvider {
	/**
	 * An optional structure to use for this type
	 * @param type IType
	 * @return ITypeContentProvider
	 */
	ITypeContentProvider getTypeContentProvider(IType type);
	
	/**
	 * An optional summary value to display in the value column for the current object.
	 * @param type IType
	 * @return IVariableValueConverter
	 */
	IVariableValueConverter getVariableValueConverter(IType type);
	
	/**
	 * An optional string to display in the detail pane when the variable is selected
	 * @param type IType
	 * @return IVariableValueConverter
	 */
	IVariableValueConverter getDetailValueConverter(IType type);
}

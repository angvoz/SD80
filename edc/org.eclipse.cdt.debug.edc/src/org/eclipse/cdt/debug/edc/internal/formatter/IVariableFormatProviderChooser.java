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
package org.eclipse.cdt.debug.edc.internal.formatter;

import java.util.Collection;

import org.eclipse.cdt.debug.edc.symbols.IType;

/**
 * An object that allows choosing between format providers
 */
public interface IVariableFormatProviderChooser {
	
	String chooseTypeContentProvider(IType type, Collection<String> ids);

	String chooseVariableValueConverter(IType type, Collection<String> ids);

	String chooseDetailValueConverter(IType type, Collection<String> ids);
}
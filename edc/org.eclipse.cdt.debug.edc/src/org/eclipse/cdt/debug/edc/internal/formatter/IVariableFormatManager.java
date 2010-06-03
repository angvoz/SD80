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

import org.eclipse.cdt.debug.edc.formatter.IVariableFormatProvider;

/**
 * An interface to the variable format provider manager singleton
 */
public interface IVariableFormatManager extends IVariableFormatProvider {

	void setFormatProviderChooser(IVariableFormatProviderChooser chooser);
	
	String[] getVariableFormatProviderIds();
	
	String getFormatProviderLabel(String id);
	
	void setEnabled(boolean enabled);

	boolean isEnabled();
	
}

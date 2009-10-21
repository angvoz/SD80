/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc.preferences;


public enum XlcPref {

	
	SUPPORT_VECTOR_TYPES("true"),
	SUPPORT_DECIMAL_FLOATING_POINT_TYPES("true"),
	SUPPORT_COMPLEX_IN_CPP("true"),
	SUPPORT_RESTRICT_IN_CPP("true");

	
	private final String defaultVal;
	
	private XlcPref(String defaultVal) {
		this.defaultVal = defaultVal;
	}
	
	public String getDefaultValue() {
		return defaultVal;
	}
}




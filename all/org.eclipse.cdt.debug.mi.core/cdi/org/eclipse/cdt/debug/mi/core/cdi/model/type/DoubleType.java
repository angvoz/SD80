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

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDoubleType;

/**
 */
public class DoubleType extends FloatingPointType implements ICDIDoubleType {

	/**
	 * @param typename
	 */
	public DoubleType(ICDITarget target, String typename) {
		this(target, typename, false, false, false);
	}

	public DoubleType(ICDITarget target, String typename, boolean isComplex, boolean isImg, boolean isLong) {
		super(target, typename, isComplex, isImg, isLong);
	}
}

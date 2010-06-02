/*******************************************************************************
 * Copyright (c) 2005, 2008 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;

public class NotExpression extends AndExpression {
	public static final String NAME = "not"; 	//$NON-NLS-1$
	
	public NotExpression(IManagedConfigElement element) {
		super(element);
	}

	public boolean evaluate(IResourceInfo rcInfo, 
            IHoldsOptions holder, 
            IOption option) {
		return !super.evaluate(rcInfo, holder, option);
	}

}

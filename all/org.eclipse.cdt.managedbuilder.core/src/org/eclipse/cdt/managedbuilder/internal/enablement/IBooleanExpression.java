/*******************************************************************************
 * Copyright (c) 2005, 2011 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * Miwako Tokugawa (Intel Corporation) - bug 222817 (OptionCategoryApplicability)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.enablement;

import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.IResourceInfo;

public interface IBooleanExpression {
	public boolean evaluate(IResourceInfo rcInfo, 
            IHoldsOptions holder, 
            IOption option);
	public boolean evaluate(IResourceInfo rcInfo, 
            IHoldsOptions holder, 
            IOptionCategory category);
}

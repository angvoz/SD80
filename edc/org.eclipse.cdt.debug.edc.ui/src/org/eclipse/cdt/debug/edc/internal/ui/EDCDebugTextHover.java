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
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.dsf.debug.ui.AbstractDsfDebugTextHover;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;

/**
 * Debug editor text hover for EDC.
 */
public class EDCDebugTextHover extends AbstractDsfDebugTextHover {

    @Override
    protected String getModelId() {
    	IAdaptable adaptable = getSelectionAdaptable();
		if (adaptable != null) {
			ILaunch launch = (ILaunch) adaptable.getAdapter(ILaunch.class);
			if (launch instanceof EDCLaunch)
				return ((EDCLaunch) launch).getDebugModelID();
		}
    	
    	return null;
    }

	@Override
	protected boolean useExpressionExplorer() {
		return true;
	}

}

/*******************************************************************************
 * Copyright (c) 2010, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.android.build.internal.ui;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.STDWizardHandler;

public class NDKWizardHandler extends STDWizardHandler {

	public NDKWizardHandler() {
		super(null, null);
	}
	
	@Override
	public IToolChain[] getSelectedToolChains() {
		IToolChain[] tcs = ManagedBuildManager.getRealToolChains();
		for (IToolChain tc : tcs) {
			if (tc.getId().equals("com.android.toolchain.gcc"))
				return new IToolChain[] { tc };
		}
		return super.getSelectedToolChains();
	}
	
}

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

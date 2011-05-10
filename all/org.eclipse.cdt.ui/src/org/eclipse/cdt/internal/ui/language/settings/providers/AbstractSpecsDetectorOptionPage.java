package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;

public abstract class AbstractSpecsDetectorOptionPage extends AbstractCOptionPage {
	protected LanguageSettingsProviderTab providerTab;
	protected String providerId;

	protected void init(LanguageSettingsProviderTab providerTab, String providerId) {
		this.providerTab = providerTab;
		this.providerId = providerId;
	}

	protected ILanguageSettingsProvider getProvider() {
		return providerTab.getProvider(providerId);
	}

	protected ILanguageSettingsProvider getWorkingCopy(String providerId) {
		return providerTab.getWorkingCopy(providerId);
	}

	protected void refreshItem() {
		providerTab.refreshItem(getProvider());
	}
}

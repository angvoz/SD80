package org.eclipse.cdt.core.language.settings.providers;

import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsExtensionManager;
import org.eclipse.cdt.internal.core.language.settings.providers.LanguageSettingsProvidersSerializer;
import org.eclipse.core.resources.IResource;

public class LanguageSettingsManager {

	/**
	 * TODO
	 */
	public static List<ICLanguageSettingEntry> getSettingEntriesUpResourceTree(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		return LanguageSettingsExtensionManager.getSettingEntriesUpResourceTree(provider, cfgDescription, rc, languageId);
	}

	/**
	 *
	 * @param cfgDescription
	 * @param resource
	 * @param languageId
	 * @param kind - can be bit flag TODO: test cases
	 * @return
	 */
	// FIXME: get rid of callers PathEntryTranslator and DescriptionScannerInfoProvider
	public static List<ICLanguageSettingEntry> getSettingEntriesByKind(ICConfigurationDescription cfgDescription, IResource resource, String languageId, int kind) {
		return LanguageSettingsExtensionManager.getSettingEntriesByKind(cfgDescription, resource, languageId, kind);
	}

	/**
	 * Get Language Settings Provider defined in the workspace. That includes user-defined
	 * providers and after that providers defined as extensions via
	 * {@code org.eclipse.cdt.core.LanguageSettingsProvider} extension point.
	 * That returns actual object, any modifications will affect any configuration
	 * referring to the provider.
	 *
	 * @param id - ID of provider to find.
	 * @return the provider or {@code null} if provider is not defined.
	 */
	public static ILanguageSettingsProvider getWorkspaceProvider(String id) {
		return LanguageSettingsProvidersSerializer.getWorkspaceProvider(id);
	}

	public static List<ILanguageSettingsProvider> getWorkspaceProviders() {
		return LanguageSettingsProvidersSerializer.getWorkspaceProviders();
	}

	/**
	 * Checks if the provider is defined on the workspace level. See {@link LanguageSettingsManager#getWorkspaceProvider(String)}.
	 *
	 * @param provider - provider to check.
	 * @return {@code true} if the given provider is workspace provider, {@code false} otherwise.
	 */
	public static boolean isWorkspaceProvider(ILanguageSettingsProvider provider) {
		return LanguageSettingsProvidersSerializer.isWorkspaceProvider(provider);
	}
	
}

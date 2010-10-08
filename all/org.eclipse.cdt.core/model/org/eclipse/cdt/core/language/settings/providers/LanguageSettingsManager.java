package org.eclipse.cdt.core.language.settings.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;

public class LanguageSettingsManager {

	/**
	 * Never returns {@code null} although individual providers return {@code null} if
	 * no settings defined.
	 */
	public static List<ICLanguageSettingEntry> getSettingEntriesUpResourceTree(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		Assert.isNotNull(cfgDescription);
	
		if (provider!=null) {
			List<ICLanguageSettingEntry> list = provider.getSettingEntries(cfgDescription, rc, languageId);
			if (list!=null) {
				return new ArrayList<ICLanguageSettingEntry>(list);
			}
		}
	
		if (rc!=null) {
			IResource parentFolder = rc.getParent();
			if (parentFolder!=null) {
				return getSettingEntriesUpResourceTree(provider, cfgDescription, parentFolder, languageId);
			}
		}
		return new ArrayList<ICLanguageSettingEntry>(0);
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
		List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();
		List<String> alreadyAdded = new ArrayList<String>();
	
		List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
		for (ILanguageSettingsProvider provider: providers) {
			List<ICLanguageSettingEntry> providerEntries = getSettingEntriesUpResourceTree(provider, cfgDescription, resource, languageId);
			for (ICLanguageSettingEntry entry : providerEntries) {
				if (entry!=null) {
					String entryName = entry.getName();
					boolean isRightKind = (entry.getKind() & kind) != 0;
					// Only first entry is considered
					// Entry flagged as "UNDEFINED" prevents adding entry with the same name down the line
					if (isRightKind && !alreadyAdded.contains(entryName)) {
						if ((entry.getFlags() & ICSettingEntry.UNDEFINED) == 0) {
							entries.add(entry);
						}
						alreadyAdded.add(entryName);
					}
				}
			}
		}
	
		return entries;
	}

}

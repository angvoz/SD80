package org.eclipse.cdt.internal.ui.newui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;

public class LanguageSettingEntriesProvidersTabEditable extends LanguageSettingEntriesProvidersTab {
	// providerId -> provider
	private Map<String, EditedProvider> editedProviders = new HashMap<String, EditedProvider>();

	private class EditedProvider implements ILanguageSettingsEditableProvider {
		private String id;
		private String name;
		// cfgId -> languageID -> lsEntries
		private Map<String, Map<String, List<ICLanguageSettingEntry>>> cfgLangEntries;

		public EditedProvider(String id, String name) {
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		/**
		 * Set setting entries for the given configuration and language.
		 * @param entries - modified entries to keep. {@code null} will remove the list from the map.
		 */
		public void setSettingEntries(ICConfigurationDescription cfgDescription, IResource rc,
				String languageId, List<ICLanguageSettingEntry> entries) {

			String cfgId = cfgDescription.getId();
			if (entries!=null) {
				Map<String, List<ICLanguageSettingEntry>> langEntries;
				if (cfgLangEntries==null) {
					cfgLangEntries = new HashMap<String, Map<String,List<ICLanguageSettingEntry>>>();
					langEntries = null;
				} else {
					langEntries = cfgLangEntries.get(cfgId);
				}
				if (langEntries==null) {
					langEntries = new HashMap<String, List<ICLanguageSettingEntry>>();
					cfgLangEntries.put(cfgId, langEntries);
				}
				langEntries.put(languageId, new ArrayList<ICLanguageSettingEntry>(entries));
			} else {
				if (cfgLangEntries!=null) {
					Map<String, List<ICLanguageSettingEntry>> langEntries = cfgLangEntries.get(cfgId);
					if (langEntries!=null) {
						langEntries.remove(languageId);
						if (langEntries.size()==0) {
							cfgLangEntries.remove(cfgId);
							langEntries=null;
						}
					}
					if (cfgLangEntries.size()==0) {
						cfgLangEntries = null;
					}
				}
			}

		}

		public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription,
				IResource rc, String languageId) {

			List<ICLanguageSettingEntry> entries = null;
			if (cfgLangEntries!=null) {
				String cfgId = cfgDescription.getId();
				Map<String, List<ICLanguageSettingEntry>> langEntries = cfgLangEntries.get(cfgId);
				if (langEntries!=null) {
					entries = langEntries.get(languageId);
				}
			}
			return entries;
		}

	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);

		treeEntriesViewer.setLabelProvider(new LanguageSettingsContributorsLabelProvider() {
			@Override
			protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
				String[] overlayKeys = new String[5];
				if (currentLanguageSetting != null) {
					List<ICLanguageSettingEntry> entries = getSettingEntries(provider);
					if (entries == null) {
						List<ICLanguageSettingEntry> entriesParent = getSettingEntriesUpResourceTree(provider);
						if (entriesParent != null && entriesParent.size() > 0) {
							overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_PARENT;
						}
					} else {
						if (provider instanceof ILanguageSettingsEditableProvider || provider instanceof LanguageSettingsSerializable) {
							overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_SETTING;
						}
					}
				}
				return overlayKeys;
			}
		});
	}

	//	/** TODO
	//	 * @return {@code true} if the error parsers are allowed to be editable,
	//	 *     i.e. Add/Edit/Delete buttons are enabled and Options page edits enabled.
	//	 *     This will evaluate to {@code true} for Preference Build Settings page but
	//	 *     not for Preference New CDT Project Wizard/Makefile Project.
	//	 */
	private boolean isProviderCustomizable(ILanguageSettingsProvider provider) {
		return page.isForPrefs() || !LanguageSettingsManager.isWorkspaceProvider(provider);
	}

	@Override
	protected ICOptionPage createOptionsPage(ILanguageSettingsProvider provider) {
		ICOptionPage optionsPage = null;
		if (provider!=null) {
			optionsPage = LanguageSettingsProviderAssociation.createOptionsPage(provider);
		}
		if (optionsPage==null) {
			optionsPage = super.createOptionsPage(provider);
		}
		
		if (optionsPage instanceof AbstractCOptionPage) {
			((AbstractCOptionPage)optionsPage).init(provider);
		}
		return optionsPage;
	}

	private EditedProvider makeEditedProvider(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription, IResource rc) {
		List<ICLanguageSettingEntry> entries;
		EditedProvider editedProvider;
		editedProvider = new EditedProvider(provider.getId(), provider.getName());
		for (ICLanguageSetting languageSetting : allLanguages) {
			String langId = languageSetting.getLanguageId();
			if (langId!=null) {
				entries = provider.getSettingEntries(cfgDescription, rc, langId);
				editedProvider.setSettingEntries(cfgDescription, rc, langId, entries);
			}
		}
		return editedProvider;
	}

	/**
	 * That method returns exact position of an element in the list.
	 * Note that {@link List#indexOf(Object)} returns position of the first element
	 * equals to the given one, not exact element.
	 *
	 * @param entries
	 * @param entry
	 * @return exact position of the element or -1 of not found.
	 */
	private int getExactIndex(List<ICLanguageSettingEntry> entries, ICLanguageSettingEntry entry) {
		if (entries!=null) {
			for (int i=0;i<entries.size();i++) {
				if (entries.get(i)==entry)
					return i;
			}
		}
		return -1;
	}

	@Override
	protected List<ILanguageSettingsProvider> getProviders(ICLanguageSetting languageSetting) {
		List<ILanguageSettingsProvider> itemsList = new LinkedList<ILanguageSettingsProvider>();
		if (languageSetting!=null) {
			String langId = languageSetting.getLanguageId();
			if (langId != null) {
				IResource rc = getResource();
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				if (rc!=null && cfgDescription!=null) {
					List<ILanguageSettingsProvider> cfgProviders = cfgDescription.getLanguageSettingProviders();
					for (ILanguageSettingsProvider cfgProvider : cfgProviders) {
						if (cfgProvider instanceof LanguageSettingsBaseProvider) {
							// filter out providers incapable of providing entries for this language
							List<String> languageIds = ((LanguageSettingsBaseProvider)cfgProvider).getLanguageIds();
							if (languageIds!=null && !languageIds.contains(langId)) {
								continue;
							}
						}
						String providerId = cfgProvider.getId();
						ILanguageSettingsProvider provider = editedProviders.get(providerId);
						if (provider==null) {
							provider = cfgProvider;
						}
						itemsList.add(provider);
					}
				}
			}
		}
		return itemsList;
	}

	private void addEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
		if (entry != null) {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			String providerId = provider.getId();
			String languageId = currentLanguageSetting.getLanguageId();
			IResource rc = getResource();

			EditedProvider editedProvider = editedProviders.get(providerId);
			if (editedProvider==null) {
				editedProvider = makeEditedProvider(provider, cfgDescription, rc);
				editedProviders.put(providerId, editedProvider);
			}

			List<ICLanguageSettingEntry> entries = getSettingEntries(editedProvider);
			if (entries==null) {
				entries = getSettingEntriesUpResourceTree(provider);
			}

			ICLanguageSettingEntry selectedEntry = getSelectedEntry();
			int pos = getExactIndex(entries, selectedEntry)+1;

			entries.add(pos, entry);
			editedProvider.setSettingEntries(cfgDescription, rc, languageId, entries);

			update();

			selectItem(providerId, entry);
			updateButtons();
		}
	}

	private ICLanguageSettingEntry doAdd() {
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		LanguageSettingEntryDialog dlg = new LanguageSettingEntryDialog(usercomp.getShell(), cfgDescription, selectedEntry, true);
		if (dlg.open()) {
			return dlg.getEntries()[0];
		}
		return null;
	}

	private ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();
		ICConfigurationDescription cfgDecsription = getConfigurationDescription();
		LanguageSettingEntryDialog dlg = new LanguageSettingEntryDialog(usercomp.getShell(), cfgDecsription, selectedEntry);
		if (dlg.open()) {
			return dlg.getEntries()[0];
		}
		return null;
	}

	@Override
	protected void performAdd(ILanguageSettingsProvider selectedProvider) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			ICLanguageSettingEntry settingEntry = doAdd();
			addEntry(selectedProvider, settingEntry);
		}
	}

	@Override
	protected void performEdit(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry entry) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			ICLanguageSettingEntry settingEntry = doEdit(entry);
			if (settingEntry!=null) {
				deleteEntry(selectedProvider, entry);
				addEntry(selectedProvider, settingEntry);
			}
		}
	}

	private void deleteEntry(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
		if (entry != null) {
			List<ICLanguageSettingEntry> entriesOld = getSettingEntriesUpResourceTree(provider);
			int pos = getExactIndex(entriesOld, entry);

			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			String providerId = provider.getId();
			String languageId = currentLanguageSetting.getLanguageId();
			IResource rc = getResource();

			List<ICLanguageSettingEntry> entries;
			EditedProvider editedProvider = editedProviders.get(providerId);
			if (editedProvider==null) {
				editedProvider = makeEditedProvider(provider, cfgDescription, rc);
				editedProviders.put(providerId, editedProvider);
			}
			entries = getSettingEntriesUpResourceTree(editedProvider);
			if (entries!=null) {
				entries.remove(entry);
				editedProvider.setSettingEntries(cfgDescription, rc, languageId, entries);
			}

			update();

			List<ICLanguageSettingEntry> entriesNew = getSettingEntriesUpResourceTree(provider);
			ICLanguageSettingEntry nextEntry=null;
			if (entriesNew!=null) {
				if (pos>=entriesNew.size()) {
					pos = entriesNew.size()-1;
				}
				if (pos>=0) {
					nextEntry=entriesNew.get(pos);
				}
			}

			selectItem(providerId, nextEntry);
			updateButtons();
		} else if (provider instanceof ILanguageSettingsEditableProvider && !LanguageSettingsManager.isWorkspaceProvider(provider) && getSettingEntriesUpResourceTree(provider)!=null) {
			// TODO: deprecated?
			String languageId = currentLanguageSetting.getLanguageId();
			if (languageId!=null) {
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				IResource rc = getResource();
				ILanguageSettingsEditableProvider epro = (ILanguageSettingsEditableProvider)provider;
				epro.setSettingEntries(cfgDescription, rc, languageId, null);
				update();
			}
		}
	}

	@Override
	protected void performDelete(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			if (selectedEntry!=null) {
				deleteEntry(selectedProvider, selectedEntry);
//			} else if (selectedProvider instanceof ???){
//				((???)selectedProvider).clear();
			}
		}
	}

	private TreeItem findProviderItem(String id) {
		TreeItem[] providerItems = treeEntries.getItems();
		for (TreeItem providerItem : providerItems) {
			Object providerItemData = providerItem.getData();
			if (providerItemData instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)providerItemData;
				if (provider.getId().equals(id)) {
					return providerItem;
				}
			}
		}
		return null;
	}

	private TreeItem findEntryItem(String id, ICLanguageSettingEntry entry) {
		TreeItem[] providerItems = treeEntries.getItems();
		for (TreeItem providerItem : providerItems) {
			Object providerItemData = providerItem.getData();
			if (providerItemData instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)providerItemData;
				if (provider.getId().equals(id)) {
					TreeItem[] entryItems = providerItem.getItems();
					for (TreeItem entryItem : entryItems) {
						Object entryItemData = entryItem.getData();
						if (entryItemData==entry)
							return entryItem;
					}
//					return providerItem;
				}
			}
		}
		return null;
	}

	private void selectItem(String providerId, ICLanguageSettingEntry entry) {
		TreeItem providerItem = findProviderItem(providerId);
		if (providerItem!=null) {
			treeEntries.select(providerItem);
			if (providerItem.getItems().length>0) {
				treeEntries.showItem(providerItem.getItems()[0]);
			}
			TreeItem entryItem = findEntryItem(providerId, entry);
			if (entryItem!=null) {
				treeEntries.showItem(entryItem);
				treeEntries.select(entryItem);
			}
		}
	}

	private void moveEntry(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry, boolean isUp) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider && selectedEntry!=null) {
			EditedProvider editedProvider = editedProviders.get(selectedProvider);
			if (editedProvider==null) {
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				IResource rc = getResource();
				editedProvider = makeEditedProvider(selectedProvider, cfgDescription, rc);
				editedProviders.put(editedProvider.getId(), editedProvider);
			}
			List<ICLanguageSettingEntry> entries = getSettingEntriesUpResourceTree(editedProvider);
			int x = getExactIndex(entries, selectedEntry);
			if (x >= 0) {
				if (!isUp)
					x++; // "down" simply means "up underlying item"
				ICLanguageSettingEntry old = entries.get(x);
				ICLanguageSettingEntry old2 = entries.get(x - 1);
				entries.remove(x);
				entries.remove(x - 1);
				entries.add(x - 1, old);
				entries.add(x, old2);
	
				setSettingEntries(editedProvider, entries);
				update();
				selectItem(editedProvider.getId(), selectedEntry);
				updateButtons();
			}
		}
	}

	@Override
	protected void performMoveDown(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (isConfigureMode) {
			super.performMoveDown(selectedProvider, selectedEntry);
		} else if (selectedEntry!=null) {
			moveEntry(selectedProvider, selectedEntry, false);
		}
	}

	@Override
	protected void performMoveUp(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (isConfigureMode) {
			super.performMoveUp(selectedProvider, selectedEntry);
		} else if (selectedEntry!=null) {
			moveEntry(selectedProvider, selectedEntry, true);
		}
	}

	@Override
	protected void performDefaults() {
		if (!page.isForPrefs()) {
			editedProviders = new HashMap<String, EditedProvider>();
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			IResource rc = getResource();
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			for (ILanguageSettingsProvider provider : providers) {
				if (provider instanceof ILanguageSettingsEditableProvider) {
					String providerId = provider.getId();
					EditedProvider editedProvider = editedProviders.get(providerId);
					if (editedProvider==null) {
						editedProvider = makeEditedProvider(provider, cfgDescription, rc);
						editedProviders.put(providerId, editedProvider);
					}
					TreeItem[] tisLang = treeLanguages.getItems();
					for (TreeItem tiLang : tisLang) {
						Object item = tiLang.getData();
						if (item instanceof ICLanguageSetting) {
							String languageId = ((ICLanguageSetting)item).getLanguageId();
							if (languageId!=null) {
								editedProvider.setSettingEntries(cfgDescription, rc, languageId, null);
							}
						}
					}
				}
			}
			updateData(this.getResDesc());
		}
		
		super.performDefaults();
	}

//	private void informOptionPages(boolean apply) {
//	Collection<ICOptionPage> pages = optionsPageMap.values();
//	for (ICOptionPage dynamicPage : pages) {
//		if (dynamicPage!=null && dynamicPage.isValid() && dynamicPage.getControl() != null) {
//			try {
//				if (apply)
//					dynamicPage.performApply(new NullProgressMonitor());
//				else
//					dynamicPage.performDefaults();
//			} catch (CoreException e) {
//				CUIPlugin.log("ErrorParsTab.error.OnApplyingSettings", e);
//			}
//		}
//	}
//}

	@Override
	protected void performApply(ICResourceDescription srcRcDescription, ICResourceDescription destRcDescription) {
//		informOptionPages(true);

		if (!page.isForPrefs()) {
			IResource rc = getResource();

			ICConfigurationDescription srcCfgDescription = srcRcDescription.getConfiguration();
			ICConfigurationDescription destCfgDescription = destRcDescription.getConfiguration();

			List<ILanguageSettingsProvider> destProviders = new ArrayList<ILanguageSettingsProvider>();

			List<ILanguageSettingsProvider> srcProviders = srcCfgDescription.getLanguageSettingProviders();
			for (ILanguageSettingsProvider pro : srcProviders) {
				EditedProvider editedProvider = editedProviders.get(pro.getId());

				if (editedProvider!=null) {
					if (pro instanceof ILanguageSettingsEditableProvider) {
						if (pro instanceof LanguageSettingsSerializable) {
							LanguageSettingsSerializable spro = (LanguageSettingsSerializable)pro;
							if (LanguageSettingsManager.isWorkspaceProvider(spro)) {
								try {
									pro = spro.clone();
									if (pro.getClass()!=spro.getClass())
										throw new CloneNotSupportedException("Class " + spro.getClass() + " does not support cloning.");
								} catch (CloneNotSupportedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						ILanguageSettingsEditableProvider epro = (ILanguageSettingsEditableProvider)pro;
						for (ICLanguageSetting languageSetting : allLanguages) {
							String languageId = languageSetting.getLanguageId();
							if (languageId!=null) {
								List<ICLanguageSettingEntry> entries = editedProvider.getSettingEntries(srcCfgDescription, rc, languageId);
								epro.setSettingEntries(srcCfgDescription, rc, languageId, entries);
							}
						}
					}
				}
				destProviders.add(pro);
			}

			destCfgDescription.setLanguageSettingProviders(destProviders);
		}
		
		super.performApply(srcRcDescription, destRcDescription);
	}

	@Override
	protected void performOK() {
		if (!page.isForPrefs()) {
			// FIXME: for now only handles current configuration
			ICResourceDescription rcDesc = getResDesc();
			IResource rc = getResource();
			ICConfigurationDescription cfgDescription = rcDesc.getConfiguration();
			
			List<ILanguageSettingsProvider> destProviders = new ArrayList<ILanguageSettingsProvider>();
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			for (ILanguageSettingsProvider pro : providers) {
				EditedProvider editedProvider = editedProviders.get(pro.getId());
	
				if (editedProvider!=null) {
					if (pro instanceof ILanguageSettingsEditableProvider) {
						if (pro instanceof LanguageSettingsSerializable) {
							LanguageSettingsSerializable spro = (LanguageSettingsSerializable)pro;
							if (LanguageSettingsManager.isWorkspaceProvider(spro)) {
								try {
									pro = spro.clone();
									if (pro.getClass()!=spro.getClass())
										throw new CloneNotSupportedException("Class " + spro.getClass() + " does not support cloning.");
								} catch (CloneNotSupportedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						ILanguageSettingsEditableProvider epro = (ILanguageSettingsEditableProvider)pro;
						for (ICLanguageSetting languageSetting : allLanguages) {
							String languageId = languageSetting.getLanguageId();
							if (languageId!=null) {
								List<ICLanguageSettingEntry> entries = editedProvider.getSettingEntries(cfgDescription, rc, languageId);
								epro.setSettingEntries(cfgDescription, rc, languageId, entries);
							}
						}
					}
				}
				destProviders.add(pro);
			}
			cfgDescription.setLanguageSettingProviders(destProviders);
		}
		
		super.performOK();
	}

	/**
	 * Updates state for all buttons Called when table selection changes.
	 */
	@Override
	protected void updateButtons() {
		super.updateButtons();
		
		if (!isConfigureMode) {
			ILanguageSettingsProvider provider = getSelectedProvider();
			ICLanguageSettingEntry entry = getSelectedEntry();
			boolean isEntrySelected = entry!=null;
			boolean isProviderSelected = !isEntrySelected && (provider!=null);
			boolean isProviderEditable = provider instanceof ILanguageSettingsEditableProvider;
			
			boolean canAdd = isProviderEditable && !isConfigureMode;
			boolean canEdit = isProviderEditable && isEntrySelected;
			boolean canDelete = isProviderEditable && isEntrySelected;
			boolean canClear = isProviderEditable && isProviderSelected
				&& !LanguageSettingsManager.isWorkspaceProvider(provider)
				&& getSettingEntries(provider)!=null;
			
			boolean canMoveUp = false;
			boolean canMoveDown = false;
			if (isProviderEditable && isEntrySelected) {
				List<ICLanguageSettingEntry> entries = getSettingEntriesUpResourceTree(provider);
				int last = entries.size()-1;
				int pos = getExactIndex(entries, entry);
				
				if (pos>=0 && pos<=last) {
					canMoveUp = pos!=0;
					canMoveDown = pos!=last;
				}
			}
			
			buttonSetEnabled(BUTTON_MOVE_UP, canMoveUp);
			buttonSetEnabled(BUTTON_MOVE_DOWN, canMoveDown);
			
			buttonSetEnabled(BUTTON_ADD, canAdd);
			buttonSetEnabled(BUTTON_EDIT, canEdit);
			buttonSetEnabled(BUTTON_DELETE, canDelete || canClear);
		}

	}

	@Override
	protected final boolean isIndexerAffected() {
		// TODO
		return true;
	}

	/**
	 * Shortcut for setting setting entries for current context.
	 *
	 */
	private void setSettingEntries(ILanguageSettingsEditableProvider provider, List<ICLanguageSettingEntry> entries) {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		String languageId = currentLanguageSetting.getLanguageId();
		if (languageId!=null)
			provider.setSettingEntries(cfgDescription, rc, languageId, entries);
	}

}

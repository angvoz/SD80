package org.eclipse.cdt.internal.ui.newui;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager_TBD;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.DialogsMessages;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

public class LanguageSettingsProviderTabEditable extends LanguageSettingsProviderTab {
	private static final String WORKSPACE_PREFERENCE_PAGE = "org.eclipse.cdt.make.ui.preferences.BuildSettings"; //$NON-NLS-1$

	private Button globalProviderCheckBox = null;
	private Link linkWorkspacePreferences = null;
	
	// providerId -> provider
	private Map<String, EditedProvider> editedProviders = new HashMap<String, EditedProvider>();

	protected class LanguageSettingsContributorsLabelProviderEnhanced extends LanguageSettingsContributorsLabelProvider {
		@Override
		protected String getBaseKey(ILanguageSettingsProvider provider) {
			String imageKey = null;
			// try id-association
			URL url = LanguageSettingsProviderAssociation.getImageUrl(provider.getId());
			// try class-association
			if (url==null) {
				url = LanguageSettingsProviderAssociation.getImage(provider.getClass());
			}
			if (url!=null) {
				imageKey = url.toString();
			}
			
			if (imageKey==null) {
				imageKey = super.getBaseKey(provider);
			}
			return imageKey;
		}
	}
		
	/**
	 * Default provider options page.
	 *
	 */
	private class DummyProviderOptionsPage extends AbstractCOptionPage {
		@Override
		public void createControl(Composite parent) {
			Label label = new Label(parent, SWT.NONE);
			if (page.isForPrefs()) {
				label.setText("No options are available.");
			}
			setControl(label);
		}
		
		@Override
		public void performApply(IProgressMonitor monitor) throws CoreException {
		}

		@Override
		public void performDefaults() {
		}

	}
	

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
		isConfigureMode = true;
		super.createControls(parent);

		tableProvidersViewer.setLabelProvider(new LanguageSettingsContributorsLabelProviderEnhanced() {
			@Override
			protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
				String[] overlayKeys = new String[5];
				if (LanguageSettingsManager.isWorkspaceProvider(provider)) {
					overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_GLOBAL;
//					overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_REFERENCE;
//					overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_PARENT;
//					overlayKeys[IDecoration.BOTTOM_RIGHT] = CDTSharedImages.IMG_OVR_LINK;
				} else {
//					overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_CONFIGURATION;
//					overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_INDEXED;
//					overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_CONTEXT;
					
//					overlayKeys[IDecoration.TOP_LEFT] = CDTSharedImages.IMG_OVR_PROJECT;
				}
				return overlayKeys;
			}
		});
	}

	private Link createLinkToPreferences(final Composite parent) {
		Link link = new Link(parent, SWT.NONE);
//		// FIXME
//		link.setText(DialogsMessages.RegexErrorParserOptionPage_LinkToPreferencesMessage + " Select Discovery Tab.");

		link.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				// Use event.text to tell which link was used
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), WORKSPACE_PREFERENCE_PAGE, null, null).open();
			}
		});

		return link;
	}


	@Override
	protected void createOptionsControl() {
		groupOptionsPage = new Group(sashFormConfigure, SWT.SHADOW_ETCHED_IN);
		groupOptionsPage.setText("Language Settings Provider Options");
		groupOptionsPage.setLayout(new GridLayout(2, false));
		
		if (!page.isForPrefs()) {
			if (globalProviderCheckBox==null) {
				globalProviderCheckBox = new Button(groupOptionsPage, SWT.CHECK);
				globalProviderCheckBox.setText("Use shared provider defined globally.");
				globalProviderCheckBox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						updateConfigureTable();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

				});
				
				linkWorkspacePreferences = createLinkToPreferences(groupOptionsPage);
			}
		}

		compositeOptionsPage = new Composite(groupOptionsPage, SWT.NONE);
		compositeOptionsPage.setLayout(new TabFolderLayout());
	}

	private void updateConfigureTable() {
		// TODO
		ILanguageSettingsProvider oldProvider = getSelectedProvider();
		ILanguageSettingsProvider newProvider = null;

		String id = oldProvider.getId();
		if (globalProviderCheckBox.getSelection()) {
			// Global provider reference chosen
			newProvider = LanguageSettingsManager.getWorkspaceProvider(id);
		} else {
			// Local provider instance chosen
			if (oldProvider instanceof LanguageSettingsSerializable) {
				try {
					// TODO: add new method to LanguageSettingsSerializable to avoid cloning data
					newProvider = ((LanguageSettingsSerializable)oldProvider).clone();
					((LanguageSettingsSerializable)newProvider).clear();
				} catch (CloneNotSupportedException e) {
					CUIPlugin.log("Exception trying to clone workspace provider "+id, e);
					return;
				}
			}
		}
		if (newProvider!=null) {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			List<ILanguageSettingsProvider> providers = cfgDescription.getLanguageSettingProviders();
			int pos = providers.indexOf(oldProvider);
			providers.remove(oldProvider);
			providers.add(pos, newProvider);
			cfgDescription.setLanguageSettingProviders(providers);
			updateTableConfigureProviders();
			tableProviders.setSelection(pos);
			initializeOptionsPage(newProvider);
			displaySelectedOptionPage();
			updateButtons();
//			updateData(getResDesc());
			tableProvidersViewer.update(newProvider, null);
		}
	}

	@Override
	protected ICOptionPage createOptionsPage(ILanguageSettingsProvider provider) {
		ICOptionPage optionsPage = null;
		if (provider!=null) {
			optionsPage = LanguageSettingsProviderAssociation.createOptionsPage(provider);
		}
		if (optionsPage==null) {
			optionsPage = new DummyProviderOptionsPage();
		}
		
		if (optionsPage instanceof AbstractCOptionPage) {
			((AbstractCOptionPage)optionsPage).init(provider);
		}
		return optionsPage;
	}

	@Override
	protected void initializeOptionsPage(ILanguageSettingsProvider provider) {
		super.initializeOptionsPage(provider);
	}

	@Override
	protected void displaySelectedOptionPage() {
		super.displaySelectedOptionPage();

		ILanguageSettingsProvider provider = getSelectedProvider();
		boolean isChecked = tableProvidersViewer.getChecked(provider);
		if (!page.isForPrefs()) {
			boolean canClone = provider instanceof LanguageSettingsSerializable || provider instanceof ILanguageSettingsEditableProvider;
			boolean isGlobal = provider!=null && LanguageSettingsManager.isWorkspaceProvider(provider);
			globalProviderCheckBox.setSelection(isGlobal);
			globalProviderCheckBox.setEnabled(isChecked && canClone);
			globalProviderCheckBox.setVisible(provider!=null);
			
			boolean needPreferencesLink = ! (currentOptionsPage instanceof DummyProviderOptionsPage) && isGlobal;
			// TODO: message
			linkWorkspacePreferences.setText(needPreferencesLink ? DialogsMessages.RegexErrorParserOptionPage_LinkToPreferencesMessage + " Select Discovery Tab." : "");
			linkWorkspacePreferences.pack();
		}
		currentOptionsPage.getControl().setEnabled(provider!=null && isChecked && !LanguageSettingsManager.isWorkspaceProvider(provider));
	}

//	@Override
//	protected void saveCheckedProviders(Object selectedElement) {
//		if (page.isForProject()) {
//			Object[] checked = tableProvidersViewer.getCheckedElements();
//			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(checked.length);
//			for (Object elem : checked) {
//				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)elem;
////				if (provider==selectedElement && provider instanceof LanguageSettingsSerializable && LanguageSettingsManager.isWorkspaceProvider(provider)) {
////					try {
////						provider = ((LanguageSettingsSerializable)provider).clone();
////						((LanguageSettingsSerializable)provider).clear();
////						selectedElement = provider;
////					} catch (Exception e) {
////						// Log error but use workspace provider in this case
////						CUIPlugin.log("Error cloning provider "+provider.getName()+ ", class = "+provider.getClass(), e);
////					}
////				}
//				providers.add(provider);
//
//			}
//
//			ICConfigurationDescription cfgDescription = getConfigurationDescription();
//				cfgDescription.setLanguageSettingProviders(providers);
//				updateData(getResDesc());
//				if (selectedElement!=null) {
//					tableProvidersViewer.update(selectedElement, null);
//				}
//		}
//	}

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

	@Override
	protected void performDelete(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedProvider instanceof LanguageSettingsSerializable){
			if (selectedEntry==null) {
				((LanguageSettingsSerializable)selectedProvider).clear();
			}
		} else if (selectedProvider instanceof ILanguageSettingsEditableProvider){
			// FIXME
//			if (selectedEntry==null) {
//				((ILanguageSettingsEditableProvider)selectedProvider).clear();
//			}
		}
	}

	@Override
	protected void performDefaults() {
		if (page.isForPrefs()) {
			if (MessageDialog.openQuestion(usercomp.getShell(),
					Messages.LanguageSettingsProviderTab_TitleResetProviders,
					Messages.LanguageSettingsProviderTab_AreYouSureToResetProviders)) {

				try {
					LanguageSettingsManager_TBD.setUserDefinedProviders(null);
				} catch (CoreException e) {
					CUIPlugin.log(Messages.LanguageSettingsProviderTab_ErrorPerformingDefaults, e);
				}
			}
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
		
		if (page.isForPrefs()) {
			// Build Settings page
			try {
				ILanguageSettingsProvider[] providers = new ILanguageSettingsProvider[tableProviders.getItemCount()];
				TableItem[] items = tableProviders.getItems();
				for (int i=0;i<items.length;i++) {
					providers[i] = (ILanguageSettingsProvider) items[i].getData();
				}

				Object[] checkedElements = tableProvidersViewer.getCheckedElements();
				String[] checkedProviderIds = new String[checkedElements.length];
				for (int i=0;i<checkedElements.length;i++) {
					checkedProviderIds[i] = ((ILanguageSettingsProvider)checkedElements[i]).getId();
				}

				LanguageSettingsManager_TBD.setUserDefinedProviders(providers);
			} catch (CoreException e) {
				CUIPlugin.log(Messages.LanguageSettingsProviderTab_ErrorApplyingSettings, e);
			}
		}
		
		super.performOK();
	}

	@Override
	protected boolean isIndexerAffected() {
		// TODO
		return true;
	}

}

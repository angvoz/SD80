/*******************************************************************************
 * Copyright (c) 2010, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.newui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager_TBD;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.DialogsMessages;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

/**
 * This tab presents language settings entries categorized by language
 * settings providers.
 *
 *@noinstantiate This class is not intended to be instantiated by clients.
 *@noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingsProviderTab extends AbstractCPropertyTab {
	private static final String WORKSPACE_PREFERENCE_PAGE = "org.eclipse.cdt.ui.preferences.BuildSettingProperties"; //$NON-NLS-1$
	private final DummyProviderOptionsPage DUMMY_PROVIDER_OPTIONS_PAGE = new DummyProviderOptionsPage();

//	private static final String RENAME_STR = "Rename...";
	private static final String RUN_STR = Messages.LanguageSettingsProviderTab_Run;
	private static final String CLEAR_STR = Messages.LanguageSettingsProviderTab_Clear;
	private static final String RESET_STR = "Reset";

//	private static final int BUTTON_RENAME = 0;
	private static final int BUTTON_RUN = 0;
	private static final int BUTTON_CLEAR = 1;
	private static final int BUTTON_RESET = 2;
	// there is a separator instead of button #3
	private static final int BUTTON_MOVE_UP = 4;
	private static final int BUTTON_MOVE_DOWN = 5;

	private final static String[] BUTTON_LABELS = {
//		RENAME_STR,
		RUN_STR,
		CLEAR_STR,
		RESET_STR,
		null,
		MOVEUP_STR,
		MOVEDOWN_STR,
	};
	
	private static final int[] DEFAULT_CONFIGURE_SASH_WEIGHTS = new int[] { 50, 50 };
	private SashForm sashFormConfigure;

	private Table tableProviders;
	private CheckboxTableViewer tableProvidersViewer;
	private Group groupOptionsPage;
	private ICOptionPage currentOptionsPage = null;
	private Composite compositeOptionsPage;
	
	private Button enableProvidersCheckBox;
	private StatusMessageLine fStatusLine;

	private Button globalProviderCheckBox = null;
	private Link linkWorkspacePreferences = null;
	
	private Page_LanguageSettingsProviders masterPropertyPage = null;

	private List<ILanguageSettingsProvider> presentedProviders = null;
	private final Map<String, ICOptionPage> optionsPageMap = new HashMap<String, ICOptionPage>();
	private Map<String, List<ILanguageSettingsProvider>> initialProvidersByCfg = new HashMap<String, List<ILanguageSettingsProvider>>();
	
	private boolean initialEnablement = false;
	
	/**
	 * The preference page can dynamically change LS Providers in the given configuration.
	 * The dependent controls should pick on that as well, so reference is provided instead
	 * of real provider.
	 *
	 */
	public class ProviderReference {
		private final String providerId;
		private final ICConfigurationDescription cfgDescription;
		
		public ProviderReference(String providerId, ICConfigurationDescription cfgDescription) {
			this.providerId = providerId;
			this.cfgDescription = cfgDescription;
			
			Assert.isNotNull(getProvider());
		}

		/**
		 * Get current provider associated with a given id. Use as read-only only.
		 * Warning: Do not cache the result as the provider can be replaced at any time.
		 * 
		 * @return the provider
		 */
		public ILanguageSettingsProvider getProvider() {
			return findProvider(providerId, presentedProviders);
		}
		
		
		/**
		 * Returns current working copy of the provider. Creates one if it has not been created yet.
		 * Use when need to modify the provider.
		 * Warning: Do not cache the result as the provider can be replaced at any time.
		 * 
		 * @return the provider
		 */
		public ILanguageSettingsProvider getWorkingCopy() {
			ILanguageSettingsProvider provider = findProvider(providerId, presentedProviders);
			Assert.isTrue(provider instanceof ILanguageSettingsEditableProvider);
			Assert.isTrue(provider==getSelectedProvider());
			
			if (isWorkingCopy(provider))
				return provider;
			
			ILanguageSettingsEditableProvider editableProvider = (ILanguageSettingsEditableProvider)provider;
			try {
				ILanguageSettingsEditableProvider newProvider = editableProvider.clone();
				replaceSelectedProvider(newProvider);
				return newProvider;
				
			} catch (CloneNotSupportedException e) {
				CUIPlugin.log("Error cloning provider " + editableProvider.getId(), e);
				// TODO warning dialog for user?
			}

			return null;
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
				label.setText(Messages.LanguageSettingsProviderTab_NoOptionsAvailable);
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

	private class ProvidersTableLabelProvider extends LanguageSettingsProvidersLabelProvider {
		@Override
		protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
			String[] overlayKeys = super.getOverlayKeys(provider);
			
			if (LanguageSettingsManager_TBD.isReconfigured(provider)) {
				overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_SETTING;
			}
			
			if (isWorkingCopy(provider)) {
				overlayKeys[IDecoration.TOP_RIGHT] = CDTSharedImages.IMG_OVR_EDITED;
			}
			return overlayKeys;
		}
	}
		
	/**
	 * Shortcut for getting the current resource for the property page.
	 */
	private IResource getResource() {
		return (IResource)page.getElement();
	}

	/**
	 * Shortcut for getting the current configuration description.
	 */
	private ICConfigurationDescription getConfigurationDescription() {
		if (page.isForPrefs())
			return null;
		
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
		return cfgDescription;
	}

	/**
	 * Shortcut for getting the currently selected provider.
	 */
	private ILanguageSettingsProvider getSelectedProvider() {
		ILanguageSettingsProvider provider = null;

		int pos = tableProviders.getSelectionIndex();
		if (pos >= 0 && pos<tableProviders.getItemCount()) {
			provider = (ILanguageSettingsProvider)tableProvidersViewer.getElementAt(pos);
		}
		return provider;
	}

	private void trackInitialSettings() {
		if (page.isForProject()) {
			ICConfigurationDescription[] cfgDescriptions = page.getCfgsEditable();
			for (ICConfigurationDescription cfgDescription : cfgDescriptions) {
				if (cfgDescription!=null) {
					String cfgId = cfgDescription.getId();
					List<ILanguageSettingsProvider> initialProviders = cfgDescription.getLanguageSettingProviders();
					initialProvidersByCfg.put(cfgId, initialProviders);
				}
			}
			initialEnablement = LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject());
		}
	}
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout());
		GridData gd = (GridData) usercomp.getLayoutData();
		// Discourage settings entry table from trying to show all its items at once, see bug 264330
		gd.heightHint =1;
		
		if (page instanceof Page_LanguageSettingsProviders) {
			masterPropertyPage = (Page_LanguageSettingsProviders) page;
		}

		trackInitialSettings();

		// SashForms for each mode
		createConfigureSashForm();
		
		// Status line
		fStatusLine = new StatusMessageLine(usercomp, SWT.LEFT, 2);

		// "I want to try new scanner discovery" temporary checkbox
		enableProvidersCheckBox = setupCheck(usercomp, Messages.CDTMainWizardPage_TrySD80, 2, GridData.FILL_HORIZONTAL);
		enableProvidersCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = enableProvidersCheckBox.getSelection();
				if (masterPropertyPage!=null)
					masterPropertyPage.setLanguageSettingsProvidersEnabled(enabled);
				enableControls(enabled);
				updateStatusLine();
			}
		});

		if (masterPropertyPage!=null)
			enableProvidersCheckBox.setSelection(masterPropertyPage.isLanguageSettingsProvidersEnabled());
		else
			enableProvidersCheckBox.setSelection(LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject()));
		// display but disable the checkbox for file/folder resource
		enableProvidersCheckBox.setEnabled(page.isForProject() /*|| page.isForPrefs()*/);
		enableControls(enableProvidersCheckBox.getSelection());

		initButtons(BUTTON_LABELS);
		updateData(getResDesc());
	}

	private void createConfigureSashForm() {
		// SashForm for Configure
		sashFormConfigure = new SashForm(usercomp, SWT.VERTICAL);
		GridLayout layout = new GridLayout();
		sashFormConfigure.setLayout(layout);

		// Providers table
		Composite compositeSashForm = new Composite(sashFormConfigure, SWT.BORDER | SWT.SINGLE);
		compositeSashForm.setLayout(new GridLayout());
		
		// items checkboxes  only for project properties page
		tableProviders = new Table(compositeSashForm, page.isForPrefs() ? SWT.NONE : SWT.CHECK);
		tableProviders.setLayoutData(new GridData(GridData.FILL_BOTH));
		tableProviders.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displaySelectedOptionPage();
				updateButtons();
			}
		});
		tableProvidersViewer = new CheckboxTableViewer(tableProviders);
		tableProvidersViewer.setContentProvider(new ArrayContentProvider());
		tableProvidersViewer.setLabelProvider(new ProvidersTableLabelProvider());

		tableProvidersViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				saveCheckedProviders(e.getElement());
				tableProvidersViewer.update(e.getElement(), null);
			}});

		createOptionsControl();

		sashFormConfigure.setWeights(DEFAULT_CONFIGURE_SASH_WEIGHTS);
		enableSashForm(sashFormConfigure, true);
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

	// Called from globalProviderCheckBox listener
	private void toggleGlobalProvider(ILanguageSettingsProvider oldProvider) {
		ILanguageSettingsProvider newProvider = null;

		String id = oldProvider.getId();
		if (globalProviderCheckBox.getSelection()) {
			// Global provider reference chosen
			newProvider = LanguageSettingsManager.getWorkspaceProvider(id);
		} else {
			// Local provider instance chosen
			if (oldProvider instanceof ILanguageSettingsEditableProvider) {
				try {
					newProvider = ((ILanguageSettingsEditableProvider)oldProvider).cloneShallow();
				} catch (CloneNotSupportedException e) {
					CUIPlugin.log("Error cloning provider " + oldProvider.getId(), e);
				}
			}
		}
		if (newProvider!=null) {
			replaceSelectedProvider(newProvider);
			
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			initializeOptionsPage(newProvider, cfgDescription);
			displaySelectedOptionPage();
		}
	}

	private void replaceSelectedProvider(ILanguageSettingsProvider newProvider) {
		int pos = tableProviders.getSelectionIndex();
		presentedProviders.set(pos, newProvider);
		
		tableProvidersViewer.setInput(presentedProviders);
		tableProviders.setSelection(pos);

		ICConfigurationDescription cfgDescription = null;
		if (!page.isForPrefs()) {
			cfgDescription = getConfigurationDescription();
			
			List<ILanguageSettingsProvider> cfgProviders = new ArrayList<ILanguageSettingsProvider>(cfgDescription.getLanguageSettingProviders());
			pos = getProviderIndex(newProvider.getId(), cfgProviders);
			cfgProviders.set(pos, newProvider);
			cfgDescription.setLanguageSettingProviders(cfgProviders);
			tableProvidersViewer.setCheckedElements(cfgProviders.toArray(new ILanguageSettingsProvider[0]));
		}
		tableProvidersViewer.refresh(newProvider);
		updateButtons();
	}

	private void createOptionsControl() {
		groupOptionsPage = new Group(sashFormConfigure, SWT.SHADOW_ETCHED_IN);
		groupOptionsPage.setText("Language Settings Provider Options");
		groupOptionsPage.setLayout(new GridLayout(2, false));
		
		if (!page.isForPrefs()) {
			if (globalProviderCheckBox==null) {
				globalProviderCheckBox = new Button(groupOptionsPage, SWT.CHECK);
				globalProviderCheckBox.setText("Shared provider defined globally.");
				globalProviderCheckBox.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						boolean isGlobal = globalProviderCheckBox.getSelection();
						ILanguageSettingsProvider provider = getSelectedProvider();
						if (isGlobal != LanguageSettingsManager.isWorkspaceProvider(provider)) {
							toggleGlobalProvider(provider);
						}
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

	private void enableSashForm(SashForm sashForm, boolean enable) {
		sashForm.setVisible(enable);
		// Some of woodoo to fill properties page vertically and still keep right border visible in preferences 
		GridData gd = new GridData(enable || page.isForPrefs() ? GridData.FILL_BOTH : SWT.NONE);
		gd.horizontalSpan = 2;
		gd.heightHint = enable ? SWT.DEFAULT : 0;
		sashForm.setLayoutData(gd);
	}

	private void enableControls(boolean enable) {
		sashFormConfigure.setEnabled(enable);
		tableProviders.setEnabled(enable);
		compositeOptionsPage.setEnabled(enable);
		
		buttoncomp.setEnabled(enable);

		if (enable) {
			displaySelectedOptionPage();
		} else {
			if (currentOptionsPage != null) {
				currentOptionsPage.setVisible(false);
			}
			disableButtons();
		}
	}
	
	/**
	 * Populate provider tables and their option pages which are used in Configure mode
	 */
	private void updateProvidersTable() {
		tableProvidersViewer.setInput(presentedProviders);

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription!=null) {
			List<ILanguageSettingsProvider> cfgProviders = cfgDescription.getLanguageSettingProviders();
			tableProvidersViewer.setCheckedElements(cfgProviders.toArray(new ILanguageSettingsProvider[0]));
		}
	
		optionsPageMap.clear();
		initializeOptionsPage(null, null); // adds default page as a placeholder
		for (ILanguageSettingsProvider provider : presentedProviders) {
			if (LanguageSettingsManager.isWorkspaceProvider(provider))
				initializeOptionsPage(provider, null);
			else
				initializeOptionsPage(provider, cfgDescription);
		}

		displaySelectedOptionPage();
	}

	private void initializeProviders() {
		// The providers list is formed to consist of configuration providers (checked elements on top of the table)
		// and after that other providers which could be possible added (unchecked) sorted by name.

		List<String> idsList = new ArrayList<String>();

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription!=null) {
			presentedProviders = new ArrayList<ILanguageSettingsProvider>(cfgDescription.getLanguageSettingProviders());
			for (ILanguageSettingsProvider provider : presentedProviders) {
				idsList.add(provider.getId());
			}
		} else {
			presentedProviders =  new ArrayList<ILanguageSettingsProvider>();
		}
		
		List<ILanguageSettingsProvider> workspaceProviders = LanguageSettingsManager.getWorkspaceProviders();
		
		// ensure sorting by name all unchecked providers
		Set<ILanguageSettingsProvider> allAvailableProvidersSet = new TreeSet<ILanguageSettingsProvider>(new Comparator<ILanguageSettingsProvider>() {
			public int compare(ILanguageSettingsProvider prov1, ILanguageSettingsProvider prov2) {
				return prov1.getName().compareTo(prov2.getName());
			}
		});
		allAvailableProvidersSet.addAll(workspaceProviders);

		for (ILanguageSettingsProvider provider : allAvailableProvidersSet) {
			String id = provider.getId();
			if (!idsList.contains(id)) {
				presentedProviders.add(provider);
				idsList.add(id);
			}
		}
		
	}

	private ICOptionPage createOptionsPage(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription) {
		ICOptionPage optionsPage = null;
		if (provider!=null) {
			optionsPage = LanguageSettingsProviderAssociation.createOptionsPage(provider);
		}
		if (optionsPage==null) {
			optionsPage = DUMMY_PROVIDER_OPTIONS_PAGE;
		}
		
		if (optionsPage instanceof AbstractCOptionPage && provider!=null) {
			((AbstractCOptionPage)optionsPage).init(new ProviderReference(provider.getId(), cfgDescription));
		}
		return optionsPage;
	}

	private void initializeOptionsPage(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription) {
		ICOptionPage optionsPage = createOptionsPage(provider, cfgDescription);
		
		boolean isChecked = tableProvidersViewer.getChecked(provider);
		boolean isEditableForProject = provider!=null && page.isForProject() && isChecked /*&& globalProviderCheckBox.isEnabled()*/ && !LanguageSettingsManager.isWorkspaceProvider(provider);
		boolean isEditableForPrefs = provider!=null && page.isForPrefs();
		boolean isEditable = isEditableForProject || isEditableForPrefs;
		compositeOptionsPage.setEnabled(isEditable);

		String id = (provider!=null) ? provider.getId() : null;
		optionsPageMap.put(id, optionsPage);
		optionsPage.setContainer(page);
		optionsPage.createControl(compositeOptionsPage);
		optionsPage.setVisible(false);
		compositeOptionsPage.layout(true);
	}

	private void displaySelectedOptionPage() {
		if (currentOptionsPage != null) {
			currentOptionsPage.setVisible(false);
		}

		ILanguageSettingsProvider provider = getSelectedProvider();
		String id = (provider!=null) ? provider.getId() : null;

		currentOptionsPage = optionsPageMap.get(id);

		boolean isChecked = tableProvidersViewer.getChecked(provider);
		if (!page.isForPrefs()) {
			boolean canClone = provider instanceof ILanguageSettingsEditableProvider;
			boolean isEditable = provider instanceof ILanguageSettingsEditableProvider;
			boolean isGlobal = provider!=null && LanguageSettingsManager.isWorkspaceProvider(provider);
			// Currently editing global editable providers is not allowed (clone will be created on attempt)
			globalProviderCheckBox.setSelection(isGlobal /*&& !isEditable*/);
			globalProviderCheckBox.setEnabled(isChecked && canClone /*&& !isEditable*/);
			globalProviderCheckBox.setVisible(provider!=null);
			
			boolean needPreferencesLink = ! (currentOptionsPage instanceof DummyProviderOptionsPage) && isGlobal;
			// TODO: message
			linkWorkspacePreferences.setText(needPreferencesLink ? DialogsMessages.RegexErrorParserOptionPage_LinkToPreferencesMessage + " Select Discovery Tab." : "");
			linkWorkspacePreferences.pack();
		}
		
		if (currentOptionsPage != null &&  ! (currentOptionsPage instanceof DummyProviderOptionsPage)) {
			boolean isEditableForProject = provider!=null && page.isForProject() && isChecked && globalProviderCheckBox.isEnabled() && !LanguageSettingsManager.isWorkspaceProvider(provider);
			boolean isEditableForPrefs = provider!=null && page.isForPrefs();
			boolean isEditable = isEditableForProject || isEditableForPrefs;
			currentOptionsPage.getControl().setEnabled(isEditable);
			currentOptionsPage.setVisible(true);

			compositeOptionsPage.setEnabled(isEditable);
//			compositeOptionsPage.layout(true);
		}
	}


	private void saveCheckedProviders(Object selectedElement) {
		if (page.isForProject()) {
			Object[] checked = tableProvidersViewer.getCheckedElements();
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(checked.length);
			for (Object element : checked) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
				providers.add(provider);
			}
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			cfgDescription.setLanguageSettingProviders(providers);
			
			if (selectedElement!=null) {
				tableProvidersViewer.update(selectedElement, null);
				if (selectedElement instanceof ILanguageSettingsProvider) {
					ILanguageSettingsProvider selectedProvider = (ILanguageSettingsProvider) selectedElement;
					initializeOptionsPage(selectedProvider, cfgDescription);
					displaySelectedOptionPage();
				}
			}
		}
	}

	private void disableButtons() {
//		buttonSetEnabled(BUTTON_RENAME, false);
		buttonSetEnabled(BUTTON_RUN, false);
		buttonSetEnabled(BUTTON_CLEAR, false);
		buttonSetEnabled(BUTTON_RESET, false);
		buttonSetEnabled(BUTTON_MOVE_UP, false);
		buttonSetEnabled(BUTTON_MOVE_DOWN, false);
//		buttonSetEnabled(BUTTON_CONFIGURE, false);
	}

	/**
	 * Updates state for all buttons. Called when table selection changes.
	 */
	@Override
	protected void updateButtons() {
		ILanguageSettingsProvider provider = getSelectedProvider();
		boolean isProviderSelected =provider!=null;
		boolean canForWorkspace = isProviderSelected && page.isForPrefs();
		boolean canForConfiguration = isProviderSelected && page.isForProject() && !LanguageSettingsManager.isWorkspaceProvider(provider);

		int pos = tableProviders.getSelectionIndex();
		int count = tableProviders.getItemCount();
		int last = count - 1;
		boolean isRangeOk = pos >= 0 && pos <= last;

		boolean canClear = false;
		if (provider instanceof ILanguageSettingsEditableProvider) {
			if (!((ILanguageSettingsEditableProvider) provider).isEmpty()) {
				canClear = canForWorkspace || canForConfiguration;
			}
		}
		
		boolean canReset = false;
		if (provider!=null && (canForWorkspace || canForConfiguration)) {
			canReset = ! LanguageSettingsManager_TBD.isEqualExtensionProvider(provider);
		}
		
		boolean canMoveUp = page.isForProject() && isProviderSelected && isRangeOk && pos!=0;
		boolean canMoveDown = page.isForProject() && isProviderSelected && isRangeOk && pos!=last;
		
//		buttonSetEnabled(BUTTON_RENAME, false);
		buttonSetEnabled(BUTTON_RUN, false);
		buttonSetEnabled(BUTTON_CLEAR, canClear);
		buttonSetEnabled(BUTTON_RESET, canReset);
		buttonSetEnabled(BUTTON_MOVE_UP, canMoveUp);
		buttonSetEnabled(BUTTON_MOVE_DOWN, canMoveDown);
	}

	/**
	 * Displays warning message - if any - for selected language settings entry.
	 */
	private void updateStatusLine() {
//		IStatus status=null;
//		fStatusLine.setErrorStatus(status);
	}

	/**
	 * Handle buttons
	 */
	@Override
	public void buttonPressed(int buttonIndex) {
		ILanguageSettingsProvider selectedProvider = getSelectedProvider();

		switch (buttonIndex) {
//		case BUTTON_RENAME:
//			performRename(selectedProvider);
//			break;
		case BUTTON_RUN:
			performRun(selectedProvider);
			break;
		case BUTTON_CLEAR:
			performClear(selectedProvider);
			break;
		case BUTTON_RESET:
			performReset(selectedProvider);
			break;
		case BUTTON_MOVE_UP:
			performMoveUp(selectedProvider);
			break;
		case BUTTON_MOVE_DOWN:
			performMoveDown(selectedProvider);
			break;
		default:
		}
	}

	private void performRun(ILanguageSettingsProvider selectedProvider) {
	}
	
	private void performClear(ILanguageSettingsProvider selectedProvider) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			ILanguageSettingsEditableProvider selectedEditableProvider = (ILanguageSettingsEditableProvider) selectedProvider;
			
			if (isWorkingCopy(selectedEditableProvider)) {
				selectedEditableProvider.clear();
				tableProvidersViewer.update(selectedEditableProvider, null);
			} else {
				try {
					ILanguageSettingsEditableProvider newProvider = selectedEditableProvider.cloneShallow();
					replaceSelectedProvider(newProvider);

					ICConfigurationDescription cfgDescription = getConfigurationDescription();
					initializeOptionsPage(newProvider, cfgDescription);
					displaySelectedOptionPage();
					
				} catch (CloneNotSupportedException e) {
					CUIPlugin.log("Error cloning provider " + selectedEditableProvider.getId(), e);
					// TODO warning dialog for user?
					return;
				}
			}
			
			updateButtons();
		}
	}

	private void performReset(ILanguageSettingsProvider selectedProvider) {
		if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
			ILanguageSettingsEditableProvider selectedEditableProvider = (ILanguageSettingsEditableProvider) selectedProvider;
			
			try {
				ILanguageSettingsProvider newProvider = LanguageSettingsManager_TBD.getExtensionProviderCopy(selectedEditableProvider.getId());
				replaceSelectedProvider(newProvider);

				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				initializeOptionsPage(newProvider, cfgDescription);
				displaySelectedOptionPage();
				
			} catch (CloneNotSupportedException e) {
				CUIPlugin.log("Error cloning provider " + selectedEditableProvider.getId(), e);
				// TODO warning dialog for user?
				return;
			}
			
			updateButtons();
		}

	}

	private boolean isWorkingCopy(ILanguageSettingsProvider provider) {
		boolean isWorkingCopy = false;
		if (page.isForPrefs()) {
			isWorkingCopy = ! LanguageSettingsManager.isWorkspaceProvider(provider);
		} else {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			List<ILanguageSettingsProvider> cfgProviders = cfgDescription.getLanguageSettingProviders();
			if (cfgProviders.contains(provider)) {
				List<ILanguageSettingsProvider> initialProviders = initialProvidersByCfg.get(cfgDescription.getId());
				ILanguageSettingsProvider initialProvider = findProvider(provider.getId(), initialProviders);
				isWorkingCopy = ! (initialProvider==provider);
			}
			
		}
		return isWorkingCopy;
	}

	private void performMoveUp(ILanguageSettingsProvider selectedProvider) {
		if (selectedProvider!=null) {
			moveProvider(selectedProvider, true);
		}
	}

	private void performMoveDown(ILanguageSettingsProvider selectedProvider) {
		if (selectedProvider!=null) {
			moveProvider(selectedProvider, false);
		}
	}

	// Move provider up / down
	private void moveProvider(ILanguageSettingsProvider selectedProvider, boolean up) {
		int currentPos = presentedProviders.indexOf(selectedProvider);
		int last = presentedProviders.size() - 1;

		if (currentPos < 0 || (up && currentPos==0) || (!up && currentPos==last))
			return;
		
		presentedProviders.remove(selectedProvider);
		int newPos = up ? currentPos-1 : currentPos+1;
		presentedProviders.add(newPos, selectedProvider);
		
		updateProvidersTable();
		tableProviders.setSelection(newPos);
		
		saveCheckedProviders(null);
		updateButtons();
	}

	private ICLanguageSetting[] getLangSettings(ICResourceDescription rcDes) {
		switch (rcDes.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription) rcDes;
			return foDes.getLanguageSettings();
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription) rcDes;
			ICLanguageSetting langSetting = fiDes.getLanguageSetting();
			return (langSetting != null) ? new ICLanguageSetting[] { langSetting } : null;
		}
		return null;
	}

	/**
	 * Called when configuration changed
	 */
	@Override
	public void updateData(ICResourceDescription rcDes) {
		if (!canBeVisible())
			return;

		if (rcDes!=null) {
			if (page.isMultiCfg()) {
				setAllVisible(false, null);
				return;
			} else {
				setAllVisible(true, null);
			}
			
			if (masterPropertyPage!=null) {
				boolean enabled = masterPropertyPage.isLanguageSettingsProvidersEnabled();
				enableProvidersCheckBox.setSelection(enabled);
				enableControls(enabled);
			}
		}

		// for Preference page initialize providers list just once as no configuration here to change
		// and re-initializing could ruins modified providers in case of switching tabs or pages
		if (!page.isForPrefs() || presentedProviders==null) {
			initializeProviders();
		}
		updateProvidersTable();
		updateButtons();
	}

	@Override
	protected void performDefaults() {
		if (enableProvidersCheckBox==null || enableProvidersCheckBox.getSelection()==false)
			return;
		
		if (page.isForPrefs()) {
			if (MessageDialog.openQuestion(usercomp.getShell(),
					Messages.LanguageSettingsProviderTab_TitleResetProviders,
					Messages.LanguageSettingsProviderTab_AreYouSureToResetProviders)) {
				
				for (int i=0;i<presentedProviders.size();i++) {
					ILanguageSettingsProvider provider = presentedProviders.get(i);
					if (provider instanceof ILanguageSettingsEditableProvider && !LanguageSettingsManager_TBD.isEqualExtensionProvider(provider)) {
						String id = provider.getId();
						try {
							ILanguageSettingsProvider newProvider = LanguageSettingsManager_TBD.getExtensionProviderCopy(id);
							presentedProviders.set(i, newProvider);
						} catch (CloneNotSupportedException e) {
							CUIPlugin.log("Error cloning provider " + id, e);
						}
					}
				}
			}
		}

		if (page.isForProject()) {
			if (MessageDialog.openQuestion(usercomp.getShell(),
					Messages.LanguageSettingsProviderTab_TitleResetProviders,
					Messages.LanguageSettingsProviderTab_AreYouSureToResetProviders)) {
				
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				List<ILanguageSettingsProvider> cfgProviders = new ArrayList<ILanguageSettingsProvider>(cfgDescription.getLanguageSettingProviders());
				boolean atLeastOneChanged = false;
				for (int i=0;i<cfgProviders.size();i++) {
					ILanguageSettingsProvider provider = cfgProviders.get(i);
					if (provider instanceof ILanguageSettingsEditableProvider && !LanguageSettingsManager_TBD.isEqualExtensionProvider(provider)) {
						String id = provider.getId();
						try {
							ILanguageSettingsProvider newProvider = LanguageSettingsManager_TBD.getExtensionProviderCopy(id);
							cfgProviders.set(i, newProvider);
							atLeastOneChanged = true;
						} catch (CloneNotSupportedException e) {
							CUIPlugin.log("Error cloning provider " + id, e);
						}
					}
				}
				if (atLeastOneChanged) {
					cfgDescription.setLanguageSettingProviders(cfgProviders);
				}
				
			}
		}

		updateData(getResDesc());
	}


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
				// TODO: clone
				destProviders.add(pro);
			}

			destCfgDescription.setLanguageSettingProviders(destProviders);
		}
		
		if (!page.isForPrefs()) {
			ICConfigurationDescription sd = srcRcDescription.getConfiguration();
			ICConfigurationDescription dd = destRcDescription.getConfiguration();
			List<ILanguageSettingsProvider> newProviders = sd.getLanguageSettingProviders();
			dd.setLanguageSettingProviders(newProviders);
		}

		performOK();
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
				// TODO: clone
				destProviders.add(pro);
			}
			cfgDescription.setLanguageSettingProviders(destProviders);
		}
		
		// Build Settings page
		if (page.isForPrefs()) {
			try {
				LanguageSettingsManager.setUserDefinedProviders(presentedProviders);
			} catch (CoreException e) {
				CUIPlugin.log("Error setting user defined providers", e);
			}
		}
		
		if (page.isForProject() && enableProvidersCheckBox!=null) {
			boolean enabled = enableProvidersCheckBox.getSelection();
			if (masterPropertyPage!=null)
				enabled = masterPropertyPage.isLanguageSettingsProvidersEnabled();
			LanguageSettingsManager.setLanguageSettingsProvidersEnabled(page.getProject(), enabled);
			enableProvidersCheckBox.setSelection(enabled);
		}
		
		Collection<ICOptionPage> optionPages = optionsPageMap.values();
		for (ICOptionPage op : optionPages) {
			try {
				op.performApply(null);
			} catch (CoreException e) {
				CUIPlugin.log("Error applying options page", e);
			}
		}

		try {
			LanguageSettingsManager_TBD.serializeWorkspaceProviders();
		} catch (CoreException e) {
			CUIPlugin.log("Internal Error", e);
			throw new UnsupportedOperationException("Internal Error");
		}

		trackInitialSettings();
		updateData(getResDesc());
	}

	@Override
	public boolean canBeVisible() {
		if (!CDTPrefUtil.getBool(CDTPrefUtil.KEY_SHOW_PROVIDERS))
			return false;
		if (page.isForPrefs())
			return true;
		
		if (!page.isForProject())
			return false;

		ICLanguageSetting [] langSettings = getLangSettings(getResDesc());
		if (langSettings == null)
			return false;

		for (ICLanguageSetting langSetting : langSettings) {
			String langId = langSetting.getLanguageId();
			if (langId!=null && langId.length()>0) {
				LanguageManager langManager = LanguageManager.getInstance();
				ILanguageDescriptor langDes = langManager.getLanguageDescriptor(langId);
				if (langDes != null)
					return true;
			}
		}

		return false;
	}

	@Override
	protected boolean isIndexerAffected() {
		List<ILanguageSettingsProvider> newProvidersList = null;
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription!=null) {
			newProvidersList = cfgDescription.getLanguageSettingProviders();
		}
		boolean newEnablement = LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject());
		
		// TODO
		boolean isEqualList = false;
//		boolean isEqualList = (newProvidersList==initialProvidersMap) || (newProvidersList!=null && newProvidersList.equals(initialProvidersMap));
		return newEnablement!=initialEnablement || (newEnablement==true && !isEqualList);
	}

	private ILanguageSettingsProvider findProvider(String id, List<ILanguageSettingsProvider> providers) {
		for (ILanguageSettingsProvider provider : providers) {
			if (provider.getId().equals(id))
				return provider;
		}
		return null;
	}

	private int getProviderIndex(String id, List<ILanguageSettingsProvider> providers) {
		int pos = 0;
		for (ILanguageSettingsProvider p : providers) {
			if (p.getId().equals(id))
				return pos;
			pos++;
		}
		return -1;
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

}

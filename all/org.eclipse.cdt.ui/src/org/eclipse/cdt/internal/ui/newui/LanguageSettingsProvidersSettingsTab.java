/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.newui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.ibm.icu.text.MessageFormat;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager_TBD;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionPage;
import org.eclipse.cdt.ui.dialogs.IInputStatusValidator;
import org.eclipse.cdt.ui.dialogs.InputStatusDialog;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.utils.ui.controls.TabFolderLayout;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;


/**
 * @deprecated. TODO: Remove this class after LanguageSettingEntriesProvidersTab implements all functionality.
 */
@Deprecated
public class LanguageSettingsProvidersSettingsTab extends AbstractCPropertyTab {
	private static final int DEFAULT_HEIGHT = 130;
	private static final int BUTTON_ADD = 0;
	private static final int BUTTON_EDIT = 1;
	private static final int BUTTON_DELETE = 2;
	// there is a separator instead of button = 3
	private static final int BUTTON_MOVEUP = 4;
	private static final int BUTTON_MOVEDOWN = 5;

	private static final String[] BUTTONS = new String[] {
		ADD_STR,
		EDIT_STR,
		DEL_STR,
		null,
		MOVEUP_STR,
		MOVEDOWN_STR,
	};

	private static final String OOPS = "OOPS"; //$NON-NLS-1$

	private Table fTable;
	private CheckboxTableViewer fTableViewer;
	private ICConfigurationDescription fCfgDesc;

	private final Map<String, ILanguageSettingsProvider> fAvailableProvidersMap = new LinkedHashMap<String, ILanguageSettingsProvider>();
	private final Map<String, ICOptionPage> fOptionsPageMap = new HashMap<String, ICOptionPage>();
	private ICOptionPage fCurrentOptionsPage = null;

	private Composite fCompositeForOptionsPage;
	private Button fCheckBoxGlobal;

	// FIXME dummy
	private class RegexProvider implements ILanguageSettingsProvider {
		public RegexProvider(String id, String name) {
			// TODO Auto-generated constructor stub
		}
		public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
			// TODO Auto-generated method stub
			return null;
		}
		public String getId() {
			// TODO Auto-generated method stub
			return null;
		}
		public String getName() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {

		super.createControls(parent);
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(usercomp, ICHelpContextIds.PROVIDERS_PAGE);

		usercomp.setLayout(new GridLayout(1, false));

		// SashForm
		SashForm sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setBackground(sashForm.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		sashForm.setOrientation(SWT.VERTICAL);
		sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 5;
		sashForm.setLayout(layout);

		// table
		Composite compositeSashForm = new Composite(sashForm, SWT.NONE);
		compositeSashForm.setLayout(new GridLayout(2, false));
		fTable = new Table(compositeSashForm, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
		fTable.setLayoutData(new GridData(GridData.FILL_BOTH));
		fTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				displaySelectedOptionPage();
				updateButtons();
		}});
		fTableViewer = new CheckboxTableViewer(fTable);
		fTableViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		fTableViewer.setLabelProvider(new LanguageSettingsContributorsLabelProvider());

		fTableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				saveChecked(e.getElement());
				fTableViewer.update(e.getElement(), null);
			}});

		// Buttons
		Composite compositeButtons = new Composite(compositeSashForm, SWT.NONE);
		compositeButtons.setLayoutData(new GridData(GridData.END));
		initButtons(compositeButtons, BUTTONS);

		if (page.isForProject()) {
			fCheckBoxGlobal = new Button(compositeSashForm, SWT.CHECK);
			fCheckBoxGlobal.setText("Use global instance defined in workspace");
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			fCheckBoxGlobal.setLayoutData(gd);
			fCheckBoxGlobal.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent event) {
					// TODO
					ILanguageSettingsProvider oldProvider = getSelectedProvider();
					ILanguageSettingsProvider newProvider = null;

					String id = oldProvider.getId();
					if (fCheckBoxGlobal.getSelection()) {
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
						List<ILanguageSettingsProvider> providers = fCfgDesc.getLanguageSettingProviders();
						int pos = providers.indexOf(oldProvider);
						providers.remove(oldProvider);
						providers.add(pos, newProvider);
						fCfgDesc.setLanguageSettingProviders(providers);
						initMapProviders();
						fTable.setSelection(pos);
						initializeOptionsPage(id);
						displaySelectedOptionPage();
						updateButtons();
//						updateData(getResDesc());
						fTableViewer.update(newProvider, null);
					}
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});

		}

		fCompositeForOptionsPage = new Composite(sashForm, SWT.NULL);
		GridData gd = new GridData();
		fCompositeForOptionsPage.setLayout(new TabFolderLayout());

		PixelConverter converter = new PixelConverter(parent);
		gd.heightHint = converter.convertHorizontalDLUsToPixels(DEFAULT_HEIGHT);

		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalSpan = 2;
		fCompositeForOptionsPage.setLayoutData(gd);

		sashForm.setWeights(new int[] {50, 50});

		// init data
		ICResourceDescription resDecs = getResDesc();
		fCfgDesc = resDecs!=null ? resDecs.getConfiguration() : null;
		initMapProviders();
		updateData(getResDesc());
	}

	private void initMapProviders() {
		fAvailableProvidersMap.clear();
		fOptionsPageMap.clear();
		List<ILanguageSettingsProvider> allProviders = LanguageSettingsManager.getWorkspaceProviders();
		for (ILanguageSettingsProvider provider : allProviders) {
			String id = provider.getId();
			fAvailableProvidersMap.put(id, provider);
			initializeOptionsPage(id);
		}

		List<ILanguageSettingsProvider> cfgProviders;
		List<ILanguageSettingsProvider> availableProviders = new ArrayList<ILanguageSettingsProvider>();
		if (!page.isForPrefs()) {
			ICConfigurationDescription srcCfgDesc = fCfgDesc; // .getConfiguration();
			cfgProviders = srcCfgDesc.getLanguageSettingProviders();
			for (ILanguageSettingsProvider provider : cfgProviders) {
				fAvailableProvidersMap.put(provider.getId(), provider);
			}
			availableProviders = new ArrayList<ILanguageSettingsProvider>(cfgProviders);
			Set<ILanguageSettingsProvider> allAvailableProvidersSet = new TreeSet<ILanguageSettingsProvider>(new Comparator<ILanguageSettingsProvider>() {
				public int compare(ILanguageSettingsProvider prov1, ILanguageSettingsProvider prov2) {
					return prov1.getName().compareTo(prov2.getName());
				}
			});
			allAvailableProvidersSet.addAll(fAvailableProvidersMap.values());
			for (ILanguageSettingsProvider provider : allAvailableProvidersSet) {
				if (!availableProviders.contains(provider)) {
					availableProviders.add(provider);
				}
			}
//			if (srcCfgDesc instanceof ICMultiConfigDescription) {
//				// FIXME
////				String[][] ss = ((ICMultiConfigDescription)srcCfgDesc).getProviderIDs();
////				ids = CDTPrefUtil.getStrListForDisplay(ss);
//				fAvailableProviders.addAll(fAvailableProviders.values());
//			} else {
//				availableProviders = new LinkedHashSet<ILanguageSettingsProvider>(cfgProviders);
//				Collection<ILanguageSettingsProvider> providers = fAvailableProviders.values();
//				for (ILanguageSettingsProvider pro : providers) {
//					String id = pro.getId();
//					if (fCfgDesc.getL)
//				}
//			}
		} else {
			availableProviders = new ArrayList<ILanguageSettingsProvider>();
			Set<ILanguageSettingsProvider> allAvailableProvidersSet = new TreeSet<ILanguageSettingsProvider>(new Comparator<ILanguageSettingsProvider>() {
				public int compare(ILanguageSettingsProvider prov1, ILanguageSettingsProvider prov2) {
					return prov1.getName().compareTo(prov2.getName());
				}
			});
			allAvailableProvidersSet.addAll(fAvailableProvidersMap.values());
			for (ILanguageSettingsProvider provider : allAvailableProvidersSet) {
				if (!availableProviders.contains(provider)) {
					availableProviders.add(provider);
				}
			}
			cfgProviders = new ArrayList<ILanguageSettingsProvider>();
		}
		fTableViewer.setInput(availableProviders.toArray(new ILanguageSettingsProvider[0]));
		fTableViewer.setCheckedElements(cfgProviders.toArray(new ILanguageSettingsProvider[0]));

		displaySelectedOptionPage();
	}


	private void initializeOptionsPage(String id) {
		List<ILanguageSettingsProvider> providers;
		if (page.isForPrefs()) {
			providers = LanguageSettingsManager.getWorkspaceProviders();
		} else {
			providers = fCfgDesc.getLanguageSettingProviders();
		}
		if (providers==null)
			return;

		ILanguageSettingsProvider provider = null;
		for (ILanguageSettingsProvider pr : providers) {
			if (id.equals(pr.getId())) {
				provider = pr;
			}
		}
		if (provider!=null) {
			String name = provider.getName();
			if (name!=null && name.length()>0) {
				ICOptionPage optionsPage = LanguageSettingsProviderAssociation.createOptionsPage(provider);
				if (optionsPage!=null) {
					if (optionsPage instanceof AbstractCOptionPage) {
						((AbstractCOptionPage)optionsPage).init(provider);
					}
					fOptionsPageMap.put(id, optionsPage);
					optionsPage.setContainer(page);
					fCompositeForOptionsPage.setEnabled(isProviderCustomizable(provider));
					optionsPage.createControl(fCompositeForOptionsPage);
					optionsPage.setVisible(false);
					fCompositeForOptionsPage.layout(true);
				}
			}
		}
	}

	private void displaySelectedOptionPage() {
		if (fCurrentOptionsPage != null)
			fCurrentOptionsPage.setVisible(false);

		int pos = fTable.getSelectionIndex();
		if (pos<0)
			return;

		ILanguageSettingsProvider provider = (ILanguageSettingsProvider)fTable.getItem(pos).getData();
		String providerId = provider.getId();
		ICOptionPage optionsPage = fOptionsPageMap.get(providerId);
		if (optionsPage != null) {
			optionsPage.setVisible(true);
		}
		fCurrentOptionsPage = optionsPage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#buttonPressed(int)
	 */
	@Override
	public void buttonPressed (int n) {
		switch (n) {
		case BUTTON_ADD:
			addProvider();
			break;
		case BUTTON_EDIT:
			editProvider();
			break;
		case BUTTON_DELETE:
			deleteProvider();
			break;
		case BUTTON_MOVEUP:
			moveItem(true);
			break;
		case BUTTON_MOVEDOWN:
			moveItem(false);
			break;
		default:
			break;
		}
		updateButtons();
	}

	// Move item up / down
	private void moveItem(boolean up) {
		int n = fTable.getSelectionIndex();
		if (n < 0 || (up && n == 0) || (!up && n+1 == fTable.getItemCount()))
			return;

		ILanguageSettingsProvider provider = (ILanguageSettingsProvider)fTableViewer.getElementAt(n);
		boolean checked = fTableViewer.getChecked(provider);
		fTableViewer.remove(provider);
		n = up ? n-1 : n+1;
		fTableViewer.insert(provider, n);
		fTableViewer.setChecked(provider, checked);
		fTable.setSelection(n);

		saveChecked(null);
	}

	private String makeId(String name) {
		return CUIPlugin.PLUGIN_ID+'.'+name;
	}

	private void addProvider() {
		IInputStatusValidator inputValidator = new IInputStatusValidator() {
			public IStatus isValid(String newText) {
				StatusInfo status = new StatusInfo();
				if (newText.trim().length() == 0) {
					status.setError("ErrorParsTab.error.NonEmptyName");
				} else if (newText.indexOf(LanguageSettingsManager_TBD.PROVIDER_DELIMITER)>=0) {
					String message = MessageFormat.format("ErrorParsTab.error.IllegalCharacter",
							new Object[] { LanguageSettingsManager_TBD.PROVIDER_DELIMITER });
					status.setError(message);
				} else if (fAvailableProvidersMap.containsKey(makeId(newText))) {
					status.setError("ErrorParsTab.error.NonUniqueID");
				}
				return status;
			}

		};
		InputStatusDialog addDialog = new InputStatusDialog(usercomp.getShell(),
				"ErrorParsTab.title.Add",
				"ErrorParsTab.label.EnterName",
				"ErrorParsTab.label.DefaultRegexProviderName",
				inputValidator);
		addDialog.setHelpAvailable(false);

		if (addDialog.open() == Window.OK) {
			String newName = addDialog.getValue();
			String newId = makeId(newName);
			ILanguageSettingsProvider provider = new RegexProvider(newId, newName);
			fAvailableProvidersMap.put(newId, provider);

			fTableViewer.add(provider);
			fTableViewer.setChecked(provider, true);
			fTable.setSelection(fTable.getItemCount()-1);

			updateData(getResDesc());
			updateButtons();
			initializeOptionsPage(newId);
			displaySelectedOptionPage();
		}
	}

	private void editProvider() {
		int n = fTable.getSelectionIndex();
		Assert.isTrue(n>=0);

//		String id = (String)fTableViewer.getElementAt(n);
//		ILanguageSettingsProvider provider = fAvailableProviders.get(id);
		ILanguageSettingsProvider provider = (ILanguageSettingsProvider) fTableViewer.getElementAt(n);

		IInputStatusValidator inputValidator = new IInputStatusValidator() {
			public IStatus isValid(String newText) {
				StatusInfo status = new StatusInfo();
				if (newText.trim().length() == 0) {
					status.setError("ErrorParsTab.error.NonEmptyName");
				} else if (newText.indexOf(LanguageSettingsManager_TBD.PROVIDER_DELIMITER)>=0) {
					String message = MessageFormat.format("ErrorParsTab.error.IllegalCharacter",
							new Object[] { LanguageSettingsManager_TBD.PROVIDER_DELIMITER });
					status.setError(message);
				}
				return status;
			}

		};
		InputStatusDialog addDialog = new InputStatusDialog(usercomp.getShell(),
				"ErrorParsTab.title.Edit",
				"ErrorParsTab.label.EnterName",
				provider.getName(),
				inputValidator);
		addDialog.setHelpAvailable(false);

		if (addDialog.open() == Window.OK) {
//			provider.setName(addDialog.getValue());
			fTableViewer.refresh(provider);
		}
	}

	private void deleteProvider() {
		int n = fTable.getSelectionIndex();
		if (n < 0)
			return;

		fTableViewer.remove(fTableViewer.getElementAt(n));

		int last = fTable.getItemCount() - 1;
		if (n>last)
			n = last;
		if (n>=0)
			fTable.setSelection(n);

		saveChecked(null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateData(org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	public void updateData(ICResourceDescription resDecs) {
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
			return;
		} else {
			setAllVisible(true, null);
		}
		
		ICConfigurationDescription oldCfgDesc = fCfgDesc;
		fCfgDesc = resDecs!=null ? resDecs.getConfiguration() : null;
//		if (oldCfgDesc!=fCfgDesc) {
			initMapProviders();
//		}
		displaySelectedOptionPage();
		updateButtons();
	}

//	private static boolean isExtensionId(String id) {
//		for (String extId : LanguageSettingsManager_TBD.getProviderExtensionIds()) {
//			if (extId.equals(id)) {
//				return true;
//			}
//		}
//		return false;
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateButtons()
	 */
	@Override
	public void updateButtons() {
		int pos = fTable.getSelectionIndex();
		int count = fTable.getItemCount();
		int last = count - 1;
		boolean selected = pos >= 0 && pos <= last;
		ILanguageSettingsProvider provider = (ILanguageSettingsProvider)fTableViewer.getElementAt(pos);
		String id = provider!=null ? provider.getId() : null;

		buttonSetEnabled(BUTTON_ADD, false);
		buttonSetEnabled(BUTTON_EDIT, false);
		buttonSetEnabled(BUTTON_DELETE, false);
		buttonSetEnabled(BUTTON_MOVEUP, selected && pos != 0);
		buttonSetEnabled(BUTTON_MOVEDOWN, selected && pos != last);


		if (page.isForProject()) {
			boolean isChecked = fTableViewer.getChecked(provider);
			boolean canClone = provider instanceof LanguageSettingsSerializable || provider instanceof ILanguageSettingsEditableProvider;
			boolean isGlobal = provider!=null && LanguageSettingsManager.isWorkspaceProvider(provider);
//			boolean select = (isChecked && isGlobal) || !canClone;
			fCheckBoxGlobal.setSelection(isGlobal);
			fCheckBoxGlobal.setEnabled(isChecked && canClone);
		}
	}


	private List<String> getProviderIds(ICConfigurationDescription cfgDescription) {
//		org.eclipse.cdt.managedbuilder.core.ManagedBuildManager.getConfigurationForDescription(cfgDescription);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription, org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();

		if (!page.isForPrefs()) {
			ICConfigurationDescription sd = src.getConfiguration();
			ICConfigurationDescription dd = dst.getConfiguration();
			List<ILanguageSettingsProvider> newProviders = null;

			if (sd instanceof ICMultiConfigDescription) {
				// FIXME
			} else {
				newProviders = sd.getLanguageSettingProviders();
				dd.setLanguageSettingProviders(newProviders);
			}
			initMapProviders();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performOK()
	 */
	@Override
	protected void performOK() {
		informPages(true);

		if (page.isForPrefs()) {
//			if (fCfgDesc==null) {
				// Build Settings page
				try {
					ILanguageSettingsProvider[] providers = new ILanguageSettingsProvider[fTable.getItemCount()];
					TableItem[] items = fTable.getItems();
					for (int i=0;i<items.length;i++) {
						providers[i] = (ILanguageSettingsProvider) items[i].getData();
					}

					Object[] checkedElements = fTableViewer.getCheckedElements();
					String[] checkedProviderIds = new String[checkedElements.length];
					for (int i=0;i<checkedElements.length;i++) {
						checkedProviderIds[i] = ((ILanguageSettingsProvider)checkedElements[i]).getId();
					}

					LanguageSettingsManager_TBD.setUserDefinedProviders(providers);
				} catch (CoreException e) {
					CUIPlugin.log("ErrorParsTab.error.OnApplyingSettings", e);
				}
			}
			initMapProviders();
//		}
	}

	private void saveChecked(Object selectedElement) {
		if (page.isForProject()) {
			Object[] checked = fTableViewer.getCheckedElements();
			List<ILanguageSettingsProvider> providers = new ArrayList<ILanguageSettingsProvider>(checked.length);
			for (Object elem : checked) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)elem;
//				if (provider==selectedElement && provider instanceof LanguageSettingsSerializable && LanguageSettingsManager.isWorkspaceProvider(provider)) {
//					try {
//						provider = ((LanguageSettingsSerializable)provider).clone();
//						((LanguageSettingsSerializable)provider).clear();
//						selectedElement = provider;
//					} catch (Exception e) {
//						// Log error but use workspace provider in this case
//						CUIPlugin.log("Error cloning provider "+provider.getName()+ ", class = "+provider.getClass(), e);
//					}
//				}
				providers.add(provider);

			}

			if (fCfgDesc instanceof ICMultiConfigDescription) {
				// FIXME
//				((ICMultiConfigDescription)fCfgDesc).setProviderIDs(ids);
			} else {
				fCfgDesc.setLanguageSettingProviders(providers);
//				updateData(getResDesc());
				if (selectedElement!=null) {
					fTableViewer.update(selectedElement, null);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#canBeVisible()
	 */
	@Override
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		if (page.isForPrefs()) {
			// Must be Build Settings Preference Page
			if (MessageDialog.openQuestion(usercomp.getShell(),
					"ErrorParsTab.title.ConfirmReset",
					"ErrorParsTab.message.ConfirmReset")) {

				try {
					LanguageSettingsManager_TBD.setUserDefinedProviders(null);
				} catch (CoreException e) {
					CUIPlugin.log("ErrorParsTab.error.OnRestoring", e);
				}
			}
		} else {
			if (fCfgDesc instanceof ICMultiConfigDescription) {
				// FIXME
//				((ICMultiConfigDescription) fCfgDesc).setProviderIDs(null);
			} else {
//				fCfgDesc.getBuildSetting().setProviderIDs(null);
				fCfgDesc.setLanguageSettingProviders(null);
			}
		}
		initMapProviders();
		updateButtons();
	}

	private void informPages(boolean apply) {
		Collection<ICOptionPage> pages = fOptionsPageMap.values();
		for (ICOptionPage dynamicPage : pages) {
			if (dynamicPage!=null && dynamicPage.isValid() && dynamicPage.getControl() != null) {
				try {
					if (apply)
						dynamicPage.performApply(new NullProgressMonitor());
					else
						dynamicPage.performDefaults();
				} catch (CoreException e) {
					CUIPlugin.log("ErrorParsTab.error.OnApplyingSettings", e);
				}
			}
		}
	}

	@Override
	protected boolean isIndexerAffected() {
		return true;
	}

	private ILanguageSettingsProvider getSelectedProvider() {
		int pos = fTable.getSelectionIndex();
		ILanguageSettingsProvider provider = (ILanguageSettingsProvider)fTableViewer.getElementAt(pos);
		return provider;
	}
}

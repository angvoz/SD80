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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager_TBD;
import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
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
public class LanguageSettingEntriesProvidersTab extends AbstractCPropertyTab {
	protected static final int[] DEFAULT_ENTRIES_SASH_WEIGHTS = new int[] { 10, 30 };
	protected static final int[] DEFAULT_CONFIGURE_SASH_WEIGHTS = new int[] { 50, 50 };

	// Show Entries mode
	protected SashForm sashFormEntries;
	protected Tree treeLanguages;
	protected Tree treeEntries;
	protected TreeViewer treeEntriesViewer;
	protected ICLanguageSetting currentLanguageSetting;
	protected ICLanguageSetting[] allLanguages;
	
	// Configure mode
	protected SashForm sashFormConfigure;
	protected Table tableProviders;
	protected CheckboxTableViewer tableProvidersViewer;
	protected Group groupOptionsPage;
	protected ICOptionPage currentOptionsPage = null;
	protected Composite compositeOptionsPage;
	protected final Map<String, ILanguageSettingsProvider> availableProvidersMap = new LinkedHashMap<String, ILanguageSettingsProvider>();
	protected final Map<String, ICOptionPage> optionsPageMap = new HashMap<String, ICOptionPage>();

	protected Button builtInCheckBox;
	protected Button enableProvidersCheckBox;
	protected StatusMessageLine fStatusLine;
	
	protected boolean isConfigureMode = false;

	protected static final int BUTTON_ADD = 0;
	protected static final int BUTTON_EDIT = 1;
	protected static final int BUTTON_DELETE = 2;
	// there is a separator instead of button #3
	protected static final int BUTTON_MOVE_UP = 4;
	protected static final int BUTTON_MOVE_DOWN = 5;
	// there is a separator instead of button #6
	protected static final int BUTTON_CONFIGURE = 7;

	protected final static String[] BUTTON_LABELS = {
		ADD_STR,
		EDIT_STR,
		DEL_STR,
		null,
		MOVEUP_STR,
		MOVEDOWN_STR,
		null,
		"Configure",
	};

	private List<ILanguageSettingsProvider> initialProvidersList = null;
	private boolean initialEnablement = false;
	
	/**
	 * Content provider for setting entries tree.
	 */
	private class LanguageSettingsContributorsContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Object[])
				return (Object[]) parentElement;
			if (parentElement instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider lsProvider = (ILanguageSettingsProvider)parentElement;
				List<ICLanguageSettingEntry> entriesList = getSettingEntriesUpResourceTree(lsProvider);

				if (builtInCheckBox.getSelection()==false) {
					for (Iterator<ICLanguageSettingEntry> iter = entriesList.iterator(); iter.hasNext();) {
						ICLanguageSettingEntry entry = iter.next();
						if (entry.isBuiltIn()) {
							iter.remove();
						}
					}
				}

				if (entriesList!=null) {
					return entriesList.toArray();
				}
			}
			return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			Object[] children = getChildren(element);
			return children!=null && children.length>0;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

	}

	/**
	 * Label provider for language settings entries and providers.
	 *
	 */
	protected class LanguageSettingsContributorsLabelProvider extends LabelProvider {
		protected static final String TEST_PLUGIN_ID = "org.eclipse.cdt.core.tests"; //$NON-NLS-1$
		protected static final String OOPS = "OOPS"; //$NON-NLS-1$

		/**
		 * Returns base image key (for image without overlay).
		 */
		protected String getBaseKey(ILanguageSettingsProvider provider) {
			String imageKey = null;
			if (provider.getId().startsWith(TEST_PLUGIN_ID)) {
				imageKey = CDTSharedImages.IMG_OBJS_CDT_TESTING;
			} else {
				imageKey = CDTSharedImages.IMG_OBJS_EXTENSION;
			}
			return imageKey;
		}

		/**
		 * Returns keys for image overlays. Returning {@code null} is not allowed.
		 */
		protected String[] getOverlayKeys(ILanguageSettingsProvider provider) {
			String[] overlayKeys = new String[5];
			return overlayKeys;
		}
		
		@Override
		public Image getImage(Object element) {
			if (element instanceof ICLanguageSettingEntry) {
				ICLanguageSettingEntry entry = (ICLanguageSettingEntry) element;
				return LanguageSettingsImages.getImage(entry);
			}

			if (element instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
				String imageKey = getBaseKey(provider);
				String[] overlayKeys = getOverlayKeys(provider);
				return CDTSharedImages.getImageOverlaid(imageKey, overlayKeys);
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof ILanguageSettingsProvider) {
				return ((ILanguageSettingsProvider) element).getName();
			} else if (element instanceof ICLanguageSettingEntry) {
				ICLanguageSettingEntry entry = (ICLanguageSettingEntry) element;
				String s = entry.getName();
				if (entry.getKind() == ICSettingEntry.MACRO) {
					s = s + '=' + entry.getValue();
				}
				return s;
			}
			return OOPS;
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
			label.setText("No options are available.");
			setControl(label);
		}
		
		@Override
		public void performApply(IProgressMonitor monitor) throws CoreException {
		}

		@Override
		public void performDefaults() {
		}

	}

	/**
	 * Shortcut for getting the current resource for the property page.
	 */
	protected IResource getResource() {
		return (IResource)page.getElement();
	}

	/**
	 * Shortcut for getting the current configuration description.
	 */
	protected ICConfigurationDescription getConfigurationDescription() {
		return getResDesc().getConfiguration();
	}

	/**
	 * Shortcut for getting the currently selected provider.
	 */
	protected ILanguageSettingsProvider getSelectedProvider() {
		ILanguageSettingsProvider provider = null;

		if (isConfigureMode) {
			int pos = tableProviders.getSelectionIndex();
			if (pos >= 0 && pos<tableProviders.getItemCount()) {
				provider = (ILanguageSettingsProvider)tableProvidersViewer.getElementAt(pos);
			}
		} else {
			TreeItem[] items = treeEntries.getSelection();
			if (items.length>0) {
				TreeItem item = items[0];
				Object itemData = item.getData();
				if (itemData instanceof ICLanguageSettingEntry) {
					item = item.getParentItem();
					if (item!=null) {
						itemData = item.getData();
					}
				}
				if (itemData instanceof ILanguageSettingsProvider) {
					provider = (ILanguageSettingsProvider)itemData;
				}
			}
		}
		return provider;
	}

	/**
	 * Shortcut for getting the currently selected setting entry.
	 */
	protected ICLanguageSettingEntry getSelectedEntry() {
		ICLanguageSettingEntry entry = null;

		if (!isConfigureMode) {
			TreeItem[] selItems = treeEntries.getSelection();
			if (selItems.length==0) {
				return null;
			}
	
			TreeItem item = selItems[0];
			Object itemData = item.getData();
			if (itemData instanceof ICLanguageSettingEntry) {
				entry = (ICLanguageSettingEntry)itemData;
			}
		}
		return entry;
	}

	/**
	 * Shortcut for getting setting entries for current context. {@link LanguageSettingsManager}
	 * will be checking parent resources if no settings defined for current resource.
	 *
	 * @return list of setting entries for the current context.
	 */
	protected List<ICLanguageSettingEntry> getSettingEntriesUpResourceTree(ILanguageSettingsProvider provider) {
		String languageId = currentLanguageSetting.getLanguageId();
		if (languageId==null)
			return null;

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, rc, languageId);
		return entries;
	}

	/**
	 * Shortcut for getting setting entries for current context without checking the parent resource.
	 * @return list of setting entries for the current context.
	 */
	protected List<ICLanguageSettingEntry> getSettingEntries(ILanguageSettingsProvider provider) {
		String languageId = currentLanguageSetting.getLanguageId();
		if (languageId==null)
			return null;

		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		IResource rc = getResource();
		return provider.getSettingEntries(cfgDescription, rc, languageId);
	}

	private void addTreeForLanguages(Composite comp) {
		treeLanguages = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		treeLanguages.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		treeLanguages.setHeaderVisible(true);

		treeLanguages.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = treeLanguages.getSelection();
				if (items.length > 0) {
					ICLanguageSetting langSetting = (ICLanguageSetting) items[0].getData();
					if (langSetting != null) {
						currentLanguageSetting = langSetting;
						updateTreeEntries();
						updateButtons();
					}
				}
			}
		});

		final TreeColumn columnLanguages = new TreeColumn(treeLanguages, SWT.NONE);
		columnLanguages.setText(Messages.AbstractLangsListTab_Languages);
		columnLanguages.setWidth(200);
		columnLanguages.setResizable(false);
		columnLanguages.setToolTipText(Messages.AbstractLangsListTab_Languages);

		treeLanguages.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int x = treeLanguages.getBounds().width - 5;
				if (columnLanguages.getWidth() != x)
					columnLanguages.setWidth(x);
			}
		});

	}

	private void addTreeForEntries(Composite comp) {
		treeEntries = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		treeEntries.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		treeEntries.setHeaderVisible(true);
		treeEntries.setLinesVisible(true);

		final TreeColumn treeCol = new TreeColumn(treeEntries, SWT.NONE);
		treeEntries.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int x = treeEntries.getClientArea().width;
				if (treeCol.getWidth() != x)
					treeCol.setWidth(x);
			}
		});

		treeCol.setText("Setting Entries");
		treeCol.setWidth(200);
		treeCol.setResizable(false);
		treeCol.setToolTipText("Setting Entries Tooltip - FIXME");

		treeEntriesViewer = new TreeViewer(treeEntries);
		treeEntriesViewer.setContentProvider(new LanguageSettingsContributorsContentProvider());
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
					}
				}
				return overlayKeys;
			}
		});
		treeEntriesViewer.setUseHashlookup(true);

		treeEntries.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatusLine();
				updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (buttonIsEnabled(BUTTON_EDIT) && treeEntries.getSelection().length>0)
					buttonPressed(BUTTON_EDIT);
			}
		});

	}

	protected void trackInitialSettings() {
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription!=null) {
			initialProvidersList = cfgDescription.getLanguageSettingProviders();
		}
		initialEnablement = LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject());
	}
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout());

		trackInitialSettings();
		isConfigureMode = page.isForPrefs();

		// SashForms for each mode
		createShowEntriesSashForm();
		createConfigureSashForm();
		
		// Status line
		fStatusLine = new StatusMessageLine(usercomp, SWT.LEFT, 2);

		// "Show built-ins" checkbox
		builtInCheckBox = setupCheck(usercomp, Messages.AbstractLangsListTab_ShowBuiltin, 1, GridData.FILL_HORIZONTAL);
		builtInCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateTreeEntries();
			}
		});
		builtInCheckBox.setSelection(true);
		builtInCheckBox.setEnabled(true);
		builtInCheckBox.setVisible(!isConfigureMode);

		// "I want to try new scanner discovery" temporary checkbox
		enableProvidersCheckBox = setupCheck(usercomp, Messages.CDTMainWizardPage_TrySD80, 2, GridData.FILL_HORIZONTAL);
		enableProvidersCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				enableControls(enableProvidersCheckBox.getSelection());
				updateStatusLine();
			}
		});

		enableProvidersCheckBox.setSelection(LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject()));
		// display but disable the checkbox for file/folder resource
		enableProvidersCheckBox.setEnabled(page.isForProject() && !isConfigureMode);
		enableControls(enableProvidersCheckBox.getSelection());

		initButtons(BUTTON_LABELS);
		updateData(getResDesc());
	}

	private void setConfigureMode(boolean configureMode) {
		if (configureMode==isConfigureMode)
			return;
		
		isConfigureMode = configureMode;
		
		enableSashForm(sashFormEntries, !isConfigureMode);
		treeLanguages.setVisible(!isConfigureMode);
		treeEntries.setVisible(!isConfigureMode);
		builtInCheckBox.setVisible(!isConfigureMode);

		enableSashForm(sashFormConfigure, isConfigureMode);
		tableProviders.setVisible(isConfigureMode);
		
		usercomp.layout();
	}

	private void createShowEntriesSashForm() {
		sashFormEntries = new SashForm(usercomp,SWT.HORIZONTAL);
		GridLayout layout = new GridLayout();
		sashFormEntries.setLayout(layout);

		addTreeForLanguages(sashFormEntries);
		addTreeForEntries(sashFormEntries);

		sashFormEntries.setWeights(DEFAULT_ENTRIES_SASH_WEIGHTS);

		enableSashForm(sashFormEntries, !isConfigureMode);
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
		tableProvidersViewer.setLabelProvider(new LanguageSettingsContributorsLabelProvider());

		tableProvidersViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				saveCheckedProviders(e.getElement());
				tableProvidersViewer.update(e.getElement(), null);
			}});

		createOptionsControl();

		sashFormConfigure.setWeights(DEFAULT_CONFIGURE_SASH_WEIGHTS);
		enableSashForm(sashFormConfigure, isConfigureMode);
		
		updateTableConfigureProviders();
	}

	protected void createOptionsControl() {
		groupOptionsPage = new Group(sashFormConfigure, SWT.SHADOW_ETCHED_IN);
		groupOptionsPage.setText("Language Settings Provider Options");
		groupOptionsPage.setLayout(new GridLayout(2, false));
		
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
		sashFormEntries.setEnabled(enable);
		treeLanguages.setEnabled(enable);
		treeEntries.setEnabled(enable);
		builtInCheckBox.setEnabled(enable);
		
		sashFormConfigure.setEnabled(enable);
		tableProviders.setEnabled(enable);
		compositeOptionsPage.setEnabled(enable);
		
		buttoncomp.setEnabled(enable);

		if (enable) {
			displaySelectedOptionPage();
			updateTreeEntries();
		} else {
			currentOptionsPage.setVisible(false);
			disableButtons();
		}
	}
	
	/**
	 * Populate provider tables and their option pages which are used in Configure mode
	 */
	protected void updateTableConfigureProviders() {
		availableProvidersMap.clear();
		optionsPageMap.clear();

		List<ILanguageSettingsProvider> allProviders = LanguageSettingsManager.getWorkspaceProviders();
		initializeOptionsPage(null); // adds default page as a placeholder
		for (ILanguageSettingsProvider provider : allProviders) {
			String id = provider.getId();
			availableProvidersMap.put(id, provider);
			initializeOptionsPage(provider);
		}
	
		// The providers list is formed to consist of configuration providers (checked elements on top of the table)
		// and after that other providers which could be possible added (unchecked) sorted by name.
		List<ILanguageSettingsProvider> providersList = new ArrayList<ILanguageSettingsProvider>();
		List<ILanguageSettingsProvider> cfgProviders = new ArrayList<ILanguageSettingsProvider>();
		
		if (!page.isForPrefs()) {
			ICConfigurationDescription cfgDescription = getConfigurationDescription();
			cfgProviders = cfgDescription.getLanguageSettingProviders();
			for (ILanguageSettingsProvider provider : cfgProviders) {
				availableProvidersMap.put(provider.getId(), provider);
			}
			providersList = new ArrayList<ILanguageSettingsProvider>(cfgProviders);
		}
		
		// ensure sorting by name all unchecked providers
		Set<ILanguageSettingsProvider> allAvailableProvidersSet = new TreeSet<ILanguageSettingsProvider>(new Comparator<ILanguageSettingsProvider>() {
			public int compare(ILanguageSettingsProvider prov1, ILanguageSettingsProvider prov2) {
				return prov1.getName().compareTo(prov2.getName());
			}
		});
		allAvailableProvidersSet.addAll(availableProvidersMap.values());
		
		for (ILanguageSettingsProvider provider : allAvailableProvidersSet) {
			if (!providersList.contains(provider)) {
				providersList.add(provider);
			}
		}
		
		tableProvidersViewer.setInput(providersList);
		tableProvidersViewer.setCheckedElements(cfgProviders.toArray(new ILanguageSettingsProvider[0]));
	
		displaySelectedOptionPage();
	}

	protected ICOptionPage createOptionsPage(ILanguageSettingsProvider provider) {
		return new DummyProviderOptionsPage();
	}

	protected void initializeOptionsPage(ILanguageSettingsProvider provider) {
		ICOptionPage optionsPage = createOptionsPage(provider);
		if (optionsPage==null) {
			optionsPage = new DummyProviderOptionsPage();
		}
		String id = (provider!=null) ? provider.getId() : null;
		optionsPageMap.put(id, optionsPage);
		optionsPage.setContainer(page);
		optionsPage.createControl(compositeOptionsPage);
		optionsPage.setVisible(false);
		compositeOptionsPage.layout(true);
	}

	protected void displaySelectedOptionPage() {
		if (currentOptionsPage != null) {
			currentOptionsPage.setVisible(false);
		}

		ILanguageSettingsProvider provider = getSelectedProvider();
		String id = (provider!=null) ? provider.getId() : null;

		ICOptionPage optionsPage = optionsPageMap.get(id);
		if (optionsPage != null) {
			optionsPage.setVisible(true);
			compositeOptionsPage.layout(true);
		}
		currentOptionsPage = optionsPage;
	}


	protected void saveCheckedProviders(Object selectedElement) {
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
			}
			updateTreeEntries();
		}
	}

	private void disableButtons() {
		buttonSetEnabled(BUTTON_ADD, false);
		buttonSetEnabled(BUTTON_EDIT, false);
		buttonSetEnabled(BUTTON_DELETE, false);
		buttonSetEnabled(BUTTON_MOVE_UP, false);
		buttonSetEnabled(BUTTON_MOVE_DOWN, false);
		buttonSetEnabled(BUTTON_CONFIGURE, false);
	}

	/**
	 * Updates state for all buttons. Called when table selection changes.
	 */
	@Override
	protected void updateButtons() {
		ILanguageSettingsProvider provider = getSelectedProvider();
		ICLanguageSettingEntry entry = getSelectedEntry();

		boolean isEntrySelected = entry!=null;
		boolean isProviderSelected = !isEntrySelected && (provider!=null);

		boolean canConfigure = page.isForProject(); // the button is only enabled in project properties
		boolean canMoveUp = false;
		boolean canMoveDown = false;
		
		if (isConfigureMode && canConfigure) {
			int pos = tableProviders.getSelectionIndex();
			int count = tableProviders.getItemCount();
			int last = count - 1;
			boolean isRangeOk = pos >= 0 && pos <= last;
			canMoveUp = isProviderSelected && isRangeOk && pos!=0;
			canMoveDown = isProviderSelected && isRangeOk && pos!=last;
		}
		
		buttonSetText(BUTTON_ADD, isConfigureMode ? "Run": ADD_STR);
		buttonSetText(BUTTON_DELETE, isProviderSelected || isConfigureMode ? "Clear" : DEL_STR);
		buttonSetText(BUTTON_CONFIGURE, isConfigureMode ? "Show Entries": "Configure");

		buttonSetEnabled(BUTTON_ADD, false);
		buttonSetEnabled(BUTTON_EDIT, false);
		buttonSetEnabled(BUTTON_DELETE, false);
		buttonSetEnabled(BUTTON_CONFIGURE, canConfigure);
		buttonSetEnabled(BUTTON_MOVE_UP, canMoveUp);
		buttonSetEnabled(BUTTON_MOVE_DOWN, canMoveDown);
	}

	/**
	 * Displays warning message - if any - for selected language settings entry.
	 */
	private void updateStatusLine() {
		IStatus status=null;
		if (enableProvidersCheckBox.getSelection()==true) {
			status = LanguageSettingsImages.getStatus(getSelectedEntry());
		}
		fStatusLine.setErrorStatus(status);
	}

	/**
	 * Handle buttons
	 */
	@Override
	public void buttonPressed(int buttonIndex) {
		ILanguageSettingsProvider selectedProvider = getSelectedProvider();
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();

		switch (buttonIndex) {
		case BUTTON_ADD:
			performAdd(selectedProvider);
			break;
		case BUTTON_EDIT:
			performEdit(selectedProvider, selectedEntry);
			break;
		case BUTTON_DELETE:
			performDelete(selectedProvider, selectedEntry);
			break;
		case BUTTON_CONFIGURE:
			performConfigure(selectedProvider);
			break;
		case BUTTON_MOVE_UP:
			performMoveUp(selectedProvider, selectedEntry);
			break;
		case BUTTON_MOVE_DOWN:
			performMoveDown(selectedProvider, selectedEntry);
			break;
		default:
		}
		treeEntries.setFocus();
	}

	protected void performAdd(ILanguageSettingsProvider selectedProvider) {
	}
	
	protected void performEdit(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
	}

	/**
	 * Switch between "Configure" mode and "Show Entries"
	 */
	protected void performConfigure(ILanguageSettingsProvider selectedProvider) {
		setConfigureMode(!isConfigureMode);
		updateButtons();
	}

	protected void performDelete(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
	}

	protected void performMoveUp(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedProvider!=null) {
			moveProvider(true);
		}
	}

	protected void performMoveDown(ILanguageSettingsProvider selectedProvider, ICLanguageSettingEntry selectedEntry) {
		if (selectedProvider!=null) {
			moveProvider(false);
		}
	}

	// Move provider up / down
	private void moveProvider(boolean up) {
		int currentPos = tableProviders.getSelectionIndex();
		int last = tableProviders.getItemCount()-1;
		if (currentPos < 0 || (up && currentPos==0) || (!up && currentPos==last))
			return;

		ILanguageSettingsProvider provider = (ILanguageSettingsProvider)tableProvidersViewer.getElementAt(currentPos);
		boolean isChecked = tableProvidersViewer.getChecked(provider);
		tableProvidersViewer.remove(provider);
		int newPos = up ? currentPos-1 : currentPos+1;
		tableProvidersViewer.insert(provider, newPos);
		tableProvidersViewer.setChecked(provider, isChecked);
		tableProviders.setSelection(newPos);

		saveCheckedProviders(null);
		updateButtons();
	}

	/**
	 * Get list of providers to display in the settings entry tree.
	 */
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
						itemsList.add(cfgProvider);
					}
				}
			}
		}
		return itemsList;
	}

	/**
	 * Refreshes the entries tree in "Show Entries" mode.
	 */
	public void updateTreeEntries() {
		List<ILanguageSettingsProvider> tableItems = getProviders(currentLanguageSetting);
		treeEntriesViewer.setInput(tableItems.toArray(new Object[tableItems.size()]));
		updateStatusLine();
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

	private void updateTreeLanguages(ICResourceDescription rcDes) {
		treeLanguages.removeAll();
		TreeItem firstItem = null;
		allLanguages = getLangSettings(rcDes);
		if (allLanguages != null) {
			Arrays.sort(allLanguages, CDTListComparator.getInstance());
			for (ICLanguageSetting langSetting : allLanguages) {
				String langId = langSetting.getLanguageId();
				if (langId==null || langId.length()==0)
					continue;

				LanguageManager langManager = LanguageManager.getInstance();
				ILanguageDescriptor langDes = langManager.getLanguageDescriptor(langId);
				if (langDes == null)
					continue;

				langId = langDes.getName();
				if (langId == null || langId.length()==0)
					continue;

				TreeItem t = new TreeItem(treeLanguages, SWT.NONE);
				t.setText(0, langId);
				t.setData(langSetting);
				if (firstItem == null) {
					firstItem = t;
					currentLanguageSetting = langSetting;
				}
			}

			if (firstItem != null) {
				treeLanguages.setSelection(firstItem);
			}
		}
	}

	/**
	 * Called when configuration changed Refreshes languages list entries tree.
	 */
	@Override
	public void updateData(ICResourceDescription rcDes) {
		if (rcDes == null || !canBeVisible())
			return;

		if (page.isMultiCfg()) {
			setAllVisible(false, null);
			return;
		} else {
			setAllVisible(true, null);
		}

		updateTreeLanguages(rcDes);
		updateTreeEntries();
		updateTableConfigureProviders();
		updateButtons();
	}

	@Override
	protected void performDefaults() {
		if (page.isForPrefs()) {
			if (MessageDialog.openQuestion(usercomp.getShell(),
					"Reset Language Settings Providers",
					"Are you sure you want to reset all customized language settings providers?")) {

				try {
					LanguageSettingsManager_TBD.setUserDefinedProviders(null);
				} catch (CoreException e) {
					CUIPlugin.log("FIXME ErrorParsTab.error.OnRestoring", e);
				}
			}
		} else {
			if (page.isForProject() && enableProvidersCheckBox!=null) {
				ICConfigurationDescription cfgDescription = getConfigurationDescription();
				cfgDescription.setLanguageSettingProviders(new ArrayList<ILanguageSettingsProvider>());
				updateTreeEntries();
				enableProvidersCheckBox.setSelection(false);
				enableControls(enableProvidersCheckBox.getSelection());
			}
		}
		updateData(getResDesc());
	}

	@Override
	protected void performApply(ICResourceDescription srcRcDescription, ICResourceDescription destRcDescription) {
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
		if (page.isForProject() && enableProvidersCheckBox!=null) {
			LanguageSettingsManager.setLanguageSettingsProvidersEnabled(page.getProject(), enableProvidersCheckBox.getSelection());
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
				CUIPlugin.log("ErrorParsTab.error.OnApplyingSettings", e);
			}
		}
		updateData(getResDesc());
		
		trackInitialSettings();
	}

	@Override
	public boolean canBeVisible() {
		if (!CDTPrefUtil.getBool(CDTPrefUtil.KEY_SHOW_PROVIDERS))
			return false;
		return super.canBeVisible();
	}

	@Override
	protected boolean isIndexerAffected() {
		List<ILanguageSettingsProvider> newProvidersList = null;
		ICConfigurationDescription cfgDescription = getConfigurationDescription();
		if (cfgDescription!=null) {
			newProvidersList = cfgDescription.getLanguageSettingProviders();
		}
		boolean newEnablement = LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject());
		
		boolean isEqualList = (newProvidersList==initialProvidersList) || (newProvidersList!=null && newProvidersList.equals(initialProvidersList));
		return newEnablement!=initialEnablement || (newEnablement==true && !isEqualList);
	}

}

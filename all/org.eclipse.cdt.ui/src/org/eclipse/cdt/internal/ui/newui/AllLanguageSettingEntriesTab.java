/*******************************************************************************
 * Copyright (c) 2010 Andrew Gvozdev and others.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.model.ILanguageDescriptor;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.model.util.CDTListComparator;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICMultiFolderDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.settings.model.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.MultiLanguageSetting;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;

public class AllLanguageSettingEntriesTab extends AbstractCPropertyTab {
	protected Tree treeEntries;
	protected TreeViewer treeEntriesViewer;
	protected Tree treeLanguages;
	protected TreeColumn columnLanguages;
	protected Button showBIButton;
//	protected boolean toAllCfgs = false;
//	protected boolean toAllLang = false;
	protected Label lb1, lb2;
//	protected TreeColumn columnToFit = null;

	protected ICLanguageSetting lang;
	protected LinkedList<ICLanguageSettingEntry> shownEntries;
	/** A set of resolved exported entries */
	protected ArrayList<ICSettingEntry> exported;
	protected SashForm sashForm;
	protected ICLanguageSetting[] ls; // all languages known
	private boolean fHadSomeModification;

	private static final int BUTTON_ADD = 0;
	private static final int BUTTON_EDIT = 1;
	private static final int BUTTON_DELETE = 2;
	// there is a separator instead of button #3
	private static final int BUTTON_MOVE_UP = 4;
	private static final int BUTTON_MOVE_DOWN = 5;

	protected final static String[] BUTTONS = {
		ADD_STR,
		EDIT_STR,
		DEL_STR,
		null,
		MOVEUP_STR,
		MOVEDOWN_STR
	};

	private static final Comparator<Object> comp = CDTListComparator.getInstance();

	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 10, 30 };

	private class LanguageSettingsContributorsContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);

		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Object[])
				return (Object[]) parentElement;
			if (parentElement instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider lsProvider = (ILanguageSettingsProvider)parentElement;
				List<ICLanguageSettingEntry> seList = getSettingEntriesConsolidated(lsProvider);
				if (seList!=null) {
					return seList.toArray();
				}
			}
			return null;
		}

		public Object getParent(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public boolean hasChildren(Object element) {
			// TODO Auto-generated method stub
			Object[] children = getChildren(element);
			return children!=null && children.length>0;
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

	}

	// Extended label provider
	private class LanguageSettingsContributorsResourceLabelProvider extends LanguageSettingsContributorsLabelProvider {
		@Override
		protected String[] getOverlayKeys(Object element, int columnIndex) {
			if (element instanceof ILanguageSettingsProvider) {
				String[] overlayKeys = new String[5];
				ILanguageSettingsProvider provider = (ILanguageSettingsProvider)element;
				if (lang!=null) {
					List<ICLanguageSettingEntry> entries = getSettingEntries(provider);
					if (entries!=null) {
						if (provider instanceof ILanguageSettingsEditableProvider || provider instanceof LanguageSettingsSerializable) {
							overlayKeys[IDecoration.TOP_RIGHT] = LanguageSettingsImages.IMG_OVR_SETTING;
						}
					} else {
						List<ICLanguageSettingEntry> entriesParent = getSettingEntriesConsolidated(provider);
						if (entriesParent!=null && entriesParent.size()>0) {
							overlayKeys[IDecoration.TOP_RIGHT] = LanguageSettingsImages.IMG_OVR_PARENT;
						}

					}
				}
				return overlayKeys;
			}
			return null;
		}

		@Override
		protected String getImageKey(Object element, int columnIndex) {
			return super.getImageKey(element, columnIndex);
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

	/**
	 * Shortcut for getting setting entries for current context. {@link LanguageSettingsManager}
	 * will be checking parent resources if no settings defined for current resource.
	 *
	 * @return list of setting entries for the current context.
	 */
	private List<ICLanguageSettingEntry> getSettingEntriesConsolidated(ILanguageSettingsProvider provider) {
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
		IResource rc = getResource();
		String languageId = lang.getLanguageId();
		if (languageId==null)
			return null;

		List<ICLanguageSettingEntry> entries = provider.getSettingEntries(cfgDescription, rc, languageId);
		if (entries==null) {
			entries = LanguageSettingsManager.getSettingEntriesConsolidated(cfgDescription, provider.getId(), rc, languageId);
		}
		return entries;
	}

	/**
	 * Shortcut for getting setting entries for current context.
	 *
	 * @return list of setting entries for the current context.
	 */
	private List<ICLanguageSettingEntry> getSettingEntries(ILanguageSettingsProvider provider) {
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
		IResource rc = getResource();
		String languageId = lang.getLanguageId();
		if (languageId==null)
			return null;
		return provider.getSettingEntries(cfgDescription, rc, languageId);
	}

	/**
	 * Shortcut for setting setting entries for current context.
	 *
	 */
	public void setSettingEntries(ILanguageSettingsEditableProvider provider, List<ICLanguageSettingEntry> entries) {
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
		IResource rc = getResource();
		String languageId = lang.getLanguageId();
		if (languageId!=null)
			provider.setSettingEntries(cfgDescription, rc, languageId, entries);
	}

	// providerId -> provider
	private Map<String, EditedProvider> editedProviders = new HashMap<String, EditedProvider>();

	private IResource getResource() {
		return (IResource)page.getElement();
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, true));
		GridData gd = (GridData) usercomp.getLayoutData();
		// Discourage settings entry table from trying to show all its items at once, see bug 264330
		gd.heightHint = 1;

		// Create the sash form
		sashForm = new SashForm(usercomp, SWT.NONE);
		sashForm.setOrientation(SWT.HORIZONTAL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		sashForm.setLayoutData(gd);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 5;
		sashForm.setLayout(layout);

		addTree(sashForm).setLayoutData(new GridData(GridData.FILL_VERTICAL));
		treeEntries = addTree2(sashForm);
//		table.setLayoutData(new GridData(GridData.FILL_VERTICAL));
//		table = new Tree(sashForm, SWT.BORDER | SWT.MULTI c | SWT.FULL_SELECTION);
//		gd = new GridData(GridData.FILL_BOTH);
//		gd.widthHint = 150;
//		table.setLayoutData(gd);
//		table.setHeaderVisible(isHeaderVisible());
		treeEntries.setLinesVisible(true);

		sashForm.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.DRAG)
					return;
				int shift = event.x - sashForm.getBounds().x;
				GridData data = (GridData) treeLanguages.getLayoutData();
				if ((data.widthHint + shift) < 20)
					return;
				Point computedSize = usercomp.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point currentSize = usercomp.getShell().getSize();
				boolean customSize = !computedSize.equals(currentSize);
				data.widthHint = data.widthHint;
				sashForm.layout(true);
				computedSize = usercomp.getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				if (customSize)
					computedSize.x = Math.max(computedSize.x, currentSize.x);
				computedSize.y = Math.max(computedSize.y, currentSize.y);
				if (computedSize.equals(currentSize)) {
					return;
				}
			}
		});

		treeEntriesViewer = new TreeViewer(treeEntries);
		treeEntriesViewer.setContentProvider(new LanguageSettingsContributorsContentProvider());
		treeEntriesViewer.setLabelProvider(new LanguageSettingsContributorsResourceLabelProvider());
		treeEntriesViewer.setUseHashlookup(true);

		sashForm.setWeights(DEFAULT_SASH_WEIGHTS);

		treeEntries.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (buttonIsEnabled(1) && treeEntries.getSelection().length>0)
					buttonPressed(1);
			}
		});

//		table.addControlListener(new ControlListener() {
//			public void controlMoved(ControlEvent e) {
//				setColumnToFit();
//			}
//
//			public void controlResized(ControlEvent e) {
//				setColumnToFit();
//			}
//		});

		setupLabel(usercomp, EMPTY_STR, 1, 0);

		lb1 = new Label(usercomp, SWT.BORDER | SWT.CENTER);
		lb1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lb1.setToolTipText(Messages.EnvironmentTab_15);
		lb1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CDTPrefUtil.spinDMode();
				update();
			}
		});

		showBIButton = setupCheck(usercomp,
				Messages.AbstractLangsListTab_0, 1, GridData.FILL_HORIZONTAL);
		showBIButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		showBIButton.setSelection(true);
		showBIButton.setEnabled(false);

		lb2 = new Label(usercomp, SWT.BORDER | SWT.CENTER);
		lb2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lb2.setToolTipText(Messages.EnvironmentTab_23);
		lb2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				CDTPrefUtil.spinWMode();
				updateLbs(null, lb2);
			}
		});

//		additionalTableSet();
		initButtons(BUTTONS);
		updateData(getResDesc());

		treeEntries.setFocus();
		// TODO
		// ImportExportWizardButtons.addWizardLaunchButtons(usercomp, page.getElement());

	}

	/**
	 * That method returns exact position of an element in the list.
	 * Note that {@link List#indexOf(Object)} returns position of the first element
	 * equals to the given one, not exact element.
	 *
	 * @param entries
	 * @param entry
	 * @return
	 */
	int getExactIndex(List<ICLanguageSettingEntry> entries, ICLanguageSettingEntry entry) {
		if (entries!=null) {
			for (int i=0;i<entries.size();i++) {
				if (entries.get(i)==entry)
					return i;
			}
		}
		return -1;
	}
	/**
	 * Updates state for all buttons Called when table selection changes.
	 */
	@Override
	protected void updateButtons() {
		ILanguageSettingsProvider provider = getSelectedProvider();
		ICLanguageSettingEntry entry = getSelectedEntry();
		boolean isEntrySelected = entry!=null;
		boolean isProviderSelected = !isEntrySelected && (provider!=null);
		boolean isProviderEditable = provider instanceof ILanguageSettingsEditableProvider;

		boolean canAdd = isProviderEditable;
		boolean canEdit = isProviderEditable && isEntrySelected;
		boolean canDelete = isProviderEditable && isEntrySelected;
		boolean canClear = isProviderEditable && isProviderSelected
			&& !LanguageSettingsManager.isWorkspaceProvider(provider)
			&& getSettingEntries(provider)!=null;

		if (isProviderSelected) {
			buttonSetText(BUTTON_DELETE, "Reset");
		} else {
			buttonSetText(BUTTON_DELETE, DEL_STR);
		}

		boolean canMoveUp = false;
		boolean canMoveDown = false;
		if (isProviderEditable && isEntrySelected) {
			List<ICLanguageSettingEntry> entries = getSettingEntriesConsolidated(provider);
			int index = getExactIndex(entries, entry);
			int itemCount = entries.size();
			int last=itemCount-1;

			canMoveUp = index>0 && index<=last;
			canMoveDown = index>=0 && index<last;
		}

		buttonSetEnabled(BUTTON_ADD, canAdd);
		buttonSetEnabled(BUTTON_EDIT, canEdit);
		buttonSetEnabled(BUTTON_DELETE, canDelete || canClear);
		buttonSetEnabled(BUTTON_MOVE_UP, canMoveUp);
		buttonSetEnabled(BUTTON_MOVE_DOWN, canMoveDown);

	}

	private ILanguageSettingsProvider getSelectedProvider() {
		TreeItem[] items = treeEntries.getSelection();
		ILanguageSettingsProvider provider = null;
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
		return provider;
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

	private Tree addTree(Composite comp) {
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
						lang = langSetting;
						update();
					}
				}
			}
		});
		treeLanguages.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int x = treeLanguages.getBounds().width - 5;
				if (columnLanguages.getWidth() != x)
					columnLanguages.setWidth(x);
			}
		});

		columnLanguages = new TreeColumn(treeLanguages, SWT.NONE);
		columnLanguages.setText(Messages.AbstractLangsListTab_1);
		columnLanguages.setWidth(200);
		columnLanguages.setResizable(false);
		columnLanguages.setToolTipText(Messages.AbstractLangsListTab_1);
		return treeLanguages;
	}

	private Tree addTree2(Composite comp) {
		final Tree tree2 = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL /*| SWT.FULL_SELECTION*/);
		tree2.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		tree2.setHeaderVisible(true);

		tree2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = tree2.getSelection();
				if (items.length > 0) {
					Object item = items[0].getData();
					if (item instanceof ICLanguageSetting) {
						lang = (ICLanguageSetting) item;
						update();
					}
				}
			}
		});
		final TreeColumn treeCol = new TreeColumn(tree2, SWT.NONE);
		tree2.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int x = treeEntries.getClientArea().width;
				if (treeCol.getWidth() != x)
					treeCol.setWidth(x);
			}
		});

		treeCol.setText("Setting Entries");
		treeCol.setWidth(200);
		treeCol.setResizable(false);
		treeCol.setToolTipText("FIXME Setting Entries");
//		columnToFit = treeCol;
		return tree2;
	}

	/**
	 * Called when language changed or item added/edited/removed. Refreshes whole table contwnts
	 *
	 * Note, this method is rewritten in Symbols tab.
	 */
	public void update() {
		update(0);
	}

	public void update(int shift) {
		if (lang != null) {
//			int x = table.getSelectionIndex();
//			if (x == -1)
//				x = 0;
//			else
//				x += shift; // used only for UP/DOWN

			LinkedList<Object> tableItems = getTableItems();
			treeEntriesViewer.setInput(tableItems.toArray(new Object[tableItems.size()]));
//			if (table.getItemCount() > x)
//				table.setSelection(x);
//			else if (table.getItemCount() > 0)
//				table.setSelection(0);
		}

		updateLbs(lb1, lb2);
		updateButtons();
	}

	protected LinkedList<Object> getTableItems() {
		LinkedList<Object> itemsList = new LinkedList<Object>();

		String langId = lang.getLanguageId();
		if (langId != null) {
			IResource rc = getResource();
			ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
			if (rc != null) {
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
		return itemsList;
	}

	/**
	 * Called when configuration changed Refreshes languages list and calls table refresh.
	 */
	@Override
	public void updateData(ICResourceDescription rcDes) {
		if (rcDes == null || !canBeVisible())
			return;
		updateExport();
		treeLanguages.removeAll();
		TreeItem firstItem = null;
		ls = getLangSetting(rcDes);
		if (ls != null) {
			Arrays.sort(ls, CDTListComparator.getInstance());
			for (ICLanguageSetting langSetting : ls) {
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
					lang = langSetting;
				}
			}

			if (firstItem != null /*&& table != null*/) {
				treeLanguages.setSelection(firstItem);
			}
		}
		update();
	}

	private void updateExport() {
		exported = new ArrayList<ICSettingEntry>();
		ICExternalSetting[] extSettings = getResDesc().getConfiguration().getExternalSettings();
		if (!(extSettings == null || extSettings.length == 0)) {
			for (ICExternalSetting extSetting : extSettings) {
				ICSettingEntry[] entries = extSetting.getEntries();
				if (entries == null || entries.length == 0)
					continue;
				for (ICSettingEntry entry : entries)
					exported.add(entry);
			}
		}
	}

	private void performAdd(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
		if (entry != null) {
			ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
			String providerId = provider.getId();
			String languageId = lang.getLanguageId();
			IResource rc = getResource();

			EditedProvider editedProvider = editedProviders.get(providerId);
			if (editedProvider==null) {
				editedProvider = makeEditedProvider(provider, cfgDescription, rc);
				editedProviders.put(providerId, editedProvider);
			}

			List<ICLanguageSettingEntry> entries = getSettingEntries(editedProvider);
			if (entries==null) {
				entries = getSettingEntriesConsolidated(provider);
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

	private EditedProvider makeEditedProvider(ILanguageSettingsProvider provider, ICConfigurationDescription cfgDescription, IResource rc) {
		List<ICLanguageSettingEntry> entries;
		EditedProvider editedProvider;
		editedProvider = new EditedProvider(provider.getId(), provider.getName());
		for (ICLanguageSetting languageSetting : ls) {
			String langId = languageSetting.getLanguageId();
			if (langId!=null) {
				entries = provider.getSettingEntries(cfgDescription, rc, langId);
				editedProvider.setSettingEntries(cfgDescription, rc, langId, entries);
			}
		}
		return editedProvider;
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

	private void performEdit(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
//		if (n == -1)
//			return;
//		ICLanguageSettingEntry old = (ICLanguageSettingEntry) (table.getItem(n).getData());
//		if (old.isReadOnly())
//			return;
//		ICLanguageSettingEntry ent = doEdit(old);
//		toAllLang = false;
//		if (ent != null) {
//			fHadSomeModification = true;
//			if (isWModifyMode() && (lang instanceof MultiLanguageSetting)) {
//				performMulti(ent, old);
//			} else {
//				ICLanguageSettingEntry[] del = null;
//				if (!ent.getName().equals(old.getName()) || ent.getFlags() != old.getFlags()) {
//					del = new ICLanguageSettingEntry[] { old };
//				}
//				changeIt(ent, del);
//			}
//			update();
//		}
	}

	private void performDelete(ILanguageSettingsProvider provider, ICLanguageSettingEntry entry) {
		if (entry != null) {
			List<ICLanguageSettingEntry> entriesOld = getSettingEntriesConsolidated(provider);
			int pos = getExactIndex(entriesOld, entry);

			ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
			String providerId = provider.getId();
			String languageId = lang.getLanguageId();
			IResource rc = getResource();

			List<ICLanguageSettingEntry> entries;
			EditedProvider editedProvider = editedProviders.get(providerId);
			if (editedProvider==null) {
				editedProvider = makeEditedProvider(provider, cfgDescription, rc);
				editedProviders.put(providerId, editedProvider);
			}
			entries = getSettingEntriesConsolidated(editedProvider);
			if (entries!=null) {
				entries.remove(entry);
				editedProvider.setSettingEntries(cfgDescription, rc, languageId, entries);
			}

			update();

			List<ICLanguageSettingEntry> entriesNew = getSettingEntriesConsolidated(provider);
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
		} else if (provider instanceof ILanguageSettingsEditableProvider && !LanguageSettingsManager.isWorkspaceProvider(provider) && getSettingEntriesConsolidated(provider)!=null) {
			String languageId = lang.getLanguageId();
			if (languageId!=null) {
				ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
				IResource rc = getResource();
				ILanguageSettingsEditableProvider epro = (ILanguageSettingsEditableProvider)provider;
				epro.setSettingEntries(cfgDescription, rc, languageId, null);
				update();
			}
		}
	}

	private ICLanguageSettingEntry getSelectedEntry() {
		ICLanguageSettingEntry entry = null;

		TreeItem[] selItems = treeEntries.getSelection();
		if (selItems.length==0) {
			return null;
		}

		// TODO multiple selection
//		for (TreeItem item : selItems) {
//		}


		TreeItem item = selItems[0];
		Object itemData = item.getData();
		if (itemData instanceof ICLanguageSettingEntry) {
			entry = (ICLanguageSettingEntry)itemData;
		}
		return entry;
	}

	/**
	 * Unified buttons handler
	 */
	@Override
	public void buttonPressed(int buttonIndex) {
		ILanguageSettingsProvider selectedProvider = getSelectedProvider();
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();

		ICLanguageSettingEntry old;
		switch (buttonIndex) {
		case BUTTON_ADD:
			if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
				ICLanguageSettingEntry settingEntry = doAdd();
				performAdd(selectedProvider, settingEntry);
			}
			break;
		case BUTTON_EDIT:
			if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
				ICLanguageSettingEntry settingEntry = doEdit(getSelectedEntry());
				if (settingEntry!=null) {
					performDelete(selectedProvider, getSelectedEntry());
					performAdd(selectedProvider, settingEntry);
				}
			}
			break;
		case BUTTON_DELETE:
			if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
				performDelete(selectedProvider, getSelectedEntry());
			}
			break;
		case BUTTON_MOVE_UP:
		case BUTTON_MOVE_DOWN:
			if (selectedProvider instanceof ILanguageSettingsEditableProvider) {
				EditedProvider editedProvider = editedProviders.get(selectedProvider);
				if (editedProvider==null) {
					ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
					IResource rc = getResource();
					editedProvider = makeEditedProvider(selectedProvider, cfgDescription, rc);
					editedProviders.put(editedProvider.getId(), editedProvider);
				}
				List<ICLanguageSettingEntry> entries = getSettingEntriesConsolidated(editedProvider);
				int x = getExactIndex(entries, selectedEntry);
				if (x < 0)
					break;
				if (buttonIndex == BUTTON_MOVE_DOWN)
					x++; // "down" simply means "up underlying item"
				old = entries.get(x);
				ICLanguageSettingEntry old2 = entries.get(x - 1);
				entries.remove(x);
				entries.remove(x - 1);
				entries.add(x - 1, old);
				entries.add(x, old2);

				setSettingEntries(editedProvider, entries);
				update(buttonIndex == BUTTON_MOVE_UP ? -1 : 1);
				selectItem(editedProvider.getId(), selectedEntry);
				updateButtons();
			}
			break;
		default:
			break;
		}
		treeEntries.setFocus();
	}

	@Override
	protected void performOK() {
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
					for (ICLanguageSetting languageSetting : ls) {
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

	@Override
	protected void performApply(ICResourceDescription srcRcDescription, ICResourceDescription destRcDescription) {
		fHadSomeModification = false;
		if (page.isMultiCfg()) {
//			ICLanguageSetting[] sr = ls;
//			if (dst instanceof ICMultiItemsHolder) {
//				for (Object item : ((ICMultiItemsHolder) dst).getItems()) {
//					if (item instanceof ICResourceDescription) {
//						ICLanguageSetting[] ds = getLangSetting((ICResourceDescription) item);
//						if (ds == null || sr.length != ds.length)
//							return;
//						for (int i = 0; i < sr.length; i++) {
//							ICLanguageSettingEntry[] ents = null;
//							ents = sr[i].getSettingEntries(getKind());
//							ds[i].setSettingEntries(getKind(), ents);
//						}
//					}
//				}
//			}
		} else {
//			ICLanguageSetting[] sr = getLangSetting(src);
//			ICLanguageSetting[] ds = getLangSetting(dst);
//			if (sr == null || ds == null || sr.length != ds.length)
//				return;
//			for (int i = 0; i < sr.length; i++) {
//				ICLanguageSettingEntry[] ents = null;
//				ents = sr[i].getSettingEntries(getKind());
//				ds[i].setSettingEntries(getKind(), ents);
//			}

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
						for (ICLanguageSetting languageSetting : ls) {
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
	}

	@Override
	protected void performDefaults() {
		fHadSomeModification = true;
		editedProviders = new HashMap<String, EditedProvider>();
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
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

	public ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
		switch (rcDes.getType()) {
		case ICSettingBase.SETTING_PROJECT:
		case ICSettingBase.SETTING_CONFIGURATION:
		case ICSettingBase.SETTING_FOLDER:
			ICFolderDescription foDes = (ICFolderDescription) rcDes;
			if (foDes instanceof ICMultiFolderDescription) {
				return getLS((ICMultiFolderDescription) foDes);
			}
			return foDes.getLanguageSettings();
		case ICSettingBase.SETTING_FILE:
			ICFileDescription fiDes = (ICFileDescription) rcDes;
			ICLanguageSetting langSetting = fiDes.getLanguageSetting();
			return (langSetting != null) ? new ICLanguageSetting[] { langSetting } : null;
		}
		return null;
	}

	private ICLanguageSetting[] getLS(ICMultiFolderDescription foDes) {
		ICLanguageSetting[] lsets;

		ICLanguageSetting[][] lsArray2D = foDes.getLanguageSettingsM(comp);
		ICLanguageSetting[] fs = conv2LS(CDTPrefUtil.getListForDisplay(lsArray2D, comp));
		lsets = new ICLanguageSetting[fs.length];
		for (int i = 0; i < fs.length; i++) {
			ArrayList<ICLanguageSetting> list = new ArrayList<ICLanguageSetting>(lsArray2D.length);
			for (ICLanguageSetting[] lsArray : lsArray2D) {
				int x = Arrays.binarySearch(lsArray, fs[i], comp);
				if (x >= 0)
					list.add(lsArray[x]);
			}
			if (list.size() == 1)
				lsets[i] = list.get(0);
			else if (list.size() > 1)
				lsets[i] = new MultiLanguageSetting(list, foDes.getConfiguration());
		}
		return lsets;
	}

	private ICLanguageSetting[] conv2LS(Object[] ob) {
		ICLanguageSetting[] se = new ICLanguageSetting[ob.length];
		System.arraycopy(ob, 0, se, 0, ob.length);
		return se;
	}

	protected boolean isHeaderVisible() {
		return true;
	}

//	protected void setColumnToFit() {
//		if (columnToFit != null && columnToFit.getWidth()!=table.getClientArea().width)
//			columnToFit.setWidth(table.getClientArea().width);
//	}

	/**
	 * Returns whether the tab was modified by the user in any way. The flag is cleared after pressing apply
	 * or ok.
	 */
	protected final boolean hadSomeModification() {
		return fHadSomeModification;
	}

	@Override
	protected final boolean isIndexerAffected() {
		// TODO
		return true;
	}

//	public void additionalTableSet() {
//		columnToFit = new TreeColumn(table, SWT.NONE);
//		columnToFit.setText("Language Setting Entries");
//		columnToFit.setToolTipText("Language Setting Entries");
//		showBIButton.setSelection(true);
//		table.getAccessible().addAccessibleListener(new AccessibleAdapter() {
//			@Override
//			public void getName(AccessibleEvent e) {
//				e.result = "Language Setting Entries";
//			}
//		});
//	}

	public ICLanguageSettingEntry doAdd() {
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
		LanguageSettingEntryDialog dlg = new LanguageSettingEntryDialog(usercomp.getShell(), cfgDescription, selectedEntry, true);
		if (dlg.open()) {
			return dlg.getEntries()[0];
		}
		return null;
	}

	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		ICLanguageSettingEntry selectedEntry = getSelectedEntry();
		ICConfigurationDescription cfgDecsription = getResDesc().getConfiguration();
		LanguageSettingEntryDialog dlg = new LanguageSettingEntryDialog(usercomp.getShell(), cfgDecsription, selectedEntry);
		if (dlg.open()) {
			return dlg.getEntries()[0];
		}
		return null;
	}

}
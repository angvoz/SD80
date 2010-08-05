/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingPathEntry;
import org.eclipse.cdt.core.settings.model.ICMultiFolderDescription;
import org.eclipse.cdt.core.settings.model.ICMultiResourceDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsEditableProvider;
import org.eclipse.cdt.core.settings.model.ILanguageSettingsProvider;
import org.eclipse.cdt.core.settings.model.LanguageSettingsSerializable;
import org.eclipse.cdt.core.settings.model.MultiLanguageSetting;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.settings.model.util.LanguageSettingsManager;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.language.UserLanguageSettingsProvider;
import org.eclipse.cdt.internal.ui.newui.Messages;

public class AllLanguageSettingEntriesTab extends AbstractCPropertyTab {
	protected Tree table;
	protected TreeViewer tv;
	protected Tree langTree;
	protected TreeColumn langCol;
	protected Button showBIButton;
//	protected boolean toAllCfgs = false;
//	protected boolean toAllLang = false;
	protected Label lb1, lb2;
	protected TreeColumn columnToFit = null;

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
	private static final int BUTTON_EXPORT_UNEXPORT = 3;
	// there is a separator instead of button #4
	private static final int BUTTON_MOVE_UP = 5;
	private static final int BUTTON_MOVE_DOWN = 6;

	protected final static String[] BUTTONS = { ADD_STR, EDIT_STR, DEL_STR,
			Messages.AbstractLangsListTab_2, 
			null, MOVEUP_STR, MOVEDOWN_STR };

	private static final Comparator<Object> comp = CDTListComparator.getInstance();

	private static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 10, 30 };
	
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
		table = addTree2(sashForm);
//		table.setLayoutData(new GridData(GridData.FILL_VERTICAL));
//		table = new Tree(sashForm, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
//		gd = new GridData(GridData.FILL_BOTH);
//		gd.widthHint = 150;
//		table.setLayoutData(gd);
//		table.setHeaderVisible(isHeaderVisible());
		table.setLinesVisible(true);

		sashForm.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.DRAG)
					return;
				int shift = event.x - sashForm.getBounds().x;
				GridData data = (GridData) langTree.getLayoutData();
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

//		tv = new TreeViewer(sashForm, SWT.BORDER | SWT.MULTI
//				| SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		tv = new TreeViewer(table);
		tv.setContentProvider(new LanguageSettingsContributorsContentProvider());
		tv.setLabelProvider(new LanguageSettingsContributorsLabelProvider());
		tv.setUseHashlookup(true);
		
		sashForm.setWeights(DEFAULT_SASH_WEIGHTS);

		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (buttonIsEnabled(1) && table.getSelection().length>0)
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

		additionalTableSet();
		initButtons(BUTTONS);
		updateData(getResDesc());

		// TODO
		// ImportExportWizardButtons.addWizardLaunchButtons(usercomp, page.getElement());

	}

	/**
	 * Updates state for all buttons Called when table selection changes.
	 */
	@Override
	protected void updateButtons() {
		ILanguageSettingsProvider provider = getSelectedProvider();
		boolean canAdd = provider instanceof ILanguageSettingsEditableProvider;

		buttonSetEnabled(BUTTON_ADD, canAdd);
		buttonSetEnabled(BUTTON_EDIT, false);
		buttonSetEnabled(BUTTON_DELETE, false);
		buttonSetEnabled(BUTTON_EXPORT_UNEXPORT, false);
		buttonSetEnabled(BUTTON_MOVE_UP, false);
		buttonSetEnabled(BUTTON_MOVE_DOWN, false);
		
//		int index = table.getSelectionIndex();
//		int[] ids = table.getSelectionIndices();
//		boolean canAdd = langTree.getItemCount() > 0;
//		boolean canExport = index != -1 && (table.getItem(index) instanceof ICLanguageSettingEntry);
//		boolean canEdit = canExport && ids.length == 1;
//		boolean canDelete = canExport;
//		ICLanguageSettingEntry ent = null;
//		if (canExport) {
//			ent = (ICLanguageSettingEntry) (table.getItem(index).getData());
//			if (ent.isReadOnly())
//				canEdit = false;
//			if (ent.isReadOnly())
//				canDelete = false;
//			if (exported.contains(resolve(ent)))
//				buttonSetText(BUTTON_EXPORT_UNEXPORT, Messages.AbstractLangsListTab_4); 
//			else
//				buttonSetText(BUTTON_EXPORT_UNEXPORT, Messages.AbstractLangsListTab_2); 
//		} else {
//			buttonSetText(BUTTON_EXPORT_UNEXPORT, Messages.AbstractLangsListTab_2); 
//		}
//		boolean canMoveUp = false;
//		boolean canMoveDown = false;
//		if (ent != null) {
//			canMoveUp = canEdit && index > 0 && !ent.isBuiltIn();
//			canMoveDown = canEdit && (index < table.getItemCount() - 1) && !ent.isBuiltIn();
//		}
//		if (canMoveDown && showBIButton.getSelection()) {
//			ent = (ICLanguageSettingEntry) (table.getItem(index + 1).getData());
//			if (ent.isBuiltIn())
//				canMoveDown = false; // cannot exchange with built in
//		}
//		buttonSetEnabled(BUTTON_ADD, canAdd);
//		buttonSetEnabled(BUTTON_EDIT, canEdit);
//		buttonSetEnabled(BUTTON_DELETE, canDelete);
//		buttonSetEnabled(BUTTON_EXPORT_UNEXPORT, canExport && !page.isMultiCfg());
//		buttonSetEnabled(BUTTON_MOVE_UP, canMoveUp && !page.isMultiCfg());
//		buttonSetEnabled(BUTTON_MOVE_DOWN, canMoveDown && !page.isMultiCfg());
	}

	private ILanguageSettingsProvider getSelectedProvider() {
		TreeItem[] items = table.getSelection();
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
		TreeItem[] providerItems = table.getItems();
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
		TreeItem[] providerItems = table.getItems();
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
		langTree = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
		langTree.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		langTree.setHeaderVisible(true);

		langTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] items = langTree.getSelection();
				if (items.length > 0) {
					ICLanguageSetting langSetting = (ICLanguageSetting) items[0].getData();
					if (langSetting != null) {
						lang = langSetting;
						update();
					}
				}
			}
		});
		langTree.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				int x = langTree.getBounds().width - 5;
				if (langCol.getWidth() != x)
					langCol.setWidth(x);
			}
		});

		langCol = new TreeColumn(langTree, SWT.NONE);
		langCol.setText(Messages.AbstractLangsListTab_1); 
		langCol.setWidth(200);
		langCol.setResizable(false);
		langCol.setToolTipText(Messages.AbstractLangsListTab_1); 
		return langTree;
	}

	private Tree addTree2(Composite comp) {
		final Tree tree2 = new Tree(comp, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL);
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
				int x = tree2.getBounds().width - 5;
				if (treeCol.getWidth() != x)
					treeCol.setWidth(x);
			}
		});
		
		treeCol.setText("Setting Entries"); 
		treeCol.setWidth(200);
		treeCol.setResizable(false);
		treeCol.setToolTipText("FIXME Setting Entries"); 
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
			tv.setInput(tableItems.toArray(new Object[tableItems.size()]));
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
		langTree.removeAll();
		TreeItem firstItem = null;
		ls = getLangSetting(rcDes);
		if (ls != null) {
			Arrays.sort(ls, CDTListComparator.getInstance());
			for (ICLanguageSetting langSetting : ls) {
				TreeItem t = new TreeItem(langTree, SWT.NONE);
				String langId = langSetting.getLanguageId();
				if (langId != null && !langId.equals(EMPTY_STR)) {
					// Bug #178033: get language name via LangManager.
					ILanguageDescriptor langDes = LanguageManager.getInstance().getLanguageDescriptor(
							langId);
					if (langDes == null)
						langId = null;
					else
						langId = langDes.getName();
				}
				if (langId == null || langId.equals(EMPTY_STR))
					langId = langSetting.getName();
				t.setText(0, langId);
				t.setData(langSetting);
				if (firstItem == null) {
					firstItem = t;
					lang = langSetting;
				}
				// FIXME temporary stub to make testing easier
				if (langId != null && langId.startsWith("GNU C++")) {
					firstItem = t;
					lang = langSetting;
				}

			}

			if (firstItem != null /*&& table != null*/) {
				langTree.setSelection(firstItem);
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
			
			List<ICLanguageSettingEntry> entries;
			EditedProvider editedProvider = editedProviders.get(providerId);
			if (editedProvider==null) {
				editedProvider = new EditedProvider(providerId, provider.getName());
				editedProviders.put(providerId, editedProvider);
				
				
				for (ICLanguageSetting languageSetting : ls) {
					String langId = languageSetting.getLanguageId();
					if (langId!=null) {
						entries = provider.getSettingEntries(cfgDescription, rc, langId);
						if (langId.equals(languageId)) {
							if (entries==null) {
								entries = new ArrayList<ICLanguageSettingEntry>();
							}
							entries.add(entry);
						}
						editedProvider.setSettingEntries(cfgDescription, rc, langId, entries);
					}
				}

			} else {
				entries = provider.getSettingEntries(cfgDescription, rc, languageId);
				if (entries==null) {
					entries = new ArrayList<ICLanguageSettingEntry>();
				}
					entries.add(entry);
				editedProvider.setSettingEntries(cfgDescription, rc, languageId, entries);
			}
			
			update();

			TreeItem providerItem = findProviderItem(providerId);
			if (providerItem!=null) {
				table.select(providerItem);
				if (providerItem.getItems().length>0) {
					table.showItem(providerItem.getItems()[0]);
				}
			}
			TreeItem entryItem = findEntryItem(providerId, entry);
			if (entryItem!=null) {
				table.select(entryItem);
			}
			updateButtons();
		}
	}

	private void changeIt(ICLanguageSettingEntry add, ICLanguageSettingEntry[] del) {
//		List<ICLanguageSettingEntry> lsEntries = getSettingEntriesList(getKind());
//		if (del != null) {
//			for (ICLanguageSettingEntry d : del) {
//				for (ICLanguageSettingEntry entry : lsEntries) {
//					if (d.getName().equals(entry.getName())) {
//						lsEntries.remove(entry);
//						break;
//					}
//				}
//			}
//		}
//		if (add != null)
//			lsEntries.add(add);
//		setSettingEntries(getKind(), lsEntries, toAllLang);
	}

	/**
	 * Add and/or delete entries in the case of multi-configuration selection in the drop-down box.<br/>
	 * Hint: {@code lang} keeps the selected language for each one of the selected configurations.
	 * 
	 * @param ent
	 *            - entry to add
	 * @param del
	 *            - entry to delete
	 */
	private void performMulti(ICLanguageSettingEntry ent, ICLanguageSettingEntry del) {
		MultiLanguageSetting ms = (MultiLanguageSetting) lang;
		ICLanguageSetting[] langSettings = (ICLanguageSetting[]) ms.getItems();
		ICLanguageSettingEntry[][] es = ms.getSettingEntriesM(getKind());
		for (int i = 0; i < langSettings.length; i++) {
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>(Arrays.asList(es[i]));
			if (del != null) {
				for (ICLanguageSettingEntry entry : entries) {
					if (entry.getName().equals(del.getName())) {
						entries.remove(entry);
						break;
					}
				}
			}
			if (ent != null)
				entries.add(ent);
			langSettings[i].setSettingEntries(getKind(), entries);
		}
	}

	private void performEdit(int n) {
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

	private void performDelete(int n) {
//		if (n == -1)
//			return;
//		fHadSomeModification = true;
//		int[] ids = table.getSelectionIndices();
//		if (isWModifyMode() && (lang instanceof MultiLanguageSetting)) {
//			for (int x = ids.length - 1; x >= 0; x--) {
//				ICLanguageSettingEntry old = (ICLanguageSettingEntry) (table.getItem(ids[x]).getData());
//				performMulti(null, old);
//			}
//		} else {
//			ICLanguageSettingEntry[] del = new ICLanguageSettingEntry[ids.length];
//			for (int x = ids.length - 1; x >= 0; x--) {
//				ICLanguageSettingEntry old = (ICLanguageSettingEntry) (table.getItem(ids[x]).getData());
//				// if (old.isReadOnly()) continue;
//				del[x] = old;
//			}
//			changeIt(null, del);
//		}
//		update();

	}

	/**
	 * Unified buttons handler
	 */
	@Override
	public void buttonPressed(int buttonIndex) {
//		ICLanguageSettingEntry old;
//		int n = table.getSelectionIndex();
//		int ids[] = table.getSelectionIndices();
//
		switch (buttonIndex) {
		case BUTTON_ADD:
			ILanguageSettingsProvider provider = getSelectedProvider();
			if (provider instanceof ILanguageSettingsEditableProvider) {
				ICLanguageSettingEntry settingEntry = doAdd();
				performAdd(provider, settingEntry);
			}
			break;
//		case BUTTON_EDIT:
//			performEdit(n);
//			break;
//		case BUTTON_DELETE:
//			performDelete(n);
//			break;
//		case BUTTON_EXPORT_UNEXPORT:
//			if (n == -1)
//				return;
//			for (int x = ids.length - 1; x >= 0; x--) {
//				old = resolve((ICLanguageSettingEntry) (table.getItem(ids[x]).getData()));
//				if (exported.contains(old)) {
//					deleteExportSetting(old);
//				} else {
//					page.getResDesc().getConfiguration()
//							.createExternalSetting(null, null, null, new ICLanguageSettingEntry[] { old });
//				}
//			}
//			updateExport();
//			update();
//			break;
//		case BUTTON_MOVE_UP:
//		case BUTTON_MOVE_DOWN:
//			old = (ICLanguageSettingEntry) (table.getItem(n).getData());
//			int x = shownEntries.indexOf(old);
//			if (x < 0)
//				break;
//			if (buttonIndex == BUTTON_MOVE_DOWN)
//				x++; // "down" simply means "up underlying item"
//			old = shownEntries.get(x);
//			ICLanguageSettingEntry old2 = shownEntries.get(x - 1);
//			shownEntries.remove(x);
//			shownEntries.remove(x - 1);
//			shownEntries.add(x - 1, old);
//			shownEntries.add(x, old2);
//
//			setSettingEntries(getKind(), shownEntries, false);
//			update(buttonIndex == BUTTON_MOVE_UP ? -1 : 1);
//			break;
		default:
			break;
		}
		table.setFocus();
	}

	/**
	 * @return resolved ICLanguageSettingEntry
	 */
	private ICLanguageSettingEntry resolve(ICLanguageSettingEntry entry) {
		ICLanguageSettingEntry[] entries = CDataUtil.resolveEntries(new ICLanguageSettingEntry[] { entry },
				getResDesc().getConfiguration());
		if (entries.length > 0)
			return entries[0];
		return entry;
	}

	private void deleteExportSetting(ICSettingEntry ent) {
		// if (ent.isReadOnly() || ent.isBuiltIn()) continue;
		ICConfigurationDescription cfg = getResDesc().getConfiguration();
		ICExternalSetting[] extSettings = cfg.getExternalSettings();
		if (!(extSettings == null || extSettings.length == 0)) {
			for (ICExternalSetting extSetting : extSettings) {
				ICSettingEntry[] entries = extSetting.getEntries(getKind());
				if (entries == null || entries.length == 0)
					continue;
				for (int j = 0; j < entries.length; j++) {
					if (entries[j].equalsByName(ent)) {
						ICSettingEntry[] arr = new ICSettingEntry[entries.length - 1];
						int index = 0;
						for (int k = 0; k < entries.length; k++)
							if (k != j)
								arr[index++] = entries[k];
						cfg.removeExternalSetting(extSetting);
						cfg.createExternalSetting(extSetting.getCompatibleLanguageIds(),
								extSetting.getCompatibleContentTypeIds(),
								extSetting.getCompatibleExtensions(), arr);
						return;
					}
				}
			}
		}
	}

//	/**
//	 * Adds entry to all configurations
//	 * 
//	 * @param ent
//	 *            - entry to add
//	 */
//	private void addToAll(ICLanguageSettingEntry ent) {
//		ICResourceDescription curRcDes = page.getResDesc();
//		String id = lang.getName(); // getLanguageId() sometimes returns null.
//		for (ICConfigurationDescription cfgDes : page.getCfgsEditable()) {
//			ICResourceDescription rcDes = page.getResDesc(cfgDes);
//			if (rcDes == null)
//				continue;
//			if (!toAllCfgs && !(curRcDes.equals(rcDes)))
//				continue;
//			for (ICLanguageSetting l : getLangSetting(rcDes)) {
//				if (toAllLang || id == l.getName() || (id != null && id.equals(l.getName()))) {
//					List<ICLanguageSettingEntry> lst = l.getSettingEntriesList(getKind());
//					lst.add(ent);
//					l.setSettingEntries(getKind(), lst);
//				}
//			}
//		}
//	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
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

			ICConfigurationDescription sd = src.getConfiguration();
			ICConfigurationDescription dd = dst.getConfiguration();
			
//			ICProjectDescription prjDesc = getResDesc().getConfiguration().getProjectDescription();
//			ICConfigurationDescription[] cfgDescs = prjDesc.getConfigurations();
//			
//			for (ICConfigurationDescription cfgDescription : cfgDescs) {
//			}
			
			List<ILanguageSettingsProvider> lsProviders = sd.getLanguageSettingProviders();
			List<ILanguageSettingsProvider> ddProviders = new ArrayList<ILanguageSettingsProvider>();
			for (ILanguageSettingsProvider pro : lsProviders) {
				EditedProvider editedProvider = editedProviders.get(pro.getId());

				if (editedProvider!=null) {
					if (pro instanceof ILanguageSettingsEditableProvider) {
						if (pro instanceof LanguageSettingsSerializable) {
							LanguageSettingsSerializable spro = (LanguageSettingsSerializable)pro;
							if (spro==LanguageSettingsManager.getWorkspaceProvider(spro.getId())) {
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
								List<ICLanguageSettingEntry> entries = editedProvider.getSettingEntries(sd, rc, languageId);
								epro.setSettingEntries(sd, rc, languageId, entries);
							}
						}
					}
				}
				ddProviders.add(pro);
			}
			
			dd.setLanguageSettingProviders(ddProviders);
				

		}
	}

	@Override
	protected void performDefaults() {
		fHadSomeModification = true;
		TreeItem[] tis = langTree.getItems();
		for (TreeItem ti : tis) {
			Object ob = ti.getData();
			if (ob != null && ob instanceof ICLanguageSetting) {
				((ICLanguageSetting) ob).setSettingEntries(getKind(), (List<ICLanguageSettingEntry>) null);
			}
		}
		editedProviders = new HashMap<String, EditedProvider>();
		updateData(this.getResDesc());
	}

	private class LanguageSettingsContributorsContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
			
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Object[])
				return (Object[]) parentElement;
			if (parentElement instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider lsProvider = (ILanguageSettingsProvider)parentElement;
				String languageId = lang.getLanguageId();
				if (languageId != null) {
					ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
					IResource rc = getResource();
					List<ICLanguageSettingEntry> seList = lsProvider.getSettingEntries(cfgDescription, rc, languageId);
					if (seList==null) {
						return new Object[0];
					}
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
	private class LanguageSettingsContributorsLabelProvider extends LabelProvider implements IFontProvider, ITableLabelProvider /*, IColorProvider */ {
		public LanguageSettingsContributorsLabelProvider() {
		}

		@Override
		public Image getImage(Object element) {
			return getColumnImage(element, 0);
		}

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex > 0)
				return null;
			String imageKey = null;
			String overlayKey = null;

			if (element instanceof ILanguageSettingsProvider) {
				if (((ILanguageSettingsProvider)element).getId().equals("org.eclipse.cdt.ui.user.LanguageSettingsProvider")) {
					imageKey = CPluginImages.IMG_OBJS_USER;
				} else if (((ILanguageSettingsProvider)element).getId().equals("org.eclipse.cdt.managedbuilder.core.LanguageSettingsProvider")) {
					imageKey = CPluginImages.IMG_OBJS_MBS;
				} else {
					imageKey = CPluginImages.IMG_OBJS_LANG_SETTINGS_PROVIDER;
				}
				
				if (element instanceof EditedProvider) {
					overlayKey = CPluginImages.IMG_OVR_EDITED;
				}
			}
			if (element instanceof ICLanguageSettingEntry) {
				ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
				int kind = le.getKind();
				boolean isWorkspacePath = (le.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) != 0;
				boolean isBuiltin = (le.getFlags() & ICSettingEntry.BUILTIN) != 0;

				switch (kind) {
				case ICSettingEntry.INCLUDE_PATH:
					if (isWorkspacePath)
						imageKey = CPluginImages.IMG_OBJS_INCLUDES_FOLDER_WORKSPACE;
					else if (isBuiltin)
						imageKey = CPluginImages.IMG_OBJS_INCLUDES_FOLDER_SYSTEM;
					else
						imageKey = CPluginImages.IMG_OBJS_INCLUDES_FOLDER;
					break;
				case ICSettingEntry.INCLUDE_FILE:
					imageKey = CPluginImages.IMG_OBJS_INCLUDES_CONTAINER;
					break;
				case ICSettingEntry.MACRO:
					imageKey = CPluginImages.IMG_OBJS_MACRO;
					break;
				case ICSettingEntry.MACRO_FILE:
					// TODO
					break;
				case ICSettingEntry.LIBRARY_PATH:
					imageKey = CPluginImages.IMG_OBJS_LIBRARY_FOLDER;
					break;
				case ICSettingEntry.LIBRARY_FILE:
					imageKey = CPluginImages.IMG_OBJS_LIBRARY;
					break;
				}
				if (imageKey==null && le instanceof ICLanguageSettingPathEntry)
					imageKey = CPluginImages.IMG_OBJS_FOLDER;
			}

			if (imageKey!=null) {
				if (overlayKey!=null) {
					return CPluginImages.getOverlaidImage(imageKey, overlayKey, IDecoration.TOP_LEFT);
				}
				return CPluginImages.get(imageKey);
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Object[]) {
				return "OOPS";
			}
			if (element instanceof ILanguageSettingsProvider) {
				return ((ILanguageSettingsProvider)element).getName();
			} else if (element instanceof ICLanguageSettingEntry) {
				ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
				if (columnIndex == 0) {
					String s = le.getName();
					if (le.getKind() == ICSettingEntry.MACRO) {
						s = s+'='+le.getValue();
					}
					if (exported.contains(resolve(le)))
						s = s + Messages.AbstractLangsListTab_3;
					return s;
				}
				if (le.getKind() == ICSettingEntry.MACRO) {
					switch (columnIndex) {
					case 1:
						return le.getValue();
					}
				}
				return EMPTY_STR;
			}
			return (columnIndex == 0) ? element.toString() : EMPTY_STR;
		}

		public Font getFont(Object element) {
			if (!(element instanceof ICLanguageSettingEntry))
				return null;
			ICLanguageSettingEntry le = (ICLanguageSettingEntry) element;
			if (le.isBuiltIn())
				return null; // built in
			if (le.isReadOnly()) // read only
				return JFaceResources.getFontRegistry().getItalic(JFaceResources.DIALOG_FONT);
			// normal
			return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
		}
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

	@Override
	public boolean canBeVisible() {
		if (getResDesc() == null)
			return true;
		ICLanguageSetting[] langSettings = getLangSetting(getResDesc());
		if (langSettings == null)
			return false;
		for (ICLanguageSetting element : langSettings) {
			if ((element.getSupportedEntryKinds() & getKind()) != 0)
				return true;
		}
		return false;
	}

	private void setSettingEntries(int kind, List<ICLanguageSettingEntry> incs, boolean toAll) {
		if (page.isMultiCfg()) {
			((ICMultiResourceDescription) getResDesc()).setSettingEntries(lang, kind, incs, toAll);
		} else
			lang.setSettingEntries(kind, incs);
	}

	private List<ICLanguageSettingEntry> getSettingEntriesList(int kind) {
		if (page.isMultiCfg() && lang instanceof MultiLanguageSetting) {
			ICLanguageSettingEntry[][] lses = ((MultiLanguageSetting) lang).getSettingEntriesM(kind);
			Object[] res = CDTPrefUtil.getListForDisplay(lses, comp);
			ICLanguageSettingEntry[] out = new ICLanguageSettingEntry[res.length];
			System.arraycopy(res, 0, out, 0, res.length);
			return Arrays.asList(out);
		}
		return lang.getSettingEntriesList(kind);
	}

	private ICLanguageSetting[] conv2LS(Object[] ob) {
		ICLanguageSetting[] se = new ICLanguageSetting[ob.length];
		System.arraycopy(ob, 0, se, 0, ob.length);
		return se;
	}

	protected boolean isHeaderVisible() {
		return true;
	}

	protected void setColumnToFit() {
//		if (columnToFit != null)
//			columnToFit.setWidth(table.getClientArea().width);
	}

	/**
	 * Returns whether the tab was modified by the user in any way. The flag is cleared after pressing apply
	 * or ok.
	 */
	protected final boolean hadSomeModification() {
		return fHadSomeModification;
	}

	@Override
	protected final boolean isIndexerAffected() {
		switch (getKind()) {
		case ICSettingEntry.INCLUDE_PATH:
		case ICSettingEntry.MACRO:
		case ICSettingEntry.INCLUDE_FILE:
		case ICSettingEntry.MACRO_FILE:
			if (hadSomeModification()) {
				return true;
			}
			break;
		}
		return false;
	}

	public void additionalTableSet() {
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
	}

	public ICLanguageSettingEntry doAdd() {
		IncludeDialog dlg = new IncludeDialog(usercomp.getShell(), IncludeDialog.NEW_DIR,
				Messages.IncludeTab_1, EMPTY_STR, getResDesc().getConfiguration(), 0);
		if (dlg.open() && dlg.text1.trim().length() > 0) {
			boolean toAllCfgs = dlg.check1;
			boolean toAllLang = dlg.check3;
			int flags = 0;
			if (dlg.check2) { // isWsp
				flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
			}
			return new CIncludePathEntry(dlg.text1, flags);
		}
		return null;
	}

	public ICLanguageSettingEntry doEdit(ICLanguageSettingEntry ent) {
		IncludeDialog dlg = new IncludeDialog(usercomp.getShell(), IncludeDialog.OLD_DIR,
				Messages.IncludeTab_2, ent.getValue(), getResDesc().getConfiguration(),
				(ent.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH));
		if (dlg.open()) {
			int flags = 0;
			if (dlg.check2)
				flags = ICSettingEntry.VALUE_WORKSPACE_PATH;
			return new CIncludePathEntry(dlg.text1, flags);
		}
		return null;
	}

	public int getKind() {
		return ICSettingEntry.ALL;
	}

}

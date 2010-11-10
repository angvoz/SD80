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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsManager;
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
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;

/**
 * This tab presents language settings entries categorized by language
 * settings providers.
 *
 *@noinstantiate This class is not intended to be instantiated by clients.
 *@noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingEntriesProvidersTab extends AbstractCPropertyTab {
	protected SashForm sashForm;
	protected static final int[] DEFAULT_SASH_WEIGHTS = new int[] { 10, 30 };

	protected Tree treeLanguages;
	protected TreeColumn columnLanguages;
	protected Tree treeEntries;
	protected TreeViewer treeEntriesViewer;
	protected Button builtInCheckBox;
	protected Button enableProvidersCheckBox;

	protected ICLanguageSetting currentLanguageSetting;
	protected ICLanguageSetting[] allLanguages; // all languages known

	protected static final int BUTTON_ADD = 0;
	protected static final int BUTTON_EDIT = 1;
	protected static final int BUTTON_DELETE = 2;
	// there is a separator instead of button #3
	protected static final int BUTTON_MOVE_UP = 4;
	protected static final int BUTTON_MOVE_DOWN = 5;

	protected final static String[] BUTTONS = {
		ADD_STR,
		EDIT_STR,
		DEL_STR,
		null,
		MOVEUP_STR,
		MOVEDOWN_STR
	};

	// Content provider for setting entries
	private class LanguageSettingsContributorsContentProvider implements ITreeContentProvider {
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Object[])
				return (Object[]) parentElement;
			if (parentElement instanceof ILanguageSettingsProvider) {
				ILanguageSettingsProvider lsProvider = (ILanguageSettingsProvider)parentElement;
				List<ICLanguageSettingEntry> seList = getSettingEntriesUpResourceTree(lsProvider);
				if (seList!=null) {
					return seList.toArray();
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
	 * Shortcut for getting setting entries for current context. {@link LanguageSettingsManager}
	 * will be checking parent resources if no settings defined for current resource.
	 *
	 * @return list of setting entries for the current context.
	 */
	protected List<ICLanguageSettingEntry> getSettingEntriesUpResourceTree(ILanguageSettingsProvider provider) {
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
		IResource rc = getResource();
		String languageId = currentLanguageSetting.getLanguageId();
		if (languageId==null)
			return null;

		List<ICLanguageSettingEntry> entries = LanguageSettingsManager.getSettingEntriesUpResourceTree(provider, cfgDescription, rc, languageId);
		return entries;
	}

	/**
	 * Shortcut for getting setting entries for current context.
	 *
	 * @return list of setting entries for the current context.
	 */
	protected List<ICLanguageSettingEntry> getSettingEntries(ILanguageSettingsProvider provider) {
		ICConfigurationDescription cfgDescription = getResDesc().getConfiguration();
		IResource rc = getResource();
		String languageId = currentLanguageSetting.getLanguageId();
		if (languageId==null)
			return null;
		return provider.getSettingEntries(cfgDescription, rc, languageId);
	}

	protected IResource getResource() {
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

		builtInCheckBox = setupCheck(usercomp, Messages.AbstractLangsListTab_0, 1, GridData.FILL_HORIZONTAL);
		builtInCheckBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				update();
			}
		});
		builtInCheckBox.setSelection(true);
		builtInCheckBox.setEnabled(false);

		if (page.isForProject()) {
			enableProvidersCheckBox = setupCheck(usercomp, Messages.CDTMainWizardPage_TrySD80, 2, GridData.FILL_HORIZONTAL);
			enableProvidersCheckBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					enableControls(enableProvidersCheckBox.getSelection());
				}
			});
			
			enableProvidersCheckBox.setSelection(LanguageSettingsManager.isLanguageSettingsProvidersEnabled(page.getProject()));
			
		}
		
		//		additionalTableSet();
		initButtons(BUTTONS);
		updateData(getResDesc());

		treeEntries.setFocus();
		// TODO
		// ImportExportWizardButtons.addWizardLaunchButtons(usercomp, page.getElement());

		if (enableProvidersCheckBox!=null)
			enableControls(enableProvidersCheckBox.getSelection());
	}

	/**
	 * Updates state for all buttons Called when table selection changes.
	 */
	@Override
	protected void updateButtons() {
		buttonSetEnabled(BUTTON_ADD, false);
		buttonSetEnabled(BUTTON_EDIT, false);
		buttonSetEnabled(BUTTON_DELETE, false);
		buttonSetEnabled(BUTTON_MOVE_UP, false);
		buttonSetEnabled(BUTTON_MOVE_DOWN, false);
	}

	protected ILanguageSettingsProvider getSelectedProvider() {
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
						currentLanguageSetting = langSetting;
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
						currentLanguageSetting = (ICLanguageSetting) item;
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
	 */
	public void update() {
		if (currentLanguageSetting != null) {
			List<ILanguageSettingsProvider> tableItems = getTableItems();
			treeEntriesViewer.setInput(tableItems.toArray(new Object[tableItems.size()]));
		}

		updateButtons();
	}

	protected List<ILanguageSettingsProvider> getTableItems() {
		List<ILanguageSettingsProvider> itemsList = new LinkedList<ILanguageSettingsProvider>();

		String langId = currentLanguageSetting.getLanguageId();
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
					itemsList.add(cfgProvider);
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
		if (page.isMultiCfg()) {
			setAllVisible(false, null);
			return;
		} else {
			setAllVisible(true, null);
		}

		treeLanguages.removeAll();
		TreeItem firstItem = null;
		allLanguages = getLangSetting(rcDes);
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

			if (firstItem != null /*&& table != null*/) {
				treeLanguages.setSelection(firstItem);
			}
		}
		update();
	}

	protected ICLanguageSettingEntry getSelectedEntry() {
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

	@Override
	protected void performDefaults() {
	}

	@Override
	protected void performApply(ICResourceDescription srcRcDescription, ICResourceDescription destRcDescription) {
	}

	@Override
	protected void performOK() {
	}

	public ICLanguageSetting[] getLangSetting(ICResourceDescription rcDes) {
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

	private void enableControls(boolean enable) {
//		builtInCheckBox.setEnabled(enable);
		sashForm.setEnabled(enable);
		treeLanguages.setEnabled(enable);
		treeEntries.setEnabled(enable);
		buttoncomp.setEnabled(enable);
		
		if (enable) {
			update();
		} else {
			buttonSetEnabled(BUTTON_ADD, enable);
			buttonSetEnabled(BUTTON_EDIT, enable);
			buttonSetEnabled(BUTTON_DELETE, enable);
			buttonSetEnabled(BUTTON_MOVE_UP, enable);
			buttonSetEnabled(BUTTON_MOVE_DOWN, enable);
		}
	}

}

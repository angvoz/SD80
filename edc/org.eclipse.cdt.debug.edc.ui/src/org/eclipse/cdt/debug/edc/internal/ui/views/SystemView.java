/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings("restriction")
public abstract class SystemView extends ViewPart {

	
	public final static String PREF_REFRESH_INTERVAL = "refresh_interval";
	public final static String PREF_SHOULD_AUTO_REFRESH = "should_auto_refresh";
	public final static String PREF_ROOT_VM_PROPERTIES = "root_vm_properties";
	
	protected static final int DEFAULT_REFRESH_INTERVAL = 30000;
	private PresentationContext presentationContext;
	private boolean autoRefresh = true;
	private int refreshInterval = DEFAULT_REFRESH_INTERVAL;
	private IEclipsePreferences prefsNode;

	private class RefreshJob extends Job {

		public RefreshJob() {
			super("Refresh " + getTitle());
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IStatus result = refresh(monitor, true);
			if (shouldAutoRefresh())
				this.schedule(refreshInterval);
			return result;
		}
	};

	class ColumnSelectionAdapter extends SelectionAdapter {

		private int selector;
		private TreeModelViewer viewer;
		private SystemVMContainer systemVMContainer;
		private int currentSortDirection;

		public ColumnSelectionAdapter(SystemVMContainer systemVMContainer, int selector, TreeModelViewer viewer) {
			this.viewer = viewer;
			this.setSelector(selector);
			this.setSystemVMContainer(systemVMContainer);
			this.currentSortDirection = viewer.getTree().getSortDirection();
		}

		public void widgetSelected(SelectionEvent e) {
			viewer.setComparator(new ViewerComparator());
			TreeColumn currentSortColumn = viewer.getTree().getSortColumn();
			TreeColumn newSortColumn = (TreeColumn) e.getSource();
			if (newSortColumn.equals(currentSortColumn))
			{
				if (currentSortDirection == SWT.UP)
					viewer.getTree().setSortDirection(SWT.DOWN);
				else
					if (currentSortDirection == SWT.DOWN)
						viewer.getTree().setSortDirection(SWT.UP);
				currentSortDirection = viewer.getTree().getSortDirection();
			}
			else
			{
				viewer.getTree().setSortColumn((TreeColumn) e.getSource());
			}
			String[] sortProperties = (String[]) systemVMContainer.getProperties().get(SystemVMContainer.PROP_COLUMN_KEYS);
			assert sortProperties != null;
			currentSortDirection = viewer.getTree().getSortDirection();
			systemVMContainer.getProperties().put(SystemVMContainer.PROP_SORT_PROPERTY, sortProperties[getSelector()]);
			systemVMContainer.getProperties().put(SystemVMContainer.PROP_SORT_DIRECTION, currentSortDirection);
			refreshViewModel();
			try {
				saveSettings();
			} catch (Exception e1) {
				EDCDebugUI.logError("", e1);
			}
		}

		public void setSelector(int selector) {
			this.selector = selector;
		}

		public int getSelector() {
			return selector;
		}

		public void setSystemVMContainer(SystemVMContainer systemVMContainer) {
			this.systemVMContainer = systemVMContainer;
		}

		public SystemVMContainer getSystemVMContainer() {
			return systemVMContainer;
		}

	};

	private RefreshJob refreshjob;

	private Text filterText;
	private StringMatcher filter_matcher = new StringMatcher("*", true, false);
	private Action refreshAction;
	private Action refreshSettingsAction;
	private SystemDataModel dataModel;
	private SystemViewModel viewModel;
	private List<CTabItem> tabs = new ArrayList<CTabItem>();
	private List<TreeModelViewer> viewers = new ArrayList<TreeModelViewer>();
	private Action debugAction;
	private TreeModelViewer selectedViewer;

	protected void createRootComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		createFilterTextBox(parent);

		CTabFolder tabFolder = new CTabFolder(parent, SWT.BORDER);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.setSelectionBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
		tabFolder.setLayout(new FillLayout(SWT.HORIZONTAL + SWT.VERTICAL));

		final List<SystemVMContainer> vmContainers = getViewModel().getRootContainers();

		for (SystemVMContainer systemVMContainer : vmContainers) {
			CTabItem tab = new CTabItem(tabFolder, SWT.NONE);
			tab.setText(systemVMContainer.getName());
			tab.setImage(systemVMContainer.getImage());
			Composite composite = new Composite(tabFolder, SWT.NONE);
			composite.setLayout(new FillLayout(SWT.HORIZONTAL + SWT.VERTICAL));
			tab.setControl(composite);
			tabs.add(tab);

			final TreeModelViewer viewer = (new TreeModelViewer(
					(Composite) tab.getControl(), SWT.VIRTUAL | SWT.FULL_SELECTION, getPresentationContext()));
			
			viewer.setInput(systemVMContainer);
			
			viewers.add(viewer);
			tab.setData("VIEWER", viewer);
	

	        TreeColumn[] columns = viewer.getTree().getColumns();
	        if (columns.length > 0)
	        {
				String[] columnKeys = (String[]) systemVMContainer.getProperties().get(SystemVMContainer.PROP_COLUMN_KEYS);
				String sortProperty = (String) systemVMContainer.getProperties().get(SystemVMContainer.PROP_SORT_PROPERTY);
				Integer sortDirection = (Integer) systemVMContainer.getProperties().get(SystemVMContainer.PROP_SORT_DIRECTION);

				for (int i = 0; i < columns.length; i++) {
		            TreeColumn treeColumn = columns[i];
		            ColumnSelectionAdapter columnSelectionAdapter = new ColumnSelectionAdapter(systemVMContainer, i, viewer);
		            treeColumn.addSelectionListener(columnSelectionAdapter);
		            if (columnKeys[i].equals(sortProperty))
		            {
				        viewer.getTree().setSortColumn(treeColumn);
				        viewer.getTree().setSortDirection(sortDirection);
		            }
		        }
	        }

		}

		selectedViewer = viewers.get(0);
		tabFolder.setSelection(tabs.get(0));
		tabFolder.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				selectedViewer = (TreeModelViewer) e.item.getData("VIEWER");
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				selectedViewer = (TreeModelViewer) e.item.getData("VIEWER");
			}
		});

		tabFolder.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				final List<SystemVMContainer> vmContainers = getViewModel().getRootContainers();					
				for (SystemVMContainer systemVMContainer : vmContainers) {
					systemVMContainer.setImage(null); // This is dispose of any image
				}
			}
		});
	}

	public void refreshViewModel() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				getViewModel().buildViewModel();
				refreshViewers();
			}
		});
	}

	@Override
	public void setFocus() {
		if (selectedViewer != null) {
			selectedViewer.getControl().setFocus();
		}
	}

	public List<CTabItem> getTabs() {
		return tabs;
	}

	public List<TreeModelViewer> getViewers() {
		return viewers;
	}

	public void setPresentationContext(PresentationContext presentationContext) {
		this.presentationContext = presentationContext;
	}

	public PresentationContext getPresentationContext() {
		return presentationContext;
	}

	public IStatus refresh(IProgressMonitor monitor, boolean refreshData) {
		if (refreshData)
			try {
				getDataModel().buildDataModel(monitor);
			} catch (Exception e) {
				return new Status(Status.WARNING, EDCDebugUI.PLUGIN_ID, e.getMessage(), e);
			}
		
		refreshViewModel();
		
		return Status.OK_STATUS;
	}

	protected void refreshViewers() {
		for (int i = 0; i < viewers.size(); i++) {
			viewers.get(i).setInput(getViewModel().getRootContainers().get(i));
		}
	}

	public void setRefreshInterval(int refreshInterval) {
		this.refreshInterval = refreshInterval;
		if (shouldAutoRefresh())
			getRefreshJob().schedule(refreshInterval);
	}

	public int getRefreshInterval() {
		return refreshInterval;
	}

	public Job getRefreshJob() {
		if (refreshjob == null) {
			refreshjob = new RefreshJob();
			refreshjob.setSystem(true);
		}
		return refreshjob;
	}

	protected void createFilterTextBox(Composite parent) {	
		filterText = new Text(parent, SWT.BORDER);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterText.setText("type filter text");
		filterText.selectAll();
		filterText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				setFilter(filterText.getText());
				getRefreshJob().schedule();
			}
		});
	}

	public void setFilter(String text) {
		setFilterMatcher(new StringMatcher("*"+text+"*", true, false)); //$NON-NLS-1$
	}

	public void setFilterMatcher(StringMatcher filter_matcher) {
		this.filter_matcher = filter_matcher;
	}

	public StringMatcher getFilterMatcher() {
		return filter_matcher;
	}

	public void createRefreshAction() {
		refreshAction = new Action() {
			public void run() {
				// need to cancel the currently scheduled interval refresh job first
				getRefreshJob().cancel();
				getRefreshJob().schedule();
			}
		};
		refreshAction.setId("SYSTEM_REFRESH");
		refreshAction.setText("Refresh");
		refreshAction.setToolTipText("Refreshes system information");
		refreshAction.setImageDescriptor(AbstractUIPlugin
				.imageDescriptorFromPlugin(EDCDebugUI.PLUGIN_ID,
				"/icons/etool16/refresh.gif")); //$NON-NLS-1$

		createRefreshSettingsAction();
	}

	public void createRefreshSettingsAction() {
		refreshSettingsAction = new Action() {
			public void run() {
				
				SystemRefreshOptionsDialog dialog = new SystemRefreshOptionsDialog(null);
				dialog.setAutoRefresh(shouldAutoRefresh());
				dialog.setRefreshInterval(getRefreshInterval() / 1000);
					
				int result = dialog.open();
				if (result == Dialog.OK) {
					setAutoRefresh(dialog.isAutoRefresh());
					setRefreshInterval(dialog.getRefreshInterval() * 1000);							
					try {
						saveSettings();
					} catch (Exception e) {
						EDCDebugUI.logError("", e);
					}
				}
			}
		};
		refreshSettingsAction.setId("SYSTEM_REFRESH_SETTINGS");
		refreshSettingsAction.setText("Refresh Options...");
		refreshSettingsAction.setToolTipText("Options for refreshing system information");
	}
	
	public void saveSettings() throws BackingStoreException, IOException
	{
		getPrefsNode().putBoolean(PREF_SHOULD_AUTO_REFRESH, shouldAutoRefresh());
		getPrefsNode().putInt(PREF_REFRESH_INTERVAL, getRefreshInterval());

		List<Map<String,Object>> rootContainerProperties = new ArrayList<Map<String,Object>>();
		for (SystemVMContainer systemVMContainer : viewModel.getRootContainers()) {
			Map<String, Object> vmProps = systemVMContainer.getProperties();
			Map<String,Object> props = new HashMap<String, Object>();
			if (vmProps.containsKey(SystemVMContainer.PROP_SORT_PROPERTY))
				props.put(SystemVMContainer.PROP_SORT_PROPERTY, vmProps.get(SystemVMContainer.PROP_SORT_PROPERTY));
			if (vmProps.containsKey(SystemVMContainer.PROP_SORT_DIRECTION))
				props.put(SystemVMContainer.PROP_SORT_DIRECTION, vmProps.get(SystemVMContainer.PROP_SORT_DIRECTION));
			rootContainerProperties.add(props);
		};
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(rootContainerProperties);
		baos.close();
		getPrefsNode().putByteArray(PREF_ROOT_VM_PROPERTIES, baos.toByteArray());
		getPrefsNode().flush();
	}

	@SuppressWarnings("unchecked")
	public void loadSettings()
	{
		setAutoRefresh(getPrefsNode().getBoolean(PREF_SHOULD_AUTO_REFRESH, shouldAutoRefresh()));
		setRefreshInterval(getPrefsNode().getInt(PREF_REFRESH_INTERVAL, getRefreshInterval()));
		byte[] prefData = getPrefsNode().getByteArray(PREF_ROOT_VM_PROPERTIES, null);
		if (prefData != null)
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(prefData);
			ObjectInputStream ois;
			try {
				ois = new ObjectInputStream(bais);
				List<Map<String,Object>> rootContainerProperties = (List<Map<String,Object>>) ois.readObject();
				int vmIndex = 0;
				for (Map<String, Object> map : rootContainerProperties) {
					SystemVMContainer rootVM = viewModel.getRootContainers().get(vmIndex++);
					rootVM.getProperties().putAll(map);
				}
			} catch (Exception e) {
				EDCDebugUI.logError("", e);
			}
		}
	}
	
	public void createDebugAction() {
		debugAction = new Action() {
			public void run() {
				try {
					TreeModelViewer viewer = getSelectedViewer();
					if (viewer != null) {
						SystemVMContainer node = (SystemVMContainer) ((IStructuredSelection) viewer.getSelection())
						.getFirstElement();
						doAttach(node);
					}
				} catch (Exception e) {
					EDCDebugUI.logError("", e);
				}
			}
		};
		debugAction.setText("Debug");
		debugAction.setToolTipText("Debug the selected process");
	}

	protected void doAttach(SystemVMContainer node) throws Exception {
	}

	protected void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				SystemView.this.fillContextMenu(manager);
			}
		});
	
		List<TreeModelViewer> allViewers = this.getViewers();
		for (TreeModelViewer viewer : allViewers) {
			Menu menu = menuMgr.createContextMenu(viewer.getControl());
			viewer.getControl().setMenu(menu);
			getSite().registerContextMenu(menuMgr, viewer);
			viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				
				public void selectionChanged(SelectionChangedEvent event) {
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					SystemVMContainer vmContainer = (SystemVMContainer) selection.getFirstElement();
					if (vmContainer != null)
					{
						Boolean canDebug = (Boolean) vmContainer.getProperties().get(SystemDMContainer.PROP_CAN_DEBUG);
						debugAction.setEnabled(canDebug != null && canDebug.booleanValue());
					}
					else
						debugAction.setEnabled(false);
				}
			});
		}

	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(debugAction);
	}

	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(refreshAction);
		manager.add(refreshSettingsAction);
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshAction);
		manager.add(new Separator());
	}

	public void setDataModel(SystemDataModel dataModel) {
		this.dataModel = dataModel;
	}

	public SystemDataModel getDataModel() {
		return dataModel;
	}

	public void setViewModel(SystemViewModel viewModel) {
		this.viewModel = viewModel;
	}

	public SystemViewModel getViewModel() {
		return viewModel;
	}
	
	public TreeModelViewer getSelectedViewer() {
		return selectedViewer;
	}

	public void setPrefsNode(IEclipsePreferences prefsNode) {
		this.prefsNode = prefsNode;
	}

	public IEclipsePreferences getPrefsNode() {
		return prefsNode;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}

	public boolean shouldAutoRefresh() {
		return autoRefresh;
	}

}

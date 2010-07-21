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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;
import org.eclipse.cdt.internal.ui.util.StringMatcher;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

@SuppressWarnings("restriction")
public abstract class SystemView extends ViewPart {

	protected static final int DEFAULT_REFRESH_INTERVAL = 30000;
	private PresentationContext presentationContext;
	private int refreshInterval = DEFAULT_REFRESH_INTERVAL;

	private Job refreshjob = new Job("Refresh " + this.getTitle()){

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IStatus result = refresh(monitor, true);
			if (getRefreshInterval() > 0)
				this.schedule(refreshInterval);
			return result;
		}};

		private Text filterText;
		private StringMatcher filter_matcher = new StringMatcher("*", true, false);
		private Action refreshAction;
		private Action refreshSettingsAction;
		private SystemDataModel dataModel;
		private SystemViewModel viewModel;
		private List<CTabItem> tabs = new ArrayList<CTabItem>();
		private List<TreeModelViewer> viewers = new ArrayList<TreeModelViewer>();
		private Action attachAction;
		private TreeModelViewer selectedViewer;

		protected void createRootComosite(Composite parent) {
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

				TreeModelViewer viewer = (new TreeModelViewer(
						(Composite) tab.getControl(), SWT.VIRTUAL | SWT.FULL_SELECTION, getPresentationContext()));
				viewer.setInput(systemVMContainer);
				viewers.add(viewer);
				tab.setData("VIEWER", viewer);
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
			
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					getViewModel().buildViewModel();
					refreshViewers();
				}
			});
			
			return Status.OK_STATUS;
		}

		protected void refreshViewers() {
			for (int i = 0; i < viewers.size(); i++) {
				viewers.get(i).setInput(getViewModel().getRootContainers().get(i));
			}
		}

		public void setRefreshInterval(int refreshInterval) {
			this.refreshInterval = refreshInterval;
			if (getRefreshInterval() > 0)
				getRefreshJob().schedule(refreshInterval);
		}

		public int getRefreshInterval() {
			return refreshInterval;
		}

		public void setRefreshJob(Job refreshjob) {
			this.refreshjob = refreshjob;
		}

		public Job getRefreshJob() {
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

		public void createRefreshAction()
		{
			refreshAction = new Action() {
				public void run() {
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

		public void createRefreshSettingsAction()
		{
			refreshSettingsAction = new Action() {
				public void run() {
					
					SystemRefreshOptionsDialog dialog = new SystemRefreshOptionsDialog(null);
					dialog.setAutoRefresh(getRefreshInterval() > 0);
					if (getRefreshInterval() == 0)
						dialog.setRefreshInterval(DEFAULT_REFRESH_INTERVAL / 1000);
					else
						dialog.setRefreshInterval(getRefreshInterval() / 1000);
						
					int result = dialog.open();
					if (result == Dialog.OK)
					{
						if (dialog.isAutoRefresh())
							setRefreshInterval(dialog.getRefreshInterval() * 1000);							
						else
							setRefreshInterval(0);
					}
				}
			};
			refreshSettingsAction.setId("SYSTEM_REFRESH_SETTINGS");
			refreshSettingsAction.setText("Refresh Options...");
			refreshSettingsAction.setToolTipText("Options for refreshing system information");
		}

		public void createAttachAction()
		{
			attachAction = new Action() {
				public void run() {
					try {
						TreeModelViewer viewer = getSelectedViewer();
						if (viewer != null)
						{
							SystemVMContainer node = (SystemVMContainer) ((IStructuredSelection) viewer.getSelection())
							.getFirstElement();
							doAttach(node);
						}
					} catch (Exception e) {
						EDCDebugUI.logError("", e);
					}
				}
			};
			attachAction.setText("Debug");
			attachAction.setToolTipText("Debug the selected process");
		}

		protected void doAttach(SystemVMContainer node) {
		}

		protected void hookContextMenu() {
			MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					SystemView.this.fillContextMenu(manager);
				}
			});
			TreeModelViewer viewer = getSelectedViewer();
			Menu menu = menuMgr.createContextMenu(viewer.getControl());
			viewer.getControl().setMenu(menu);
			getSite().registerContextMenu(menuMgr, viewer);
		}

		private void fillContextMenu(IMenuManager manager) {
			manager.add(attachAction);
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
		
		public TreeModelViewer getSelectedViewer()
		{
			return selectedViewer;
		}

}

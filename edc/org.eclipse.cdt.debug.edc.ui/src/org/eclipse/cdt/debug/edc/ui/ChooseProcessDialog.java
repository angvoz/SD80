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
package org.eclipse.cdt.debug.edc.ui;

import org.eclipse.cdt.debug.edc.launch.ChooseProcessItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SearchPattern;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Dialog to allow user to choose which target process to attach to. Uses
 * ChooseProcessItem objects to hold process information (ID, Name)
 * 
 */
public class ChooseProcessDialog extends TrayDialog {

	/**
	 * Filtering class
	 * 
	 */
	public class ItemFilter extends ViewerFilter {

		private SearchPattern searchPattern;

		public ItemFilter() {
			searchPattern = new SearchPattern();
			searchPattern.setPattern(""); //$NON-NLS-1$
		}

		public boolean match(String str) {
			return searchPattern.matches(str);
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return match(((ChooseProcessItem) element).processName);
		}

		public void setPattern(String pattern) {
			searchPattern.setPattern(pattern);
		}
	}

	/**
	 * Label provider for processes
	 * 
	 */
	private class ProcessesLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof ChooseProcessItem) {
				ChooseProcessItem process = (ChooseProcessItem) element;
				if (columnIndex == PROCESS_NAME_COLUMN)
					return process.processName;
				else if (columnIndex == PROCESS_ID_COLUMN)
					return process.processID;
			}
			return null;
		}

	}

	private static final String AUTOTEST_UID = ".uid"; //$NON-NLS-1$
	private static final int PROCESS_ID_COLUMN = 0;
	private static final int PROCESS_NAME_COLUMN = 1;

	private ChooseProcessItem[] processesOnTarget;
	private ChooseProcessItem selectedProcess;
	private String defaultProcessName;
	private Text filterText;
	private ToolBarManager clearFilterToolBar;
	private ItemFilter nameFilter = new ItemFilter();
	private TableViewer viewer;
	private int lastSortColumn = PROCESS_ID_COLUMN; // default
	private int sortDirection = 1; // default

	public static final ImageDescriptor clearImageDesc = AbstractUIPlugin.imageDescriptorFromPlugin(
			PlatformUI.PLUGIN_ID, "$nl$/icons/full/etool16/clear_co.gif"); //$NON-NLS-1$

	/*
	 * Contstructor
	 */
	public ChooseProcessDialog(ChooseProcessItem[] processesOnTarget, String defaultProcessName, Shell parentShell) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.processesOnTarget = processesOnTarget;
		this.defaultProcessName = defaultProcessName;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Attach to Process");
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
		// LaunchTabHelpIds.ATTACH_CHOOSE_PROCESS);
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);

		// disable the ok button. it will be enabled when they select something
		getButton(IDialogConstants.OK_ID).setEnabled(false);

		return control;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		final Label label = new Label(dialogArea, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		label.setText("Choose a process to attach");

		Composite filterComposite = new Composite(dialogArea, SWT.NONE);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginTop = 5;
		gridLayout.marginHeight = 0;
		gridLayout.numColumns = 2;
		filterComposite.setLayout(gridLayout);

		filterText = new Text(filterComposite, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.CANCEL);
		filterText.setText("type filter text");
		filterText
				.setToolTipText("Filter the visible items by name, using prefix, ? = any character, * = any string, or camel case initials");
		final GridData gd_filterText = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd_filterText.widthHint = 200;
		filterText.setLayoutData(gd_filterText);
		filterText.addFocusListener(new FocusAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt
			 * .events.FocusEvent)
			 */
			@Override
			public void focusGained(FocusEvent e) {
				/*
				 * Running in an asyncExec because the selectAll() does not
				 * appear to work when using mouse to give focus to text.
				 */
				Display display = filterText.getDisplay();
				display.asyncExec(new Runnable() {
					public void run() {
						if (!filterText.isDisposed()) {
							filterText.selectAll();
						}
					}
				});
			}
		});

		filterText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				FilterItems();
				clearFilterToolBar.getControl().setVisible(filterText.getText().length() > 0);
			}
		});
		clearFilterToolBar = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		clearFilterToolBar.createControl(filterComposite);

		IAction clearTextAction = new Action("", IAction.AS_PUSH_BUTTON) {//$NON-NLS-1$
			@Override
			public void run() {
				filterText.setText(""); //$NON-NLS-1$
				FilterItems();
			}
		};
		clearTextAction.setToolTipText("Clear");
		clearTextAction.setImageDescriptor(clearImageDesc);
		clearTextAction.setDisabledImageDescriptor(clearImageDesc);
		clearFilterToolBar.add(clearTextAction);
		clearFilterToolBar.update(false);
		// initially there is no text to clear
		clearFilterToolBar.getControl().setVisible(false);

		viewer = new TableViewer(dialogArea, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setData(AUTOTEST_UID, "table");

		// process id column
		TableColumn processIdColumn = new TableColumn(table, SWT.LEFT);
		processIdColumn.setText("ID");
		processIdColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSortDirection(PROCESS_ID_COLUMN);
				ViewerComparator comparator = getViewerComparator(PROCESS_ID_COLUMN);
				viewer.setComparator(comparator);
				setColumnSorting((TableColumn) e.getSource(), sortDirection);
			}
		});
		processIdColumn.setData(AUTOTEST_UID, "processIdColumn");

		// process name column
		TableColumn processNameColumn = new TableColumn(table, SWT.LEFT);
		processNameColumn.setText("Name");
		processNameColumn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateSortDirection(PROCESS_NAME_COLUMN);
				ViewerComparator comparator = getViewerComparator(PROCESS_NAME_COLUMN);
				viewer.setComparator(comparator);
				setColumnSorting((TableColumn) e.getSource(), sortDirection);
			}
		});
		processNameColumn.setData(AUTOTEST_UID, "processNameColumn"); //$NON-NLS-1$

		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new ProcessesLabelProvider());
		viewer.setInput(processesOnTarget);

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				Object object = selection.getFirstElement();
				// Check.checkState(object instanceof ParsedProcess);
				selectedProcess = (ChooseProcessItem) object; // )parsedProcessMap.get(object);
				okPressed();
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				Button okButton = getButton(IDialogConstants.OK_ID);
				if (okButton != null) {
					okButton.setEnabled(!event.getSelection().isEmpty());
				}
			}

		});

		setColumnSorting(processIdColumn, sortDirection);
		table.setHeaderVisible(true);

		TableColumn[] columns = viewer.getTable().getColumns();
		for (TableColumn tableColumn : columns) {
			tableColumn.pack();
		}
		ChooseProcessItem defaultProcess = findProcessByName(defaultProcessName);
		if (defaultProcess == null)
			defaultProcess = processesOnTarget[0];
		viewer.setSelection(new StructuredSelection(defaultProcess), true);

		return dialogArea;
	}

	/**
	 * Filter the items in the viewer based on the current filter text
	 */
	private void FilterItems() {
		String pattern = filterText.getText();

		if (pattern.length() == 0)
			// This will call viewer.refresh().
			viewer.removeFilter(nameFilter);
		else {
			nameFilter.setPattern(pattern);

			if (viewer.getFilters().length == 0)
				// This will call viewer.refresh().
				viewer.addFilter(nameFilter);
			else
				viewer.refresh();
		}
	}

	/**
	 * Find a process name in the list that matches(contains) the given process
	 * name it doesn't have to match exactly
	 * 
	 * @param processName
	 * @return
	 */
	private ChooseProcessItem findProcessByName(String processName) {
		for (ChooseProcessItem process : processesOnTarget) {
			if (processName.startsWith(process.processName))
				return process;
		}
		return null;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(300, 375);
	}

	/**
	 * 
	 * @return the selected process item
	 */
	public ChooseProcessItem getSelectedProcess() {
		return selectedProcess;
	}

	protected ViewerComparator getViewerComparator(int column) {
		switch (column) {
		case PROCESS_NAME_COLUMN:
			return new ViewerComparator() {
				@Override
				public int compare(Viewer viewer, Object e1, Object e2) {
					String p1 = ((ChooseProcessItem) e1).processName;
					String p2 = ((ChooseProcessItem) e2).processName;
					if (p1 == null || p2 == null)
						return 0;
					return p1.compareToIgnoreCase(p2) * sortDirection;
				}
			};
		case PROCESS_ID_COLUMN:
			return new ViewerComparator() {
				@Override
				public int compare(Viewer viewer, Object e1, Object e2) {
					String p1 = ((ChooseProcessItem) e1).processID;
					String p2 = ((ChooseProcessItem) e2).processID;
					int i1 = 0;
					int i2 = 0;
					try {
						i1 = Integer.parseInt(p1);
						i2 = Integer.parseInt(p2);
					} catch (Exception e) {
						// ignore, just return 0
					}
					return (i1 - i2) * sortDirection;
				}
			};
		}
		return null;
	}

	@Override
	protected void okPressed() {
		StructuredSelection selectedItems = (StructuredSelection) viewer.getSelection();
		Object object = selectedItems.getFirstElement();
		selectedProcess = (ChooseProcessItem) object;
		super.okPressed();
	}

	protected void setColumnSorting(TableColumn column, int order) {
		Table table = viewer.getTable();
		table.setSortColumn(column);
		table.setSortDirection(order > 0 ? SWT.UP : SWT.DOWN);
	}

	public void setProcessesOnTarget(ChooseProcessItem[] processesOnTarget) {
		this.processesOnTarget = processesOnTarget;
	}

	protected void updateSortDirection(int column) {
		if (lastSortColumn == column) {
			sortDirection *= -1;
		} else {
			sortDirection = 1;
			lastSortColumn = column;
		}
	}
}

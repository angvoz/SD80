/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *   IBM Corporation - initial API and implementation 
 ************************************************************************/
package org.eclipse.cdt.internal.ui.cview;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.editor.FileSearchAction;
import org.eclipse.cdt.internal.ui.editor.FileSearchActionInWorkingSet;
import org.eclipse.cdt.internal.ui.editor.OpenIncludeAction;
import org.eclipse.cdt.internal.ui.editor.SearchDialogAction;
import org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.actions.RefactoringActionGroup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.actions.AddTaskAction;
import org.eclipse.ui.actions.ExportResourcesAction;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.actions.NewWizardMenu;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.ide.IDEActionFactory;

/**
 * The main action group for the cview. This contains a few actions and several
 * subgroups.
 */
public class MainActionGroup extends CViewActionGroup {

	// Actions for Menu context.
	AddBookmarkAction addBookmarkAction;
	AddTaskAction addTaskAction;
	PropertyDialogAction propertyDialogAction;

	ImportResourcesAction importAction;
	ExportResourcesAction exportAction;

	// CElement action
	OpenIncludeAction openIncludeAction;
	ShowLibrariesAction clibFilterAction;
	// Collapsing
	CollapseAllAction collapseAllAction;
	ToggleLinkingAction toggleLinkingAction;

	//Search
	FileSearchAction fFileSearchAction;
	FileSearchActionInWorkingSet fFileSearchActionInWorkingSet;
	SearchDialogAction fSearchDialogAction;
	FilterSelectionAction patternFilterAction;

	BuildGroup buildGroup;
	OpenFileGroup openFileGroup;
	GotoActionGroup gotoGroup;
	RefactorActionGroup refactorGroup;
	OpenProjectGroup openProjectGroup;
	WorkingSetFilterActionGroup workingSetGroup;

	SelectionSearchGroup selectionSearchGroup;
	RefactoringActionGroup refactoringActionGroup;
	
	public MainActionGroup(CView cview) {
		super(cview);
	}

	/**
	 * Handles key events in viewer.
	 */
	public void handleKeyPressed(KeyEvent event) {
		refactorGroup.handleKeyPressed(event);
		openFileGroup.handleKeyPressed(event);
		openProjectGroup.handleKeyPressed(event);
		gotoGroup.handleKeyPressed(event);
		buildGroup.handleKeyPressed(event);
	}

	/**
	 * Handles key events in viewer.
	 */
	public void handleKeyReleased(KeyEvent event) {
		refactorGroup.handleKeyReleased(event);
		openFileGroup.handleKeyReleased(event);
		openProjectGroup.handleKeyReleased(event);
		gotoGroup.handleKeyReleased(event);
		buildGroup.handleKeyReleased(event);
	}

	protected void makeActions() {
		final Viewer viewer = getCView().getViewer();
		Shell shell = getCView().getViewSite().getShell();

		openFileGroup = new OpenFileGroup(getCView());
		openProjectGroup = new OpenProjectGroup(getCView());
		gotoGroup = new GotoActionGroup(getCView());
		buildGroup = new BuildGroup(getCView());
		refactorGroup = new RefactorActionGroup(getCView());

		openIncludeAction = new OpenIncludeAction(viewer);

		//sortByNameAction = new SortViewAction(this, false);
		//sortByTypeAction = new SortViewAction(this, true);
		patternFilterAction = new FilterSelectionAction(shell, getCView(), CViewMessages.getString("FilterSelectionAction.label")); //$NON-NLS-1$
		clibFilterAction = new ShowLibrariesAction(shell, getCView(), CViewMessages.getString("ShowLibrariesAction.label")); //$NON-NLS-1$

		IPropertyChangeListener workingSetUpdater = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
                                 
				if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET.equals(property)) {
					Object newValue = event.getNewValue();
                                         
					if (newValue instanceof IWorkingSet) {
						getCView().setWorkingSet((IWorkingSet) newValue);
					} else if (newValue == null) {
						getCView().setWorkingSet(null);
					}
				}
			}
		};
		workingSetGroup = new WorkingSetFilterActionGroup(shell, workingSetUpdater);
		workingSetGroup.setWorkingSet(getCView().getWorkingSet());

		addBookmarkAction = new AddBookmarkAction(shell);
		addTaskAction = new AddTaskAction(shell);
		propertyDialogAction = new PropertyDialogAction(shell, viewer);

		// Importing/exporting.
		importAction = new ImportResourcesAction(getCView().getSite().getWorkbenchWindow());
		exportAction = new ExportResourcesAction(getCView().getSite().getWorkbenchWindow());

		collapseAllAction = new CollapseAllAction(getCView());

		toggleLinkingAction = new ToggleLinkingAction(getCView(), CViewMessages.getString("ToggleLinkingAction.text")); //$NON-NLS-1$
		toggleLinkingAction.setToolTipText(CViewMessages.getString("ToggleLinkingAction.toolTip")); //$NON-NLS-1$
		toggleLinkingAction.setImageDescriptor(getImageDescriptor("elcl16/synced.gif"));//$NON-NLS-1$
		toggleLinkingAction.setHoverImageDescriptor(getImageDescriptor("clcl16/synced.gif"));//$NON-NLS-1$

		fFileSearchAction = new FileSearchAction(viewer);
		fFileSearchActionInWorkingSet = new FileSearchActionInWorkingSet(viewer);
		fSearchDialogAction = new SearchDialogAction(viewer, getCView().getViewSite().getWorkbenchWindow());
		
		selectionSearchGroup = new SelectionSearchGroup(getCView().getSite());
		refactoringActionGroup = new RefactoringActionGroup(getCView().getSite());	
		
	}

	/**
	 * Called when the context menu is about to open. Override to add your own
	 * context dependent menu contributions.
	 */
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection celements = (IStructuredSelection) getCView().getViewer().getSelection();
		IStructuredSelection resources = SelectionConverter.convertSelectionToResources(celements);

		if (resources.isEmpty()) {
			new NewWizardMenu(menu, getCView().getSite().getWorkbenchWindow(), false);
			menu.add(new Separator(IContextMenuConstants.GROUP_REORGANIZE));
			refactoringActionGroup.fillContextMenu(menu);						
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
			menu.add(new Separator());
			addSelectionSearchMenu(menu, resources);
			return;
		}

		addNewMenu(menu, resources);
		menu.add(new Separator());
		gotoGroup.fillContextMenu(menu);
		menu.add(new Separator());
		openFileGroup.fillContextMenu(menu);
		menu.add(new Separator());
		buildGroup.fillContextMenu(menu);
		menu.add(new Separator());
		refactorGroup.fillContextMenu(menu);
		menu.add(new Separator());
		importAction.selectionChanged(resources);
		menu.add(importAction);
		exportAction.selectionChanged(resources);
		menu.add(exportAction);
		menu.add(new Separator());
		openProjectGroup.fillContextMenu(menu);
		addBookMarkMenu(menu, resources);
		menu.add(new Separator());
		addSearchMenu(menu, resources);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
		addPropertyMenu(menu, resources);
		
	}
	/**
	 * Extends the superclass implementation to set the context in the
	 * subgroups.
	 */
	public void setContext(ActionContext context) {
		super.setContext(context);
		gotoGroup.setContext(context);
		openFileGroup.setContext(context);
		openProjectGroup.setContext(context);
		refactorGroup.setContext(context);
		buildGroup.setContext(context);
		//sortAndFilterGroup.setContext(context);
		//workspaceGroup.setContext(context);
	}

	void addNewMenu(IMenuManager menu, IStructuredSelection selection) {
		MenuManager newMenu = new MenuManager(CViewMessages.getString("NewWizardsActionGroup.new")); //$NON-NLS-1$
		new NewWizardMenu(newMenu, getCView().getSite().getWorkbenchWindow(), false);
		menu.add(newMenu);
	}

	void addBookMarkMenu(IMenuManager menu, IStructuredSelection selection) {
		Object obj = selection.getFirstElement();
		if (obj instanceof IAdaptable) {
			IAdaptable element = (IAdaptable) obj;
			IResource resource = (IResource) element.getAdapter(IResource.class);
			if (resource instanceof IFile) {
				addBookmarkAction.selectionChanged(selection);
				menu.add(addBookmarkAction);
			}
		}
	}

	void addPropertyMenu(IMenuManager menu, IStructuredSelection selection) {
		propertyDialogAction.selectionChanged(selection);
		if (propertyDialogAction.isApplicableForSelection()) {
			menu.add(propertyDialogAction);
		}
	}

	void addSearchMenu(IMenuManager menu, IStructuredSelection selection) {
		IAdaptable element = (IAdaptable) selection.getFirstElement();

		if (element instanceof ITranslationUnit || element instanceof ICProject) {
			return;
		}

		MenuManager search = new MenuManager(CViewMessages.getString("SearchAction.label"), IContextMenuConstants.GROUP_SEARCH); //$NON-NLS-1$

		if (SearchDialogAction.canActionBeAdded(selection)) {
			search.add(fSearchDialogAction);
		}

		if (FileSearchAction.canActionBeAdded(selection)) {
			MenuManager fileSearch = new MenuManager(CViewMessages.getString("FileSearchAction.label"));//$NON-NLS-1$
			fileSearch.add(fFileSearchAction);
			fileSearch.add(fFileSearchActionInWorkingSet);
			search.add(fileSearch);
		}

		menu.add(search);
	}
	
	/**
	 * @param menu
	 */
	void addSelectionSearchMenu(IMenuManager menu, IStructuredSelection selection) {
		selectionSearchGroup.fillContextMenu(menu);
	}


	public void runDefaultAction(IStructuredSelection selection) {
		openFileGroup.runDefaultAction(selection);
		openProjectGroup.runDefaultAction(selection);
		gotoGroup.runDefaultAction(selection);
		buildGroup.runDefaultAction(selection);
		refactorGroup.runDefaultAction(selection);
		//workingSetGroup.runDefaultAction(selection);
	}

	/**
	 * Updates all actions with the given selection. Necessary when popping up
	 * a menu, because some of the enablement criteria may have changed, even
	 * if the selection in the viewer hasn't. E.g. A project was opened or
	 * closed.
	 */
	public void updateActionBars() {
		IStructuredSelection selection = (IStructuredSelection) getContext().getSelection();

		propertyDialogAction.setEnabled(selection.size() == 1);
		//sortByTypeAction.selectionChanged(selection);
		//sortByNameAction.selectionChanged(selection);
		addBookmarkAction.selectionChanged(selection);
		addTaskAction.selectionChanged(selection);

		openFileGroup.updateActionBars();
		openProjectGroup.updateActionBars();
		gotoGroup.updateActionBars();
		buildGroup.updateActionBars();
		refactorGroup.updateActionBars();
		workingSetGroup.updateActionBars();
	}

	public void fillActionBars(IActionBars actionBars) {
		actionBars.setGlobalActionHandler(IDEActionFactory.BOOKMARK.getId(), addBookmarkAction);
		actionBars.setGlobalActionHandler(IDEActionFactory.ADD_TASK.getId(), addTaskAction);
        actionBars.setGlobalActionHandler(ActionFactory.PROPERTIES.getId(), propertyDialogAction);

		workingSetGroup.fillActionBars(actionBars);
		gotoGroup.fillActionBars(actionBars);
		refactorGroup.fillActionBars(actionBars);
		openFileGroup.fillActionBars(actionBars);
		openProjectGroup.fillActionBars(actionBars);
		buildGroup.fillActionBars(actionBars);

		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(new Separator());
		toolBar.add(collapseAllAction);
		toolBar.add(toggleLinkingAction);

		IMenuManager menu = actionBars.getMenuManager();
		//menu.add (clibFilterAction);
		menu.add(patternFilterAction);
		menu.add(toggleLinkingAction);
	}

	public void dispose() {
		IWorkspace workspace = CUIPlugin.getWorkspace();
		refactorGroup.dispose();
		openFileGroup.dispose();
		openProjectGroup.dispose();
		gotoGroup.dispose();
		buildGroup.dispose();
		super.dispose();
	}

}

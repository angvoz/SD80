/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IBuildConsoleEvent;
import org.eclipse.cdt.ui.IBuildConsoleListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.actions.ClearOutputAction;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.console.actions.TextViewerGotoLineAction;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.texteditor.FindReplaceAction;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;

public class BuildConsolePage extends Page implements ISelectionListener, IPropertyChangeListener, IBuildConsoleListener {

	private BuildConsole fConsole;
	private IConsoleView fConsoleView;
	private BuildConsoleViewer fViewer;
	private IProject fProject;

	// text selection listener
	private ISelectionChangedListener fTextListener = new ISelectionChangedListener() {

		public void selectionChanged(SelectionChangedEvent event) {
			updateSelectionDependentActions();
		}
	};

	// actions
	private ClearOutputAction fClearOutputAction;
	private Map fGlobalActions = new HashMap(10);
	private List fSelectionActions = new ArrayList(3);

	// menus
	private Menu fMenu;
	private ScrollLockAction fScrollLockAction;
	private boolean fIsLocked;

	/**
	 * @param view
	 * @param console
	 */
	public BuildConsolePage(IConsoleView view, BuildConsole console) {
		fConsole = console;
		fConsoleView = view;
	}

	protected void setProject(IProject project) {
		fProject = project;
	}

	protected IProject getProject() {
		return fProject;
	}

	protected BuildConsole getConsole() {
		return fConsole;
	}

	protected IDocument setDocument() {
		IProject project = getProject();
		if (project != null) {
			getViewer().setDocument(getConsole().getConsoleManager().getConsoleDocument(project));
		}
		return null;
	}

	public void consoleChange(IBuildConsoleEvent event) {
		if (event.getType() == IBuildConsoleEvent.CONSOLE_START || event.getType() == IBuildConsoleEvent.CONSOLE_CLOSE) {
			if (isAvailable()) {
				Display display = getControl().getDisplay();
				if (event.getType() == IBuildConsoleEvent.CONSOLE_CLOSE && getProject() != event.getProject()) {
					return;
				}
				setProject(event.getProject());
				display.asyncExec(new Runnable() {

					public void run() {
						if (isAvailable()) {
							setDocument();
							getConsole().setTitle(getProject());
						}
					}
				});
			}
		}
	}

	boolean isAvailable() {
		return getControl() != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fViewer = new BuildConsoleViewer(parent);

		MenuManager manager = new MenuManager("#MessageConsole", "#MessageConsole"); //$NON-NLS-1$ //$NON-NLS-2$
		manager.setRemoveAllWhenShown(true);
		manager.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager m) {
				contextMenuAboutToShow(m);
			}
		});
		fMenu = manager.createContextMenu(getControl());
		getControl().setMenu(fMenu);
		IPageSite site = getSite();
		site.registerContextMenu(CUIPlugin.PLUGIN_ID + ".CBuildConole", manager, getViewer()); //$NON-NLS-1$
		site.setSelectionProvider(getViewer());
		createActions();
		configureToolBar(site.getActionBars().getToolBarManager());
		fViewer.getSelectionProvider().addSelectionChangedListener(fTextListener);

		JFaceResources.getFontRegistry().addListener(this);
		setFont(JFaceResources.getFont(BuildConsolePreferencePage.PREF_BUILDCONSOLE_FONT));
		setTabs(CUIPlugin.getDefault().getPluginPreferences().getInt(BuildConsolePreferencePage.PREF_BUILDCONSOLE_TAB_WIDTH));

		getConsole().addPropertyChangeListener(this);

		setDocument();
		getConsole().setTitle(getProject());
	}

	/**
	 * Fill the context menu
	 * 
	 * @param menu
	 *            menu
	 */
	protected void contextMenuAboutToShow(IMenuManager menu) {
		menu.add((IAction) fGlobalActions.get(ActionFactory.COPY.getId()));
		menu.add((IAction) fGlobalActions.get(ActionFactory.SELECT_ALL.getId()));
		menu.add(new Separator("FIND")); //$NON-NLS-1$
		menu.add((IAction) fGlobalActions.get(ActionFactory.FIND.getId()));
		menu.add((IAction) fGlobalActions.get(ITextEditorActionConstants.GOTO_LINE));
		menu.add(fClearOutputAction);
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		Object source = event.getSource();
		String property = event.getProperty();
		if (BuildConsole.P_STREAM_COLOR.equals(property) && source instanceof BuildConsoleStream) {
			BuildConsoleStream stream = (BuildConsoleStream) source;
			if (stream.getConsole().equals(getConsole())) {
				getViewer().getTextWidget().redraw();
			}
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_FONT)) {
			setFont(JFaceResources.getFont(BuildConsolePreferencePage.PREF_BUILDCONSOLE_FONT));
		} else if (property.equals(BuildConsolePreferencePage.PREF_BUILDCONSOLE_TAB_WIDTH)) {
			setTabs(CUIPlugin.getDefault().getPluginPreferences().getInt(BuildConsolePreferencePage.PREF_BUILDCONSOLE_TAB_WIDTH));
		}

	}

	protected void createActions() {
		fClearOutputAction = new ClearOutputAction(getViewer());
		fScrollLockAction = new ScrollLockAction(getViewer());
		fScrollLockAction.setChecked(fIsLocked);
		getViewer().setAutoScroll(!fIsLocked);
		// In order for the clipboard actions to accessible via their shortcuts
		// (e.g., Ctrl-C, Ctrl-V), we *must* set a global action handler for
		// each action
		IActionBars actionBars = getSite().getActionBars();
		TextViewerAction action = new TextViewerAction(getViewer(), ITextOperationTarget.COPY);
		action.configureAction(ConsoleMessages.getString("BuildConsolePage.&Copy@Ctrl+C_6"), //$NON-NLS-1$
				ConsoleMessages.getString("BuildConsolePage.Copy_7"), ConsoleMessages.getString("BuildConsolePage.Copy_7")); //$NON-NLS-1$ //$NON-NLS-2$
		action.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		action.setDisabledImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_COPY_DISABLED));
		action.setHoverImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_TOOL_COPY_HOVER));
		setGlobalAction(actionBars, ActionFactory.COPY.getId(), action);
		action = new TextViewerAction(getViewer(), ITextOperationTarget.SELECT_ALL);
		action.configureAction(ConsoleMessages.getString("BuildConsolePage.Select_&All@Ctrl+A_12"), //$NON-NLS-1$
				ConsoleMessages.getString("BuildConsolePage.Select_All"), //$NON-NLS-1$
				ConsoleMessages.getString("BuildConsolePage.Select_All")); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.SELECT_ALL.getId(), action);
		//XXX Still using "old" resource access
		ResourceBundle bundle = ResourceBundle.getBundle(ConsoleMessages.BUNDLE_NAME); //$NON-NLS-1$
		setGlobalAction(actionBars, ActionFactory.FIND.getId(), new FindReplaceAction(bundle, "find_replace_action.", //$NON-NLS-1$
				getConsoleView())); //$NON-NLS-1$
		action = new TextViewerGotoLineAction(getViewer());
		setGlobalAction(actionBars, ITextEditorActionConstants.GOTO_LINE, action);
		actionBars.updateActionBars();
		fSelectionActions.add(ActionFactory.COPY.getId());
		fSelectionActions.add(ActionFactory.FIND.getId());
	}

	protected void updateSelectionDependentActions() {
		Iterator iterator = fSelectionActions.iterator();
		while (iterator.hasNext()) {
			updateAction((String) iterator.next());
		}
	}

	protected void updateAction(String actionId) {
		IAction action = (IAction) fGlobalActions.get(actionId);
		if (action instanceof IUpdate) {
			((IUpdate) action).update();
		}
	}

	protected void setGlobalAction(IActionBars actionBars, String actionID, IAction action) {
		fGlobalActions.put(actionID, action);
		actionBars.setGlobalActionHandler(actionID, action);
	}

	protected void configureToolBar(IToolBarManager mgr) {
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fScrollLockAction);
		mgr.appendToGroup(IConsoleConstants.OUTPUT_GROUP, fClearOutputAction);
	}

	protected BuildConsoleViewer getViewer() {
		return fViewer;
	}

	/**
	 * Returns the view this page is contained in
	 * 
	 * @return the view this page is contained in
	 */
	protected IConsoleView getConsoleView() {
		return fConsoleView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IPage#dispose()
	 */
	public void dispose() {
		super.dispose();
		getSite().getPage().removeSelectionListener(this);
		getConsole().getConsoleManager().addConsoleListener(this);
	}

	public void init(IPageSite pageSite) {
		super.init(pageSite);
		setProject(convertSelectionToProject(getSite().getPage().getSelection()));

		getSite().getPage().addSelectionListener(this);
		getConsole().getConsoleManager().addConsoleListener(this);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IProject newProject = convertSelectionToProject(selection);
		IProject oldProject = getProject();
		if (oldProject == null || (newProject != null && !newProject.equals(oldProject))) {
			setProject(newProject);
			setDocument();
			getConsole().setTitle(getProject());
		}
	}

	IProject convertSelectionToProject(ISelection selection) {
		IProject project = null;
		if (selection == null || !(selection instanceof IStructuredSelection)) {
			return project;
		}
		IStructuredSelection ssel = (IStructuredSelection) selection;
		Object element = ssel.getFirstElement();
		if (element instanceof IAdaptable) {
			IAdaptable input = (IAdaptable) element;
			if (input != null) {
				IResource resource = null;
				if (input instanceof IResource) {
					resource = (IResource) input;
				} else {
					resource = (IResource) input.getAdapter(IResource.class);
				}
				if (resource != null) {
					project = resource.getProject();
				}
			}
		}
		return project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IPage#getControl()
	 */
	public Control getControl() {
		if (fViewer != null) {
			return fViewer.getControl();
		}
		return null;
	}

	//	/*
	//	 * (non-Javadoc)
	//	 *
	//	 * @see
	// org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	//	 */
	//	public void setActionBars(IActionBars actionBars) {
	//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.IPage#setFocus()
	 */
	public void setFocus() {
		Control control = getControl();
		if (control != null) {
			control.setFocus();
		}
		updateSelectionDependentActions();
	}

	/**
	 * Sets the font for this page.
	 * 
	 * @param font
	 *            font
	 */
	protected void setFont(Font font) {
		getViewer().getTextWidget().setFont(font);
	}

	/**
	 * Sets the tab width for this page.
	 * 
	 * @param int 
	 *            tab width
	 */
	protected void setTabs(int tabs) {
		getViewer().getTextWidget().setTabs(tabs);
	}
	
	/**
	 * Refreshes this page
	 */
	protected void refresh() {
		getViewer().refresh();
	}
}

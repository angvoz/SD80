/*******************************************************************************
 * Copyright (c) 2007, 2008 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.actions.BuildAction;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;

import org.eclipse.cdt.internal.ui.actions.ActionMessages;

/**
 * Implements a toolbar button that builds the active configuration
 * of selected projects. Also includes a menu that builds any of the
 * other configurations.
 *
 */
public class BuildActiveConfigMenuAction extends ChangeBuildConfigActionBase
		implements IWorkbenchWindowPulldownDelegate2, ICProjectDescriptionListener {

	private BuildAction buildaction;
	private IAction actionMenuCache; // cache the menu action so we can update the tool tip when the configuration changes

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate2#getMenu(org.eclipse.swt.widgets.Menu)
	 */
	public Menu getMenu(Menu parent) {
		Menu menu = new Menu(parent);
		addMenuListener(menu);
		return menu;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		Menu menu = new Menu(parent);
		addMenuListener(menu);
		return menu;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		mngr.removeCProjectDescriptionListener(this);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		buildaction = new BuildAction(window, IncrementalProjectBuilder.INCREMENTAL_BUILD);
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		mngr.addCProjectDescriptionListener(this, CProjectDescriptionEvent.DATA_APPLIED);
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		buildaction.selectionChanged(new StructuredSelection(fProjects.toArray()));
		buildaction.run();
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (actionMenuCache == null){
			actionMenuCache = action;
		}
		onSelectionChanged(action, selection);
		updateBuildConfigMenuToolTip(action);
	}
	
	/**
	 * Adds a listener to the given menu to re-populate it each time is is shown
	 * @param menu The menu to add listener to
	 */
	private void addMenuListener(Menu menu) {
		menu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent e) {
				fillMenu((Menu)e.widget);
			}
		});
	}

	@Override
	protected IAction makeAction(String sName, StringBuffer builder, int accel) {
		return new BuildConfigAction(fProjects, sName, builder.toString(), accel + 1, buildaction);
	}
	
	/**
	 * Update the tool tip based on the currently selected project and active configuration.
	 * @param action - The build configuration menu to change the tool tip on
	 */
	public void updateBuildConfigMenuToolTip(IAction action){
		String toolTipText = ActionMessages.getString("BuildActiveConfigMenuAction_defaultTooltip"); //$NON-NLS-1$
		if (fProjects.size() == 1) {
			Iterator<IProject> projIter = fProjects.iterator();
			IProject prj = projIter.next();
			if (prj != null){
				ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(prj, false);
				if (prjd != null) {
					toolTipText = ActionMessages.getFormattedString(
									"BuildActiveConfigMenuAction_buildConfigTooltip", //$NON-NLS-1$
									new Object[] { prjd.getActiveConfiguration().getName(), prj.getName() });
				}
			}
		}
		action.setToolTipText(toolTipText);
	}

	public void handleEvent(CProjectDescriptionEvent event) {
		if (actionMenuCache != null){
			updateBuildConfigMenuToolTip(actionMenuCache);
		}
	}

}

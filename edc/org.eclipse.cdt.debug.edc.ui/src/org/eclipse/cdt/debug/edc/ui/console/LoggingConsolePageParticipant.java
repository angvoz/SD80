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

package org.eclipse.cdt.debug.edc.ui.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

public class LoggingConsolePageParticipant implements IConsolePageParticipant {

	private Action removeAction;
	private IPageBookViewPage page;

	public void init(IPageBookViewPage page, final IConsole console) {
		this.page = page;
		removeAction = new Action() {
			@Override
			public void run() {
				ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { console });
			}
		};
		removeAction.setToolTipText("Close Console");
		removeAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
				ISharedImages.IMG_ELCL_REMOVE));
		removeAction.setId(getClass().getName() + ".removeAction");
		IToolBarManager toolBarManager = getToolBarManager(page);
		toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP, removeAction);
	}

	private IToolBarManager getToolBarManager(IPageBookViewPage page) {
		return page.getSite().getActionBars().getToolBarManager();
	}

	public void activated() {
	}

	public void deactivated() {
	}

	public void dispose() {
		IToolBarManager toolBarManager = getToolBarManager(page);
		toolBarManager.remove(removeAction.getId());
		removeAction = null;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

}

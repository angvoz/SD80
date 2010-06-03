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
package org.eclipse.cdt.debug.edc.internal.ui.actions;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugUI;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

public class CreationOptionsCommandHandler extends AbstractSnapshotCommandHandler {

	public CreationOptionsCommandHandler() {
		super();
		IWorkbench workbench = PlatformUI.getWorkbench();
		ICommandService commandSupport = (ICommandService)workbench.getAdapter(ICommandService.class);
		if (commandSupport != null)
		{
			Command command = commandSupport.getCommand("org.eclipse.cdt.debug.edc.ui.snapshotCreation");
			if (command != null)
			{
				try {
					HandlerUtil.updateRadioState(command, Album.getSnapshotCreationControl());
				} catch (ExecutionException e) {
					EDCDebugUI.getMessageLogger().logError(null, e);
				}
			}
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		String newSetting = event.getParameter("org.eclipse.ui.commands.radioStateParameter");
		Album.setSnapshotCreationControl(newSetting);
		HandlerUtil.updateRadioState(event.getCommand(), newSetting);
		return null;
	}

	@Override
	public void debugContextChanged(DebugContextEvent event) {
		super.debugContextChanged(event);
		setBaseEnabled(isEnabled() && !isSnapshotSession());
	}

}

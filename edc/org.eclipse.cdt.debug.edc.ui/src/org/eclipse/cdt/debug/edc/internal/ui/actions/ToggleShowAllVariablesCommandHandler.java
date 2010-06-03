/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.ui.actions;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.services.IEDCSymbols;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings("restriction")
public class ToggleShowAllVariablesCommandHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();

		// Note: toggleCommandState() returns the state before it is toggled
		boolean enabled = !HandlerUtil.toggleCommandState(command);

		IEclipsePreferences scope = new InstanceScope().getNode(EDCDebugger.PLUGIN_ID);
		scope.putBoolean(IEDCSymbols.SHOW_ALL_VARIABLES_ENABLED, enabled);
		try {
			scope.flush();
		} catch (BackingStoreException e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
		refreshViewer(event);
		return null;
	}

	private void refreshViewer(ExecutionEvent event) {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof AbstractDebugView) {
	        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			IDebugContextService debugContextService = 
				DebugUITools.getDebugContextManager().getContextService(window);
			ISelection context = debugContextService.getActiveContext();
			if (context instanceof IStructuredSelection) {
				Object viewerInput = ((IStructuredSelection) context).getFirstElement();
			    IPresentationContext presentationContext = 
			    	((TreeModelViewer) ((AbstractDebugView) activePart).getViewer()).getPresentationContext();
		        if (viewerInput instanceof IAdaptable && presentationContext != null) {
		            IVMAdapter adapter = (IVMAdapter) ((IAdaptable)viewerInput).getAdapter(IVMAdapter.class);
		            if (adapter != null) {
		            	IVMProvider vmProvider = adapter.getVMProvider(presentationContext);
		                if (vmProvider instanceof ICachingVMProvider) {
		                    ((ICachingVMProvider) vmProvider).refresh();
		                }
		            }
		        }
			}
		}
	}
}

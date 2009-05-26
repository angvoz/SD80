/*******************************************************************************
 *  Copyright (c) 2000, 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * A base class for the CDT filtering actions added to views. We disable the action if
 * the view has no CDT content.
 */
public abstract class ViewFilterAction extends ViewerFilter implements IViewActionDelegate, IActionDelegate2 {
	
	private IViewPart fView;
	private IAction fAction;

	public ViewFilterAction() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		fView = view;
		fAction.setChecked(getPreferenceValue(view));
		run(fAction);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	public void init(IAction action) {
		fAction = action;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		StructuredViewer viewer = getStructuredViewer();
		ViewerFilter[] filters = viewer.getFilters();
		ViewerFilter filter = null;
		for (int i = 0; i < filters.length; i++) {
			if (filters[i] == this) {
				filter = filters[i];
				break;
			}
		}
		if (filter == null) {
			viewer.addFilter(this);
		}
		viewer.refresh();
		IPreferenceStore store = getPreferenceStore();
		String key = getView().getSite().getId() + "." + getPreferenceKey(); //$NON-NLS-1$
		store.setValue(key, action.isChecked());
		CDebugUIPlugin.getDefault().savePluginPreferences();
	}

	/**
	 * Disable the action if there is no CDT content in the view. There is no
	 * practical generic way to test that so we have to use view specific tests.
	 * Currently, we support the Debug and Breakpoints view. Support for other
	 * views should be added as needed.
	 * 
	 * Note that because we do this test on a view selection change, there can
	 * be some edge cases where we'll be enabled even though there is no CDT
	 * content. Closing those gaps would not be easy, and thus not worth the
	 * effort as no harm is done by an unintentional enablement.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		boolean enable = false;
		Object input = getStructuredViewer().getInput();
		
		// Debug view
		if (input instanceof ILaunchManager) {
			ILaunchManager launchmgr = (ILaunchManager)input;
			IDebugTarget[] debugTargets = launchmgr.getDebugTargets();
			for (IDebugTarget debugTarget : debugTargets) {
				if (debugTarget instanceof ICDebugElement) {
					enable = true;
					break;
				}
			}
		}
		// Breakpoints view
		else if (input instanceof IBreakpointManager) {
			IBreakpointManager bkptmgr = (IBreakpointManager)input;
			IBreakpoint[] bkpts = bkptmgr.getBreakpoints();
			for (IBreakpoint bkpt : bkpts) {
				if (bkpt instanceof ICBreakpoint) {
					enable = true;
					break;
				}
			}
		}
		// unsupported view; action will always be enabled.
		else {
			enable = true;
		}
		fAction.setEnabled(enable);
	}

	protected IPreferenceStore getPreferenceStore() {
		return CDebugUIPlugin.getDefault().getPreferenceStore();
	}
	
	/**
	 * Returns the value of this filters preference (on/off) for the given
	 * view.
	 * 
	 * @param part
	 * @return boolean
	 */
	protected boolean getPreferenceValue(IViewPart part) {
		String baseKey = getPreferenceKey();
		String viewKey = part.getSite().getId();
		String compositeKey = viewKey + "." + baseKey; //$NON-NLS-1$
		IPreferenceStore store = getPreferenceStore();
		boolean value = false;
		if (store.contains(compositeKey)) {
			value = store.getBoolean(compositeKey);
		} else {
			value = store.getBoolean(baseKey);
		}
		return value;		
	}
	
	/**
	 * Returns the key for this action's preference
	 * 
	 * @return String
	 */
	protected abstract String getPreferenceKey(); 

	protected IViewPart getView() {
		return fView;
	}
	
	protected StructuredViewer getStructuredViewer() {
		IDebugView view = (IDebugView)getView().getAdapter(IDebugView.class);
		if (view != null) {
			Viewer viewer = view.getViewer();
			if (viewer instanceof StructuredViewer) {
				return (StructuredViewer)viewer;
			}
		}		
		return null;
	}
	
	/**
	 * Returns whether this action is seleted/checked.
	 * 
	 * @return whether this action is seleted/checked
	 */
	protected boolean getValue() {
		return fAction.isChecked();
	}
}

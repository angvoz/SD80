/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator;
import org.eclipse.cdt.dsf.debug.service.IBreakpointAttributeTranslator;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;

public class BreakpointAttributeTranslator implements IBreakpointAttributeTranslator {

	public boolean canUpdateAttributes(IBreakpointDMContext bp, Map<String, Object> delta) {
		// This method decides whether we need to re-install the breakpoint
		// based on the
		// attributes change (refer to caller in BreakpointsMediator).
		// For EDC, following changed attributes justify re-installation.
		// 
		// Check if there is any modified attribute
		if (delta == null || delta.size() == 0)
			return true;

		// Check the "critical" attributes
		// TODO: threadID change
		if (delta.containsKey(IMarker.LINE_NUMBER) // Line number
				|| delta.containsKey(IBreakpoint.ENABLED) // enabled
				|| delta.containsKey(ICLineBreakpoint.FUNCTION) // Function name
				|| delta.containsKey(ICLineBreakpoint.ADDRESS) // Absolute
																// address
				|| delta.containsKey(ICWatchpoint.EXPRESSION) // Watchpoint
																// expression
				|| delta.containsKey(ICWatchpoint.READ) // Watchpoint type
				|| delta.containsKey(ICWatchpoint.WRITE)) { // Watchpoint type
			return false;
		}

		// for other attrs (ICBreakpoint.INSTALL_COUNT,
		// ICBreakpoint.IGNORE_COUNT,
		// ICBreakpoint.CONDITION, etc), we can update by just copying.
		return true;
	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public List<Map<String, Object>> getBreakpointAttributes(IBreakpoint bp, boolean bpManagerEnabled)
			throws CoreException {
		// Check that the marker exists and retrieve its attributes.
		// Due to accepted race conditions, the breakpoint marker may become
		// null
		// while this method is being invoked. In this case throw an exception
		// and let the caller handle it.
		IMarker marker = bp.getMarker();
		if (marker == null || !marker.exists()) {
			throw new DebugException(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, DebugException.REQUEST_FAILED,
					"Breakpoint marker does not exist", null));
		}
		// Suppress cast warning: platform is still on Java 1.3
		@SuppressWarnings("unchecked")
		Map<String, Object> platformBpAttrs = marker.getAttributes();

		// Just make a copy of the platform attributes.
		// Add conversion or addition when needed.
		Map<String, Object> attrs = new HashMap<String, Object>(platformBpAttrs);

		if (bp instanceof ICWatchpoint) {
			attrs.put(Breakpoints.BREAKPOINT_TYPE, Breakpoints.WATCHPOINT);
			/*
			 * Related Attributes attributes.get(ICWatchpoint.EXPRESSION));
			 * attributes.get(ICWatchpoint.READ));
			 * attributes.get(ICWatchpoint.WRITE));
			 */
		} else if (bp instanceof ICLineBreakpoint) {
			attrs.put(Breakpoints.BREAKPOINT_TYPE, Breakpoints.BREAKPOINT);
			/*
			 * Related attributes: String
			 * attributes.get(ICBreakpoint.SOURCE_HANDLE)); Int
			 * attributes.get(IMarker.LINE_NUMBER)); String
			 * attributes.get(ICLineBreakpoint.FUNCTION)); String
			 * attributes.get(ICLineBreakpoint.ADDRESS));
			 */
		} else {
			// catchpoint?
		}

		/*
		 * Common fields attributes.get(ICBreakpoint.CONDITION));
		 * attributes.get(ICBreakpoint.IGNORE_COUNT));
		 * attributes.get(ICBreakpoint.INSTALL_COUNT));
		 * attributes.get(ICBreakpoint.ENABLED));
		 * attributes.get(ATTR_THREAD_ID)); // TODO: check: gdb specific ?
		 */

		// If the breakpoint manager is disabled, override the enabled
		// attribute.
		if (!bpManagerEnabled) {
			attrs.put(IBreakpoint.ENABLED, false);
		}

		// The breakpoint mediator allows for multiple target-side breakpoints
		// to be created for each IDE breakpoint. Currently this
		// feature is not used, we still have to return a list of attributes.
		List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>(1);
		retVal.add(attrs);
		return retVal;
	}

	public void initialize(BreakpointsMediator mediator) {

	}

	public boolean supportsBreakpoint(IBreakpoint bp) {
		// We support only CDT breakpoints.
		return bp instanceof ICBreakpoint;
	}

/*	public void updateBreakpointStatus(IBreakpoint bp, EBreakpointStatusChange breakpointStatusChange) {
		if (bp instanceof ICBreakpoint) {
			ICBreakpoint icbp = (ICBreakpoint) bp;
			switch (breakpointStatusChange) {
			case EInstalled:
				try {
					icbp.incrementInstallCount();
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().log(e.getStatus());
				}
				break;
			case EModified:
				// NO IMPLEMENTED
				break;
			case EUninstalled:
				try {
					icbp.decrementInstallCount();
				} catch (CoreException e) {
					EDCDebugger.getMessageLogger().log(e.getStatus());
				}
				break;
			}
		}
	}
*/
	public void updateBreakpointStatus(IBreakpoint bp) {
		if (bp instanceof ICBreakpoint) {
			ICBreakpoint icbp = (ICBreakpoint) bp;
			try {
				icbp.incrementInstallCount();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}

}

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
package org.eclipse.cdt.debug.edc.internal.scripting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IEDCSymbolReader;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.debug.internal.core.breakpoints.CAddressBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CFunctionBreakpoint;
import org.eclipse.cdt.debug.internal.core.breakpoints.CLineBreakpoint;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;

public class EDC {

	public static String[] getSessions() {
		ArrayList<String> ids = new ArrayList<String>();
		DsfSession[] sessions = DsfSession.getActiveSessions();
		for (DsfSession dsfSession : sessions) {
			ids.add(dsfSession.getId());
		}
		return ids.toArray(new String[ids.size()]);
	}

	public static Map<String, Object>[] getContexts(String sessionId) throws Exception {
		return DOMUtils.getDMContextProperties(DOMUtils.getContexts(sessionId));
	}

	public static Map<String, Object>[] getSuspendedContexts(String sessionId) throws Exception {
		return DOMUtils.getDMContextProperties(DOMUtils.getSuspendedContexts(sessionId));
	}

	public static Map<String, Object>[] getSuspendedThreads(String sessionId) throws Exception {
		return DOMUtils.getDMContextProperties(DOMUtils.getSuspendedThreads(sessionId));
	}

	public static DsfServicesTracker getDsfServicesTracker(String sessionId) {
		return new DsfServicesTracker(EDCDebugger.getDefault().getBundle().getBundleContext(), sessionId);
	}

	public static IBreakpoint[] getBreakpoints() {
		return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
	}

	public static IFunctionScope getFunctionAtAddress(String sessionId, String addressIdentifier) throws Exception {
		return DOMUtils.getFunctionAtAddress(sessionId, addressIdentifier);
	}

	public static IEDCSymbolReader getSymbolReader(String modulePath) {
		return DOMUtils.getSymbolReader(new Path(modulePath));
	}

	public static IBreakpoint createAddressBreakpoint(long address, String condition) throws Exception {
		Map<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, EDCDebugger.PLUGIN_ID);
		attributes.put(IMarker.CHAR_START, -1);
		attributes.put(IMarker.CHAR_END, -1);
		attributes.put(IMarker.LINE_NUMBER, -1);
		attributes.put(ICLineBreakpoint.ADDRESS, Long.toHexString(address));
		attributes.put(IBreakpoint.ENABLED, true);
		attributes.put(ICBreakpoint.IGNORE_COUNT, 0);
		attributes.put(ICBreakpoint.CONDITION, condition);
		attributes.put(ICBreakpointType.TYPE, ICBreakpointType.REGULAR);
		IResource rootResource = ResourcesPlugin.getWorkspace().getRoot();
		return new CAddressBreakpoint(rootResource, attributes, true);
	}

	public static IBreakpoint createFunctionBreakpoint(IResource resource, String sourceFilePath, String functionName, int lineNumber, String condition)
			throws Exception {
		Map<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, EDCDebugger.PLUGIN_ID);
		attributes.put(IMarker.CHAR_START, -1);
		attributes.put(IMarker.CHAR_END, -1);
		attributes.put(IMarker.LINE_NUMBER, lineNumber);
		attributes.put(ICLineBreakpoint.FUNCTION, functionName);
		attributes.put(ICBreakpoint.SOURCE_HANDLE, sourceFilePath);
		attributes.put(IBreakpoint.ENABLED, true);
		attributes.put(ICBreakpoint.IGNORE_COUNT, 0);
		attributes.put(ICBreakpoint.CONDITION, condition);
		attributes.put(ICBreakpointType.TYPE, ICBreakpointType.REGULAR);
		if (resource == null) {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}
		return new CFunctionBreakpoint(resource, attributes, true);
	}

	public static IBreakpoint createLineBreakpoint(IResource resource, String sourceFilePath, int lineNumber, String condition)
			throws Exception {
		Map<String, Object> attributes = new HashMap<String, Object>(10);
		attributes.put(IBreakpoint.ID, EDCDebugger.PLUGIN_ID);
		attributes.put(IMarker.CHAR_START, -1);
		attributes.put(IMarker.CHAR_END, -1);
		attributes.put(IMarker.LINE_NUMBER, lineNumber);
		attributes.put(ICBreakpoint.SOURCE_HANDLE, sourceFilePath);
		attributes.put(IBreakpoint.ENABLED, true);
		attributes.put(ICBreakpoint.IGNORE_COUNT, 0);
		attributes.put(ICBreakpoint.CONDITION, condition);
		attributes.put(ICBreakpointType.TYPE, ICBreakpointType.REGULAR);
		if (resource == null) {
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}
		return new CLineBreakpoint(resource, attributes, true);
	}

	public static IEDCExpression createExpression(final String sessionId, final IDMContext frame, final String expr)
		throws Exception {
		Query<IEDCExpression> runnable = new Query<IEDCExpression>() {
			@Override
			protected void execute(DataRequestMonitor<IEDCExpression> rm) {
				DsfServicesTracker servicesTracker = getDsfServicesTracker(sessionId);
				Expressions expressionsService = servicesTracker.getService(Expressions.class);
				IEDCExpression expression = (IEDCExpression) expressionsService.createExpression(frame, expr);
				expression.evaluateExpression();
				rm.setData(expression);
				rm.done();
			}
		};
		DsfSession.getSession(sessionId).getExecutor().execute(runnable);
		return runnable.get();
	}
}

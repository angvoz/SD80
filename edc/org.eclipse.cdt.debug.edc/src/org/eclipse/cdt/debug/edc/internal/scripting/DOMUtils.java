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
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.services.DMContext;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCSymbols;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IFunctionScope;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.tm.tcf.services.IRunControl;

public class DOMUtils {

	static private Map<String, String> contextsInSessions = new HashMap<String, String>();

	static public void addContext(String sessionID, String contextID) {
		contextsInSessions.put(contextID, sessionID);
	}

	static public String getSessionForContext(String contextID) {
		return contextsInSessions.get(contextID);
	}

	static protected DsfServicesTracker getDsfServicesTracker(final DsfSession session) {
		return new DsfServicesTracker(EDCDebugger.getBundleContext(), session.getId());
	}

	public static List<ExecutionDMC> getContexts(String sessionId) throws Exception {
		final List<ExecutionDMC> contexts = new ArrayList<ExecutionDMC>();

		final DsfSession session = DsfSession.getSession(sessionId);
		session.getExecutor().submit(new DsfRunnable() {
			public void run() {
				DsfServicesTracker servicesTracker = DOMUtils.getDsfServicesTracker(session);
				RunControl runControlService = servicesTracker.getService(RunControl.class);
				if (runControlService != null) {
					ExecutionDMC rootDMC = runControlService.getRootDMC();
					if (rootDMC != null) {
						ExecutionDMC[] children = rootDMC.getChildren();
						for (ExecutionDMC executionDMC : children) {
							addContextRecursive(executionDMC, contexts);
						}
					}
				}
			}

		}).get();

		return contexts;
	}

	public static List<ExecutionDMC> getSuspendedContexts(String sessionId) throws Exception {
		List<ExecutionDMC> suspendedContexts = new ArrayList<ExecutionDMC>();
		for (ExecutionDMC context : getContexts(sessionId)) {
			if (context.isSuspended())
				suspendedContexts.add(context);
		}

		return suspendedContexts;
	}

	public static List<ExecutionDMC> getSuspendedThreads(String sessionId) throws Exception {
		List<ExecutionDMC> threadList = new ArrayList<ExecutionDMC>();
		for (ExecutionDMC context : getSuspendedContexts(sessionId)) {
			String parentId = (String) context.getProperties().get(RunControl.PROP_PARENT_ID);
			if (parentId != null && !"root".equals(parentId)) {
				// filter out internal reasons for suspension
				String message = (String) context.getProperties().get(RunControl.PROP_MESSAGE);
				if (message != null && !IRunControl.REASON_SHAREDLIB.equals(message))
					threadList.add(context);
			}
		}

		return threadList;
	}

	private static void addContextRecursive(ExecutionDMC executionDMC, List<ExecutionDMC> contexts) {
		addContext(executionDMC, contexts);
		ExecutionDMC[] children = executionDMC.getChildren();
		for (ExecutionDMC childDMC : children) {
			addContextRecursive(childDMC, contexts);
		}
	}

	private static void addContext(ExecutionDMC executionDMC, List<ExecutionDMC> contexts) {
		contexts.add(executionDMC);
		addContext(executionDMC.getSessionId(), executionDMC.getID());
	}

	public static List<StackFrameDMC> getStackFrames(final String contextId) throws Exception {
		final List<StackFrameDMC> frames = new ArrayList<StackFrameDMC>();
		final DsfSession session = DsfSession.getSession(DOMUtils.getSessionForContext(contextId));
		session.getExecutor().submit(new DsfRunnable() {
			public void run() {
				DsfServicesTracker servicesTracker = DOMUtils.getDsfServicesTracker(session);
				Stack stackService = servicesTracker.getService(Stack.class);
				RunControl runControlService = servicesTracker.getService(RunControl.class);
				if (runControlService != null && stackService != null) {
					IFrameDMContext[] serviceFrames = stackService.getFramesForDMC(runControlService
							.getContext(contextId), 0, IStack.ALL_FRAMES);
					for (IFrameDMContext serviceFrame : serviceFrames) {
						frames.add((StackFrameDMC) serviceFrame);
					}
				}
			}
		}).get();

		return frames;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object>[] getDMContextProperties(List<? extends DMContext> contexts) {
		List<Map<String, Object>> contextPropsList = new ArrayList<Map<String, Object>>();
		for (IEDCDMContext context : contexts) {
			contextPropsList.add(context.getProperties());
		}
		return contextPropsList.toArray(new Map[contextPropsList.size()]);
	}

	public static IFunctionScope getFunctionAtAddress(String sessionId,
			String runtimeAddressIdentifier) throws Exception {
		
		final IFunctionScope[] result = new IFunctionScope[] { null };
		final IAddress runtimeAddress = new Addr64(runtimeAddressIdentifier, 16);

		for (final ExecutionDMC context : getContexts(sessionId)) {
			if (context instanceof ISymbolDMContext)
			{
				final DsfSession session = DsfSession.getSession(sessionId);
				session.getExecutor().submit(new DsfRunnable() {
					public void run() {
						DsfServicesTracker servicesTracker = DOMUtils.getDsfServicesTracker(session);
						IEDCSymbols edcSymbols = servicesTracker.getService(IEDCSymbols.class);
						if (edcSymbols != null) {
							result[0] = edcSymbols.getFunctionAtAddress((ISymbolDMContext) context, runtimeAddress);
						}
					}

				}).get();
			}
			if (result[0] != null)
				break;
		}
		return result[0];
	}

}

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
package org.eclipse.cdt.debug.edc.internal.scripting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.edc.internal.services.dsf.DMContext;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.scripting.ScriptingPlugin;

public class ExecutionContext {
	public static final class SuspendedEventAdapter {
		private final DsfSession session;
		private final int activity;
		private final String contextId;

		private SuspendedEventAdapter(DsfSession session, int activity, String contextId) {
			this.session = session;
			this.activity = activity;
			this.contextId = contextId;
		}

		@DsfServiceEventHandler
		public void eventDispatched(ISuspendedDMEvent e) {
			DMContext dmContext = (DMContext) e.getDMContext();
			String id = (String) dmContext.getProperty(DMContext.PROP_ID);
			if (contextId.equals(id)) {
				ScriptingPlugin.setActivityDone(activity);
				session.removeServiceEventListener(suspendedListener);
			}
		}
	}

	public final static String STEP_OVER = "StepOver";
	public final static String STEP_INTO = "StepInto";
	public final static String STEP_RETURN = "StepReturn";

	private final Map<String, Object> properties;
	private static Object suspendedListener;

	public ExecutionContext() {
		properties = new HashMap<String, Object>();
	}

	public ExecutionContext(ExecutionDMC executionDMC) {
		this.properties = executionDMC.getProperties();
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public static Map<String, Object>[] getStackFrames(Map<String, Object> contextData) throws Exception {
		final String contextId = (String) contextData.get(DMContext.PROP_ID);
		return DOMUtils.getDMContextProperties(DOMUtils.getStackFrames(contextId));
	}

	public static int resume(Map<String, Object> contextData) throws InterruptedException, ExecutionException {
		final String contextID = (String) contextData.get(DMContext.PROP_ID);
		final DsfSession session = DsfSession.getSession(DOMUtils.getSessionForContext(contextID));
		final int activity = ScriptingPlugin.newPendingActivityId();
		session.getExecutor().submit(new DsfRunnable() {
			public void run() {
				DsfServicesTracker servicesTracker = DOMUtils.getDsfServicesTracker(session);
				final RunControl runControlService = servicesTracker.getService(RunControl.class);
				if (runControlService != null) {
					runControlService.canResume(runControlService.getContext(contextID),
							new DataRequestMonitor<Boolean>(session.getExecutor(), null) {
								@Override
								protected void handleCompleted() {
									if (getData()) {
										runControlService.resume(runControlService.getContext(contextID),
												new RequestMonitor(session.getExecutor(), null) {
													@Override
													protected void handleCompleted() {
														ScriptingPlugin.setActivityDone(activity);
													};
												});
									}
								}
							});
				}
			}
		});
		return activity;
	}

	private static StepType getStepType(String stepType) {
		if (stepType.equals(STEP_OVER))
			return StepType.STEP_OVER;
		else if (stepType.equals(STEP_INTO))
			return StepType.STEP_INTO;
		else if (stepType.equals(STEP_RETURN)) {
			return StepType.STEP_RETURN;
		}

		return StepType.STEP_OVER; // default
	}

	public static int step(Map<String, Object> contextData, final String stepTypeName) {
		final String contextId = (String) contextData.get(DMContext.PROP_ID);
		final DsfSession session = DsfSession.getSession(DOMUtils.getSessionForContext(contextId));
		final int activity = ScriptingPlugin.newPendingActivityId();
		suspendedListener = new SuspendedEventAdapter(session, activity, contextId);
		session.getExecutor().submit(new DsfRunnable() {
			public void run() {
				session.addServiceEventListener(suspendedListener, null);
				DsfServicesTracker servicesTracker = DOMUtils.getDsfServicesTracker(session);
				final RunControl runControlService = servicesTracker.getService(RunControl.class);
				if (runControlService != null) {
					final StepType stepType = getStepType(stepTypeName);
					runControlService.canStep(runControlService.getContext(contextId), stepType,
							new DataRequestMonitor<Boolean>(session.getExecutor(), null) {
								@Override
								protected void handleCompleted() {
									if (getData()) {
										ExecutionDMC context = runControlService.getContext(contextId);
										runControlService.step(context, stepType, new RequestMonitor(session
												.getExecutor(), null) {
											@Override
											protected void handleCompleted() {
												// TODO this is currently set
												// done when request is handled
												// should really set up an event
												// listener and use that to
												// determine that stepping is
												// complete
												// ScriptingPlugin.setActivityDone(activity);
											};
										});
									}
								}
							});
				}
			}
		});
		return activity;
	}

}

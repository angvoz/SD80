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
package org.eclipse.cdt.debug.edc.internal.launch;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Snapshots;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class ShutdownSequence extends Sequence {

	String sessionId;

	String applicationName;

	String debugModelId;

	DsfServicesTracker tracker;

	public ShutdownSequence(DsfExecutor executor, String sessionId, RequestMonitor requestMonitor) {
		super(executor, requestMonitor);
		this.sessionId = sessionId;
	}

	@Override
	public Step[] getSteps() {
		return steps;
	}

	private final Step[] steps = new Step[] {

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			assert EDCDebugger.getDefault().getBundle().getBundleContext() != null;
			tracker = new DsfServicesTracker(EDCDebugger.getDefault().getBundle().getBundleContext(), sessionId);
			requestMonitor.done();
		}

		@Override
		public void rollBack(RequestMonitor requestMonitor) {
			tracker.dispose();
			tracker = null;
			requestMonitor.done();
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(Snapshots.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IDisassembly.class, requestMonitor);
		}
	}, new Step() {
		// Call this to make sure breakpoints are removed.
		// Do this before we shutdown other services to ensure
		// breakpoints can be removed.
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(BreakpointsMediator2.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IRegisters.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IBreakpoints.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(ISourceLookup.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IExpressions.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IStack.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IModules.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IMemory.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IRunControl.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(IProcesses.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(ITargetEnvironment.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			shutdownService(Symbols.class, requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			tracker.dispose();
			tracker = null;
			requestMonitor.done();
		}
	} };

	private <V> void shutdownService(Class<V> clazz, final RequestMonitor requestMonitor) {
		IDsfService service = (IDsfService) tracker.getService(clazz);
		if (service != null) {
			service.shutdown(new RequestMonitor(getExecutor(), requestMonitor) {
				@Override
				protected void handleCompleted() {
					if (!isSuccess()) {

					}
					requestMonitor.done();
				}
			});
		} else {
			requestMonitor
					.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
							"AbstractEDCService '" + clazz.getName() + "' not found.", null)); //$NON-NLS-1$//$NON-NLS-2$
			requestMonitor.done();
		}
	}
}

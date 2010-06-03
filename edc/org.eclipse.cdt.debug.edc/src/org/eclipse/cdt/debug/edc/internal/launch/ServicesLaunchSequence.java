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
package org.eclipse.cdt.debug.edc.internal.launch;

import org.eclipse.cdt.debug.edc.internal.services.dsf.BreakpointAttributeTranslator;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Breakpoints;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Snapshots;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
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
import org.eclipse.cdt.dsf.debug.service.ISymbols;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup.ISourceLookupDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IProgressMonitor;

public class ServicesLaunchSequence extends Sequence {

	Step[] fSteps = new Step[] {
	/*
	 * create this service as the first one as it's needed when
	 * constructing/initializing other services.
	 */
	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			launch.getServiceFactory().createService(ITargetEnvironment.class, session, launch).initialize(
					requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			launch.getServiceFactory().createService(IProcesses.class, session).initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			runControlService = (RunControl) launch.getServiceFactory().createService(IRunControl.class, session);
			runControlService.initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			launch.getServiceFactory().createService(IMemory.class, session).initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			Modules modules = (Modules) launch.getServiceFactory().createService(IModules.class, session);
			modules.initialize(requestMonitor);
			modules.setSourceLocator(launch.getExecutableLocator());
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			launch.getServiceFactory().createService(IStack.class, session).initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			Symbols symbols = (Symbols) launch.getServiceFactory().createService(ISymbols.class, session);
			symbols.initialize(requestMonitor);
			symbols.setSourceLocator(launch.getSourceLocator());
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			launch.getServiceFactory().createService(IExpressions.class, session).initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			sourceLookup = (CSourceLookup) launch.getServiceFactory().createService(ISourceLookup.class, session);
			sourceLookup.initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			ISourceLookupDMContext sourceLookupDmc = (ISourceLookupDMContext) (runControlService.getRootDMC());
			sourceLookup.setSourceLookupDirector(sourceLookupDmc, (CSourceLookupDirector) launch.getSourceLocator());
			requestMonitor.done();
		}
	},

	new Step() {
		@Override
		public void execute(final RequestMonitor requestMonitor) {
			Breakpoints breakpoints = (Breakpoints) launch.getServiceFactory().createService(IBreakpoints.class,
					session);
			breakpoints.initialize(new RequestMonitor(getExecutor(), requestMonitor));
			breakpoints.setSourceLocator(launch.getSourceLocator());
		}
	},

	new Step() {
		@Override
		public void execute(final RequestMonitor requestMonitor) {
			final BreakpointsMediator2 bpmService = new BreakpointsMediator2(session, new BreakpointAttributeTranslator(
					session));
			bpmService.initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			launch.getServiceFactory().createService(IRegisters.class, session).initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			launch.getServiceFactory().createService(IDisassembly.class, session).initialize(requestMonitor);
		}
	},

	new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			launch.getServiceFactory().createService(Snapshots.class, session).initialize(
					requestMonitor);
		}
	} };

	DsfSession session;
	EDCLaunch launch;
	RunControl runControlService;
	CSourceLookup sourceLookup;

	public ServicesLaunchSequence(DsfSession session, EDCLaunch launch, IProgressMonitor pm) {
		super(session.getExecutor(), pm, "Initializing debugger services", "Aborting debugger services initialization");
		this.session = session;
		this.launch = launch;
	}

	@Override
	public Step[] getSteps() {
		return fSteps;
	}

}

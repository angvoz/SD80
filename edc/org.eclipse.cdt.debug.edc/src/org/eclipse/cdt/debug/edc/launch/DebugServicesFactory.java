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
package org.eclipse.cdt.debug.edc.launch;

import org.eclipse.cdt.debug.edc.internal.launch.CSourceLookup;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Breakpoints;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Processes;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Signals;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Snapshots;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Symbols;
import org.eclipse.cdt.debug.edc.services.Disassembly;
import org.eclipse.cdt.debug.edc.services.ISnapshots;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.debug.service.AbstractDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IMemory;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.ISignals;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.ISymbols;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;

public abstract class DebugServicesFactory extends AbstractDsfDebugServicesFactory {

	public DebugServicesFactory() {

	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> V createService(Class<V> clazz, DsfSession session, Object... optionalArguments) {
		if (ITargetEnvironment.class.isAssignableFrom(clazz)) {
			for (Object arg : optionalArguments)
				if (arg instanceof ILaunch)
					return (V) createTargetEnvironmentService(session, ((ILaunch) arg));
		}
		if (Snapshots.class.isAssignableFrom(clazz)) {
			return (V)createSnapshotsService(session);
		}

		return super.createService(clazz, session, optionalArguments);
	}

	/**
	 * EDC requires adopters to provide an {@link ITargetEnvironment} service.
	 * 
	 * @param session
	 *            the DSF session the service will be used in
	 * @param launch
	 *            the launch object which spawned the DSF session. It's likely
	 *            the launch's configuration will be needed to service some of
	 *            the ITargetEnvironment methods.
	 * @return the DSF service
	 */
	abstract protected ITargetEnvironment createTargetEnvironmentService(DsfSession session, ILaunch launch);

	/**
	 * EDC requires adopters to provide an {@link IStack} service.
	 * 
	 * @param session
	 *            the DSF session the service will be used in
	 * @return the DSF service
	 */
	abstract protected IStack createStackService(DsfSession session);

	/**
	 * EDC requires adopters to provide an {@link IRegisters} service.
	 * 
	 * @param session
	 *            the DSF session the service will be used in
	 * @return the DSF service
	 */
	abstract protected IRegisters createRegistersService(DsfSession session);

	@Override
	protected ISourceLookup createSourceLookupService(DsfSession session) {
		return new CSourceLookup(session);
	}

	@Override
	protected IRunControl createRunControlService(DsfSession session) {
		return new RunControl(session);
	}

	@Override
	protected IMemory createMemoryService(DsfSession session) {
		return new Memory(session);
	}

	@Override
	protected IBreakpoints createBreakpointService(DsfSession session) {
		return new Breakpoints(session);
	}

	@Override
	protected IDisassembly createDisassemblyService(DsfSession session) {
		return new Disassembly(session, new String[0]);
	}

	@Override
	protected IExpressions createExpressionService(DsfSession session) {
		return new Expressions(session);
	}

	@Override
	protected IModules createModulesService(DsfSession session) {
		return new Modules(session);
	}

	@Override
	protected IProcesses createProcessesService(DsfSession session) {
		return new Processes(session);
	}

	@Override
	protected ISignals createSignalsService(DsfSession session) {
		return new Signals(session);
	}

	@Override
	protected ISymbols createSymbolsService(DsfSession session) {
		return new Symbols(session);
	}

	protected ISnapshots createSnapshotsService(DsfSession session) {
		return new Snapshots(session);
	}
}

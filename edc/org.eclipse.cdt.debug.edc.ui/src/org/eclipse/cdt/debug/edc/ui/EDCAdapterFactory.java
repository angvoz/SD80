/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.edc.internal.ui.DsfTerminateCommand;
import org.eclipse.cdt.debug.edc.internal.ui.EDCDebugTextHover;
import org.eclipse.cdt.debug.edc.internal.ui.EDCViewModelAdapter;
import org.eclipse.cdt.debug.edc.internal.ui.actions.EDCDisconnectCommand;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfResumeCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepIntoCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepOverCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfStepReturnCommand;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSuspendCommand;
import org.eclipse.cdt.dsf.debug.ui.contexts.DsfSuspendTrigger;
import org.eclipse.cdt.dsf.debug.ui.sourcelookup.DsfSourceDisplayAdapter;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.DefaultRefreshAllTarget;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.IRefreshAllTarget;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.debug.core.commands.IStepIntoHandler;
import org.eclipse.debug.core.commands.IStepOverHandler;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.debug.core.commands.ISuspendHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDebugModelProvider;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicyFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;
import org.eclipse.debug.ui.contexts.ISuspendTrigger;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;

/**
 * This implementation of platform adapter factory only retrieves the adapters
 * for the launch object. But it also manages the creation and destruction of
 * the session-based adapters which are returned by the IDMContext.getAdapter()
 * methods.
 */
@ThreadSafe
@SuppressWarnings( { "restriction" })
public class EDCAdapterFactory implements IAdapterFactory, ILaunchesListener2 {

	@Immutable
	class SessionAdapterSet {
		final EDCLaunch fLaunch;
		final EDCViewModelAdapter fViewModelAdapter;
		final DsfSourceDisplayAdapter fSourceDisplayAdapter;
		final DsfStepIntoCommand fStepIntoCommand;
		final DsfStepOverCommand fStepOverCommand;
		final DsfStepReturnCommand fStepReturnCommand;
		final DsfTerminateCommand fTerminateCommand;
		final DsfSuspendCommand fSuspendCommand;
		final DsfResumeCommand fResumeCommand;
		final EDCDisconnectCommand fDisconnectCommand;
		
		final IDebugModelProvider fDebugModelProvider;
		final DsfSuspendTrigger fSuspendTrigger;
		final DsfSteppingModeTarget fSteppingModeTarget;
		final IModelSelectionPolicyFactory fModelSelectionPolicyFactory;
		final SteppingController fSteppingController;
		final DefaultRefreshAllTarget fRefreshAllTarget;

		final EDCDebugTextHover fDebugTextHover;
		
		SessionAdapterSet(final EDCLaunch launch) {
			fLaunch = launch;
			DsfSession session = launch.getSession();

			// register stepping controller
			fSteppingController = new SteppingController(session);
			session.registerModelAdapter(SteppingController.class, fSteppingController);

			fViewModelAdapter = new EDCViewModelAdapter(session, fSteppingController);

			if (launch.getSourceLocator() instanceof ISourceLookupDirector) {
				fSourceDisplayAdapter = new DsfSourceDisplayAdapter(session, (ISourceLookupDirector) launch
						.getSourceLocator(), fSteppingController);
			} else {
				fSourceDisplayAdapter = null;
			}
			session.registerModelAdapter(ISourceDisplay.class, fSourceDisplayAdapter);

			fDisconnectCommand = new EDCDisconnectCommand(session);
			fSteppingModeTarget = new DsfSteppingModeTarget();
			fStepIntoCommand = new DsfStepIntoCommand(session, fSteppingModeTarget);
			fStepOverCommand = new DsfStepOverCommand(session, fSteppingModeTarget);
			fStepReturnCommand = new DsfStepReturnCommand(session);
			fSuspendCommand = new DsfSuspendCommand(session);
			fResumeCommand = new DsfResumeCommand(session);
			fTerminateCommand = new DsfTerminateCommand(session);
			fSuspendTrigger = new DsfSuspendTrigger(session, fLaunch);
			fModelSelectionPolicyFactory = new DefaultEDCModelSelectionPolicyFactory();
			fRefreshAllTarget = new DefaultRefreshAllTarget();

			session.registerModelAdapter(IDisconnectHandler.class, fDisconnectCommand);
			session.registerModelAdapter(ISteppingModeTarget.class, fSteppingModeTarget);
			session.registerModelAdapter(IStepIntoHandler.class, fStepIntoCommand);
			session.registerModelAdapter(IStepOverHandler.class, fStepOverCommand);
			session.registerModelAdapter(IStepReturnHandler.class, fStepReturnCommand);
			session.registerModelAdapter(ISuspendHandler.class, fSuspendCommand);
			session.registerModelAdapter(IResumeHandler.class, fResumeCommand);
			session.registerModelAdapter(IModelSelectionPolicyFactory.class, fModelSelectionPolicyFactory);
			session.registerModelAdapter(IRefreshAllTarget.class, fRefreshAllTarget);
			session.registerModelAdapter(ITerminateHandler.class, fTerminateCommand);

			fDebugModelProvider = new IDebugModelProvider() {
				// @see
				// org.eclipse.debug.core.model.IDebugModelProvider#getModelIdentifiers()
				public String[] getModelIdentifiers() {
					return new String[] { launch.getDebugModelID(), ICBreakpoint.C_BREAKPOINTS_DEBUG_MODEL_ID };
				}
			};
			session.registerModelAdapter(IDebugModelProvider.class, fDebugModelProvider);

			/*
			 * Registering the launch as an adapter, ensures that this launch,
			 * and debug model ID will be associated with all DMContexts from
			 * this session.
			 */
			session.registerModelAdapter(ILaunch.class, fLaunch);

            /*
             * Register debug hover adapter (bug 309001).
             */
            fDebugTextHover = new EDCDebugTextHover();
            session.registerModelAdapter(ICEditorTextHover.class, fDebugTextHover);
            
            session.registerModelAdapter(IViewerInputProvider.class, fViewModelAdapter);
		}

		void dispose() {
			DsfSession session = fLaunch.getSession();

			fViewModelAdapter.dispose();

			session.unregisterModelAdapter(ISourceDisplay.class);
			if (fSourceDisplayAdapter != null)
				fSourceDisplayAdapter.dispose();

			session.unregisterModelAdapter(SteppingController.class);
			fSteppingController.dispose();

			session.unregisterModelAdapter(ISteppingModeTarget.class);
			session.unregisterModelAdapter(IStepIntoHandler.class);
			session.unregisterModelAdapter(IStepOverHandler.class);
			session.unregisterModelAdapter(IStepReturnHandler.class);
			session.unregisterModelAdapter(ISuspendHandler.class);
			session.unregisterModelAdapter(IResumeHandler.class);
			session.unregisterModelAdapter(IRestart.class);
			session.unregisterModelAdapter(ITerminateHandler.class);
			session.unregisterModelAdapter(IDisconnectHandler.class);
			session.unregisterModelAdapter(IModelSelectionPolicyFactory.class);
			session.unregisterModelAdapter(IRefreshAllTarget.class);
			session.unregisterModelAdapter(ITerminateHandler.class);

            session.unregisterModelAdapter(ICEditorTextHover.class);

			fDisconnectCommand.dispose();
			fStepIntoCommand.dispose();
			fStepOverCommand.dispose();
			fStepReturnCommand.dispose();
			fSuspendCommand.dispose();
			fResumeCommand.dispose();
			fSuspendTrigger.dispose();
		}
	}

	/**
	 * Active adapter sets. They are accessed using the launch instance which
	 * owns the debug services session.
	 */
	private static Map<EDCLaunch, SessionAdapterSet> fgLaunchAdapterSets = Collections
			.synchronizedMap(new HashMap<EDCLaunch, SessionAdapterSet>());

	/**
	 * Map of launches for which adapter sets have already been disposed. This
	 * map (used as a set) is maintained in order to avoid re-creating an
	 * adapter set after the launch was removed from the launch manager, but
	 * while the launch is still being held by other classes which may request
	 * its adapters. A weak map is used to avoid leaking memory once the
	 * launches are no longer referenced.
	 * <p>
	 * Access to this map is synchronized using the fgLaunchAdapterSets
	 * instance.
	 * </p>
	 */
	private static Map<ILaunch, SessionAdapterSet> fgDisposedLaunchAdapterSets = new WeakHashMap<ILaunch, SessionAdapterSet>();

	static void disposeAdapterSet(ILaunch launch) {
		synchronized (fgLaunchAdapterSets) {
			if (fgLaunchAdapterSets.containsKey(launch)) {
				fgLaunchAdapterSets.remove(launch).dispose();
				fgDisposedLaunchAdapterSets.put(launch, null);
			}
		}
	}

	public EDCAdapterFactory() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	/**
	 * This method only actually returns adapters for the launch object.
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (!(adaptableObject instanceof EDCLaunch))
			return null;

		EDCLaunch launch = (EDCLaunch) adaptableObject;

		// Check for valid session.
		// Note: even if the session is no longer active, the adapter set
		// should still be returned. This is because the view model may still
		// need to show elements representing a terminated process/thread/etc.
		DsfSession session = launch.getSession();
		if (session == null)
			return null;

		// Find the correct set of adapters based on the launch session-ID. If
		// not found
		// it means that we have a new launch and new session, and we have to
		// create a
		// new set of adapters.

		SessionAdapterSet adapterSet;
		synchronized (fgLaunchAdapterSets) {
			// The adapter set for the given launch was already disposed.
			// Return a null adapter.
			if (fgDisposedLaunchAdapterSets.containsKey(launch)) {
				return null;
			}
			adapterSet = fgLaunchAdapterSets.get(launch);
			if (adapterSet == null) {
				adapterSet = new SessionAdapterSet(launch);
				fgLaunchAdapterSets.put(launch, adapterSet);
			}
		}

		// Returns the adapter type for the launch object.
		if (adapterType.equals(IElementContentProvider.class))
			return adapterSet.fViewModelAdapter;
		else if (adapterType.equals(IModelProxyFactory.class))
			return adapterSet.fViewModelAdapter;
		else if (adapterType.equals(IColumnPresentationFactory.class))
			return adapterSet.fViewModelAdapter;
		else if (adapterType.equals(ISuspendTrigger.class))
			return adapterSet.fSuspendTrigger;
		else
			return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IElementContentProvider.class, IModelProxyFactory.class, ISuspendTrigger.class,
				IColumnPresentationFactory.class };
	}

	public void launchesRemoved(ILaunch[] launches) {
		// Dispose the set of adapters for a launch only after the launch is
		// removed.
		for (ILaunch launch : launches) {
			if (launch instanceof EDCLaunch) {
				disposeAdapterSet(launch);
			}
		}
	}

	public void launchesTerminated(ILaunch[] launches) {
	}

	public void launchesAdded(ILaunch[] launches) {
	}

	public void launchesChanged(ILaunch[] launches) {
	}

}

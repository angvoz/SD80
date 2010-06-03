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
package org.eclipse.cdt.debug.edc.internal.services.dsf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.PathUtils;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2.BreakpointEventType;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2.ITargetBreakpointInfo;
import org.eclipse.cdt.dsf.debug.service.IBreakpointAttributeTranslator2;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.AddressRange;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;

public class BreakpointAttributeTranslator implements IBreakpointAttributeTranslator2 {

	private DsfServicesTracker	dsfServicesTracker;
	private DsfSession			dsfSession;
	private ITargetEnvironment	targetEnvService;
	
	public BreakpointAttributeTranslator(DsfSession dsfSession) {
		super();
		this.dsfSession = dsfSession;
		
		dsfServicesTracker = new DsfServicesTracker(EDCDebugger.getDefault().getBundle().getBundleContext(), dsfSession.getId());
		targetEnvService = dsfServicesTracker.getService(ITargetEnvironment.class);
		assert targetEnvService != null;
	}

	public boolean canUpdateAttributes(IBreakpointDMContext bp, Map<String, Object> delta) {
		/*
		 * This method decides whether we need to re-install the breakpoint
		 * based on the attributes change (refer to caller in
		 * BreakpointsMediator). For EDC, following changed attributes justify
		 * re-installation.
		 */         
        // Check if there is any modified attribute
        if (delta == null || delta.size() == 0)
            return true;

        // Check the "critical" attributes
        // TODO: threadID change
        if (delta.containsKey(IMarker.LINE_NUMBER)     // Line number
        ||  delta.containsKey(IBreakpoint.ENABLED)        // EDC don't handle enable/disable. TODO: ask ITargetEnvironment service if it can handle it. 
        ||  delta.containsKey(ICLineBreakpoint.FUNCTION)        // Function name
        ||  delta.containsKey(ICLineBreakpoint.ADDRESS)         // Absolute address
        ||  delta.containsKey(ICWatchpoint.EXPRESSION)      // Watchpoint expression
        ||  delta.containsKey(ICWatchpoint.READ)            // Watchpoint type
        ||  delta.containsKey(ICWatchpoint.WRITE)) {        // Watchpoint type
            return false;
        }

        // for other attrs (ICBreakpoint.INSTALL_COUNT, ICBreakpoint.IGNORE_COUNT,
        // ICBreakpoint.CONDITION, etc), we can update by just copying.
        return true;
	}

	public void dispose() {
		if (dsfServicesTracker != null)
			dsfServicesTracker.dispose();
		dsfSession = null;
	}

	public List<Map<String, Object>> getBreakpointAttributes(IBreakpoint bp, boolean bpManagerEnabled)
			throws CoreException {
		// The breakpoint mediator allows for multiple target-side breakpoints
		// to be created for each IDE breakpoint. But the API is not good enough.

		// obsolete 
		List<Map<String, Object>> retVal = new ArrayList<Map<String, Object>>(1);
		return retVal;
	}

	public void initialize(BreakpointsMediator2 mediator) {

	}

	public boolean supportsBreakpoint(IBreakpoint bp) {
		// We support only CDT breakpoints.
		return bp instanceof ICBreakpoint;
	}

	public void updateBreakpointStatus(IBreakpoint bp) {
		// obsolet, do nothing.
	}

	public void updateBreakpointsStatus(Map<IBreakpoint, Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]>> bpsInfo,
			BreakpointEventType eventType) {
		for (IBreakpoint bp : bpsInfo.keySet()) {
			if (! (bp instanceof ICBreakpoint))	// not C breakpoints, bail out.
				return;
			
			final ICBreakpoint icbp = (ICBreakpoint) bp;

			Map<IBreakpointsTargetDMContext, ITargetBreakpointInfo[]> targetBpPerContext = bpsInfo.get(bp);
			
			switch (eventType) {
			case ADDED: {
				int installCountTotal = 0;
				for (ITargetBreakpointInfo[] tbpInfos : targetBpPerContext.values()) {
					// For each BpTargetDMContext, we increment the installCount for each
					// target BP that has been successfully installed.
					int installCountPerContext = 0;
					for (ITargetBreakpointInfo tbp : tbpInfos) {
						if (tbp.getTargetBreakpoint() != null)
							installCountPerContext++;
					}
					installCountTotal += installCountPerContext;
				}

				for (int i=0; i < installCountTotal; i++)
					try {
						// this will eventually carried out in a workbench runnable.
						icbp.incrementInstallCount();
					} catch (CoreException e) {
						EDCDebugger.getMessageLogger().log(e.getStatus());
					}
				break;
				}
			case MODIFIED:
				break;

			case REMOVED: {
				int removeCountTotal = 0;
				for (ITargetBreakpointInfo[] tbpInfos : targetBpPerContext.values()) {
					// For each BpTargetDMContext, we decrement the installCount for each
					// target BP that we tried to remove, even if the removal failed. That's
					// because I've not seen a way to tell platform that removal fails 
					// and the BP should be kept in UI.
					removeCountTotal += tbpInfos.length;
				}

				for (int i=0; i < removeCountTotal; i++)
					try {
						if (icbp.isRegistered())	// not deleted in UI
							icbp.decrementInstallCount();
					} catch (CoreException e) {
						EDCDebugger.getMessageLogger().log(e.getStatus());
					}
				break;
				}
			}
		}
	}

	public Map<String, Object> convertAttributes(Map<String, Object> platformBPAttrDelta) {
		// For EDC, we don't need any conversion yet....11/08/09.
		return new HashMap<String, Object>(platformBPAttrDelta);
	}

    public void resolveBreakpoint(IBreakpointsTargetDMContext context, IBreakpoint breakpoint, 
    		final Map<String, Object> attributes, final DataRequestMonitor<List<Map<String, Object>>> drm) {
		
    	final List<Map<String, Object>>	targetBPAttrs = new ArrayList<Map<String, Object>>(1);

    	if (dsfSession == null) {
    		// already disposed
			drm.setData(targetBPAttrs);
			drm.done();
    	}
    	
    	Map<String, Object> oneBPAttr;
    	
		final ModuleDMC module = (ModuleDMC) context;

		String bpType = (String)attributes.get(Breakpoints.BREAKPOINT_SUBTYPE);
		
		if (bpType.equals(Breakpoints.ADDRESS_BREAKPOINT)) {
			String addr = (String)attributes.get(ICLineBreakpoint.ADDRESS);
			// This is hex string with "0x".
			assert addr != null;
			
			oneBPAttr = new HashMap<String, Object>(attributes);
			String s = addr.toLowerCase().startsWith("0x") ? addr.substring(2) : addr;
			oneBPAttr.put(Breakpoints.RUNTIME_ADDRESS, s);
			targetBPAttrs.add(oneBPAttr);

			drm.setData(targetBPAttrs);
			drm.done();
		}
		else if (bpType.equals(Breakpoints.FUNCTION_BREAKPOINT)) {
			String function = (String) attributes.get(ICLineBreakpoint.FUNCTION);
			assert (function != null && function.length() > 0);
			
			// the point is a symbol
			Symbols symService = dsfServicesTracker.getService(Symbols.class);
			List<IAddress> addrs = symService.getFunctionAddress(module, function);
			for (IAddress a : addrs) {
				oneBPAttr = new HashMap<String, Object>(attributes);
				oneBPAttr.put(Breakpoints.RUNTIME_ADDRESS, a.toString(16));
				
				targetBPAttrs.add(oneBPAttr);
			}

			drm.setData(targetBPAttrs);
			drm.done();
		}
		else {
			assert bpType.equals(Breakpoints.LINE_BREAKPOINT);
			
			String file = (String) attributes.get(ICBreakpoint.SOURCE_HANDLE);
			Integer line = (Integer) attributes.get(IMarker.LINE_NUMBER);

			final IExecutionDMContext exe_dmc = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);

			assert exe_dmc != null : "ExecutionDMContext is unknown in resolveBreakpoint().";

			Modules modulesService = dsfServicesTracker.getService(Modules.class);
			ISymbolDMContext sym_dmc = DMContexts.getAncestorOfType(context, ISymbolDMContext.class);

			file = getCompilationPath(file);	// but symbol reader has done opposite conversion ?

			modulesService.calcAddressInfo(sym_dmc, file, line, 0, 
					new DataRequestMonitor<AddressRange[]>(dsfSession.getExecutor(), drm) {

				@Override
				protected void handleFailure() {
					drm.setStatus(getStatus());
					drm.done();
				}

				@Override
				protected void handleSuccess() {
					AddressRange[] addr_ranges = getData();

					/*
					 * there could be multiple address ranges for the same
					 * source line. e.g. for templates or inlined functions. if
					 * so we need to set breakpoints on all locations
					 */
					for (AddressRange range : addr_ranges) {
						IAddress breakAddr = range.getStartAddress(); // this is runtime address

						Map<String, Object> targetAttr = new HashMap<String, Object>(attributes);
						targetAttr.put(Breakpoints.RUNTIME_ADDRESS, breakAddr.toString(16));
						
						targetBPAttrs.add(targetAttr);
					}

					drm.setData(targetBPAttrs);
					drm.done();
				}
			});
		}
	}

	public Map<String, Object> getAllBreakpointAttributes(IBreakpoint platformBP, boolean bpManagerEnabled)
			throws CoreException {
		// Check that the marker exists and retrieve its attributes.
		// Due to accepted race conditions, the breakpoint marker may become
		// null while this method is being invoked. In this case throw an exception
		// and let the caller handle it.
		IMarker marker = platformBP.getMarker();
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

		if (platformBP instanceof ICWatchpoint) {
			attrs.put(Breakpoints.BREAKPOINT_TYPE, Breakpoints.WATCHPOINT);
			/*
			 * Related Attributes attributes.get(ICWatchpoint.EXPRESSION));
			 * attributes.get(ICWatchpoint.READ));
			 * attributes.get(ICWatchpoint.WRITE));
			 */
		} else if (platformBP instanceof ICLineBreakpoint) {
			attrs.put(Breakpoints.BREAKPOINT_TYPE, Breakpoints.BREAKPOINT);

			String file = (String) attrs.get(ICBreakpoint.SOURCE_HANDLE);
			String address = (String) attrs.get(ICLineBreakpoint.ADDRESS);
			String function = (String) attrs.get(ICLineBreakpoint.FUNCTION);
			Integer line = (Integer) attrs.get(IMarker.LINE_NUMBER);

			if (address != null && address.length() > 0)
				attrs.put(Breakpoints.BREAKPOINT_SUBTYPE, Breakpoints.ADDRESS_BREAKPOINT);
			else if (function != null && function.length() > 0)
				attrs.put(Breakpoints.BREAKPOINT_SUBTYPE, Breakpoints.FUNCTION_BREAKPOINT);
			else {
				assert file != null && file.length() > 0 && line != null;
				attrs.put(Breakpoints.BREAKPOINT_SUBTYPE, Breakpoints.LINE_BREAKPOINT);
			}
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

		return attrs;
	}

	public boolean canUpdateAttributes(IBreakpoint bp, IBreakpointsTargetDMContext context, Map<String, Object> attrDelta) {
		// no special handling needed for EDC yet.
		return canUpdateAttributes(null, attrDelta);
	}

	private String getCompilationPath(String sourceHandle) {
		IPath path = Path.EMPTY;
		if (Path.EMPTY.isValidPath(sourceHandle)) {
			sourceHandle = PathUtils.convertPathToNative(sourceHandle);
			ISourceLocator sl = targetEnvService.getLaunch().getSourceLocator();
			if (sl instanceof CSourceLookupDirector) {
				path = ((CSourceLookupDirector) sl).getCompilationPath(sourceHandle);
			}
			if (path == null) {
				path = PathUtils.findExistingPathIfCaseSensitive(new Path(sourceHandle));
			}
		}
		return path.toOSString();
	}
}
 
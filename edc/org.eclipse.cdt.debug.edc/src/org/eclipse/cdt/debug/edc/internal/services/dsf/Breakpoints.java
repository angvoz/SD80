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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.EDCTrace;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleLoadedEvent;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleUnloadedEvent;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.launch.EDCLaunch;
import org.eclipse.cdt.debug.edc.services.AbstractEDCService;
import org.eclipse.cdt.debug.edc.services.DMContext;
import org.eclipse.cdt.debug.edc.services.IDSFServiceUsingTCF;
import org.eclipse.cdt.debug.edc.services.IEDCDMContext;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.debug.edc.services.Stack;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants.IModuleProperty;
import org.eclipse.cdt.debug.internal.core.breakpoints.BreakpointProblems;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.BreakpointsMediator2;
import org.eclipse.cdt.dsf.debug.service.IBreakpointAttributeTranslator;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.ModuleLoadedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IModules.ModuleUnloadedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints.DoneCommand;

public class Breakpoints extends AbstractEDCService implements IBreakpoints, IDSFServiceUsingTCF {

	/**
	 * Breakpoint attributes markers used in the map parameters of
	 * insert/updateBreakpoint(). All are optional with the possible exception
	 * of TYPE. It is the responsibility of the
	 * {@link IBreakpointAttributeTranslator} to ensure that the set of
	 * attributes provided is sufficient to create/update a valid breakpoint on
	 * the back-end.
	 */
	public static final String PREFIX = "org.eclipse.cdt.debug.edc.breakpoint"; //$NON-NLS-1$

	// Our own attribute keys.
	//
	/**
	 * Breakpoint type: value is string.
	 */
	public static final String BREAKPOINT_TYPE = PREFIX + ".type"; //$NON-NLS-1$
		// type values:
		public static final String BREAKPOINT = "breakpoint"; //$NON-NLS-1$
		public static final String WATCHPOINT = "watchpoint"; //$NON-NLS-1$
		public static final String CATCHPOINT = "catchpoint"; //$NON-NLS-1$

	/**
	 * breakponint sub-type: value is string.
	 */
	public static final String BREAKPOINT_SUBTYPE = PREFIX + ".subtype"; //$NON-NLS-1$
		// sub-type values:
		public static final String LINE_BREAKPOINT = "line_bp"; //$NON-NLS-1$
		public static final String FUNCTION_BREAKPOINT = "function_bp"; //$NON-NLS-1$
		public static final String ADDRESS_BREAKPOINT = "address_bp"; //$NON-NLS-1$
	
	/**
	 * breakpoint runtime address: value is hex string with no preceding "0x". 
	 */
	public static final String RUNTIME_ADDRESS = PREFIX + ".runtime_addr";

	// Error messages
	static final String NULL_STRING = ""; //$NON-NLS-1$
	static final String UNKNOWN_EXECUTION_CONTEXT = "Unknown execution context"; //$NON-NLS-1$
	static final String UNKNOWN_BREAKPOINT_CONTEXT = "Unknown breakpoint context"; //$NON-NLS-1$
	static final String UNKNOWN_BREAKPOINT_TYPE = "Unknown breakpoint type"; //$NON-NLS-1$
	static final String UNKNOWN_BREAKPOINT = "Unknown breakpoint"; //$NON-NLS-1$
	static final String BREAKPOINT_INSERTION_FAILURE = "Breakpoint insertion failure"; //$NON-NLS-1$
	static final String WATCHPOINT_INSERTION_FAILURE = "Watchpoint insertion failure"; //$NON-NLS-1$
	static final String INVALID_CONDITION = "Invalid condition"; //$NON-NLS-1$

	// User breakpoints (those from the IDE) currently installed.
	private final Map<IBreakpointDMContext, BreakpointDMData> userBreakpoints = new HashMap<IBreakpointDMContext, BreakpointDMData>();

	/**
	 * Internal temporary breakpoints set by debugger for stepping.
	 */
	private final List<BreakpointDMData> tempBreakpoints = new ArrayList<BreakpointDMData>();

	private org.eclipse.tm.tcf.services.IBreakpoints tcfBreakpointService;

	// Module in which startup breakpoint is installed for the debug session.
	private Map<ILaunchConfiguration, ModuleDMC> startupBreakpointModule = new HashMap<ILaunchConfiguration, ModuleDMC>();

	private ISourceLocator sourceLocator;

	/**
	 * Breakpoint problem markers added by EDC. Note these markers have life-span
	 * of a debug session, namely they are removed at end of a debug session or
	 * removed when corresponding breakpoint is removed by user.
	 * 
	 * Currently the markers are only used for breakpoint conditions, though it should
	 * be easy to extend the scope.....06/07/2011.
	 */
    private Map<IBreakpointDMContext, IMarker> fBreakpointMarkers = new HashMap<IBreakpointDMContext, IMarker>();

	static private long nextBreakpointID = 1;

	// /////////////////////////////////////////////////////////////////////////
	// Breakpoint Events
	// /////////////////////////////////////////////////////////////////////////

	public class BreakpointsChangedEvent extends AbstractDMEvent<IBreakpointsTargetDMContext> implements
			IBreakpointsChangedEvent {
		private IBreakpointDMContext[] eventBreakpoints;

		public BreakpointsChangedEvent(IBreakpointDMContext bp) {
			super(DMContexts.getAncestorOfType(bp, IBreakpointsTargetDMContext.class));
			eventBreakpoints = new IBreakpointDMContext[] { bp };
		}

		public IBreakpointDMContext[] getBreakpoints() {
			return eventBreakpoints;
		}
	}

	public class BreakpointAddedEvent extends BreakpointsChangedEvent implements IBreakpointsAddedEvent {
		public BreakpointAddedEvent(IBreakpointDMContext context) {
			super(context);
		}
	}

	public class BreakpointUpdatedEvent extends BreakpointsChangedEvent implements IBreakpointsUpdatedEvent {
		public BreakpointUpdatedEvent(IBreakpointDMContext context) {
			super(context);
		}
	}

	public class BreakpointRemovedEvent extends BreakpointsChangedEvent implements IBreakpointsRemovedEvent {
		public BreakpointRemovedEvent(IBreakpointDMContext context) {
			super(context);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// IBreakpointDMContext
	// /////////////////////////////////////////////////////////////////////////
	@Immutable
	public static final class BreakpointDMContext extends DMContext implements IBreakpointDMContext {
		public BreakpointDMContext(String sessionID, IDMContext[] parents, long id) {
			super(sessionID, parents, Long.toString(id));
		}

		@Override
		public String toString() {
			return "BreakpointDMContext [id=" + getID() + "]";
		}
	}

	public class BreakpointDMData implements IBreakpointDMData {

		private final long id; // internal ID.
		private final IBreakpointDMContext context;
		private final IAddress[] addresses;
		private final byte[] originalInstruction;
		private Map<String, Object> properties;
		private int hitCount;

		public BreakpointDMData(long id, IBreakpointDMContext context, IAddress[] addresses,
				Map<String, Object> properties) {
			super();
			this.id = id;
			this.context = context;
			this.addresses = addresses;
			this.originalInstruction = null;
			this.properties = new HashMap<String, Object>(properties); // make a  copy
		}

		public BreakpointDMData(long id, IBreakpointDMContext context, IAddress[] addresses,
				byte[] fOriginalInstruction, Map<String, Object> properties) {
			super();
			this.id = id;
			this.context = context;
			this.addresses = addresses;
			this.originalInstruction = fOriginalInstruction;
			this.properties = new HashMap<String, Object>(properties);
		}

		public IAddress[] getAddresses() {
			return addresses;
		}

		public String getBreakpointType() {
			return (String) properties.get(BREAKPOINT_TYPE);
		}

		public String getCondition() {
			return (String) properties.get(ICBreakpoint.CONDITION);
		}

		public String getExpression() {
			return (String) properties.get(ICWatchpoint.EXPRESSION);
		}

		public String getFileName() {
			return (String) properties.get(ICBreakpoint.SOURCE_HANDLE);
		}

		public String getFunctionName() {
			return (String) properties.get(ICLineBreakpoint.FUNCTION);
		}

		public int getIgnoreCount() {
			return (Integer) properties.get(ICBreakpoint.IGNORE_COUNT);
		}

		public int getLineNumber() {
			return (Integer) properties.get(IMarker.LINE_NUMBER);
		}

		public boolean isEnabled() {
			return (Boolean) properties.get(IBreakpoint.ENABLED);
		}

		public long getID() {
			return id;
		}

		public byte[] getOriginalInstruction() {
			return originalInstruction;
		}

		/**
		 * @return reference to properties map of the bp.
		 */
		public Map<String, Object> getProperties() {
			return properties;
		}

		public IBreakpointDMContext getContext() {
			return context;
		}

		public void setProperties(Map<String, Object> props) {
			properties = new HashMap<String, Object>(props);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (id ^ (id >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BreakpointDMData other = (BreakpointDMData) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id != other.id)
				return false;
			return true;
		}

		private Breakpoints getOuterType() {
			return Breakpoints.this;
		}

		@Override
		public String toString() {
			String s = getFileName();
			if (s == null) // address breakpoint
				s = getAddresses()[0].toHexAddressString();
			else {
				if (getFunctionName() != null)
					s += ": " + getFunctionName();
				else
					s += ":line " + getLineNumber();
			}
			return "Breakpoint@" + s;
		}

		public void incrementHitCount() {
			hitCount++;
		}
		
		public int getHitCount()
		{
			return hitCount;
		}
	}

	public Breakpoints(DsfSession session) {
		super(session, new String[] { IBreakpoints.class.getName(), Breakpoints.class.getName() });
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new RequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				// Register as event listener.
				getSession().addServiceEventListener(Breakpoints.this, null);
				rm.done();
			}
		});
	}

	public void getBreakpointDMData(IBreakpointDMContext dmc, DataRequestMonitor<IBreakpointDMData> drm) {
		if (!userBreakpoints.containsKey(dmc)) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, UNKNOWN_BREAKPOINT));
		} else
			drm.setData(userBreakpoints.get(dmc));

		drm.done();
	}

	public void getBreakpoints(IBreakpointsTargetDMContext context, DataRequestMonitor<IBreakpointDMContext[]> drm) {
		Set<IBreakpointDMContext> breakpointIDs = userBreakpoints.keySet();
		drm.setData(breakpointIDs.toArray(new IBreakpointDMContext[breakpointIDs.size()]));
		drm.done();
	}

	/**
	 * Find breakpoint, either user-set or debugger internal temporary one, at
	 * the given address.
	 * 
	 * @param addr
	 *            - absolute runtime address.
	 * @return null if not found.
	 */
	public BreakpointDMData findBreakpoint(IAddress addr) {
		BreakpointDMData bp = findUserBreakpoint(addr);
		if (bp == null)
			bp = findTempBreakpoint(addr);
		return bp;
	}

	/**
	 * Find user breakpoint at the given address.
	 * 
	 * @param addr
	 *            - absolute runtime address.
	 * @return null if not found.
	 */
	public BreakpointDMData findUserBreakpoint(IAddress addr) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, "Find user breakpoint at " + addr.toHexAddressString()); }

		for (BreakpointDMData bp : userBreakpoints.values())
			if (bp.getAddresses()[0].equals(addr)) {
				if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArg(bp.toString())); }
				return bp;
			}

		if (EDCTrace.BREAKPOINTS_TRACE_ON) {EDCTrace.getTrace().traceExit(null, "not found.");}
		return null;
	}

	/**
	 * Find a temporary breakpoint at the given address.
	 * 
	 * @param addr
	 *            - absolute runtime address.
	 * @return null if not found.
	 */
	public BreakpointDMData findTempBreakpoint(IAddress addr) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, "Find temp breakpoint at " + addr.toHexAddressString()); }

		for (BreakpointDMData bp : tempBreakpoints) {
			if (bp.getAddresses()[0].equals(addr)) {
				if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArg(bp.toString())); }
				return bp;
			}
		}

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null, "not found."); }
		return null;
	}

	/**
	 * Remove software breakpoints inserted in memory by debugger from the given
	 * memory buffer starting from given address.
	 * 
	 * @param startAddr
	 *            start address of the memory data.
	 * @param memBuffer
	 *            a buffer containing data from memory. Its content will be
	 *            changed by this method if a breakpoint falls in the address
	 *            range.
	 */
	public void removeBreakpointFromMemoryBuffer(IAddress startAddr, MemoryByte[] memBuffer) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, "remove bp in memory area:" + startAddr.toHexAddressString() + "," + memBuffer.length); }

		// If the breakpoint is actually set by TCF agent, we have to assume
		// that the TCF agent would do this breakpoint removing for us as
		// we have no idea how the agent set the breakpoint (e.g. is it software
		// breakpoint ? if yes, what breakpoint instruction is used ?)
		//
		if (usesTCFBreakpointService())
			return;

		for (BreakpointDMData edcBp : userBreakpoints.values()) {
			// TODO: bail out if the bp is not software breakpoint.

			IAddress bpAddr = edcBp.getAddresses()[0];
			int bpOffset = (int) startAddr.distanceTo(bpAddr).longValue();
			if (bpOffset >= 0 && bpOffset < memBuffer.length) {
				// the breakpoint falls in the buffer. Restore the original
				// instruction.
				byte[] orgInst = edcBp.getOriginalInstruction();
				for (int i = 0; i < orgInst.length && i + bpOffset < memBuffer.length; i++) {
					memBuffer[bpOffset + i].setValue(orgInst[i]);
				}

				if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().trace(null, "breakpoint removed at offset " + bpOffset); }
			}
		}

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	public void insertBreakpoint(IBreakpointsTargetDMContext context, Map<String, Object> attributes,
			DataRequestMonitor<IBreakpointDMContext> drm) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { attributes })); }

		// Validate the context
		if (context == null) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_EXECUTION_CONTEXT,
					null));
			drm.done();
			return;
		}

		// Validate the breakpoint type
		String type = (String) attributes.get(BREAKPOINT_TYPE);
		if (type == null) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE,
					null));
			drm.done();
			return;
		}

		// And go...
		if (type.equals(BREAKPOINT)) {
			addBreakpoint(context, attributes, drm);
		} else if (type.equals(WATCHPOINT)) {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
					"Watchpoint is not supported yet.", null));
			drm.done();
		} else {
			drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED, UNKNOWN_BREAKPOINT_TYPE,
					null));
			drm.done();
		}

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	/**
	 * Set one target breakpoint.
	 * 
	 * @param context
	 * @param attributes
	 *            attributes for the target breakpoint. For EDC, it must contain
	 *            the RUNTIME_ADDRESS attribute.
	 * @param drm
	 */
	private void addBreakpoint(final IBreakpointsTargetDMContext context, final Map<String, Object> attributes,
			final DataRequestMonitor<IBreakpointDMContext> drm) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { attributes })); }

		IExecutionDMContext exe_dmc = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
		String bpAddr = (String)attributes.get(RUNTIME_ADDRESS);
		
		assert exe_dmc != null : "ExecutionDMContext is unknown in addBreakpoint().";
		assert bpAddr != null;
		
		createBreakpoint(exe_dmc, new Addr64(bpAddr, 16), attributes, new DataRequestMonitor<BreakpointDMData>(
				getExecutor(), drm) {

			@Override
			protected void handleSuccess() {
				final BreakpointDMData bpd = getData();

				enableBreakpoint(bpd, new RequestMonitor(getExecutor(), drm) {

					@Override
					protected void handleSuccess() {
						IBreakpointDMContext bp_dmc = bpd.getContext();
						drm.setData(bp_dmc);

						// Remember this in our global list.
						userBreakpoints.put(bp_dmc, bpd);

						drm.done();
					}
				});
			}
		});

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	public ISourceLocator getSourceLocator() {
		return sourceLocator;
	}

	public void setSourceLocator(ISourceLocator sourceLocator) {
		this.sourceLocator = sourceLocator;
	}

	/**
	 * Set temporary breakpoint at given address. This is for cases such as
	 * stepping and initial startup breakpoint (aka entry breakpoint).<br>
	 * If a user or temporary breakpoint already exists at the address, no
	 * temporary breakpoint will be set.
	 * 
	 * @param context
	 * @param address
	 * @param rm
	 */
	public void setTempBreakpoint(final IExecutionDMContext context, final IAddress address, final RequestMonitor rm) {

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, "set temp breakpoint at " + address.toHexAddressString()); }

		// If a breakpoint (user-set or temp) exists at the address, we are
		// done.
		if (findBreakpoint(address) != null) {
			rm.done();
			if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null, "A breakpoint exists at " + address.toHexAddressString()); }
			return;
		}

		createBreakpoint(context, address, new HashMap<String, Object>(), new DataRequestMonitor<BreakpointDMData>(
				getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				final BreakpointDMData bp_data = getData();

				enableBreakpoint(bp_data, new RequestMonitor(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						// Remember this in our list.
						tempBreakpoints.add(bp_data);
						rm.done();

						if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null, EDCTrace.fixArg("A temp breakpoint successfully set at " + address.toHexAddressString())); }
					}
				});
			}
		});
	}

	/**
	 * Remove all temporary breakpoints set so far.
	 * 
	 * @param rm
	 */
	public void removeAllTempBreakpoints(final RequestMonitor rm) {

		int numTempBps = tempBreakpoints.size();
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, "remove " + numTempBps + " temp breakpoint" + ((numTempBps == 1)?"":"s") + "."); }

		if (numTempBps == 0) {
			rm.done();
			return;
		}

		CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleCompleted() {
				if (getStatus().isOK()) {
					tempBreakpoints.clear();
				}
				super.handleCompleted();
			}
		};

		crm.setDoneCount(tempBreakpoints.size());

		for (BreakpointDMData bp : tempBreakpoints)
			disableBreakpoint(bp, crm);
	}

	private void createBreakpoint(final IExecutionDMContext exeDMC, final IAddress address, final Map<String, Object> props,
			final DataRequestMonitor<BreakpointDMData> drm) {

		asyncExec(new Runnable() {
			public void run() {
				final long id = getNewBreakpointID();
				final IBreakpointDMContext bp_dmc = new BreakpointDMContext(getSession().getId(), new IDMContext[] { exeDMC },
						id);
				final IAddress[] bp_addrs = new IAddress[] { address };
				final Map<String, Object> properties = new HashMap<String, Object>(props);

				if (usesTCFBreakpointService()) {
					properties.put(org.eclipse.tm.tcf.services.IBreakpoints.PROP_ID, Long.toString(id));
					properties.put(org.eclipse.tm.tcf.services.IBreakpoints.PROP_ENABLED, true);
					properties.put(org.eclipse.tm.tcf.services.IBreakpoints.PROP_TYPE,
							org.eclipse.tm.tcf.services.IBreakpoints.TYPE_AUTO);
					properties.put(org.eclipse.tm.tcf.services.IBreakpoints.PROP_LOCATION, address.toString());


					// Pass "contexts" for which the BP is supposed to work.
					// NOTE: EDC extension to TCF: 
					//  TCF only define "IBreakpoints.PROP_CONTEXTIDS" as a array of IDs. 
					//  In EDC, it's required the first element in the array be IBreakpointsTargetDMContext
					//  which is usually (but not always) a process. This makes EDC backward compatible with
					//  some existing TCF agents.
					IBreakpointsTargetDMContext bpTargetDMC = DMContexts.getAncestorOfType(exeDMC, IBreakpointsTargetDMContext.class);
					// pass "exeDMC" as a context if it's different from bpTargetDMC, say, if "exeDMC" is a thread and
					// the "bpTargetDMC" is the owner process. This allows for thread-specific BP if agent supports it.
					String[] contexts;
					if (! exeDMC.equals(bpTargetDMC))
						contexts = new String[] {((IEDCDMContext)bpTargetDMC).getID(), ((IEDCDMContext)exeDMC).getID()};
					else 
						contexts = new String[] {((IEDCDMContext)bpTargetDMC).getID()};
					properties.put(org.eclipse.tm.tcf.services.IBreakpoints.PROP_CONTEXTIDS, contexts);

					getTargetEnvironmentService().updateBreakpointProperties(exeDMC, address, properties);

					drm.setData(new BreakpointDMData(id, bp_dmc, bp_addrs, properties));
					drm.done();
				} else { // generic software breakpoint
					final byte[] bpInstruction = getTargetEnvironmentService().getBreakpointInstruction(exeDMC, address);
					final int inst_size = bpInstruction.length;

					Memory memoryService = getService(Memory.class);
					IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(exeDMC, IMemoryDMContext.class);

					memoryService.getMemory(mem_dmc, address, 0, 1, inst_size, new DataRequestMonitor<MemoryByte[]>(
							getExecutor(), drm) {
						@Override
						protected void handleSuccess() {
							MemoryByte[] org_inst = getData();
							final byte[] org_inst_bytes = new byte[org_inst.length];
							for (int i = 0; i < org_inst.length; i++) {
								// make sure each byte is okay
								if (!org_inst[i].isReadable()) {
									drm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
												("Cannot read memory at 0x" + address.add(i).getValue().toString(16)),
												null));
									drm.done();
									return;
								}
								org_inst_bytes[i] = org_inst[i].getValue();
							}

							drm.setData(new BreakpointDMData(id, bp_dmc, bp_addrs, org_inst_bytes, properties));
							drm.done();
						}
					});
				}
			}
			
		}, drm);

	}

	/**
	 * Install the breakpoint in the target process.
	 * 
	 * @param bp
	 * @param rm
	 */
	public void enableBreakpoint(final BreakpointDMData bp, final RequestMonitor rm) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArg(bp)); }
		
		if (usesTCFBreakpointService()) {
			Protocol.invokeLater(new Runnable() {
				public void run() {
					tcfBreakpointService.add(bp.getProperties(), new DoneCommand() {

						public void doneCommand(IToken token, Exception error) {
							if (error != null) {
								// Make sure "done()" is called for the rm.
								rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, REQUEST_FAILED,
										"TCF agent fails to install " + bp + " because:\n"
												+ error.getLocalizedMessage(), error));
								rm.done();
							} else {
								getSession().dispatchEvent(new BreakpointAddedEvent(bp.getContext()),
										new Hashtable<String, Object>(bp.getProperties()));

								rm.done();
							}
						}
					});
				}
			});
		} else {
			IAddress bp_addr = bp.getAddresses()[0];
			IExecutionDMContext exe_dmc = DMContexts.getAncestorOfType(bp.getContext(), IExecutionDMContext.class);
			byte[] bpInstruction = getTargetEnvironmentService().getBreakpointInstruction(exe_dmc, bp_addr);

			Memory memoryService = getService(Memory.class);
			IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(bp.getContext(), IMemoryDMContext.class);

			memoryService.setMemory(mem_dmc, bp_addr, 0, 1, bpInstruction.length, bpInstruction, rm);
			getSession().dispatchEvent(new BreakpointAddedEvent(bp.getContext()),
					new Hashtable<String, Object>(bp.getProperties()));
		}

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	private synchronized long getNewBreakpointID() {
		return nextBreakpointID++;
	}

	public void removeBreakpoint(final IBreakpointDMContext dmc, RequestMonitor rm) {
		// Remove user breakpoint in the target. 
		// This is called when user remove a breakpoint or when debug session ends.
		//
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { dmc })); }

		if (!(dmc instanceof BreakpointDMContext)) {
			// not our breakpoint, should not happen
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, "Unrecognized breakpoint context."));
			rm.done();
			return;
		}

		if (!userBreakpoints.containsKey(dmc)) {
			rm.setStatus(new Status(IStatus.ERROR, EDCDebugger.PLUGIN_ID, UNKNOWN_BREAKPOINT));
			rm.done();
			return;
		}

		disableBreakpoint(userBreakpoints.get(dmc), new RequestMonitor(getExecutor(), rm) {

			@Override
			protected void handleCompleted() {
				// Regardless of success or failure of removing the bp on target, we'll do following.
				//
				// If user removes a breakpoint in UI, the platform won't keep the BP in UI just because 
				// of failure in removing the bp on target. However, any error passed by the "rm" will
				// be displayed in Error Log view. 
			
				// Remove it from our record.
				userBreakpoints.remove(dmc);

				// Remove problem marker if any. 
				// Note this may be called when the debug session is shut down.
				removeBreakpointProblemMarker(dmc);
				
				super.handleCompleted();
			}
		});

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	/**
	 * Remove the breakpoint from the target process.
	 * 
	 * @param bp
	 * @param rm
	 */
	public void disableBreakpoint(final BreakpointDMData bp, final RequestMonitor rm) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { bp })); } 

		if (!usesTCFBreakpointService()) {
			final Memory memoryService = getService(Memory.class);
			IMemoryDMContext mem_dmc = DMContexts.getAncestorOfType(bp.getContext(), IMemoryDMContext.class);
			byte[] orgInst = bp.getOriginalInstruction();
			memoryService.setMemory(mem_dmc, bp.getAddresses()[0], 0, 1, orgInst.length, orgInst, rm);
		} else {
			Protocol.invokeLater(new Runnable() {
				public void run() {
					Map<String, Object> properties = bp.getProperties();
					String id = (String) properties.get(org.eclipse.tm.tcf.services.IBreakpoints.PROP_ID);
					tcfBreakpointService.remove(new String[] { id }, new DoneCommand() {

						public void doneCommand(IToken token, Exception error) {
							rm.done();
						}
					});
				}
			});
		}

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	public void updateBreakpoint(IBreakpointDMContext dmc, Map<String, Object> delta, RequestMonitor rm) {
		/*
		 * For EDC, we don't need to do any update on non-significant attribute
		 * change, e.g. change of Install_count, ignore_count. For significant
		 * change, the breakpoint will just be re-installed. 
		 * See canUpdateAttributes().
		 */
		BreakpointDMData bp = userBreakpoints.get(dmc);
		if (bp == null)
			assert false : "Fail to find BreakpointDMData linked with the IBreakpointDMContext:" + dmc;
		else {
			Map<String, Object> existingProps = bp.getProperties();
			for (String key : delta.keySet())
				existingProps.put(key, delta.get(key));
		}
		rm.done();
	}

	public boolean usesTCFBreakpointService() {
		return tcfBreakpointService != null;
	}

	public void tcfServiceReady(IService service) {
		tcfBreakpointService = (org.eclipse.tm.tcf.services.IBreakpoints) service;
	}

	@DsfServiceEventHandler
	public void eventHandler_installBreakpointsForModule(ModuleLoadedDMEvent e) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { e })); }

		// A new module (including main exe) is loaded. Install breakpoints for
		// it.
		ModuleLoadedEvent event = (ModuleLoadedEvent) e;
		final IExecutionDMContext executionDMC = event.getExecutionDMC();
		final ModuleDMC module = (ModuleDMC) e.getLoadedModuleContext();
		BreakpointsMediator2 bm = getService(BreakpointsMediator2.class);
		if (bm == null) {
			EDCDebugger.getMessageLogger().logError("Fail to get BreakpointsMediator service to install breakpoints for loaded module "+module, null);
			assert false;
			return;
		}
		
		final boolean requireResume	= requireResume(module);
	
		IBreakpointsTargetDMContext bt_dmc = DMContexts.getAncestorOfType(module, IBreakpointsTargetDMContext.class);
		bm.startTrackingBreakpoints(bt_dmc, new RequestMonitor(getExecutor(), null) {

			@Override
			protected void handleCompleted() {
				if (!isSuccess()) {
					// do we want to display a dialog for user ?
					// No, as it's expected not all breakpoints can be resolved
					// in the module.

					// Form readable message and log it.
					IStatus status = getStatus();
					String msg = MessageFormat.format(
							"Failed to install some breakpoints in the module [{0}]. Errors: \n", module.getName());
					if (status.isMultiStatus()) {
						for (IStatus s : ((MultiStatus) status).getChildren())
							msg += s.getMessage() + "\n";
					} else
						msg += status.getMessage();

					if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().trace(null, msg); }
				}

				// We should do these regardless of whether installing
				// breakpoints succeeded or not.
				setStartupBreakpoint(module, new RequestMonitor(getExecutor(), null) {

					@Override
					protected void handleCompleted() {
						// do this regardless of status of installing entry
						// breakpoint
						// as it's expected the startup bp not resolvable in all
						// modules.
						//
						if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().trace(null, "resume process after module load event ..."); }
						if (requireResume)
							((ExecutionDMC) executionDMC).resume(new RequestMonitor(getExecutor(), null));
					}
				});
			}
		});

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	/**
	 * Check if resume is required after handling load/unload of the given module.
	 * 
	 * @param module
	 * @return
	 */
	private boolean requireResume(ModuleDMC module) {
		boolean requireResume = true;
		Object propvalue = module.getProperties().get(IModuleProperty.PROP_RESUME);
		if (propvalue != null)
			if (propvalue instanceof Boolean)
				requireResume = (Boolean) propvalue;

		return requireResume;
	}

	/**
	 * Set breakpoint at startup point specified by user.
	 * 
	 * @param module
	 * @param rm
	 */
	protected void setStartupBreakpoint(ModuleDMC module, RequestMonitor rm) {
		EDCLaunch launch = EDCLaunch.getLaunchForSession(getSession().getId());
		ILaunchConfiguration launchConfig = launch.getLaunchConfiguration();
		if (startupBreakpointModule.get(launchConfig) != null) {
			// already set in a module for this launch configuration, no need to try it for any other module
			rm.done();
			return;
		}

		String startupStopAt = launch.getStartupStopAtPoint();
		// Is this even requested by user ?
		if (startupStopAt == null) {
			rm.done();
			return;
		}

		// Ask target environment whether it wants us to try installing
		// startup breakpoint in the module.
		ITargetEnvironment te = getTargetEnvironmentService();
		if (! te.needStartupBreakpointInExecutable(module.getName())) {
			rm.done();
			return;
		}

		IAddress iaddr = null;
		long address = 0;

		// Check if the point is absolute runtime address.
		//
		try {
			// first check if it's decimal number
			address = Long.parseLong(startupStopAt);
		} catch (NumberFormatException e) {
			// then check if it's hex
			if (startupStopAt.toLowerCase().startsWith("0x")) {
				try {
					address = Long.parseLong(startupStopAt.substring(2), 16);
				} catch (IllegalFormatException e1) {
					// ignore
				}
			}
		}

		if (address != 0) {
			iaddr = new Addr32(address);
			
			// Assume it is a link-time address first.  Run-time addresses are not predictable across launches.
			IAddress runAddr = module.toRuntimeAddress(iaddr);
			if (module.containsAddress(runAddr)) {
				iaddr = runAddr;
			} else {
				// Try for a runtime address.
				if (!module.containsAddress(iaddr)) {
					// address not in the module, don't bother.
					// This is to ensure the address breakpoint is installed
					// after the container module is loaded. 
					iaddr = null;
				}
			}
		} else {
			// the point is a symbol
			Symbols symService = getService(Symbols.class);
			List<IAddress> addrs = symService.getFunctionAddress(module, startupStopAt);
			
			if (addrs.size() > 0)
				// just choose the first one
				iaddr = addrs.get(0);
		}

		if (iaddr == null) {
			EDCDebugger.getMessageLogger().logError(
					"Could not resolve startup breakpoint: "+ startupStopAt, null);
			rm.done();
		} else {
			// The breakpoint is resolved in the module.
			startupBreakpointModule.put(launchConfig, module);

			IExecutionDMContext exe_dmc = DMContexts.getAncestorOfType(module, IExecutionDMContext.class);
			setTempBreakpoint(exe_dmc, iaddr, rm);
		}
	}

	@DsfServiceEventHandler
	public void eventHandler_uninstallBreakpointsForModule(ModuleUnloadedDMEvent e) {
		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceEntry(null, EDCTrace.fixArgs(new Object[] { e.getClass().getName(), e })); }

		// An existing module (including main exe) is unloaded. Uninstall
		// breakpoints for it.
		ModuleUnloadedEvent event = (ModuleUnloadedEvent) e;
		final ExecutionDMC executionDMC = (ExecutionDMC)event.getExecutionDMC();
		final ModuleDMC module = (ModuleDMC) e.getUnloadedModuleContext();

		/*
		 * If module containing startup break is unloaded, mark the startup
		 * break as gone so that we can reinstall it the next time the module is
		 * loaded again in the same debug session.
		 */
		if (startupBreakpointModule != null &&
			startupBreakpointModule.equals(module))
			startupBreakpointModule = null;
		
		final boolean requireResume	= requireResume(module);
		
		BreakpointsMediator2 bm = getService(BreakpointsMediator2.class);
		IBreakpointsTargetDMContext bt_dmc = DMContexts.getAncestorOfType(e.getUnloadedModuleContext(),
				IBreakpointsTargetDMContext.class);
		bm.stopTrackingBreakpoints(bt_dmc, new RequestMonitor(getExecutor(), null) {
			@Override
			protected void handleFailure() {
				// super will just log the error.
				super.handleFailure();
				if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().trace(null, "uninstalling breakpoints failed"); }
			}

			@Override
			protected void handleSuccess() {
				super.handleSuccess();
				if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().trace(null, "breakpoints uninstalled and resume process..."); }
				if (requireResume)
					executionDMC.resume(new RequestMonitor(getExecutor(), null));
			}
		});

		if (EDCTrace.BREAKPOINTS_TRACE_ON) { EDCTrace.getTrace().traceExit(null); }
	}

	protected void addBreakpointProblemMarker(final IBreakpointDMContext targetBP,
			final String description, final int severity) {
		BreakpointsMediator2 bmService = getService(BreakpointsMediator2.class);
		if (bmService == null)
			return;
		
		final IBreakpoint breakpoint = bmService.getPlatformBreakpoint(null, targetBP);
		if (breakpoint == null)
			return;

        if (! (breakpoint instanceof ICLineBreakpoint))
        	return;

        new Job("Add Breakpoint Problem Marker") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
            	// If we have already have a problem marker on this breakpoint
            	// we should remove it first.
                IMarker marker = fBreakpointMarkers.remove(targetBP);
                if (marker != null) {
                    try {
                        marker.delete();
                    } catch (CoreException e) {
                    }
            	}

                ICLineBreakpoint lineBreakpoint = (ICLineBreakpoint) breakpoint;
                try {
                    // Locate the workspace resource via the breakpoint marker
                    IMarker breakpoint_marker = lineBreakpoint.getMarker();
                    IResource resource = breakpoint_marker.getResource();

                    // Add a problem marker to the resource
                    IMarker problem_marker = resource.createMarker(BreakpointProblems.BREAKPOINT_PROBLEM_MARKER_ID);
                    int line_number = lineBreakpoint.getLineNumber();
                    problem_marker.setAttribute(IMarker.LOCATION,    String.valueOf(line_number));
                    problem_marker.setAttribute(IMarker.MESSAGE,     description);
                    problem_marker.setAttribute(IMarker.SEVERITY,    severity);
                    problem_marker.setAttribute(IMarker.LINE_NUMBER, line_number);

                    // And save the baby
                    fBreakpointMarkers.put(targetBP, problem_marker);
                } catch (CoreException e) {
                }
                
                return Status.OK_STATUS;
            }
        }.schedule();
	}
	
	/**
	 * Remove problem marker added for the given target breakpoint.
	 * Note this may be called when debug session is shutdown.
	 *  
	 * @param breakpoint
	 */
    protected void removeBreakpointProblemMarker(final IBreakpointDMContext breakpoint) {

        final IMarker marker = fBreakpointMarkers.remove(breakpoint);
        if (marker == null)
        	return;
        
        new Job("Remove Breakpoint Problem Marker") { //$NON-NLS-1$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    marker.delete();
                } catch (CoreException e) {
                }

                return Status.OK_STATUS;
            }
        }.schedule();
    }

	/**
	 * Evaluate condition of given breakpoint, if any.
	 * 
	 * @param context 
	 *			  execution context in which to evaluate the condition.
	 * @param bp
	 *            the breakpoint.
	 * @param drm
	 *            DataRequestMonitor that contains result indicating whether
	 *            to stop execution of debugged program. The result value is
	 *            true if <br>
	 *            1. the breakpoint has no condition, or <br>
	 *            2. the breakpoint condition is invalid in syntax, or <br>
	 *            3. the breakpoint condition cannot be resolved, or <br>
	 *            4. the breakpoint condition is true.<br>
	 *            Otherwise the result in the drm is false.
	 * 
	 */
	public void evaluateBreakpointCondition(IExecutionDMContext context, final BreakpointDMData bp, final DataRequestMonitor<Boolean> drm) {
		final String expr = bp.getCondition();
		if (expr == null || expr.length() == 0) {
			bp.incrementHitCount();
			drm.setData(bp.getHitCount() > bp.getIgnoreCount());
			drm.done();
			return;
		}

		Stack stackService = getService(Stack.class);

		stackService.getTopFrame(context, new DataRequestMonitor<IFrameDMContext>(getExecutor(), drm) {

			@Override
			protected void handleCompleted() {
				if (!isSuccess()) { // fail to get frame, namely cannot
									// evaluate the condition
					bp.incrementHitCount();
					drm.setData(bp.getHitCount() > bp.getIgnoreCount());
					drm.done();
				} else {
					Expressions exprService = getService(Expressions.class);
					IEDCExpression expression = (IEDCExpression) exprService.createExpression(getData(), expr);
					FormattedValueDMContext fvc = exprService.getFormattedValueContext(expression,
							IFormattedValues.NATURAL_FORMAT);
					FormattedValueDMData value = expression.getFormattedValue(fvc);
					/*
					 * honor the breakpoint if the condition is true or
					 * invalid.
					 */
					String vstr = value.getFormattedValue();
					if (! vstr.equals("true") && ! vstr.equals("false")) //$NON-NLS-1$ //$NON-NLS-2$
						addBreakpointProblemMarker(bp.getContext(), "Breakpoint condition failed to resolve to boolean: " + vstr, IMarker.SEVERITY_WARNING);
					else // remove any problem marker
						removeBreakpointProblemMarker(bp.getContext());
					
					if (!vstr.equals("false"))
					{
						bp.incrementHitCount();
						drm.setData(bp.getHitCount() > bp.getIgnoreCount());
					}
					else
						drm.setData(false); //$NON-NLS-1$

					drm.done();
				}
			}
		});
	}
}

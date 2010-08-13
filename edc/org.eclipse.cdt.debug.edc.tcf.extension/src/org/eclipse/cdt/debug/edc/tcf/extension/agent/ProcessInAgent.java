/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia Corporation and/or its subsidiaries
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Wang (Nokia) - initial version. Oct 20, 2009
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension.agent;

import java.util.Map;

import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ContextInAgent.IRunControlContext;
import org.eclipse.tm.tcf.services.IRunControl;

/**
 * Context in a TCF agent representing a process.
 */
public class ProcessInAgent extends ContextInAgent implements IRunControlContext {

	private final long osID;

	/**
	 * Construct a process context. The internal ID of the context will be
	 * auto-generated. <br>
	 * The constructed context will be added in debugged context cache or
	 * running context cache per request.
	 * 
	 * @param osID
	 *            ID of the process in target OS.
	 * @param props
	 *            initial properties, can be null.
	 * @param cacheAsDebugged
	 *            whether to put the new context in cache of contexts under
	 *            debug or cache of running contexts. See {@link ContextManager}
	 *            .
	 */
	public ProcessInAgent(long osID, Map<String, Object> props, boolean cacheAsDebugged) {
		super(props);

		this.osID = osID;

		Map<String, Object> internalProps = getProperties();
		internalProps.put(IRunControl.PROP_ID, createInternalID(osID));

		internalProps.put(ProtocolConstants.PROP_OS_ID, Long.toString(osID));
		internalProps.put(IRunControl.PROP_IS_CONTAINER, Boolean.TRUE);
		internalProps.put(IRunControl.PROP_HAS_STATE, Boolean.TRUE);
		internalProps.put(IRunControl.PROP_CAN_SUSPEND, Boolean.FALSE);
		internalProps.put(IRunControl.PROP_CAN_RESUME, IRunControl.RM_RESUME | (1 << IRunControl.RM_STEP_INTO));
		internalProps.put(IRunControl.PROP_CAN_TERMINATE, Boolean.TRUE);

		if (cacheAsDebugged)
			ContextManager.addDebuggedContext(this);
		else
			ContextManager.addRunningContext(this);
	}

	/**
	 * Get the process ID in target OS.<br>
	 * vs. {@link #getID()} is to get internal ID.
	 * 
	 * @return
	 */
	public long getProcessID() {
		return osID;
	}

	/**
	 * Create internal unique ID with the given process ID in target OS. This
	 * method ensures the same internal ID is generated for the same OS process
	 * ID.
	 * 
	 * @param osID
	 * @return
	 */
	static public String createInternalID(long osID) {
		return "p" + osID;
	}

	static public long contextID2ProcessID(String contextID) throws AgentException {
		ContextInAgent c = ContextManager.findDebuggedContext(contextID);
		
		if (c == null && contextID.startsWith("p"))
		{
			String[] contextPieces = contextID.split("[.]");
			String processID = contextPieces[0];
			processID = processID.substring(1, processID.length());
			return Long.parseLong(processID);
		}
		
		if (c == null || !(c instanceof ProcessInAgent))
			throw new AgentException("Invalid process context ID:" + contextID);

		return ((ProcessInAgent) c).getProcessID();
	}
}

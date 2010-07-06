/*******************************************************************************
 * Copyright (c) 2009,2010 Nokia Corporation and/or its subsidiaries
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

import org.eclipse.cdt.debug.edc.tcf.extension.ProtocolConstants;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ContextInAgent.IRegisterOwnerContext;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.ContextInAgent.IRunControlContext;
import org.eclipse.tm.tcf.services.IRunControl;

/**
 * Context in a TCF agent representing a thread.
 */
public class ThreadInAgent extends ContextInAgent implements IRegisterOwnerContext, IRunControlContext {

	private final long osID;

	/**
	 * Construct a thread context. The internal ID of the context will be
	 * auto-generated. <br>
	 * The constructed context will be added in debugged context cache or
	 * running context cache per request. And it will be added as child of the
	 * parent context.
	 * 
	 * @param osID
	 *            ID of the thread in target OS.
	 * @param parentID
	 *            internal ID of the parent process
	 * @param props
	 *            initial properties, cannot be null but can be empty.
	 * @param cacheAsDebugged
	 *            whether to put the new context in cache of contexts under
	 *            debug or cache of running contexts. See {@link ContextManager}
	 *            .
	 */
	public ThreadInAgent(long osID, String parentID, Map<String, Object> props, boolean cacheAsDebugged) {
		super(props);

		this.osID = osID;

		Map<String, Object> internalProps = getProperties();
		internalProps.put(IRunControl.PROP_ID, createInternalID(osID, parentID));
		internalProps.put(IRunControl.PROP_PARENT_ID, parentID);

		// store thread ID as hex string
		internalProps.put(ProtocolConstants.PROP_OS_ID, Long.toString(osID));
		internalProps.put(IRunControl.PROP_IS_CONTAINER, Boolean.FALSE);
		internalProps.put(IRunControl.PROP_HAS_STATE, Boolean.TRUE);
		internalProps.put(IRunControl.PROP_CAN_SUSPEND, Boolean.TRUE);
		internalProps.put(IRunControl.PROP_CAN_RESUME, IRunControl.RM_RESUME | (1 << IRunControl.RM_STEP_INTO));
		// We cannot terminate one thread without killing the others.
		// But we mark it as "canTerminate" in order that user can terminate the
		// process when focus is on a thread in Debug View.
		//
		internalProps.put(IRunControl.PROP_CAN_TERMINATE, Boolean.TRUE);

		ContextInAgent parent;
		if (cacheAsDebugged) {
			ContextManager.addDebuggedContext(this);
			parent = ContextManager.findDebuggedContext(parentID);
		} else {
			ContextManager.addRunningContext(this);
			parent = ContextManager.findRunningContext(parentID);
		}

		if (parent != null)
			parent.addChild(this);
		else
			// parent is not cached, should not happen.
			assert (false);
	}

	static public String createInternalID(long osID, String processID) {
		return processID + ".t" + osID;
	}

	/**
	 * Get thread ID from the target OS.
	 * 
	 * @return thread ID.
	 */
	public long getThreadID() {
		return osID;
	}
}

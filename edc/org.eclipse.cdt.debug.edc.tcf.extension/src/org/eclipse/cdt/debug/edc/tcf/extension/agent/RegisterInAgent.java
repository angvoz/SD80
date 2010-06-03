/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation  Feb, 2010
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension.agent;

import java.util.Map;

import org.eclipse.tm.tcf.services.IRegisters;

/**
 * Register context in a TCF agent.
 *
 */
public class RegisterInAgent extends ContextInAgent {

	/**
	 * Construct a register context. The internal ID of the context will be
	 * auto-generated. <br>
	 * The constructed context will be added in debugged context cache. And it
	 * will be added as child of the parent context.
	 * 
	 * @param name
	 *            name of the register group.
	 * @param parentID
	 *            internal ID of the parent (usually a register group).
	 * @param props
	 *            initial properties, cannot be null but can be empty. An
	 *            internal copy of it will be made in this object.
	 */
	public RegisterInAgent(String name, String parentID, Map<String, Object> props) {
		super(props);

		Map<String, Object> internalProps = getProperties();
		internalProps.put(IRegisters.PROP_ID, createInternalID(name, parentID));
		internalProps.put(IRegisters.PROP_PARENT_ID, parentID);

		internalProps.put(IRegisters.PROP_NAME, name);

		// We only need to add register group under a debugged thread.
		ContextManager.addDebuggedContext(this);
		ContextInAgent parent = ContextManager.findDebuggedContext(parentID);

		if (parent != null)
			parent.addChild(this);
		else
			// parent is not cached, should not happen.
			assert (false);
	}

	static public String createInternalID(String name, String parentID) {
		return parentID + ".r" + name;
	}

	public String getName() {
		return (String)getProperties().get(IRegisters.PROP_NAME);
	}

	public String getDescription() {
		return (String)getProperties().get(IRegisters.PROP_DESCRIPTION);
	}
}

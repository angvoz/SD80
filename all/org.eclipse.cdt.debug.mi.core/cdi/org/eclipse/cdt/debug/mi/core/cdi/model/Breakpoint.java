/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.mi.core.cdi.BreakpointManager;
import org.eclipse.cdt.debug.mi.core.cdi.Condition;
import org.eclipse.cdt.debug.mi.core.cdi.Location;
import org.eclipse.cdt.debug.mi.core.output.MIBreakpoint;

/**
 */
public class Breakpoint extends CObject implements ICDILocationBreakpoint {

	ICDILocation location;
	ICDICondition condition;
	MIBreakpoint miBreakpoint;
	BreakpointManager mgr;
	int type;
	String tid;
	boolean enable;

	public Breakpoint(BreakpointManager m, int kind, ICDILocation loc, ICDICondition cond, String threadId) {
		super(m.getSession().getCurrentTarget());
		mgr = m;
		type = kind;
		location = loc;
		condition = cond;
		tid = threadId;
		enable = true;
	}

	public Breakpoint(BreakpointManager m, MIBreakpoint miBreak) {
		super(m.getSession().getCurrentTarget());
		miBreakpoint = miBreak;
		mgr = m;
	}

	public MIBreakpoint getMIBreakpoint() {
		return miBreakpoint;
	}

	public void setMIBreakpoint(MIBreakpoint newMIBreakpoint) {
		miBreakpoint = newMIBreakpoint;
		// Force the reset to use GDB's values.
		condition = null;
		location = null;
	}

	public boolean isDeferred() {
		return (miBreakpoint == null);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getCondition()
	 */
	public ICDICondition getCondition() throws CDIException {
		if (condition == null) {
			if (miBreakpoint != null) {
				condition =  new Condition(miBreakpoint.getIgnoreCount(), miBreakpoint.getCondition());
			}
		}
		return condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getThreadId()
	 */
	public String getThreadId() throws CDIException {
		if (miBreakpoint != null) {
			return miBreakpoint.getThreadId();
		}
		return tid;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isEnabled()
	 */
	public boolean isEnabled() throws CDIException {
		if (miBreakpoint != null) {
			return miBreakpoint.isEnabled();
		}
		return enable;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		if (miBreakpoint != null) {
			return miBreakpoint.isHardware();
		}
		return (type == ICDIBreakpoint.HARDWARE);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		if (miBreakpoint != null) {
			return miBreakpoint.isTemporary();
		}
		return (type == ICDIBreakpoint.TEMPORARY);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setCondition(ICDICondition)
	 */
	public void setCondition(ICDICondition condition) throws CDIException {
		if (isEnabled()) {
			mgr.setCondition(this, condition);
		}
		this.condition = condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean on) throws CDIException {
		if (miBreakpoint != null) {
			if (on == false && isEnabled() == true) { 
				mgr.disableBreakpoint(this);
			} else if (on == true && isEnabled() == false) {
				mgr.enableBreakpoint(this);
			}
		}
		enable = on;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocationBreakpoint#getLocation()
	 */
	public ICDILocation getLocation() throws CDIException {
		if (location == null) {
			if (miBreakpoint != null) {
				location = new Location (miBreakpoint.getFile(),
					miBreakpoint.getFunction(),
					miBreakpoint.getLine(),
					miBreakpoint.getAddress());
			}
		}
		return location;
	}

	public void setLocation(ICDILocation loc) {
		location = loc;
	}
}

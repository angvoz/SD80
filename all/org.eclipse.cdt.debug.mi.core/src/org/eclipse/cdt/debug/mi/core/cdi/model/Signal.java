/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 *
 */
package org.eclipse.cdt.debug.mi.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.cdi.SignalManager;
import org.eclipse.cdt.debug.mi.core.output.MISigHandle;

/**
 */
public class Signal extends CObject implements ICDISignal {

	SignalManager mgr;
	MISigHandle sig;

	public Signal(SignalManager m, MISigHandle s) {
		super(m.getSession().getCurrentTarget());
		mgr = m;
		sig = s;
	}
		
	public void setMISignal(MISigHandle s) {
		sig = s;
	}

	public MISigHandle getMISignal() {
		return sig;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getMeaning()
	 */
	public String getDescription() {
		return sig.getDescription();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#getName()
	 */
	public String getName() {
		return sig.getName();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#handle()
	 */
	public void handle(boolean ignore, boolean stop) throws CDIException {
		mgr.handle(this, ignore, stop);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#isIgnore()
	 */
	public boolean isIgnore() {
		return !sig.isPass();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignal#isStopSet()
	 */
	public boolean isStopSet() {
		return sig.isStop();
	}

	/**
	 * Continue program giving it signal specified by the argument.
	 */
	public void signal() throws CDIException {
		mgr.signal(this);
	}
}

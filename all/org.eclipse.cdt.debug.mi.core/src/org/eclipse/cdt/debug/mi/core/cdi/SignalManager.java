/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISignalManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.Signal;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIHandle;
import org.eclipse.cdt.debug.mi.core.command.MIInfoSignals;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MISignalChangedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSignalsInfo;
import org.eclipse.cdt.debug.mi.core.output.MISigHandle;

/**
 */
public class SignalManager extends SessionObject implements ICDISignalManager {

	boolean autoupdate;
	MISigHandle[] noSigs =  new MISigHandle[0];
	List signalsList = null;

	public SignalManager(Session session) {
		super(session);
		autoupdate = false;
	}
	
	MISigHandle[] getMISignals() throws CDIException {
		MISigHandle[] miSigs = noSigs;
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIInfoSignals sigs = factory.createMIInfoSignals();
		try {
			mi.postCommand(sigs);
			MIInfoSignalsInfo info = sigs.getMIInfoSignalsInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			miSigs =  info.getMISignals();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return miSigs;
	}

	MISigHandle getMISignal(String name) throws CDIException {
		MISigHandle sig = null;
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIInfoSignals sigs = factory.createMIInfoSignals(name);
		try {
			mi.postCommand(sigs);
			MIInfoSignalsInfo info = sigs.getMIInfoSignalsInfo();
			if (info == null) {
				throw new CDIException("No answer");
			}
			MISigHandle[] miSigs =  info.getMISignals();
			if (miSigs.length > 0) {
				sig = miSigs[0];
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return sig;
	}

	/**
	 * Method hasSignalChanged.
	 * @param sig
	 * @param mISignal
	 * @return boolean
	 */
	private boolean hasSignalChanged(ICDISignal sig, MISigHandle miSignal) {
		return !sig.getName().equals(miSignal.getName()) ||
			sig.isStopSet() != miSignal.isStop() ||
			sig.isIgnore() != !miSignal.isPass();
	}

	public ICDISignal findSignal(String name) {
		ICDISignal sig = null;
		if (signalsList != null) {
			ICDISignal[] sigs = (ICDISignal[])signalsList.toArray(new ICDISignal[0]);
			for (int i = 0; i < sigs.length; i++) {
				if (sigs[i].getName().equals(name)) {
					sig = sigs[i];
					break;
				}
			}
		}
		return sig;
	}

	public ICDISignal getSignal(String name) {
		ICDISignal sig = findSignal(name);
		if (sig == null) {
			try {
				MISigHandle miSig = getMISignal(name);
				sig = new Signal(this, miSig);
				if (signalsList != null) {
					signalsList.add(sig);
				}
			} catch (CDIException e) {
			}
		}
		return sig;
	}

	public void handle(ICDISignal sig, boolean isIgnore, boolean isStop) throws CDIException {
		Session session = (Session)getSession();
		MISession mi = session.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		StringBuffer buffer = new StringBuffer(sig.getName());
		buffer.append(" ");
		if (isIgnore) {
			buffer.append("ignore");
		} else {
			buffer.append("noignore");
		}
		buffer.append(" ");
		if (isStop) {
			buffer.append("stop");
		} else  {
			buffer.append("nostop");
		}
		MIHandle handle = factory.createMIHandle(buffer.toString());
		try {
			mi.postCommand(handle);
			handle.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		((Signal)sig).getMISignal().handle(isIgnore, isStop);
		mi.fireEvent(new MISignalChangedEvent(sig.getName()));
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#getSignals()
	 */
	public ICDISignal[] getSignals() throws CDIException {
		if (signalsList == null) {
			update();
		}
		return (ICDISignal[])signalsList.toArray(new ICDISignal[0]);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#isAutoUpdate()
	 */
	public boolean isAutoUpdate() {
		return autoupdate;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#setAutoUpdate(boolean)
	 */
	public void setAutoUpdate(boolean update) {
		autoupdate = update;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISignalManager#update()
	 */
	public void update() throws CDIException {
		if (signalsList == null) {
			signalsList = Collections.synchronizedList(new ArrayList(5));
		}
		Session session = (Session)getSession();
		MISigHandle[] miSigs = getMISignals();
		List eventList = new ArrayList(miSigs.length);
		for (int i = 0; i < miSigs.length; i++) {
			ICDISignal sig = findSignal(miSigs[i].getName());
			if (sig != null) {
				if (hasSignalChanged(sig, miSigs[i])) {
					// Fire ChangedEvent
					((Signal)sig).setMISignal(miSigs[i]);
					eventList.add(new MISignalChangedEvent(miSigs[i].getName())); 
				}
			} else {
				// add the new breakpoint and fire CreatedEvent
				signalsList.add(new Signal(this, miSigs[i]));
			}
		}
		MISession mi = session.getMISession();
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	/**
	 * Method signal.
	 */
	public void signal() {
	}

} 

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

package org.eclipse.cdt.debug.mi.core.cdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.mi.core.MIFormat;
import org.eclipse.cdt.debug.mi.core.MISession;
import org.eclipse.cdt.debug.mi.core.cdi.model.SharedLibrary;
import org.eclipse.cdt.debug.mi.core.cdi.model.Target;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetAutoSolib;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSetStopOnSolibEvents;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShow;
import org.eclipse.cdt.debug.mi.core.command.MIGDBShowSolibSearchPath;
import org.eclipse.cdt.debug.mi.core.command.MIInfoSharedLibrary;
import org.eclipse.cdt.debug.mi.core.command.MISharedLibrary;
import org.eclipse.cdt.debug.mi.core.event.MIEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibChangedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibCreatedEvent;
import org.eclipse.cdt.debug.mi.core.event.MISharedLibUnloadedEvent;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowInfo;
import org.eclipse.cdt.debug.mi.core.output.MIGDBShowSolibSearchPathInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.cdt.debug.mi.core.output.MIInfoSharedLibraryInfo;
import org.eclipse.cdt.debug.mi.core.output.MIShared;

/**
 * Manager of the CDI shared libraries.
 */
public class SharedLibraryManager extends Manager implements ICDISharedLibraryManager {

	ICDISharedLibrary[] EMPTY_SHAREDLIB = {};
	Map sharedMap;
	boolean isDeferred;

	public SharedLibraryManager (Session session) {
		super(session, true);
		sharedMap = new Hashtable();
	}

	synchronized List getSharedList(Target target) {
		List sharedList = (List)sharedMap.get(target);
		if (sharedList == null) {
			sharedList = Collections.synchronizedList(new ArrayList());
			sharedMap.put(target, sharedList);
		}
		return sharedList;
	}

	MIShared[] getMIShareds(MISession miSession) throws CDIException {
		MIShared[] miLibs = new MIShared[0];
		CommandFactory factory = miSession.getCommandFactory();
		MIInfoSharedLibrary infoShared = factory.createMIInfoSharedLibrary();
		try {
			miSession.postCommand(infoShared);
			MIInfoSharedLibraryInfo info = infoShared.getMIInfoSharedLibraryInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
			miLibs = info.getMIShared();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return miLibs;
	}

	/**
	 * @deprecated
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#update()
	 */
	public void update() throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		update(target);
	}
	public void update(Target target) throws CDIException {
		MISession mi = target.getMISession();
		List eventList = updateState(target);
		MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		mi.fireEvents(events);
	}

	public List updateState(Target target) throws CDIException {
		MISession miSession = target.getMISession();
		Session session = (Session)getSession();
		ICDIConfiguration conf = session.getConfiguration();
		if (!conf.supportsSharedLibrary()) {
			return Collections.EMPTY_LIST; // Bail out early;
		}

		MIShared[] miLibs = getMIShareds(miSession);
		ArrayList eventList = new ArrayList(miLibs.length);
		for (int i = 0; i < miLibs.length; i++) {
			ICDISharedLibrary sharedlib = getSharedLibrary(target, miLibs[i].getName());
			if (sharedlib != null) {
				if (hasSharedLibChanged(sharedlib, miLibs[i])) {
					// Fire ChangedEvent
					((SharedLibrary)sharedlib).setMIShared(miLibs[i]);
					eventList.add(new MISharedLibChangedEvent(miSession, miLibs[i].getName())); 
				}
			} else {
				// add the new breakpoint and fire CreatedEvent
				List sharedList = getSharedList(target);
				sharedList.add(new SharedLibrary(target, miLibs[i]));
				eventList.add(new MISharedLibCreatedEvent(miSession, miLibs[i].getName())); 
			}
		}
		// Check if any libraries was unloaded.
		List sharedList = (List)sharedMap.get(target);
		if (sharedList != null) {
			ICDISharedLibrary[] oldlibs = (ICDISharedLibrary[]) sharedList.toArray(new ICDISharedLibrary[sharedList.size()]);
			for (int i = 0; i < oldlibs.length; i++) {
				boolean found = false;
				for (int j = 0; j < miLibs.length; j++) {
					if (miLibs[j].getName().equals(oldlibs[i].getFileName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					// Fire destroyed Events.
					eventList.add(new MISharedLibUnloadedEvent(miSession, oldlibs[i].getFileName())); 
				}
			}
		}
		return eventList;
	}

	public boolean hasSharedLibChanged(ICDISharedLibrary lib, MIShared miLib) {
		return !miLib.getName().equals(lib.getFileName()) ||
			!MIFormat.getBigInteger(miLib.getFrom()).equals(lib.getStartAddress())   ||
		    !MIFormat.getBigInteger(miLib.getTo()).equals(lib.getEndAddress()) ||
			miLib.isRead() != lib.areSymbolsLoaded();
	}

	/*
	 * this for the events
	 */
	public void deleteSharedLibrary(MISession miSession, ICDISharedLibrary lib) {
		Target target = ((Session)getSession()).getTarget(miSession);
		List sharedList = (List)sharedMap.get(target);
		if (sharedList != null) {
			sharedList.remove(lib);
		}
	}

	public ICDISharedLibrary getSharedLibrary(MISession miSession, String name) {
		Target target = ((Session)getSession()).getTarget(miSession);
		return getSharedLibrary(target, name);
	}
	public ICDISharedLibrary getSharedLibrary(Target target, String name) {
		List sharedList = (List)sharedMap.get(target);
		if (sharedList != null) {
			ICDISharedLibrary[] libs = (ICDISharedLibrary[]) sharedList.toArray(new ICDISharedLibrary[sharedList.size()]);
			for (int i = 0; i < libs.length; i++) {
				if (name.equals(libs[i].getFileName())) {
					return libs[i];
				}
			}
		}
		return null;
	}

	public void setDeferredBreakpoint (boolean set) {
		isDeferred = set;
	}

	public boolean isDeferredBreakpoint() {
		return isDeferred;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#setSharedLibraryPaths(String[])
	 */
	public void setAutoLoadSymbols(boolean set) throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		setAutoLoadSymbols(target, set);
	}
	public void setAutoLoadSymbols(Target target, boolean set) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetAutoSolib solib = factory.createMIGDBSetAutoSolib(set);
		try {
			mi.postCommand(solib);
			solib.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 */
	public boolean isAutoLoadSymbols() throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		return isAutoLoadSymbols(target);
	}
	public boolean isAutoLoadSymbols(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShow show = factory.createMIGDBShow(new String[]{"auto-solib-add"}); //$NON-NLS-1$
		try {
			mi.postCommand(show);
			MIGDBShowInfo info = show.getMIGDBShowInfo();
			String value = info.getValue();
			if (value != null) {
				return value.equalsIgnoreCase("on"); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return false;
	}

	public void setStopOnSolibEvents(boolean set) throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		setStopOnSolibEvents(target, set);
	}
	public void setStopOnSolibEvents(Target target, boolean set) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetStopOnSolibEvents stop = factory.createMIGDBSetStopOnSolibEvents(set);
		try {
			mi.postCommand(stop);
			stop.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	public boolean isStopOnSolibEvents() throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		return isStopOnSolibEvents(target);
	}
	public boolean isStopOnSolibEvents(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShow show = factory.createMIGDBShow(new String[]{"stop-on-solib-events"}); //$NON-NLS-1$
		try {
			mi.postCommand(show);
			MIGDBShowInfo info = show.getMIGDBShowInfo();
			String value = info.getValue();
			if (value != null) {
				return value.equalsIgnoreCase("1"); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		return false;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#setSharedLibraryPaths(String[])
	 */
	public void setSharedLibraryPaths(String[] libPaths) throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		setSharedLibraryPaths(target, libPaths);
	}
	public void setSharedLibraryPaths(Target target, String[] libPaths) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBSetSolibSearchPath solib = factory.createMIGDBSetSolibSearchPath(libPaths);
		try {
			mi.postCommand(solib);
			solib.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#getSharedLibraryPaths()
	 */
	public String[] getSharedLibraryPaths() throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		return getSharedLibraryPaths(target);
	}
	public String[] getSharedLibraryPaths(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MIGDBShowSolibSearchPath dir = factory.createMIGDBShowSolibSearchPath();
		try {
			mi.postCommand(dir);
			MIGDBShowSolibSearchPathInfo info = dir.getMIGDBShowSolibSearchPathInfo();
			return info.getDirectories();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#getSharedLibraries()
	 */
	public ICDISharedLibrary[] getSharedLibraries() throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		return getSharedLibraries(target);
	}
	public ICDISharedLibrary[] getSharedLibraries(Target target) throws CDIException {
		List sharedList = (List)sharedMap.get(target);
		if (sharedList != null) {
			return (ICDISharedLibrary[]) sharedList.toArray(new ICDISharedLibrary[sharedList.size()]);
		}
		return EMPTY_SHAREDLIB;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#loadSymbols()
	 */
	public void loadSymbols() throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		loadSymbols(target);
	}
	public void loadSymbols(Target target) throws CDIException {
		MISession mi = target.getMISession();
		CommandFactory factory = mi.getCommandFactory();
		MISharedLibrary sharedlibrary = factory.createMISharedLibrary();
		try {
			mi.postCommand(sharedlibrary);
			MIInfo info = sharedlibrary.getMIInfo();
			if (info == null) {
				throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
			}
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
		update();
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#loadSymbols(ICDISharedLibrary[])
	 */
	public void loadSymbols(ICDISharedLibrary[] libs) throws CDIException {
		Target target = (Target)getSession().getCurrentTarget();
		loadSymbols(target, libs);
	}
	public void loadSymbols(Target target, ICDISharedLibrary[] libs) throws CDIException {
		MISession miSession = target.getMISession();
		CommandFactory factory = miSession.getCommandFactory();
		for (int i = 0; i < libs.length; i++) {
			if (libs[i].areSymbolsLoaded()) {
				continue;
			}
			MISharedLibrary sharedlibrary = factory.createMISharedLibrary(libs[i].getFileName());
			try {
				miSession.postCommand(sharedlibrary);
				MIInfo info = sharedlibrary.getMIInfo();
				if (info == null) {
					throw new CDIException(CdiResources.getString("cdi.Common.No_answer")); //$NON-NLS-1$
				}
			} catch (MIException e) {
				throw new MI2CDIException(e);
			}
			// Do not do this, error are not propagate by the CLI "shared command
			// So we have to manually recheck all the shared with "info shared"
			//((SharedLibrary)libs[i]).getMIShared().setSymbolsRead(true);
			//mi.fireEvent(new MISharedLibChangedEvent(libs[i].getFileName()));
			update(target);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#supportsAutoLoadSymbols()
	 */
	public boolean supportsAutoLoadSymbols() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#supportsStopOnSolibEvents()
	 */
	public boolean supportsStopOnSolibEvents() {
		return true;
	}
}

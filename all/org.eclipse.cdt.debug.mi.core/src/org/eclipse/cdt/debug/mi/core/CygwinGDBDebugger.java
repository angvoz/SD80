/*
 * (c) Copyright Rational Software Corporation. 2002.
 * All Rights Reserved.
 */

package org.eclipse.cdt.debug.mi.core;

import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SharedLibraryManager;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.cdt.debug.mi.core.command.CygwinCommandFactory;
import org.eclipse.cdt.debug.mi.core.command.MIGDBSet;
import org.eclipse.cdt.debug.mi.core.output.MIInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Cygwin GDB Debugger overrides the GDB Debugger to apply the Cygwin
 * Command Factory to the MI Session.
 */
public class CygwinGDBDebugger extends GDBDebugger {

	static final CygwinCommandFactory commandFactory = new CygwinCommandFactory();

	protected void initializeLibraries(ILaunchConfiguration config, Session session) throws CDIException {
		try {
			ICDISharedLibraryManager manager = session.getSharedLibraryManager();
			if (manager instanceof SharedLibraryManager) {
				SharedLibraryManager mgr = (SharedLibraryManager) manager;
				boolean stopOnSolibEvents =
					config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_STOP_ON_SOLIB_EVENTS, false);
				try {
					mgr.setStopOnSolibEvents(stopOnSolibEvents);
					// By default, we provide with the capability of deferred breakpoints
					// And we set setStopOnSolib events for them(but they should not see the dll events ).
					//
					// If the user explicitly set stopOnSolibEvents well it probably
					// means that they wanted to see those events so do no do deferred breakpoints.
					if (!stopOnSolibEvents) {
						mgr.setStopOnSolibEvents(true);
						mgr.setDeferredBreakpoint(true);
					}
				} catch (CDIException e) {
					// Ignore this error
					// it seems to be a real problem on many gdb platform
				}
			}
			List p = config.getAttribute(IMILaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH, Collections.EMPTY_LIST);
			if (p.size() > 0) {
				String[] oldPaths = manager.getSharedLibraryPaths();
				String[] paths = new String[oldPaths.length + p.size()];
				System.arraycopy(p.toArray(new String[p.size()]), 0, paths, 0, p.size());
				System.arraycopy(oldPaths, 0, paths, p.size(), oldPaths.length);
				manager.setSharedLibraryPaths(paths);
			}
		} catch (CoreException e) {
			throw new CDIException("Error initializing shared library options: " + e.getMessage());
		}
	}

	public ICDISession createLaunchSession(ILaunchConfiguration config, IFile exe) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			session = (Session) super.createLaunchSession(config, exe);
			session.getMISession().setCommandFactory(commandFactory);
			// For windows we need to start the inferior in a new console window
			// to separate the Inferior std{in,out,err} from gdb std{in,out,err}
			MISession mi = session.getMISession();
			try {
				CommandFactory factory = mi.getCommandFactory();
				MIGDBSet set = factory.createMIGDBSet(new String[] { "new-console" });
				mi.postCommand(set);
				MIInfo info = set.getMIInfo();
				if (info == null) {
					throw new MIException("No answer");
				}
			} catch (MIException e) {
				// We ignore this exception, for example
				// on GNU/Linux the new-console is an error.
			}
			initializeLibraries(config, session);
			return session;
		} catch (CDIException e) {
			failed = true;
			throw e;
		} finally {
			if (failed) {
				if (session != null) {
					try {
						session.terminate();
					} catch (Exception ex) {
						// ignore the exception here.
					}
				}
			}
		}
	}

	public ICDISession createAttachSession(ILaunchConfiguration config, IFile exe, int pid) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			session = (Session) super.createAttachSession(config, exe, pid);
			session.getMISession().setCommandFactory(commandFactory);
			initializeLibraries(config, session);
			return session;
		} catch (CDIException e) {
			failed = true;
			throw e;
		} finally {
			if (failed) {
				if (session != null) {
					try {
						session.terminate();
					} catch (Exception ex) {
						// ignore the exception here.
					}
				}
			}
		}
	}

	public ICDISession createCoreSession(ILaunchConfiguration config, IFile exe, IPath corefile) throws CDIException {
		Session session = null;
		boolean failed = false;
		try {
			session = (Session) super.createCoreSession(config, exe, corefile);
			session.getMISession().setCommandFactory(commandFactory);
			initializeLibraries(config, session);
			return session;
		} catch (CDIException e) {
			failed = true;
			throw e;
		} finally {
			if (failed) {
				if (session != null) {
					try {
						session.terminate();
					} catch (Exception ex) {
						// ignore the exception here.
					}
				}
			}
		}
	}
}

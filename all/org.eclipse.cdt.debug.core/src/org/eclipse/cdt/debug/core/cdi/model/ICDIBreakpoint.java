/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;

/**
 * 
 * A breakpoint is capable of suspending the execution of a program 
 * whenever a certain point in the program is reached. Provides a 
 * basic functionality for the location breakpoints, watchpoints,
 * and catchpoints.
 * 
 * @see ICDILocationBreakpoint
 * @see ICDIWatchpoint
 * @see ICDICatchpoint
 * 
 * @since Jul 9, 2002
 */
public interface ICDIBreakpoint extends ICDIObject {
	final static public int REGULAR = 0x0;
	final static public int TEMPORARY = 0x1;
	final static public int HARDWARE = 0x2;

	/**
	 * Returns whether this breakpoint is temporary.
	 * 
	 * @return whether this breakpoint is temporary
	 */
	boolean isTemporary();
	
	/**
	 * Returns whether this breakpoint is hardware-assisted.
	 * 
	 * @return whether this breakpoint is hardware-assisted
	 */
	boolean isHardware();

	/**
	 * Returns whether this breakpoint is enabled.
	 * 
	 * @return whether this breakpoint is enabled
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	boolean isEnabled() throws CDIException;
	
	/**
	 * Sets the enabled state of this breakpoint. This has no effect 
	 * if the current enabled state is the same as specified by 
	 * the enabled parameter.
	 * 
	 * @param enabled - whether this breakpoint should be enabled 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void setEnabled( boolean enabled ) throws CDIException;
	
	/**
	 * Returns the condition of this breakpoint or <code>null</code>
	 * if the breakpoint's condition is not set.
	 * 
	 * @return the condition of this breakpoint
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDICondition getCondition() throws CDIException;
	
	/**
	 * Sets the condition of this breakpoint.
	 * 
	 * @param the condition to set
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void setCondition( ICDICondition condition ) throws CDIException;
	
	/**
	 * Returns a thread identifier or <code>null</code> is the breakpoint 
	 * is not thread-specific.
	 * 
	 * @return a thread identifier
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	String getThreadId() throws CDIException;
}

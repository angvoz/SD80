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

package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;

/**
 * @author User
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ICDIThreadGroup extends ICDIBreakpointManagement, ICDIExecuteStep, ICDIExecuteResume,
	ICDISuspend, ICDIObject {

	/**
	 * Returns the threads contained in this target. 
	 * An empty collection is returned if this target contains no 
	 * threads.
	 * 
	 * @return a collection of threads
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIThread[] getThreads() throws CDIException;

	/**
	 * Returns the currently selected thread.
	 * 
	 * @return the currently selected thread
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	ICDIThread getCurrentThread() throws CDIException;

}

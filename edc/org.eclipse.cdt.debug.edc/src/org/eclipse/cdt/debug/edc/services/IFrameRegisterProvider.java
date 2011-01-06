/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.services;

import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;

/**
 * Provide access to reading values of registers from other stack frames.
 */
public interface IFrameRegisterProvider {

	/**
	 * Get the registers available at the given frame and address.
	 * @param session
	 * @param tracker
	 * @param context the frame
	 * @return {@link IFrameRegisters} or <code>null</code> if no information found
	 * @throws CoreException if fatal error handling the symbolics for the frame
	 * @since 2.0
	 */
	IFrameRegisters getFrameRegisters(DsfSession session, EDCServicesTracker tracker, IFrameDMContext context) throws CoreException;
	
	void dispose();
}

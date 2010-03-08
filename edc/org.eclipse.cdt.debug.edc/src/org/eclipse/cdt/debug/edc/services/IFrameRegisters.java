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

import java.math.BigInteger;

import org.eclipse.core.runtime.CoreException;

/**
 * Provide values of registers which may be in other stack frames.
 * This instance is only valid for a given stack trace, since the current
 * PC at each stack level is used to precisely determine the state of
 * stored registers.
 */
public interface IFrameRegisters {
	/**
	 * Get the value of the register.  
	 * @param regnum common register #
	 * @param bytes size of register to read (starting from least significant byte)
	 * @return value, never <code>null</code>
	 * @throws CoreException if cannot read
	 */
	BigInteger getRegister(int regnum, int bytes) throws CoreException;
}

/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.symbols;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.symbols.IInvalidVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

public class InvalidVariableLocation implements IInvalidVariableLocation {

	private String message;

	public InvalidVariableLocation(String message) {
		if (message == null)
			this.message = ""; //$NON-NLS-1$
		else
			this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Invalid: " + getMessage(); //$NON-NLS-1$
	}
	
	
	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		if (message == null)
			this.message = ""; //$NON-NLS-1$
		else
			this.message = message;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IVariableLocation#readValue()
	 */
	public BigInteger readValue(int bytes) throws CoreException {
		throw EDCDebugger.newCoreException(message);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IVariableLocation#addOffset(long)
	 */
	public IVariableLocation addOffset(long offset) {
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IVariableLocation#getLocationName(org.eclipse.cdt.dsf.service.DsfServicesTracker)
	 */
	public String getLocationName() {
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IVariableLocation#getAddress()
	 */
	public IAddress getAddress() {
		return null;
	}

	public void writeValue(int bytes, BigInteger value) throws CoreException {
		throw EDCDebugger.newCoreException(SymbolsMessages.InvalidVariableLocation_CannotWriteInvalidLocation);
	}

	public IDMContext getContext() {
		return null;
	}

	public DsfServicesTracker getServicesTracker() {
		return null;
	}
}

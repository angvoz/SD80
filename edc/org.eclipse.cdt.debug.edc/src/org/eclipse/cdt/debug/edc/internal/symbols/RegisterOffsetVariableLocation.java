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
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.debug.edc.symbols.IRegisterOffsetVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

public class RegisterOffsetVariableLocation extends RegisterVariableLocation implements IRegisterOffsetVariableLocation {

	protected final long offset;
	private int addressSize;

	public RegisterOffsetVariableLocation(DsfServicesTracker tracker, IDMContext context, String name, int id, long offset) {
		super(tracker, context, name, id);
		this.offset = offset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " + " + getOffset(); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.IRegisterOffsetVariableLocation#getOffset()
	 */
	public long getOffset() {
		return offset;
	}
	
	public BigInteger readValue(int bytes) throws CoreException {
		BigInteger regval = super.readValue(bytes);
		return regval.add(BigInteger.valueOf(offset));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation#addOffset(long)
	 */
	@Override
	public IVariableLocation addOffset(long offset) {
		return new RegisterOffsetVariableLocation(tracker, context, name, id, offset + this.offset);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation#getLocationName(org.eclipse.cdt.dsf.service.DsfServicesTracker)
	 */
	@Override
	public String getLocationName() {
		try {
			if (addressSize == 0) {
				addressSize = 4;
				ITargetEnvironment targetEnvironment = tracker.getService(ITargetEnvironment.class);
				if (targetEnvironment != null)
					addressSize = targetEnvironment.getPointerSize();
			}
			BigInteger regval = super.readValue(addressSize);
			regval = regval.add(BigInteger.valueOf(offset));
			return SymbolsMessages.RegisterOffsetVariableLocation_Hex + Long.toHexString(regval.longValue());
		} catch (CoreException e) {
			// fallback
			return super.getLocationName() + (offset < 0 ? SymbolsMessages.RegisterOffsetVariableLocation_Positive : SymbolsMessages.RegisterOffsetVariableLocation_Negative ) + Math.abs(offset);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation#getAddress()
	 */
	@Override
	public IAddress getAddress() {
		return null;
	}
}

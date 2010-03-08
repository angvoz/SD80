/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
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

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

public class RegisterVariableLocation implements IRegisterVariableLocation {

	protected String name;
	protected int id;
	protected final IDMContext context;

	public RegisterVariableLocation(IDMContext context, String name, int id) {
		this.context = context;
		this.name = name;
		this.id = id;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getRegisterName() != null ? getRegisterName() : "R" + id +
				(context instanceof StackFrameDMC && ((StackFrameDMC) context).getLevel() > 0 ?
						" (level " + ((StackFrameDMC) context).getLevel() + ")" : "");
	}
	
	public String getRegisterName() {
		return name;
	}

	public int getRegisterID() {
		return id;
	}

	public BigInteger readValue(int bytes) throws CoreException {
		if (context instanceof StackFrameDMC)
			return ((StackFrameDMC)context).getFrameRegisters().getRegister(id, bytes);
		else
			throw EDCDebugger.newCoreException("cannot read register without frame");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IVariableLocation#addOffset(long)
	 */
	public IVariableLocation addOffset(long offset) {
		return new RegisterOffsetVariableLocation(context, name, id, offset);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IVariableLocation#getLocationName(org.eclipse.cdt.dsf.service.DsfServicesTracker)
	 */
	public String getLocationName(DsfServicesTracker servicesTracker) {
		if (getRegisterName() == null) {
			Registers registerservice = servicesTracker.getService(Registers.class);
			return "$" + registerservice.getRegisterNameFromCommonID(getRegisterID()); //$NON-NLS-1$
		} else
			return "$" + getRegisterName(); //$NON-NLS-1$
	}
}

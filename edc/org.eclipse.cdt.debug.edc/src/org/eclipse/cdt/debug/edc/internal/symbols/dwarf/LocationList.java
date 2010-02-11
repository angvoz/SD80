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
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.internal.symbols.ICompileUnitScope;
import org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.internal.symbols.IScope;
import org.eclipse.cdt.debug.edc.internal.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.InvalidVariableLocation;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

public class LocationList implements ILocationProvider {

	protected EDCDwarfReader reader;
	protected LocationEntry[] locationList;
	protected int addressSize;
	protected IScope scope;

	public LocationList(EDCDwarfReader reader, LocationEntry[] locationList, int addressSize, IScope scope) {
		this.reader = reader;
		this.locationList = locationList;
		this.addressSize = addressSize;
		this.scope = scope;
	}

	public IVariableLocation getLocation(DsfServicesTracker tracker, IFrameDMContext context, IAddress forLinkAddress) {
		// the high/low addresses of a location list entry are relative to
		// the base of the compile unit, so make the adjustment here.
		IScope compileUnitScope = scope;
		while (!(compileUnitScope instanceof ICompileUnitScope)) {
			compileUnitScope = compileUnitScope.getParent();
		}

		assert (compileUnitScope != null && compileUnitScope instanceof ICompileUnitScope);

		long address = compileUnitScope.getLowAddress().distanceTo(forLinkAddress).longValue();
		// find the location entry for the given address
		for (LocationEntry entry : locationList) {
			if (address >= entry.lowPC && address < entry.highPC) {
				LocationExpression expression = new LocationExpression(reader, entry.bytes, addressSize, scope);
				return expression.getLocation(tracker, context, forLinkAddress);
			}
		}

		// variable may exist in the current scope, but not be live at the given
		// address
		return locationList != null ? new InvalidVariableLocation(DwarfMessages.UnknownVariableAddress) : null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider#isLocationKnown(org.eclipse.cdt.core.IAddress)
	 */
	public boolean isLocationKnown(IAddress forLinkAddress) {
		// no-op for old reader
		return true;
	}
}

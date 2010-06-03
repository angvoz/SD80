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
package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.nio.ByteOrder;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.MemoryStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.symbols.InvalidVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.ILocationProvider;
import org.eclipse.cdt.debug.edc.symbols.IModuleScope;
import org.eclipse.cdt.debug.edc.symbols.IScope;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

public class LocationList implements ILocationProvider {

	protected LocationEntry[] locationList;
	protected int addressSize;
	protected IScope scope;
	protected ByteOrder byteOrder;

	public LocationList(LocationEntry[] locationList, ByteOrder byteOrder, int addressSize, IScope scope) {
		this.locationList = locationList;
		this.byteOrder = byteOrder;
		this.addressSize = addressSize;
		this.scope = scope;
	}

	public LocationEntry[] getLocationEntries() {
		return locationList;
	}
	public IVariableLocation getLocation(DsfServicesTracker tracker, IFrameDMContext context, IAddress forLinkAddress) {
		
		if (locationList != null) {
			IScope searchScope = scope;
			do {
				// the scope may be an inlined function scope, whose frame base is only provided in a parent.
				IVariableLocation location = searchForLocation(tracker, context, forLinkAddress, searchScope);
				if (location != null) {
					return location;
				}
				searchScope = searchScope.getParent();
			} while (!(searchScope instanceof IModuleScope));
		}
		
		// variable may exist in the current scope, but not be live at the given
		// address
		return locationList != null ? new InvalidVariableLocation(DwarfMessages.UnknownVariableAddress) : null;
	}

	private IVariableLocation searchForLocation(DsfServicesTracker tracker,
			IFrameDMContext context, IAddress forLinkAddress,
			IScope scope) {
		long address = forLinkAddress.getValue().longValue();
		
		// find the location entry for the given address
		for (LocationEntry entry : locationList) {
			if (address >= entry.getLowPC() && address < entry.getHighPC()) {
				IStreamBuffer locationData = new MemoryStreamBuffer(entry.getBytes(), byteOrder);
				LocationExpression expression = new LocationExpression(locationData, addressSize, scope);
				return expression.getLocation(tracker, context, forLinkAddress);
			}
		}
		
		return null;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.symbols.ILocationProvider#isLocationKnown(org.eclipse.cdt.core.IAddress)
	 */
	public boolean isLocationKnown(IAddress forLinkAddress) {
		long address = forLinkAddress.getValue().longValue();
		
		for (LocationEntry entry : locationList) {
			if (address >= entry.getLowPC() && address < entry.getHighPC()) {
				return true;
			}
		}
		return false;
	}
}

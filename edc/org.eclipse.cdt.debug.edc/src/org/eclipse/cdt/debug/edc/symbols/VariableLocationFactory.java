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

package org.eclipse.cdt.debug.edc.symbols;

import java.math.BigInteger;

import org.eclipse.cdt.debug.edc.internal.symbols.InvalidVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.MemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.ValueVariableLocation;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * Create {@link IVariableLocation} instances
 */
public final class VariableLocationFactory {
	protected VariableLocationFactory() { }
	
	public static IMemoryVariableLocation createMemoryVariableLocation(DsfServicesTracker tracker, 
			IDMContext context, BigInteger addressValue, boolean isRuntimeAddress) {
		return new MemoryVariableLocation(tracker, context, addressValue, isRuntimeAddress);
	}
	
	public static IMemoryVariableLocation createMemoryVariableLocation(DsfServicesTracker tracker, 
			IDMContext context, BigInteger addressValue) {
		return new MemoryVariableLocation(tracker, context, addressValue, true);
	}
	public static IMemoryVariableLocation createMemoryVariableLocation(DsfServicesTracker tracker, 
			IDMContext context, long addressValue) {
		return new MemoryVariableLocation(tracker, context, BigInteger.valueOf(addressValue), true);
	}
	
	public static IMemoryVariableLocation createMemoryVariableLocation(
			DsfServicesTracker tracker, IDMContext context,
			Number addressValue) {
		BigInteger addr;
		if (addressValue instanceof BigInteger)
			addr = (BigInteger) addressValue;
		else
			addr = BigInteger.valueOf(addressValue.longValue());
		return new MemoryVariableLocation(tracker, context, addr, true);
	}

	public static IRegisterVariableLocation createRegisterVariableLocation(
			DsfServicesTracker tracker,  IDMContext context, String name, int id) {
		return new RegisterVariableLocation(tracker, context, name, id);
	}
	public static IRegisterVariableLocation createRegisterVariableLocation(
			DsfServicesTracker tracker,  IDMContext context, int id) {
		return new RegisterVariableLocation(tracker, context, null, id);
	}
	
	public static IInvalidVariableLocation createInvalidVariableLocation(String message) {
		return new InvalidVariableLocation(message);
	}

	public static IValueVariableLocation createValueVariableLocation(BigInteger value) {
		return new ValueVariableLocation(value);
	}

}

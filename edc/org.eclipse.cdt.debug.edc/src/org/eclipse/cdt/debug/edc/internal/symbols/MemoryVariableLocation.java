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
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.MemoryUtils;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Memory;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IMemoryVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.MemoryByte;

public class MemoryVariableLocation implements IMemoryVariableLocation {

	protected final IAddress address;
	protected final boolean isRuntimeAddress;
	protected final DsfServicesTracker tracker;
	private final IDMContext context;

	public MemoryVariableLocation(DsfServicesTracker tracker, 
			IDMContext context,
			BigInteger addressValue, boolean isRuntimeAddress) {
		this.tracker = tracker;
		this.context = context;
		BigInteger MAXADDR = BigInteger.valueOf(0xffffffffL);
		ITargetEnvironment targetEnvironment = tracker.getService(ITargetEnvironment.class);
		if (targetEnvironment != null && targetEnvironment.getPointerSize() == 8) {
			MAXADDR = BigInteger.valueOf(0xffffffffffffffffL);
		}
		this.address = new Addr64(addressValue.and(MAXADDR));
		this.isRuntimeAddress = isRuntimeAddress;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "0x" + Long.toHexString(address.getValue().longValue()) + //$NON-NLS-1$
				(isRuntimeAddress ? "" : " (link address)"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public IAddress getAddress() {
		try {
			return getRealAddress();
		} catch (CoreException e) {
			return null;
		}
	}

	public boolean isRuntimeAddress() {
		return isRuntimeAddress;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IVariableLocation#readValue()
	 */
	public BigInteger readValue(int varSize) throws CoreException {
		IAddress theAddress = address;
		if (!isRuntimeAddress) {
			theAddress = getRealAddress();
		}
		
		ExecutionDMC exeDMC = DMContexts.getAncestorOfType(context, ExecutionDMC.class);
		
		Memory memoryService = tracker.getService(Memory.class);
		ArrayList<MemoryByte> memBuffer = new ArrayList<MemoryByte>();
		IStatus memGetStatus = memoryService.getMemory(exeDMC, theAddress, memBuffer, varSize, 1);
		if (!memGetStatus.isOK()) {
			throw EDCDebugger.newCoreException(MessageFormat.format(
					SymbolsMessages.MemoryVariableLocation_CannotReadAddrFormat, theAddress.toHexAddressString()));
		}

		return MemoryUtils.convertByteArrayToUnsignedLong(
				memBuffer.toArray(new MemoryByte[varSize]), getEndian());
	}

	private int getEndian() {
		ITargetEnvironment targetEnvironment = tracker.getService(ITargetEnvironment.class);
		int endian = MemoryUtils.LITTLE_ENDIAN;
		if (targetEnvironment != null)
			endian = targetEnvironment.isLittleEndian(context) ? MemoryUtils.LITTLE_ENDIAN : MemoryUtils.BIG_ENDIAN;
		return endian;
	}

	/**
	 * @return
	 * @throws CoreException
	 */
	private IAddress getRealAddress() throws CoreException {
		IAddress theAddress = address;
		if (!isRuntimeAddress) {
			StackFrameDMC frame = DMContexts.getAncestorOfType(context, StackFrameDMC.class);
			if (frame == null) 
				throw EDCDebugger.newCoreException(SymbolsMessages.MemoryVariableLocation_CannotFindFrame);
			theAddress = frame.getModule().toRuntimeAddress(theAddress);
		}
		return theAddress;
	}
	
	public IVariableLocation addOffset(long offset) {
		return new MemoryVariableLocation(tracker, context, 
				address.getValue().add(BigInteger.valueOf(offset)), isRuntimeAddress);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.symbols.IVariableLocation#getLocationName(org.eclipse.cdt.dsf.service.DsfServicesTracker)
	 */
	public String getLocationName() {
		if (!isRuntimeAddress) {
			try {
				return SymbolsMessages.MemoryVariableLocation_Hex + Long.toHexString(getRealAddress().getValue().longValue());
			} catch (CoreException e) {
				return SymbolsMessages.MemoryVariableLocation_Hex + Long.toHexString(address.getValue().longValue()) + SymbolsMessages.MemoryVariableLocation_LinkTime; // should not happen
			}
		} else {
			return SymbolsMessages.MemoryVariableLocation_Hex + Long.toHexString(address.getValue().longValue());
		}
	}

	public void writeValue(final int bytes, BigInteger value) throws CoreException {
		final byte[] buffer = MemoryUtils.convertSignedBigIntToByteArray(value, getEndian(), bytes);
		final IAddress theAddress = !isRuntimeAddress ? getRealAddress() : address;
		final ExecutionDMC exeDMC = DMContexts.getAncestorOfType(context, ExecutionDMC.class);
	    final Memory memory = tracker.getService(Memory.class);
		IStatus status = memory.setMemory(exeDMC, theAddress, 1, bytes, buffer);
		if (!status.isOK()) {
			throw EDCDebugger.newCoreException(MessageFormat.format(
					SymbolsMessages.MemoryVariableLocation_CannotWriteAddrFormat, theAddress.toHexAddressString()), status.getException());
		}
	}
}

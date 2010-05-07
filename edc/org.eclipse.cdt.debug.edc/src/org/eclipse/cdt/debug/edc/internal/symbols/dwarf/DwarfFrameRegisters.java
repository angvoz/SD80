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

package org.eclipse.cdt.debug.edc.internal.symbols.dwarf;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.ValueVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFrameRegisterProvider.AbstractInstruction;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFrameRegisterProvider.AbstractRule;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFrameRegisterProvider.FrameDescriptionEntry;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFrameRegisterProvider.InstructionState;
import org.eclipse.cdt.debug.edc.services.IFrameRegisters;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IRangeList.Entry;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

/**
 * This class wraps the cached values from reading unwind information from the CIE and FDE
 * records along each frame of the call stack to a given stack frame.
 * This must be recreated on each context suspend.
 */
public class DwarfFrameRegisters implements IFrameRegisters {

	private final DsfServicesTracker tracker;
	private final StackFrameDMC context;
	private final IFrameRegisters childRegisters;

	// holds BigInteger or CoreException
	private Map<Integer, Object> cachedRegisters = new TreeMap<Integer, Object>();
	private final FrameDescriptionEntry fde;
	private final DwarfDebugInfoProvider provider;
	private Map<Integer, AbstractRule> frameRegisterRules;
	private InstructionState state;
	
	/**
	 * @param provider 
	 * 
	 */
	public DwarfFrameRegisters(DsfServicesTracker tracker,
			StackFrameDMC context, 
			FrameDescriptionEntry fde,
			IFrameRegisters childRegisters, DwarfDebugInfoProvider provider) {
		if (childRegisters == null)
			throw new NullPointerException();
		this.provider = provider;
		this.tracker = tracker;
		this.context = context;
		this.fde = fde;
		this.childRegisters = childRegisters;
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IFrameRegisters#getRegister(int)
	 */
	public BigInteger getRegister(int regnum, int bytes) throws CoreException {
		BigInteger value = null;
		CoreException exception = null;
		Object object = cachedRegisters.get(regnum);
		if (object == null) {
			try {
				IVariableLocation loc = getRegisterLocation(regnum);
				if (loc == null)
					throw EDCDebugger.newCoreException(MessageFormat.format(DwarfMessages.DwarfFrameRegisters_CannotReadRegister,
							regnum, context.getIPAddress().toHexAddressString()));
				value = loc.readValue(bytes);
				cachedRegisters.put(regnum, value);
			} catch (CoreException e) {
				exception = e;
				cachedRegisters.put(regnum, exception);
			}
		} else {
			if (object instanceof CoreException)
				exception = (CoreException) object;
			else
				value = (BigInteger) object;
		}
		
		if (exception != null)
			throw exception;
		
		return value;
	}


	/**
	 * Calculate the location of a register for the current frame and PC.
	 * @param regnum
	 * @return location or <code>null</code> if undefined
	 * @throws CoreException in case of a problem calculating the location
	 */
	private IVariableLocation getRegisterLocation(int regnum) throws CoreException {
		
		if (frameRegisterRules == null) {
			state = new InstructionState(tracker, context, childRegisters, fde);
			frameRegisterRules = calculateFrameRegisterRules(state);
		}
		
		AbstractRule rule = frameRegisterRules.get(regnum);
		if (rule == null) {
			// Note: according to DWARF-3 spec, any register not mentioned here is undefined.
			// But DWARF-2 producers didn't know what to do (or is it ABI-defined?), 
			// so for these, assume the current register for now
			if (fde.getCIE().version < 3)
				if (regnum == state.getCFARegister())
					return new ValueVariableLocation(state.readCFA());
				else
					return new ValueVariableLocation(childRegisters.getRegister(regnum, fde.addressSize));
			else
				return null;
		}
		
		return rule.evaluate(state);
	}


	/**
	 * Create locations for every register in this frame.
	 * @param state 
	 * @return mapping of register to location
	 * @throws CoreException
	 */
	private Map<Integer, AbstractRule> calculateFrameRegisterRules(InstructionState state) throws CoreException {
		if (fde.getCIE() == null)
			throw EDCDebugger.newCoreException(MessageFormat.format(DwarfMessages.DwarfFrameRegisters_NoCommonInfoEntry, fde));
		
		List<AbstractInstruction> initialLocationInstructions =
			fde.getCIE().getInitialLocations(provider);

		for (AbstractInstruction instr : initialLocationInstructions) {
			instr.applyInstruction(state);
		}

		// Run through rules until we hit one past our range
		TreeMap<Entry, List<AbstractInstruction>> fdeInstrs = fde.getInstructions(provider); 
		
		Modules modules = tracker.getService(Modules.class);
		ModuleDMC module = modules.getModuleByAddress(DMContexts.getAncestorOfType(context, ISymbolDMContext.class), context.getIPAddress());
		long currentPC = module.toLinkAddress(context.getIPAddress()).getValue().longValue();
		
		for (Map.Entry<Entry, List<AbstractInstruction>> instrEntry : fdeInstrs.entrySet()) {
			Entry entry = instrEntry.getKey();
			if (entry.low > currentPC)		// execute all instructions <= PC
				break;
			
			try {
				for (AbstractInstruction instr : instrEntry.getValue()) {
					instr.applyInstruction(state);
				}
			} catch (Exception e) {
				throw EDCDebugger.newCoreException(DwarfMessages.DwarfFrameRegisters_ErrorCalculatingLocation, e);
			}
		}
		
		return state.regRules;
	}


	public void writeRegister(int regnum, int bytes, BigInteger value) throws CoreException {
		CoreException exception = null;
		try {
			IVariableLocation loc = getRegisterLocation(regnum);
			if (loc == null)
				throw EDCDebugger.newCoreException(MessageFormat.format(DwarfMessages.DwarfFrameRegisters_CannotWriteRegister,
						regnum, context.getIPAddress().toHexAddressString()));
			loc.writeValue(bytes, value);
			cachedRegisters.put(regnum, value);
		} catch (CoreException e) {
			exception = e;
			cachedRegisters.put(regnum, exception);
		}
		
		if (exception != null)
			throw exception;
	}

}

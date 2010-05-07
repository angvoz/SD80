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

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.edc.IStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.MemoryStreamBuffer;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules;
import org.eclipse.cdt.debug.edc.internal.services.dsf.Modules.ModuleDMC;
import org.eclipse.cdt.debug.edc.internal.services.dsf.RunControl.ExecutionDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.MemoryVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.RegisterOffsetVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.RegisterVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.ValueVariableLocation;
import org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfDebugInfoProvider.AttributeValue;
import org.eclipse.cdt.debug.edc.services.IFrameRegisterProvider;
import org.eclipse.cdt.debug.edc.services.IFrameRegisters;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.debug.edc.symbols.IRangeList;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.debug.edc.symbols.IRangeList.Entry;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;

/**
 * Apply the rules for the CIE and FDE to a given frame.
 */
public class DwarfFrameRegisterProvider implements IFrameRegisterProvider {
	
	public static class InstructionState {
		public DsfServicesTracker tracker;
		/**  Calculated register locations for the current frame */
		public Map<Integer, AbstractRule> regRules;
		/**  Context for the current frame */
		public IFrameDMContext context;
		/**  Accessor for registers from child frame */
		public IFrameRegisters childRegisters;
		/**  Initial locations from CIE */
		public Map<Integer, AbstractRule> initialRules;
		/**  Implicit stack for DW_CFA_remember_state and DW_CFA_restore_state*/
		public Stack<Map<Integer, AbstractRule>> stateStack;
		
		CFARegisterRule cfaRegRule = new CFARegisterRule(0, 0);
		
		private final int addressSize;
		public boolean cfaOffsetsAreReversed;
		
		public InstructionState(DsfServicesTracker tracker,
				IFrameDMContext context,
				IFrameRegisters childRegisters,
				FrameDescriptionEntry fde) {
			this.tracker = tracker;
			this.addressSize = fde.addressSize;
			this.cfaOffsetsAreReversed = fde.getCIE().cfaOffsetsAreReversed;
			this.regRules = new TreeMap<Integer, AbstractRule>();
			this.stateStack = new Stack<Map<Integer,AbstractRule>>();
			this.initialRules = new TreeMap<Integer, AbstractRule>();
	
			this.context = context;
			this.childRegisters = childRegisters;
			//this.cfaValue = BigInteger.ZERO;
		}

		// pseudo register for current CFA
		public static final int CFA = -1;
		
		/**
		 * Read the current CFA.
		 * @return
		 */
		public BigInteger readCFA() throws CoreException {
			BigInteger regval = childRegisters.getRegister(cfaRegRule.regnum, addressSize);
			return regval.add(BigInteger.valueOf(cfaOffsetsAreReversed ? -cfaRegRule.offset : cfaRegRule.offset));
		}

		/**
		 * Get the register which is the CFA base.
		 * @return register number
		 */
		public int getCFARegister() {
			return cfaRegRule.regnum;
		}
		
		
	};
	
	public static abstract class AbstractRule {
		public abstract IVariableLocation evaluate(InstructionState state) throws CoreException;
	}
	
	static class ErrorRule extends AbstractRule {
		private String message;

		public ErrorRule(String message) {
			this.message = message;
		}
		@Override
		public String toString() {
			return "error: " + message;
		}
		
		@Override
		public IVariableLocation evaluate(InstructionState state) throws CoreException {
			throw EDCDebugger.newCoreException(message);
		}
	}
	
	static class UndefinedRule extends AbstractRule {
		private final int regnum;

		public UndefinedRule(int regnum) {
			this.regnum = regnum;
		}
		@Override
		public String toString() {
			return "R"+regnum+": undefined";
		}
		@Override
		public IVariableLocation evaluate(InstructionState state) throws CoreException {
			return null;
		}
	}
	
	static class SameValueRule extends AbstractRule {
		private final int regnum;

		public SameValueRule(int regnum) {
			this.regnum = regnum;
		}
		@Override
		public String toString() {
			return "R"+regnum+": same value";
		}
		@Override
		public IVariableLocation evaluate(InstructionState state) throws CoreException {
			return new ValueVariableLocation(state.childRegisters.getRegister(regnum, state.addressSize));
		}
	}
	
	static class OffsetRule extends AbstractRule {
		private final long offset;

		public OffsetRule(long offset) {
			this.offset = offset;
		}
		@Override
		public String toString() {
			return "offset("+offset+")";
		}
		@Override
		public IVariableLocation evaluate(InstructionState state) throws CoreException {
			BigInteger cfa = state.readCFA();
			return new MemoryVariableLocation(state.tracker, state.context, 
					cfa.add(BigInteger.valueOf(offset)), true);
		}
	}
	static class ValueOffsetRule extends AbstractRule {
		private final long offset;

		public ValueOffsetRule(long offset) {
			this.offset = offset;
		}
		@Override
		public String toString() {
			return "val_offset("+offset+")";
		}
		@Override
		public IVariableLocation evaluate(InstructionState state) throws CoreException {
			BigInteger cfa = state.readCFA();
			return new ValueVariableLocation(cfa.add(BigInteger.valueOf(offset)));
		}
	}
	static class RegisterRule extends AbstractRule {
		private final int regnum;

		public RegisterRule(int regnum) {
			this.regnum = regnum;
		}
		@Override
		public String toString() {
			return "register("+regnum+")";
		}
		@Override
		public IVariableLocation evaluate(InstructionState state) throws CoreException {
			return new RegisterVariableLocation(state.tracker, state.context, null, regnum);
		}
	}
	
	static class CFARegisterRule extends AbstractRule {
		private int regnum;
		private long offset;

		public CFARegisterRule(int regnum, long offset) {
			this.regnum = regnum;
			this.offset = offset;
		}
		@Override
		public String toString() {
			return "["+regnum+"]"+(offset < 0 ? "-" : "+")+Math.abs(offset);
		}
		@Override
		public IVariableLocation evaluate(InstructionState state) throws CoreException {
			return new RegisterOffsetVariableLocation(state.tracker, state.context, null, regnum, offset);
		}
	}
	private static ErrorRule UNIMPLEMENTED = new ErrorRule(
		"unimplemented support for reading this location");
	

	/** The base class for instructions
	 */
	public static abstract class AbstractInstruction {
		
		public AbstractInstruction() {
		}
		
		/**
		 * Apply this instruction to the state.
		 * @param state 
		 * @throws CoreException
		 */
		abstract public void applyInstruction(InstructionState state) throws CoreException;
	}
	
	/** The base class for rules that apply to registers 
	 */
	public static abstract class AbstractRegisterInstruction extends AbstractInstruction {
		protected final int thereg;
		
		public AbstractRegisterInstruction(int thereg) {
			super();
			this.thereg = thereg;
		}
		
		@Override
		public String toString() {
			return "R"+thereg;
		}
	}
	
	public static class NopInstruction extends AbstractInstruction {

		public NopInstruction() {
			super();
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFrameRegisterProvider.AbstractRegisterRule#toString()
		 */
		@Override
		public String toString() {
			return "nop";
		}
		

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.debug.edc.internal.symbols.dwarf.DwarfFrameRegisterProvider.AbstractRegisterRule#applyRule(org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext, java.util.Map, java.util.Map)
		 */
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			
		}
		
	}
	/** CFA=RN+o */
	public static class DefCFAInstruction extends AbstractInstruction {
		long regnum;
		long offset;
		public DefCFAInstruction(long regnum, long offset) {
			this.regnum = regnum;
		}
		@Override
		public String toString() {
			return "CFA := R"+regnum + " + " + offset;
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.cfaRegRule.regnum = (int) regnum;
			state.cfaRegRule.offset = offset; 
		}
	}
	
	/** CFA=RN (+o) */
	public static class DefCFARegisterInstruction extends AbstractInstruction {
		long regnum;
		public DefCFARegisterInstruction(long regnum) {
			this.regnum = regnum;
		}
		@Override
		public String toString() {
			return "CFA register := R"+regnum;
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.cfaRegRule.regnum = (int) regnum;
		}
	}
	

	/** CFA=(RN) +o */
	public static class DefCFAOffsetInstruction extends AbstractInstruction {
		long offset;
		public DefCFAOffsetInstruction(long offset) {
			this.offset = offset;
		}
		@Override
		public String toString() {
			return "CFA offset := "+offset;
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.cfaRegRule.offset = offset;
		}
	}
	

	public static class DefCFAExpressionInstruction extends AbstractInstruction {
		IStreamBuffer expression;
		public DefCFAExpressionInstruction(IStreamBuffer expression) {
			this.expression = expression;
		}
		@Override
		public String toString() {
			return "CFA := expression";
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			throw EDCDebugger.newCoreException("CFA expression not implemented");
		}
	}
	
	public static class SameValueInstruction extends AbstractRegisterInstruction {
		public SameValueInstruction(int thereg) {
			super(thereg);
		}
		@Override
		public String toString() {
			return super.toString() + "same value";
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.regRules.put(thereg, new SameValueRule(thereg));
		}
	}
	
	public static class UndefinedInstruction extends AbstractRegisterInstruction {
		public UndefinedInstruction(int thereg) {
			super(thereg);
		}
		@Override
		public String toString() {
			return super.toString() + "undefined";
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.regRules.put(thereg, new UndefinedRule(thereg));
		}
	}

	public static class OffsetInstruction extends AbstractRegisterInstruction {
		long offset;

		public OffsetInstruction(int thereg, long offset) {
			super(thereg);
			this.offset = offset;
		}
		@Override
		public String toString() {
			return super.toString() + "@ CFA + " + offset;
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.regRules.put(thereg, new OffsetRule(offset));
		}
	}
	
	public static class ValueOffsetInstruction extends AbstractRegisterInstruction {
		long offset;
		public ValueOffsetInstruction(int thereg, long offset) {
			super(thereg);
			this.offset = offset;
		}
		@Override
		public String toString() {
			return super.toString() + "CFA + " + offset;
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.regRules.put(thereg, new ValueOffsetRule(offset));
		}
	}
	
	public static class RegisterInstruction extends AbstractRegisterInstruction {
		int regnum;
		public RegisterInstruction(int thereg, int regnum) {
			super(thereg);
			this.regnum = regnum;
		}
		@Override
		public String toString() {
			return super.toString() + "copy R"+regnum;
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			if (state.regRules.containsKey(regnum))
				state.regRules.put(thereg, state.regRules.get(regnum));
			else
				state.regRules.put(thereg, new UndefinedRule(regnum));
		}
	}
	
	public static class ExpressionInstruction extends AbstractRegisterInstruction {
		IStreamBuffer expression;
		public ExpressionInstruction(int thereg, IStreamBuffer expression) {
			super(thereg);
			this.expression = expression;
		}
		@Override
		public String toString() {
			return super.toString() + "@ expression";
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.regRules.put(thereg, UNIMPLEMENTED);
		}
	}
	public static class ValueExpressionInstruction extends AbstractRegisterInstruction {
		IStreamBuffer expression;
		public ValueExpressionInstruction(int thereg, IStreamBuffer expression) {
			super(thereg);
			this.expression = expression;
		}
		@Override
		public String toString() {
			return super.toString() + " expression";
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.regRules.put(thereg, UNIMPLEMENTED);
		}
	}
	public static class RestoreInstruction extends AbstractRegisterInstruction {
		public RestoreInstruction(int thereg) {
			super(thereg);
		}
		@Override
		public String toString() {
			return super.toString() + " restore";
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			if (state.initialRules.containsKey(thereg))
				state.regRules.put(thereg, state.initialRules.get(thereg));
			else
				state.regRules.remove(thereg);	
		}
	}
	
	public static class RememberStateInstruction extends AbstractInstruction {
		public RememberStateInstruction() {
			super();
		}
		@Override
		public String toString() {
			return "remember state";
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.stateStack.push(new TreeMap<Integer, AbstractRule>(state.regRules));
		}
	}
	public static class RestoreStateInstruction extends AbstractInstruction {
		public RestoreStateInstruction() {
			super();
		}
		@Override
		public String toString() {
			return "restore state";
		}
		@Override
		public void applyInstruction(InstructionState state) throws CoreException {
			state.regRules.clear();
			state.regRules.putAll(state.stateStack.pop());
		}
	}


	/**
	 * A "CIE" from the .debug_frame section.
	 */
	public static class CommonInformationEntry {
	
		final long codeAlignmentFactor;
		final long dataAlignmentFactor;
		final int version;
		final int returnAddressRegister;
		final int addressSize;
		final IStreamBuffer instructions;
		private List<AbstractInstruction> initialLocations;
		private CoreException initialLocationsError;
		private boolean cfaOffsetSfIsFactored;
		private boolean cfaOffsetsAreReversed;
		
		public CommonInformationEntry(long codeAlignmentFactor,
				long dataAlignmentFactor, int returnAddressRegister,
				int version,
				IStreamBuffer instructions,
				int addressSize, 
				String producer, String augmentation) {
			
			this.codeAlignmentFactor = codeAlignmentFactor;
			this.dataAlignmentFactor = dataAlignmentFactor;
			this.returnAddressRegister = returnAddressRegister;
			this.version = version;
			this.instructions = instructions;
			this.addressSize = addressSize;
			
			checkAugmentations(producer, augmentation);
		}
		
		/**
		 * Handle augmentations we find.
		 */
		private void checkAugmentations(String producer, String augmentation) {
			// RVCT has bugs with the frame info in DWARF 1/2 and some DWARF 3
			
			if (augmentation.startsWith("armcc") && augmentation.contains("+")) {
				// this means bugs are fixed
				
			} else {
				if (producer != null && producer.contains("RVCT")) { //$NON-NLS-1$
					if (version == 1) {
						cfaOffsetSfIsFactored = true;
						cfaOffsetsAreReversed = true;
					}
					
					if (version == 3) {
						cfaOffsetsAreReversed = true;
					}
				}
			}
		}

		/**
		 * Get the rules defining initial locations for all the registers 
		 * @return list of instructions
		 */
		public List<AbstractInstruction> getInitialLocations(DwarfDebugInfoProvider provider) throws CoreException {
			if (initialLocations == null) {
				try {
					initialLocations = parseInitialLocations(provider);
				} catch (CoreException e) {
					initialLocationsError = e;
				}
			}
			if (initialLocationsError != null)
				throw initialLocationsError;
			return initialLocations;
		}
		
		/**
		 * Parse the instructions for calculating the initial locations for all the registers 
		 * @return map of register to rule
		 */
		public List<AbstractInstruction> parseInitialLocations(DwarfDebugInfoProvider provider) throws CoreException {
			List<AbstractInstruction> instrs = new ArrayList<AbstractInstruction>();

			IStreamBuffer buffer = instructions.wrapSubsection(instructions.capacity());
			
			buffer.position(0);
			
			try {
				while (buffer.hasRemaining()) {
					int opcode = buffer.get() & 0xff;
					AbstractInstruction inst = parseInstruction(opcode, buffer, provider, addressSize, dataAlignmentFactor);
					if (inst != null)
						instrs.add(inst);
				}
			} catch (Exception e) {
				throw EDCDebugger.newCoreException("Malformed data at " + buffer, e);
			}

			
			return instrs;
		}

		public AbstractInstruction parseInstruction(int opcode, IStreamBuffer buffer, 
				DwarfDebugInfoProvider provider, int addressSize, long dataAlignmentFactor) throws IOException {
			int reg;
			long offset;
			IStreamBuffer expr;
			
			//
			// CFA DEFINITION INSTRUCTIONS
			//
			if (opcode >= DwarfConstants.DW_CFA_offset && opcode < DwarfConstants.DW_CFA_offset + 0x40) {
				reg = opcode - DwarfConstants.DW_CFA_offset;
				offset = DwarfInfoReader.read_unsigned_leb128(buffer) * dataAlignmentFactor;
				return new OffsetInstruction(reg, offset);
			} else if (opcode >= DwarfConstants.DW_CFA_restore && opcode < DwarfConstants.DW_CFA_restore + 0x40) {
				reg = opcode - DwarfConstants.DW_CFA_restore;
				return new RestoreInstruction(reg);
			} else {
				switch (opcode) {
				case DwarfConstants.DW_CFA_nop:
					// ignore
					return new NopInstruction();
				case DwarfConstants.DW_CFA_def_cfa:
					reg = readRegister(buffer);
					offset = DwarfInfoReader.read_unsigned_leb128(buffer);
					if (cfaOffsetSfIsFactored) {
						offset *= dataAlignmentFactor;
					}
					return new DefCFAInstruction(reg, offset);
				case DwarfConstants.DW_CFA_def_cfa_sf:
					reg = readRegister(buffer);
					offset = DwarfInfoReader.read_signed_leb128(buffer) * dataAlignmentFactor;
					return new DefCFAInstruction(reg, offset);
				case DwarfConstants.DW_CFA_def_cfa_register: {
					reg = readRegister(buffer);
					return new DefCFARegisterInstruction(reg);
				}
				case DwarfConstants.DW_CFA_def_cfa_offset: {
					offset = DwarfInfoReader.read_unsigned_leb128(buffer); // non-factored, usually
					if (cfaOffsetSfIsFactored) {
						offset *= dataAlignmentFactor;
					}
					return new DefCFAOffsetInstruction(offset);
				}
				case DwarfConstants.DW_CFA_def_cfa_offset_sf: {
					offset = DwarfInfoReader.read_signed_leb128(buffer) * dataAlignmentFactor;
					return new DefCFAOffsetInstruction(offset);
				}
				case DwarfConstants.DW_CFA_def_cfa_expression: {
					byte form = buffer.get();
					expr = readExpression(form, addressSize, provider, buffer);
					return new DefCFAExpressionInstruction(expr);
				}
				}
			}
				
			//
			// REGISTER RULE INSTRUCTIONS
			//
			if (opcode >= DwarfConstants.DW_CFA_offset && opcode < DwarfConstants.DW_CFA_offset + 0x40) {
				reg = opcode - DwarfConstants.DW_CFA_offset;
				offset = DwarfInfoReader.read_unsigned_leb128(buffer) * dataAlignmentFactor;
				return new OffsetInstruction(reg, offset);
			} else if (opcode >= DwarfConstants.DW_CFA_restore && opcode < DwarfConstants.DW_CFA_restore + 0x40) {
				reg = opcode - DwarfConstants.DW_CFA_restore;
				return new RestoreInstruction(reg);
			} else {
				switch (opcode) {
				case DwarfConstants.DW_CFA_nop:
					break;
					
				case DwarfConstants.DW_CFA_undefined:
					reg = readRegister(buffer);
					return new UndefinedInstruction(reg);
				case DwarfConstants.DW_CFA_same_value:
					reg = readRegister(buffer);
					return new SameValueInstruction(reg);
				case DwarfConstants.DW_CFA_offset_extended:
					reg = readRegister(buffer);
					offset = DwarfInfoReader.read_unsigned_leb128(buffer) * dataAlignmentFactor;
					return new OffsetInstruction(reg, offset);
				case DwarfConstants.DW_CFA_offset_extended_sf:
					reg = readRegister(buffer);
					offset = DwarfInfoReader.read_signed_leb128(buffer) * dataAlignmentFactor;
					return new OffsetInstruction(reg, offset);
				case DwarfConstants.DW_CFA_val_offset:
					reg = readRegister(buffer);
					offset = DwarfInfoReader.read_unsigned_leb128(buffer) * dataAlignmentFactor;
					return new ValueOffsetInstruction(reg, offset);
				case DwarfConstants.DW_CFA_val_offset_sf:
					reg = readRegister(buffer);
					offset = DwarfInfoReader.read_signed_leb128(buffer) * dataAlignmentFactor;
					return new ValueOffsetInstruction(reg, offset);
				case DwarfConstants.DW_CFA_register: {
					reg = readRegister(buffer);
					int otherReg = readRegister(buffer);
					return new RegisterInstruction(reg, otherReg);
				}
				case DwarfConstants.DW_CFA_expression: {
					reg = readRegister(buffer);
					byte form = buffer.get();
					expr = readExpression(form, addressSize, provider, buffer);
					return new ExpressionInstruction(reg, expr);
				}
				case DwarfConstants.DW_CFA_val_expression: {
					reg = readRegister(buffer);
					byte form = buffer.get();
					expr = readExpression(form, addressSize, provider, buffer);
					return new ValueExpressionInstruction(reg, expr);
				}
				case DwarfConstants.DW_CFA_restore_extended:
					reg = readRegister(buffer);
					return new RestoreInstruction(reg);
				case DwarfConstants.DW_CFA_remember_state:
					return new RememberStateInstruction();
				case DwarfConstants.DW_CFA_restore_state:
					return new RestoreStateInstruction();
				}
			}
			return null;
		}

		/**
		 * @param buffer
		 * @return
		 * @throws IOException
		 */
		private int readRegister(IStreamBuffer buffer) throws IOException {
			return (int) DwarfInfoReader.read_unsigned_leb128(buffer);
		}

		private IStreamBuffer readExpression(byte form, int addressSize, 
				DwarfDebugInfoProvider provider, IStreamBuffer buffer) {
			AttributeValue value = new AttributeValue(form, buffer, (byte) addressSize, null);
			return new MemoryStreamBuffer(value.getValueAsBytes(), 
					provider.getExecutableSymbolicsReader().getByteOrder());
		}
		
	}


	/**
	 * A "FDE" entry from the .debug_frame section.
	 */
	public static class FrameDescriptionEntry {
	
		final Long fdePtr;
		final Long ciePtr;
		final int addressSize;
		private final IStreamBuffer instructions;
		
		private final long low, high;
		
		private CommonInformationEntry cie;
		private CoreException parseException;
		private TreeMap<Entry, List<AbstractInstruction>> instructionMap;
		
		public CommonInformationEntry getCIE() {
			return cie;
		}
	
		public void setCIE(CommonInformationEntry cie) {
			this.cie = cie;
		}
	
		public FrameDescriptionEntry(long fdePtr, long ciePtr, long low, long high, 
				IStreamBuffer instructions, int addressSize) {
			this.ciePtr = ciePtr;
			this.fdePtr = fdePtr;
			this.instructions = instructions;
			this.addressSize = addressSize;
			this.low = low;
			this.high = high;
		}
	
		@Override
		public String toString() {
			return "FDE at " + instructions + " with CIE " + (cie != null ? cie : ciePtr);
		}
	
		/**
		 * Get the register for this frame which denotes the return address
		 * @return register number
		 */
		public int getReturnAddressRegister() {
			return cie != null ? cie.returnAddressRegister : 0;
		}

		/**
		 * Get the instruction map for the FDE
		 * @param provider
		 * @return
		 */
		public TreeMap<IRangeList.Entry, List<AbstractInstruction>> getInstructions(
				DwarfDebugInfoProvider provider) throws CoreException {
			if (instructionMap == null) {
				try {
					instructionMap = parseRules(provider);
				} catch (CoreException e) {
					parseException = e;
				}
			}
			if (parseException != null)
				throw parseException;
			return instructionMap;
		}
		
		private TreeMap<Entry, List<AbstractInstruction>> parseRules(
				DwarfDebugInfoProvider provider) throws CoreException {
			TreeMap<IRangeList.Entry, List<AbstractInstruction>> rules = new TreeMap<IRangeList.Entry, List<AbstractInstruction>>();

			IStreamBuffer buffer = instructions.wrapSubsection(instructions.capacity());
			
			buffer.position(0);
			
			// adjust range and store row as we see set_loc/advance_loc instructions
			long current = low;
			List<AbstractInstruction> row = new ArrayList<AbstractInstruction>();
			
			try {
				while (buffer.hasRemaining()) {
					int opcode = buffer.get() & 0xff;
					
					//
					// ROW CREATION INSTRUCTIONS
					//
					long offset = -1;
					if (opcode >= DwarfConstants.DW_CFA_advance_loc && opcode < DwarfConstants.DW_CFA_advance_loc + 0x40) {
						offset = (opcode - DwarfConstants.DW_CFA_advance_loc) * cie.codeAlignmentFactor;
					} else {
						switch (opcode) {
						case DwarfConstants.DW_CFA_set_loc:
							offset = DwarfInfoReader.readAddress(buffer, addressSize) - current;
							break;
						case DwarfConstants.DW_CFA_advance_loc1:
							offset = (buffer.get() & 0xff) * cie.codeAlignmentFactor;
							break;
						case DwarfConstants.DW_CFA_advance_loc2:
							offset = (buffer.getShort() & 0xffff) * cie.codeAlignmentFactor;
							break;
						case DwarfConstants.DW_CFA_advance_loc4:
							offset = (buffer.getInt() & 0xffffffff) * cie.codeAlignmentFactor;
							break;
						}
					}

					if (offset > 0) {
						rules.put(new IRangeList.Entry(current, current + offset), row);
						current += offset;
						row = new ArrayList<DwarfFrameRegisterProvider.AbstractInstruction>();
						continue;
					}
					
					//
					// REGISTER RULE INSTRUCTIONS
					// 
					
					AbstractInstruction instr = cie.parseInstruction(opcode, buffer, provider, addressSize, cie.dataAlignmentFactor);
					if (instr != null) {
						row.add(instr);
					} else {
						assert(false);
						throw EDCDebugger.newCoreException("Unimplemented opcode " + opcode + " at " + buffer);
					}
				}
			} catch (CoreException e) {
				throw e;
			} catch (Exception e) {
				throw EDCDebugger.newCoreException("error parsing FDE rules at " + buffer, e);
			}
			
			if (!row.isEmpty()) {
				// finish instruction stream
				rules.put(new IRangeList.Entry(current, high), row);
			}
			return rules;
		}
	}

	private DwarfDebugInfoProvider provider;

	public DwarfFrameRegisterProvider(
			DwarfDebugInfoProvider provider) {
		this.provider = provider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.internal.services.dsf.IFrameRegisterProvider#dispose()
	 */
	public void dispose() {
		provider = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.edc.services.IFrameRegisterProvider#getFrameRegisters(org.eclipse.cdt.dsf.service.DsfSession, org.eclipse.cdt.dsf.service.DsfServicesTracker, org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext)
	 */
	public IFrameRegisters getFrameRegisters(DsfSession session,
			DsfServicesTracker tracker,
			IFrameDMContext context) throws CoreException {
		Registers registers = tracker.getService(Registers.class);
		IStack stack = tracker.getService(IStack.class);
		ExecutionDMC exeDMC = DMContexts.getAncestorOfType(context, ExecutionDMC.class);
		
		if (registers == null || exeDMC == null || !(context instanceof StackFrameDMC))
			throw EDCDebugger.newCoreException("cannot read frame");
		
		StackFrameDMC stackFrame = (StackFrameDMC) context;

		if (stackFrame.getLevel() == 0) {
			// shouldn't actually get here, but whatevah 
			return new org.eclipse.cdt.debug.edc.services.Stack.CurrentFrameRegisters(exeDMC, registers);
		}
		
		// get the child frame's registers
		StackFrameDMC childFrame = getChildFrame(session, stack, exeDMC, stackFrame);
		if (childFrame == null)
			return null;
		
		IFrameRegisters childRegisters = childFrame.getFrameRegisters();
		
		Modules modules = tracker.getService(Modules.class);
		FrameDescriptionEntry currentFrameEntry = provider.findFrameDescriptionEntry(getLinkAddress(
				exeDMC, modules, childFrame.getIPAddress()));
		if (currentFrameEntry == null) 
			return null;
		
		return new DwarfFrameRegisters(tracker, childFrame, currentFrameEntry, childRegisters, provider);
	}

	/**
	 * Find the frame called by stackFrame
	 */
	private StackFrameDMC getChildFrame(DsfSession session, IStack stack,
			ExecutionDMC exeDMC, StackFrameDMC stackFrame) throws CoreException {
		StackFrameDMC childFrame = stackFrame.getCalledFrame();
		return childFrame;
	}

	/**
	 * @param modules
	 * @param ipAddress
	 * @param exeDMC 
	 * @return
	 */
	private IAddress getLinkAddress(ExecutionDMC exeDMC, Modules modules, IAddress ipAddress) {
		IAddress linkaddress = ipAddress;
		ModuleDMC module = modules.getModuleByAddress(exeDMC.getSymbolDMContext(), ipAddress);
		if (module != null) {
			linkaddress = module.toLinkAddress(ipAddress);
		}
		return linkaddress;
	}

}

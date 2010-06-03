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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.edc.internal.symbols.IArrayType;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.core.runtime.CoreException;

/**
 * Instruction sequence
 */
public class InstructionSequence {

	private List<Instruction> fInstructions;
	/**
	 * A collection of error messages (<code>String</code>) that occurred while
	 * creating this expression
	 */
	private List<String> fErrors;
	private String fSnippet;
	private CoreException fException;

	/**
	 * Constructor for an instruction sequence
	 * 
	 * @param snippet
	 *            - expression the instruction sequence represents
	 */
	public InstructionSequence(String snippet) {
		fInstructions = new ArrayList<Instruction>(10);
		fErrors = new ArrayList<String>();
		fSnippet = snippet;
	}

	/**
	 * Get the runtime exception that occurred while evaluating this expression
	 * 
	 * @param runtime
	 *            exception, or <code>null</code> if no exception occurred
	 */
	public CoreException getException() {
		return fException;
	}

	/**
	 * @see ICompiledExpression#getSnippet()
	 */
	public String getSnippet() {
		return fSnippet;
	}

	/**
	 * Adds the given error to the list of errors that occurred while compiling
	 * this instruction sequence
	 */
	public void addError(String error) {
		fErrors.add(error);
	}

	/**
	 * @see ICompiledExpression#hasErrors()
	 */
	public boolean hasErrors() {
		return !fErrors.isEmpty();
	}

	/**
	 * @see org.eclipse.jdt.debug.eval.ICompiledExpression#getErrorMessages()
	 */
	public String[] getErrorMessages() {
		return fErrors.toArray(new String[fErrors.size()]);
	}

	/**
	 * Get the array of instructions
	 * 
	 * return array of instructions, or an empty array
	 */
	public Instruction[] getInstructions() {
		int size = fInstructions.size();
		Instruction[] instructions = new Instruction[size];
		if (size > 0) {
			fInstructions.toArray(instructions);
		}
		return instructions;
	}

	/**
	 * Get the instruction at the given address
	 * 
	 * @param address
	 *            - address of instruction
	 * @return instruction at the address
	 */
	public Instruction getInstruction(int address) {
		return fInstructions.get(address);
	}

	/**
	 * Add the given instruction to the end of the list
	 * 
	 * @param instruction
	 *            - instruction to add
	 */
	public void add(Instruction instruction) {
		fInstructions.add(instruction);
	}

	/**
	 * Get the index of an instruction
	 * 
	 * @param instruction
	 *            - instruction to find
	 * @return index of instruction, or -1 if the instruction does not exist
	 */
	public int indexOf(Instruction instruction) {
		return fInstructions.indexOf(instruction);
	}

	/**
	 * Tell whether the instruction sequence is empty
	 * 
	 * @return true if the instruction sequence is empty, and false otherwise
	 */
	public boolean isEmpty() {
		return fInstructions.isEmpty();
	}

	/**
	 * Insert the instruction at the given index. If the index is less than 0 or
	 * greater than the current instruction count, the instruction is added at
	 * the end of the sequence.
	 * 
	 * Instructs the instructions to update their program counters.
	 */
	public void insert(Instruction instruction, int index) {
		fInstructions.add(index, instruction);
	}

	/**
	 * Get the instruction at the given address
	 * 
	 * @param address
	 *            - instruction address
	 * @return instruction at the given address
	 */
	public Instruction get(int address) {
		return fInstructions.get(address);
	}

	/**
	 * Get the index of the last instruction in the sequence
	 * 
	 * @return size of the instruction sequence - 1
	 */
	public int getEnd() {
		return fInstructions.size() - 1;
	}

	/**
	 * Remove no-ops from the instruction list
	 */
	public void removeNoOps() {
		for (Iterator<Instruction> iter = fInstructions.iterator(); iter.hasNext(); ) {
			if (iter.next() instanceof NoOp) {
				iter.remove();
			}
		}
	}

	/**
	 * Fixup instructions like:  *(<type>*)&<something>
	 *<p>
	 * We want to avoid getting the address of a register or bitfield and
	 * causing an error, so just make a synthetic "cast value" operator here.
	 * <p>
	 * If there is a concern about incorrectly avoiding an error here -- 
	 * "if the variable is in a register, then this should fail" -- I argue that 
	 * the user doesn't necessarily know it's in a register at compile time, and 
	 * we should try harder to allow evaluating such an expression.  Asking him to 
	 * modify and rebuild his code so the variable will be in memory is a bit draconian.   
	 * @param typeEngine 
	 */
	public void reduceCasts(TypeEngine typeEngine) {
		boolean anyChanges = false;
		for (int i = 0; i < fInstructions.size(); i++) {
			Instruction inst = fInstructions.get(i);
			if ((inst instanceof OperatorAddrOf) && i + 1 < fInstructions.size()) {
				Instruction inst2 = fInstructions.get(i + 1);
				if (inst2 instanceof OperatorCast && i + 2 < fInstructions.size()) {
					Instruction inst3 = fInstructions.get(i + 2);
					if (inst3 instanceof OperatorIndirection) {
						// see if it's a pointer or array
						IType castType = null;
						try {
							castType = TypeUtils.getStrippedType(((OperatorCast) inst2).getCastType(typeEngine));
						} catch (CoreException e) {
							// ignore
							continue;
						}
						if (castType instanceof IPointerType || castType instanceof IArrayType) {
							OperatorCastValue cv = new OperatorCastValue(inst.getSize(), castType.getType());
							fInstructions.set(i, cv);
							fInstructions.set(i + 1, new NoOp(inst2.getSize()));
							fInstructions.set(i + 2, new NoOp(inst3.getSize()));
							anyChanges = true;
						}
					}
				} else if (inst2 instanceof OperatorCastValue) {
					// see if it's a pointer or array
					IType castType;
					try {
						castType = TypeUtils.getStrippedType(((OperatorCastValue)inst2).getCastType(typeEngine));
					} catch (CoreException e) {
						// ignore
						continue;
					}
					if (castType instanceof IPointerType || castType instanceof IArrayType) {
						OperatorCastValue cv = new OperatorCastValue(inst.getSize(), castType.getType());
						fInstructions.set(i, cv);
						fInstructions.set(i + 1, new NoOp(inst2.getSize()));
						anyChanges = true;
					}
				}
			}
		}
		if (anyChanges) {
			removeNoOps();
		}
	}
}

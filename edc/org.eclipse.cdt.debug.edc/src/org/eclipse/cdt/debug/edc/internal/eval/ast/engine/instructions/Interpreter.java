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

import java.util.EmptyStackException;
import java.util.Stack;

import org.eclipse.cdt.debug.edc.symbols.TypeEngine;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;

public class Interpreter {
	private final Instruction[] instructions;
	private int instructionCounter;
	private final IDMContext context;
	private Stack<OperandValue> stack;
	private OperandValue lastValue;

	private boolean fStopped = false;
	private final DsfServicesTracker tracker;
	private final TypeEngine typeEngine;

	/**
	 * Constructor for fInterpreter
	 * @param context 
	 * @param instructionSequence
	 *            - instruction sequence to execute
	 * @param context
	 *            - instruction context
	 */
	public Interpreter(DsfServicesTracker tracker, IDMContext context, 
			TypeEngine typeEngine,
			InstructionSequence instructionSequence) {
		this.tracker = tracker;
		this.context = context;
		this.typeEngine = typeEngine;
		this.instructions = instructionSequence.getInstructions();
	}

	public DsfServicesTracker getServicesTracker() {
		return tracker;
	}

	/**
	 * Execute an instruction sequence
	 * 
	 * @throws CoreException
	 */
	public void execute() throws CoreException {
		reset();
		while (instructionCounter < instructions.length && !fStopped) {
			Instruction instruction = instructions[instructionCounter++];
			Interpreter old = instruction.fInterpreter;
			instruction.setInterpreter(this);
			instruction.execute();
			instruction.setInterpreter(old);
		}
	}

	/**
	 * Stop the fInterpreter
	 */
	public void stop() {
		fStopped = true;
	}

	/**
	 * Reset the fInterpreter
	 */
	private void reset() {
		stack = new Stack<OperandValue>();
		instructionCounter = 0;
	}

	/**
	 * Jump to a relative instruction counter offset
	 * 
	 * @param offset
	 *            - offset from the current instruction counter
	 */
	public void jump(int offset) {
		instructionCounter += offset;
	}

	/**
	 * Push an object on the stack. Disables garbage collection for any interim
	 * object pushed onto the stack. Objects are released after the evaluation
	 * completes.
	 * 
	 * @param object
	 */
	public void push(OperandValue object) {
		stack.push(object);
	}

	/**
	 * Tell whether the stack is empty
	 * 
	 * @return true if the stack is empty, and false otherwise
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * Peek at the top object of the stack
	 * 
	 * @return object on the top of the stack
	 */
	public OperandValue peek() {
		return stack.peek();
	}

	/**
	 * Pop an object off of the stack
	 * 
	 * @return object on the top of the stack
	 */
	public OperandValue pop() throws EmptyStackException {
		return stack.pop();
	}

	/**
	 * Get the context for the fInterpreter
	 * 
	 * @return fInterpreter context
	 */
	public IDMContext getContext() {
		return context;
	}

	/**
	 * Get current instruction result
	 * 
	 * @return current top of stack, or the last stack value if the stack is
	 *         <code>null</code> or empty
	 */
	public OperandValue getResult() {
		if (stack == null || stack.isEmpty()) {
			return lastValue;
		}
		OperandValue top = stack.peek();
		return top;
	}

	public TypeEngine getTypeEngine() {
		return typeEngine;
	}
}

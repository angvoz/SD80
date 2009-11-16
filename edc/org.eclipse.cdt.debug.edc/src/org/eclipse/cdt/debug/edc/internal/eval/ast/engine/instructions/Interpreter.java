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
package org.eclipse.cdt.debug.edc.internal.eval.ast.engine.instructions;

import java.math.BigInteger;
import java.util.EmptyStackException;
import java.util.Stack;

import org.eclipse.cdt.debug.edc.internal.eval.ast.engine.ASTEvaluationEngine;
import org.eclipse.core.runtime.CoreException;

public class Interpreter {
	private final Instruction[] instructions;
	private int instructionCounter;
	private final Object context;
	private Stack<Object> stack;
	private Object lastValue;
	private Object valueLocation;
	private Object valueType;

	/**
	 * Get current instruction's result location
	 * 
	 * @return current instruction's result location
	 */
	public Object getValueLocation() {
		return valueLocation;
	}

	/**
	 * Set current instruction's result location
	 * 
	 * @param valueLocation
	 *            - instruction result location
	 */
	public void setValueLocation(Object valueLocation) {
		this.valueLocation = valueLocation;
	}

	/**
	 * Get current instruction's result type
	 * 
	 * @return current instruction's result type
	 */
	public Object getValueType() {

		// if possible, change an unknown result type to the type of the
		// expression result
		if (valueType instanceof String && ((String) valueType).equals(ASTEvaluationEngine.UNKNOWN_TYPE)) {
			Object result = getResult();
			if (result instanceof Long) {
				// TODO: use architecture-specific limits to either set this to
				// "long" or "long long"
				valueType = "long"; //$NON-NLS-1$
			} else if (result instanceof Double)
				valueType = "double"; //$NON-NLS-1$
			else if (result instanceof Boolean)
				valueType = "bool"; //$NON-NLS-1$
			else if (result instanceof String)
				valueType = "char[]"; //$NON-NLS-1$
			else if (result instanceof Character)
				valueType = "char"; //$NON-NLS-1$
			else if (result instanceof Integer)
				valueType = "int"; //$NON-NLS-1$
			else if (result instanceof Float)
				valueType = "float"; //$NON-NLS-1$
			else if (result instanceof BigInteger) {
				// TODO: use architecture-specific limits to either set this to
				// "long" or "long long"
				valueType = "long long"; //$NON-NLS-1$
			}

			setValueType(valueType);
		}

		return this.valueType;
	}

	/**
	 * Set current instruction's result type
	 * 
	 * @param valueLocation
	 *            - instruction result type
	 */
	public void setValueType(Object valueType) {
		this.valueType = valueType;
	}

	private boolean fStopped = false;

	/**
	 * Constructor for interpreter
	 * 
	 * @param instructionSequence
	 *            - instruction sequence to execute
	 * @param context
	 *            - instruction context
	 */
	public Interpreter(InstructionSequence instructionSequence, Object context) {
		this.instructions = instructionSequence.getInstructions();
		this.context = context;
		setValueType(ASTEvaluationEngine.UNKNOWN_TYPE);
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
			instruction.setInterpreter(this);
			instruction.execute();
			instruction.setInterpreter(null);
		}
	}

	/**
	 * Stop the interpreter
	 */
	public void stop() {
		fStopped = true;
	}

	/**
	 * Reset the interpreter
	 */
	private void reset() {
		stack = new Stack<Object>();
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
	public void push(Object object) {
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
	public Object peek() {
		return stack.peek();
	}

	/**
	 * Pop an object off of the stack
	 * 
	 * @return object on the top of the stack
	 */
	public Object pop() throws EmptyStackException {
		return stack.pop();
	}

	/**
	 * Get the context for the interpreter
	 * 
	 * @return interpreter context
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * Get current instruction result
	 * 
	 * @return current top of stack, or the last stack value if the stack is
	 *         <code>null</code> or empty
	 */
	public Object getResult() {
		if (stack == null || stack.isEmpty()) {
			return lastValue;
		}
		Object top = stack.peek();
		return top;
	}

	/**
	 * Set the last stack value
	 * 
	 * @param value
	 */
	public void setLastValue(Object value) {
		lastValue = value;
	}

}

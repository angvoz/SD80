/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.c99;

import java.util.Stack;

import lpg.lpgjavaruntime.IToken;


/**
 * Parser symantic actions that evaluate preprocessor conditional expressions.
 *
 * @author Mike Kucera
 */
public class C99ExprEvaluatorAction {

	// Stores intermediate values as the expression is being evaluated
	private final Stack valueStack = new Stack(); // Stack<Integer>
	
	// A reference to the expression parser, used to get access to the raw tokens
	private final C99ExprEvaluator parser;
	
	
	private boolean errorEncountered;
	
	
	/**
	 * All the operators that are available, 
	 * some binary some unary, some like '-' are both.
	 */
	public final static int 
		op_amper = 1,
		op_star = 2,
		op_plus = 3,
		op_minus = 4, 
		op_tilde = 5,
		op_not = 6,
		op_multiply = 7,
		op_divide = 8,
		op_modulo = 9,
		op_shiftLeft = 10,
		op_shiftRight = 11,
		op_lessThan = 12,
		op_greaterThan = 13,
		op_lessEqual = 14, 
		op_greaterEqual = 15,
		op_equals = 16,
		op_notequals = 17,
		op_binaryAnd = 18, 
		op_binaryXor = 19,
		op_binaryOr = 20,
		op_logicalAnd = 21,
		op_logicalOr = 22;
	
	
	
	
	public C99ExprEvaluatorAction(C99ExprEvaluator parser) {
		this.parser = parser;
		this.errorEncountered = false;
	}

	/**
	 * Returns the result of evaluating the constant expression.
	 * Returns null if an error was encountered that prevented 
	 * the expression from being fully evaluated.
	 */
	public Integer result() {
		if(errorEncountered || valueStack.isEmpty())
			return null;
		
		return (Integer) valueStack.peek();
	}

	
	protected void evalExpressionBinaryOperator(int op) {
		if(errorEncountered) return;
		
		int y = ((Integer)valueStack.pop()).intValue();
		int x = ((Integer)valueStack.pop()).intValue();
		int result;
		
		switch(op) {
			case op_multiply:
				result = x * y;
				break;
			case op_divide:
				if(y == 0) {
					errorEncountered = true;
					return;
				}
				result = x / y;	
				break;
			case op_modulo:
				result = x % y;
				break;
			case op_plus:
				result = x + y;
				break;
			case op_minus:
				result = x - y;
				break;
			case op_shiftLeft:
				result = x << y;
				break;
			case op_shiftRight:
				result = x >> y;
				break;
			case op_lessThan:
				result = x < y ? 1 : 0;
				break;
			case op_greaterThan:
				result = x > y ? 1 : 0;
				break;
			case op_lessEqual:
				result = x <= y ? 1 : 0;
				break;
			case op_greaterEqual:
				result = x >= y ? 1 : 0;
				break;
			case op_equals:
				result = x == y ? 1 : 0;
				break;
			case op_notequals:
				result = x != y ? 1 : 0;
				break;
			case op_binaryAnd:
				result = x & y;
				break;
			case op_binaryXor:
				result = x ^ y;
				break;
			case op_binaryOr:
				result = x | y;
				break;
			case op_logicalAnd:
				result = (x != 0) && (y != 0) ? 1 : 0;
				break;
			case op_logicalOr:
				result = (x != 0) || (y != 0) ? 1 : 0;
				break;
			default:
				throw new RuntimeException("Impossible to reach here");
		}
		
		valueStack.push(new Integer(result));
	}
	
	
	protected void evalExpressionUnaryOperator(int op) {
		if(errorEncountered) return;
		
		int x = ((Integer)valueStack.pop()).intValue();
		int result;
		
		switch(op) {
			case op_minus:
				result = -x;
				break;
			case op_tilde:
				result = ~x;
				break;
			case op_not:
				result = x == 0 ? 1 : 0;
				break;
			case op_plus:
				result = x;
				break;
			default:
				throw new RuntimeException("Impossible to reach here");
		}
		
		valueStack.push(new Integer(result));
	}
	
	
	protected void evalExpressionConditional() {
		if(errorEncountered) return;
		
		Integer x3 = (Integer) valueStack.pop();
		Integer x2 = (Integer) valueStack.pop();
		Integer x1 = (Integer) valueStack.pop();
		
		valueStack.push(x1.intValue() != 0 ? x2 : x3); 
	}
	
	
	protected void evalExpressionConstantInteger() {
		if(errorEncountered) return;
		
		IToken token = parser.getRightIToken();
		valueStack.push(new Integer(token.toString()));
	}
	
	
	protected void evalExpressionConstantChar() {
		if(errorEncountered) return;
		// TODO: this is not correct
		valueStack.push(new Integer(0));
	}

	
	protected void evalExpressionID() {
		if(errorEncountered) return;
		
		valueStack.push(new Integer(0));
	}
	
}

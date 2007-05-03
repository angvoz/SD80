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


/**
 * Parser symantic actions that evaluate preprocessor conditional expressions.
 * 
 * The 'defined' operator is not handled here, it is handled in the 
 * C99Preprocessor before the conditional is passed to the expression
 * evaluator.
 *
 * @author Mike Kucera
 */
public class C99ExprEvaluatorAction {

	private static int HEXADECIMAL_BASE = 16;
	private static int OCTAL_BASE = 8;
	
	
	// Stores intermediate values as the expression is being evaluated
	private final Stack valueStack = new Stack(); // Stack<Long>
	
	// A reference to the expression parser, used to get access to the raw tokens
	private final C99ExprEvaluator parser;
	
	// used mainly to protect against division by zero
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
	public Long result() {
		if(errorEncountered || valueStack.size() != 1)
			return null;
		
		return (Long) valueStack.peek();
	}

	
	protected void evalExpressionBinaryOperator(int op) {
		if(errorEncountered) return;
		
		long y = ((Long)valueStack.pop()).intValue();
		long x = ((Long)valueStack.pop()).intValue();
		long result;
		
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
				errorEncountered = true;
				return;
		}
		
		valueStack.push(new Long(result));
	}
	
	
	protected void evalExpressionUnaryOperator(int op) {
		if(errorEncountered) return;
		
		long x = ((Long)valueStack.pop()).longValue();
		long result;
		
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
				errorEncountered = true;
				return;
		}
		
		valueStack.push(new Long(result));
	}
	
	
	protected void evalExpressionConditional() {
		if(errorEncountered) return;
		
		Long x3 = (Long) valueStack.pop();
		Long x2 = (Long) valueStack.pop();
		Long x1 = (Long) valueStack.pop();
		
		valueStack.push(x1.longValue() != 0 ? x2 : x3); 
	}
	
	
	/**
	 * According to the spec, all identifiers (that were not macro-replaced)
	 * evaluate to 0.
	 */
	protected void evalExpressionID() {
		if(errorEncountered) return;
		
		valueStack.push(new Long(0));
	}
	
	
	protected void evalExpressionConstantInteger() {
		if(errorEncountered) return;
		
		String val = parser.getRightIToken().toString();
		long result;
	
		val = val.replaceAll("[UuLl]", ""); // remove the suffix
		
		if(val.startsWith("0x")) {
			result = Long.parseLong(val.substring(2), HEXADECIMAL_BASE);
		}
		else if(val.startsWith("0")) {
			result = Long.parseLong(val, OCTAL_BASE);
		}
		else {
			result = Long.parseLong(val);
		}
			
		valueStack.push(new Long(result));
	}
	
	
	
	private final static int MULTIPLIER = 256;
	
	
	/**
	 * Computes the numeric value of a character constant.
	 * 
	 * The C99 spec states that the representation of multi-character character constants 
     * is implementation dependant. The GCC manual states that multi-character constants are 
	 * handled by shifting the previous result left by the number of bits per character, 
	 * see http://sunsite.ualberta.ca/Documentation/Gnu/gcc-3.0.2/html_chapter/cpp_11.html.
	 * This is the strategy that will be used here.
	 * 
	 * Invalid escape sequences are rejected by the lexer, therefore we can assume
	 * that the character constant is completely vaild.
	 */
	protected void evalExpressionConstantChar() {
		if(errorEncountered) return;
		
		String val = parser.getRightIToken().toString();
		// strip the '' or L''
		val = val.substring(val.startsWith("L") ? 2 : 1, val.length()-1); 

		long result = 0;
		
		char[] chars = val.toCharArray();
		
		int i = 0;
		while(i < chars.length) {
			if(i > 0)
				result *= MULTIPLIER;
			
			if(chars[i] == '\\') { // escape sequence encountered
				i++;
				switch(chars[i]) { 
					// simple escape sequences: \' \" \? \\ \a \b \f \n \r \t \v
					case '\'': result += 0x27; i++; break;
					case '"' : result += 0x22; i++; break;
					case '?' : result += 0x3F; i++; break;
					case '\\': result += 0x5C; i++; break;
					case 'a' : result += 0x07; i++; break;
					case 'b' : result += 0x08; i++; break;
					case 'f' : result += 0x0C; i++; break;
					case 'n' : result += 0x0A; i++; break;
					case 'r' : result += 0x0D; i++; break;
					case 't' : result += 0x09; i++; break;
					case 'v' : result += 0x0B; i++; break;
					
					case 'u' :   // universal character constant
					case 'U' : { // consists of one or two hex quads
						i++;
						int end = i+3; // end location of first hex quad
						// test for second hex quad
						if(end + 4 < chars.length && isHex(chars, end+1, end+4)) {
							end += 4;
						}
						int length = (end-i)+1;
						// just convert it as hex (perhaps this is not correct)
						result += Long.parseLong(new String(chars, i, length), HEXADECIMAL_BASE);
						i += length;
						break;
					}
					case 'x' : { // hexadecimal escape sequence, terminated by non-hex character
						i++;
						StringBuffer hexVal = new StringBuffer();
						while(isHex(chars[i]) && i < chars.length) { // scan until non-hex character is encountered
							hexVal.append(chars[i]);
							i++;
						}
						result += Long.parseLong(hexVal.toString(), HEXADECIMAL_BASE);
						break;
					}
					default: { // octal escape sequence, 1 to 3 octal characters
						StringBuffer octalVal = new StringBuffer(3);
						for(int j = 0; j < 3 && isOctal(chars[i]) && i < chars.length; j++) { // iterate maximum 3 times
							octalVal.append(chars[i]);
							i++;
						}
						result += Long.parseLong(octalVal.toString(), HEXADECIMAL_BASE);
						break;
					}
				}
			}
			else {
				result += chars[i];
				i++;
			}
		}

		valueStack.push(new Long(result));
	}

	
	
	private static boolean isHex(char c) {
		return Character.digit(c, HEXADECIMAL_BASE) != -1;
	}
	
	
	/**
	 * Checks if a section of a char array is all chars that represent
	 * hex symbols.
	 */
	private static boolean isHex(char[] chars, int start, int end) {
		for(int i = start; i <= end; i++) {
			if(!isHex(chars[i])) {
				return false;
			}
		}
		return true;
	}
	
	
	private static boolean isOctal(char c) {
		return Character.digit(c, OCTAL_BASE) != -1;
	}
	
	
}

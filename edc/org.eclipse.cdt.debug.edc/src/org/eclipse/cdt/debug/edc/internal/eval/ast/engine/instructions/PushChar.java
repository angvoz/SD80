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

import org.eclipse.cdt.debug.edc.symbols.TypeUtils;
import org.eclipse.core.runtime.CoreException;

/*
 * Push a character on the instruction stack
 * 
 * Note: if the character does not fit in a byte, it is represented by the low 4 bytes of a Java long */
public class PushChar extends SimpleInstruction {

	static private final char BELL = '\u0007';
	static private final char VERTICAL_TAB = '\u000B';

	// character value
	private long fValue;
	// is wchar_t?
	private boolean isWide;
	// if true, the value is multiple characters (e.g. 'AB' or 'CWIE')
	private boolean multiChar;
	
	/**
	 * Constructor for pushing a char on the stack
	 * 
	 * @param value
	 *            - char value
	 */
	public PushChar(char value) {
		fValue = (short) value;
	}

	/**
	 * Constructor for pushing a char on the stack
	 * 
	 * @param value
	 *            - string value of form 'X' to convert to char X
	 * @throws NumberFormatException
	 */
	public PushChar(String value) throws NumberFormatException {
		parseCharValue(value);
	}

	/**
	 * Execute pushing a char on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		if (multiChar) {
			pushNewValue(fInterpreter.getTypeEngine().getIntegerTypeOfSize(4, false), fValue);
			return;
		}
		
		if (!isWide) {
			// TODO: truncate to size of this type
			pushNewValue(fInterpreter.getTypeEngine().getCharacterType(
					fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_CHAR)), fValue);
		} else {
			pushNewValue(fInterpreter.getTypeEngine().getCharacterType(
					fInterpreter.getTypeEngine().getTypeSize(TypeUtils.BASIC_TYPE_WCHAR_T)), fValue);
		}
	}

	/**
	 * Show a char value as a string
	 * 
	 * @return string version of a char
	 */
	@Override
	public String toString() {
		if (fValue < 65536)
			return "" + ((char) fValue); //$NON-NLS-1$
		char[] surrogate = { (char)(fValue >> 16), (char)(fValue & 0xffff) };
		return new String(surrogate);
	}

	/**
	 * Convert string value of form 'X' to char X.  This may be either a single
	 * character (char or wchar_t) or a multi-character constant, which is treated
	 * as an integer.
	 * 
	 * @param value
	 *            - string of form 'X'
	 * @throws NumberFormatException
	 */
	private void parseCharValue(String value) throws NumberFormatException {
		if (value.startsWith("L")) { //$NON-NLS-1$
			isWide = true;
			value = value.substring(1);
		}
		if (value.length() < 3 || value.charAt(0) != '\'' || !value.endsWith("'")) //$NON-NLS-1$
			throw new NumberFormatException();

		value = value.substring(1, value.length() - 1);

		if (value.startsWith("\\u")) { //$NON-NLS-1$
			// hex representation
			if (value.length() < 3)
				throw new NumberFormatException();

			fValue = Long.parseLong(value.substring(2), 16);
			return;
		}

		// escape character
		if (value.startsWith("\\")) { //$NON-NLS-1$
			if (value.length() < 2)
				throw new NumberFormatException();

			if (value.charAt(1) >= '0' && value.charAt(1) <= '7') {
				// octal representation
				if (value.length() > 4)
					throw new NumberFormatException();

				fValue = Long.parseLong(value.substring(1), 8);
				return;
			}

			if (value.length() > 2)
				throw new NumberFormatException();

			switch (value.charAt(1)) {
			case 'n':
				fValue = '\n'; break;
			case 't':
				fValue = '\t'; break;
			case 'v':
				fValue = VERTICAL_TAB; break;
			case 'b':
				fValue = '\b'; break;
			case 'r':
				fValue = '\r'; break;
			case 'f':
				fValue = '\f'; break;
			case 'a':
				fValue = BELL; break;
			case '\\':
				fValue = '\\'; break;
			case '?':
				fValue = '?'; break;
			case '\'':
				fValue = '\''; break;
			case '"':
				fValue = '"'; break;
			default:
				fValue = value.charAt(1); break;
			}
			return;
		}

		multiChar = (value.length() > 1);
		
		fValue = 0;
		for (int i = 0; i < value.length(); i++) {
			fValue = (fValue << 8) + value.charAt(i);
		}
	}

}

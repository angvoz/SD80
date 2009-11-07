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

import org.eclipse.core.runtime.CoreException;

/*
 * Push a character on the instruction stack
 * 
 * Note: if the character does not fit in a byte, it is represented by the low 4 bytes of a Java long */
public class PushChar extends SimpleInstruction {

	static private final char BELL = '\u0007';
	static private final char VERTICAL_TAB = '\u000B';

	// Character or Long value
	private Object fValue;

	/**
	 * Constructor for pushing a char on the stack
	 * 
	 * @param value
	 *            - char value
	 */
	public PushChar(char value) {
		fValue = value;
	}

	/**
	 * Constructor for pushing a char on the stack
	 * 
	 * @param value
	 *            - string value of form 'X' to convert to char X
	 * @throws NumberFormatException
	 */
	public PushChar(String value) throws NumberFormatException {
		fValue = parseCharValue(value);
	}

	/**
	 * Execute pushing a char on the stack
	 * 
	 * @throws CoreException
	 */
	@Override
	public void execute() {
		if (fValue instanceof Character)
			pushNewValue((Character) fValue);
		else
			pushNewValue((Long) fValue);
	}

	/**
	 * Show a char value as a string
	 * 
	 * @return string version of a char
	 */
	@Override
	public String toString() {
		if (fValue instanceof Character)
			return ((Character) fValue).toString();
		else
			return ((Long) fValue).toString();
	}

	/**
	 * Convert string value of form 'X' to char X
	 * 
	 * @param value
	 *            - string of form 'X'
	 * @return char X if X is a single character; integer X otherwise
	 * @throws NumberFormatException
	 */
	public static Object parseCharValue(String value) throws NumberFormatException {
		// TODO: handle wide character constant somewhere
		if (value.length() < 3 || value.charAt(0) != '\'' || !value.endsWith("'")) //$NON-NLS-1$
			throw new NumberFormatException();

		value = value.substring(1, value.length() - 1);

		if (value.startsWith("\\u")) { //$NON-NLS-1$
			// hex representation
			if (value.length() < 3)
				throw new NumberFormatException();

			long longValue = Long.parseLong(value.substring(2), 16);

			if (longValue <= 0xff)
				return new Character((char) longValue);
			else
				return new Long(longValue & 0xffffffff);
		}

		if (value.startsWith("\\")) { //$NON-NLS-1$
			if (value.length() < 2)
				throw new NumberFormatException();

			if (value.charAt(1) >= '0' && value.charAt(1) <= '7') {
				// octal representation
				if (value.length() > 4)
					throw new NumberFormatException();

				return new Character((char) Long.parseLong(value.substring(1), 8));
			}

			if (value.length() > 2)
				throw new NumberFormatException();

			switch (value.charAt(1)) {
			case 'n':
				return '\n';
			case 't':
				return '\t';
			case 'v':
				return VERTICAL_TAB;
			case 'b':
				return '\b';
			case 'r':
				return '\r';
			case 'f':
				return '\f';
			case 'a':
				return BELL;
			case '\\':
				return '\\';
			case '?':
				return '?';
			case '\'':
				return '\'';
			case '"':
				return '"';
			default:
				return new Character(value.charAt(1));
			}
		}

		long longValue = 0;

		for (int i = 0; i < value.length(); i++) {
			longValue = (longValue << 8) + value.charAt(i);
		}

		if (longValue <= 0xff)
			return new Character((char) longValue);
		else
			return new Long(longValue & 0xffffffff);
	}

}

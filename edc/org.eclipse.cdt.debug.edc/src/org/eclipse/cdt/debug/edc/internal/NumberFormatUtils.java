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
package org.eclipse.cdt.debug.edc.internal;

import java.math.BigInteger;

import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;

public class NumberFormatUtils {

	private static final String HEX_PREFIX = "0x"; //$NON-NLS-1$

	private static final String OCTAL_PREFIX = "0"; //$NON-NLS-1$

	private static final String BINARY_PREFIX = "0b"; //$NON-NLS-1$

	private static final String SINGLE_QUOTE = "'"; //$NON-NLS-1$

	private static final String DECIMAL_SUFFIX = " (Decimal)"; //$NON-NLS-1$

	static public String toHexString(Number number) {
		String str = null;
		if (number instanceof Integer)
			str = Integer.toHexString((Integer) number);
		else if (number instanceof Long)
			str = Long.toHexString((Long) number);
		else if (number instanceof BigInteger)
			str = ((BigInteger) number).toString(16);
		else if (number instanceof Float)
			str = Float.toHexString((Float) number);
		else if (number instanceof Double)
			str = Double.toHexString((Double) number);
		if (str != null && !str.startsWith(HEX_PREFIX))
			return HEX_PREFIX + str;
		return str;
	}

	static public String toOctalString(Number number) {
		String str = null;
		if (number instanceof Integer)
			str = Integer.toOctalString((Integer) number);
		else if (number instanceof Long)
			str = Long.toOctalString((Long) number);
		else if (number instanceof BigInteger)
			str = ((BigInteger) number).toString(8);
		if (str != null && !str.startsWith(OCTAL_PREFIX))
			str = OCTAL_PREFIX + str;
		if (str == null && (number instanceof Float || number instanceof Double))
			str = number.toString() + DECIMAL_SUFFIX;
		return str;
	}

	static public String asBinary(Number number) {
		String str = null;
		if (number instanceof Integer)
			str = Integer.toBinaryString((Integer) number);
		else if (number instanceof Long)
			str = Long.toBinaryString((Long) number);
		else if (number instanceof BigInteger)
			str = ((BigInteger) number).toString(2);
		if (str != null && !str.startsWith(BINARY_PREFIX))
			str = BINARY_PREFIX + str;
		if (str == null && (number instanceof Float || number instanceof Double))
			str = number.toString() + DECIMAL_SUFFIX;
		return str;
	}

	static public String toCharString(Number number, IType valueType) {
		int intValue = number.intValue();
		String charVal = null;
		if (intValue < 128) {
			switch ((char) intValue) {
				case 0:
					charVal = ("\\0"); //$NON-NLS-1$
					break;
				case '\b':
					charVal = ("\\b"); //$NON-NLS-1$
					break;
				case '\f':
					charVal = ("\\f"); //$NON-NLS-1$
					break;
				case '\n':
					charVal = ("\\n"); //$NON-NLS-1$
					break;
				case '\r':
					charVal = ("\\r"); //$NON-NLS-1$
					break;
				case '\t':
					charVal = ("\\t"); //$NON-NLS-1$
					break;
				case '\'':
					charVal = ("\\'"); //$NON-NLS-1$
					break;
				case '\"':
					charVal = ("\\\""); //$NON-NLS-1$
					break;
				case '\\':
					charVal = ("\\\\"); //$NON-NLS-1$
					break;
				case 0xb:
					charVal = ("\\v"); //$NON-NLS-1$
					break;
			}
		}
	
		// Show the numeric value (decimal for char, since it's short, and hex for wchar_t)
		// then the character value.  Note that at the system font may not be able to show
		// all characters in the variables/expressions view, which is why we show the 
		// more meaningful numeric value before the possibly "boxy" character representation.
		//
		// Also, we assume wchar_t == Unicode.
		boolean isWchart = (valueType instanceof ICPPBasicType 
			&& ((ICPPBasicType) valueType).getBaseType() == ICPPBasicType.t_wchar_t)
			 || valueType.getName().equals("wchar_t"); //$NON-NLS-1$
		
		StringBuilder info = new StringBuilder();
	
		if (isWchart) {
			info.append(HEX_PREFIX);
			if (valueType.getByteSize() == 2)
				info.append(String.format("%04X", intValue)); //$NON-NLS-1$
			else
				info.append(String.format("%08X", intValue)); //$NON-NLS-1$
			info.append(" (L"); //$NON-NLS-1$
		} else {
			info.append("" + intValue); //$NON-NLS-1$
			info.append(" ("); //$NON-NLS-1$
		}
	
		if (charVal == null) {
			// treat chars as unsigned for getting the char representation
			String fmt = "\\U%08X"; //$NON-NLS-1$
			switch (valueType.getByteSize()) {
			case 1:
				fmt = "\\%03o"; //$NON-NLS-1$
				intValue &= 0xff; break;
			case 2:
				fmt = "\\u%04X"; //$NON-NLS-1$
				intValue &= 0xffff; break;
			case 4:
				// note: may still be too large to be legal
				fmt = "\\U%08X"; //$NON-NLS-1$
				intValue &= 0xffffffff; break;
			}
			
			boolean gotRepr = false;
			try {
				if (!Character.isISOControl(intValue)) {
					char[] chars = Character.toChars(intValue);
					info.append(asStringQuoted(new String(chars)));
					gotRepr = true;
				}
			} catch (IllegalArgumentException e) {
				// some character values are negative or outside the UCS range;
				// these throw exceptions
			}
			if (!gotRepr) {
				info.append(asStringQuoted(String.format(fmt, intValue)));
			}
		} else {
			info.append(asStringQuoted(charVal));
		}
		info.append(')');
		
		return info.toString();
	}

	static public String asStringQuoted(String val) {
		StringBuilder sb = new StringBuilder(SINGLE_QUOTE);
		sb.append(val);
		sb.append(SINGLE_QUOTE);
		return sb.toString();
	}

	static public BigInteger parseIntegerByFormat(String expressionValue, String formatId) {
		int radix = 10;
		if (IFormattedValues.HEX_FORMAT.equals(formatId)) {
			if (expressionValue.startsWith(HEX_PREFIX)) 
				expressionValue = expressionValue.substring(HEX_PREFIX.length());
			radix = 16;
		} else if (IFormattedValues.OCTAL_FORMAT.equals(formatId)) {
			if (expressionValue.startsWith(OCTAL_PREFIX)) 
				expressionValue = expressionValue.substring(OCTAL_PREFIX.length()); 
			radix = 8;
		} else if (IFormattedValues.BINARY_FORMAT.equals(formatId)) {
			if (expressionValue.startsWith(BINARY_PREFIX)) 
				expressionValue = expressionValue.substring(BINARY_PREFIX.length()); 
			radix = 2;
		} else if (IFormattedValues.NATURAL_FORMAT.equals(formatId)) {
			if (expressionValue.startsWith(BINARY_PREFIX)) {
				expressionValue = expressionValue.substring(BINARY_PREFIX.length());
				radix = 2;
			} else if (expressionValue.startsWith(OCTAL_PREFIX)) { 
				expressionValue = expressionValue.substring(OCTAL_PREFIX.length());
				radix = 8;
			} else if (expressionValue.startsWith(HEX_PREFIX)) { 
				expressionValue = expressionValue.substring(HEX_PREFIX.length());
				radix = 16;
			} 
			// else, decimal
		}
        try {
        	return new BigInteger(expressionValue, radix);
        } catch (NumberFormatException e) {
        	// just return null
        }
        
        return null;
	}

}

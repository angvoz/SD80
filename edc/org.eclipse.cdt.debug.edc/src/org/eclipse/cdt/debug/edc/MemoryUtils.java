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
package org.eclipse.cdt.debug.edc;

import java.math.BigInteger;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.internal.symbols.IBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.ICPPBasicType;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.symbols.IEnumerator;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.MemoryByte;

public class MemoryUtils {

	public static final int LITTLE_ENDIAN = 0;
	public static final int BIG_ENDIAN = 1;
	public static final int ENDIANESS_UNKNOWN = 2;

	/**
	 * Pad byte array with 0's or 1's when the byte array's length is shorter
	 * than what's expected by the conversion functions.
	 * 
	 * @param array
	 * @param size
	 * @param endianess
	 * @param isSigned
	 * @return an array of bytes
	 */
	protected static byte[] fillArray(byte[] array, int size, int endianess, boolean isSigned) {

		byte[] temp = new byte[size];

		// either 0 fill or sign extend
		byte fillByte = 0;
		if (isSigned && ((array[array.length - 1] & 0x80) != 0))
			fillByte = (byte) 0xff;

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {

			for (int i = 0; i < array.length; i++) {
				temp[i] = array[i];
			}

			// fill up the rest of the array
			for (int i = array.length; i < size; i++) {
				temp[i] = fillByte;
			}

			array = temp;
			return array;
		}

		for (int i = 0; i < size - array.length; i++) {
			temp[i] = fillByte;
		}

		int j = 0;
		// fill up the rest of the array
		for (int i = size - array.length; i < size; i++) {
			temp[i] = array[j];
			j++;
		}

		array = temp;
		return array;
	}

	/**
	 * Convert byte array to unsigned long.
	 * 
	 * @param data
	 * @param endianess
	 * @return result of the conversion in long
	 */
	static public BigInteger convertByteArrayToUnsignedLong(MemoryByte[] data, int endianess) {
		byte[] array = new byte[data.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = data[i].getValue();
		}

		if (array.length < 8) {
			array = fillArray(array, 8, endianess, false);
		}

		BigInteger value = new BigInteger("0"); //$NON-NLS-1$
		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int i = 0; i < 8; i++) {
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft(i * 8);
				value = value.or(b);
			}
		} else {
			for (int i = 0; i < 8; i++) {
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft((7 - i) * 8);
				value = value.or(b);
			}
		}
		return value;
	}

	/**
	 * Convert byte array to signed long.
	 * 
	 * @param data
	 * @param endianess
	 * @return result of the conversion in long
	 */
	static public long convertByteArrayToLong(MemoryByte[] data, int endianess) {
		byte[] array = new byte[data.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = data[i].getValue();
		}

		if (array.length < 8) {
			array = fillArray(array, 8, endianess, true);
		}

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			long value = 0;
			for (int i = 0; i < 8; i++) {
				long b = array[i];
				b &= 0xff;
				value |= (b << (i * 8));
			}
			return value;
		}
		long value = 0;
		for (int i = 0; i < 8; i++) {
			long b = array[i];
			b &= 0xff;
			value |= (b << ((7 - i) * 8));
		}

		return value;
	}

	static public BigInteger convertByteArrayToSignedBigInt(byte[] array, int endianess) {
		if (array.length < 16) {
			array = fillArray(array, 16, endianess, false);
		}

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			// reverse bytes
			byte[] holder = new byte[16];
			int j = 15;
			for (int i = 0; i < 16; i++, j--) {
				holder[i] = array[j];
			}

			// create BigInteger
			BigInteger value = new BigInteger(holder);
			return value;
		}
		BigInteger value = new BigInteger(array);
		return value;
	}

	static public BigInteger convertByteArrayToSignedBigInt(byte[] array, int endianess, int arraySize) {
		if (array.length < arraySize) {
			array = fillArray(array, arraySize, endianess, true);
		}

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			// reverse bytes
			byte[] holder = new byte[arraySize];
			int j = arraySize - 1;
			for (int i = 0; i < arraySize; i++, j--) {
				holder[i] = array[j];
			}

			// create BigInteger
			BigInteger value = new BigInteger(holder);
			return value;
		}
		BigInteger value = new BigInteger(array);
		return value;
	}

	static public BigInteger convertByteArrayToUnsignedBigInt(byte[] array, int endianess) {
		if (array.length < 16) {
			array = fillArray(array, 16, endianess, false);
		}

		BigInteger value = new BigInteger("0"); //$NON-NLS-1$
		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int i = 0; i < 16; i++) {
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft(i * 8);
				value = value.or(b);
			}
		} else {
			for (int i = 0; i < 16; i++) {
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft((15 - i) * 8);
				value = value.or(b);
			}
		}
		return value;
	}

	static public BigInteger convertByteArrayToUnsignedBigInt(byte[] array, int endianess, int arraySize) {
		if (array.length < arraySize) {
			array = fillArray(array, arraySize, endianess, false);
		}

		BigInteger value = new BigInteger("0"); //$NON-NLS-1$
		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int i = 0; i < arraySize; i++) {
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft(i * 8);
				value = value.or(b);
			}
		} else {
			for (int i = 0; i < arraySize; i++) {
				byte[] temp = new byte[1];
				temp[0] = array[i];
				BigInteger b = new BigInteger(temp);
				b = b.and(new BigInteger("ff", 16)); //$NON-NLS-1$
				b = b.shiftLeft((arraySize - 1 - i) * 8);
				value = value.or(b);
			}
		}
		return value;
	}

	/**
	 * Convert byte array to integer.
	 * 
	 * @param array
	 * @param endianess
	 * @return result of the conversion in int
	 */
	static public int convertByteArrayToInt(MemoryByte[] data, int endianess) {
		byte[] array = new byte[data.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = data[i].getValue();
		}

		if (array.length < 4) {
			array = fillArray(array, 4, endianess, true);
		}

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			int value = 0;
			for (int i = 0; i < 4; i++) {
				int b = array[i];
				b &= 0xff;
				value |= (b << (i * 8));
			}
			return value;
		}
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int b = array[i];
			b &= 0xff;
			value |= (b << ((3 - i) * 8));
		}

		return value;
	}

	/**
	 * Convert byte array to short.
	 * 
	 * @param array
	 * @param endianess
	 * @return result of teh conversion in short
	 */
	static public short convertByteArrayToShort(MemoryByte[] data, int endianess) {
		byte[] array = new byte[data.length];
		for (int i = 0; i < array.length; i++) {
			array[i] = data[i].getValue();
		}
		if (array.length < 2) {
			array = fillArray(array, 2, endianess, true);
		}

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			short value = 0;
			for (int i = 0; i < 2; i++) {
				short b = array[i];
				b &= 0xff;
				value |= (b << (i * 8));
			}
			return value;
		}
		short value = 0;
		for (int i = 0; i < 2; i++) {
			short b = array[i];
			b &= 0xff;
			value |= (b << ((1 - i) * 8));
		}
		return value;
	}

	/**
	 * Convert big integer to byte array.
	 * 
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertBigIntegerToByteArray(BigInteger i, int endianess) {
		byte buf[] = new byte[16];

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int j = 0; j < 16; j++) {
				BigInteger x = i.shiftRight(j * 8);
				buf[j] = x.byteValue();
			}
			return buf;
		}
		for (int j = 15; j >= 0; j--) {
			BigInteger x = i.shiftRight((15 - j) * 8);
			buf[j] = x.byteValue();
		}
		return buf;
	}

	static public byte[] convertSignedBigIntToByteArray(BigInteger i, int endianess, int arraySize) {
		byte buf[] = new byte[arraySize];

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int j = 0; j < arraySize; j++) {
				BigInteger x = i.shiftRight(j * 8);
				buf[j] = x.byteValue();
			}
			return buf;
		}
		for (int j = arraySize - 1; j >= 0; j--) {
			BigInteger x = i.shiftRight((arraySize - 1 - j) * 8);
			buf[j] = x.byteValue();
		}
		return buf;
	}

	/**
	 * Convert big integer to byte array.
	 * 
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertUnsignedBigIntegerToByteArray(BigInteger i, int endianess) {
		byte buf[] = new byte[32];

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int j = 0; j < 32; j++) {
				BigInteger x = i.shiftRight(j * 8);
				buf[j] = x.byteValue();
			}
			return buf;
		}
		for (int j = 31; j >= 0; j--) {
			BigInteger x = i.shiftRight((31 - j) * 8);
			buf[j] = x.byteValue();
		}
		return buf;
	}

	static public byte[] convertUnsignedBigIntToByteArray(BigInteger i, int endianess, int arraySize) {
		byte buf[] = new byte[arraySize * 2];

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int j = 0; j < arraySize * 2; j++) {
				BigInteger x = i.shiftRight(j * 8);
				buf[j] = x.byteValue();
			}
			return buf;
		}
		for (int j = (arraySize * 2) - 1; j >= 0; j--) {
			BigInteger x = i.shiftRight(((arraySize * 2) - 1 - j) * 8);
			buf[j] = x.byteValue();
		}
		return buf;
	}

	/**
	 * Convert long to byte array.
	 * 
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertLongToByteArray(long i, int endianess) {
		byte buf[] = new byte[8];

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int j = 0; j < 8; j++) {
				buf[j] = new Long(i >> j * 8).byteValue();
			}
			return buf;
		}
		for (int j = 7; j >= 0; j--) {
			buf[j] = new Long(i >> (7 - j) * 8).byteValue();
		}
		return buf;
	}

	/**
	 * Convert integer to byte array.
	 * 
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertIntToByteArray(int i, int endianess) {
		byte buf[] = new byte[4];

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (int j = 0; j < 4; j++) {
				buf[j] = new Integer(i >> j * 8).byteValue();
			}
			return buf;
		}
		for (int j = 3; j >= 0; j--) {
			buf[j] = new Integer(i >> (3 - j) * 8).byteValue();
		}
		return buf;
	}

	/**
	 * Convert short to byte array.
	 * 
	 * @param i
	 * @param endianess
	 * @return result of the conversion in raw byte array
	 */
	static public byte[] convertShortToByteArray(short i, int endianess) {
		byte buf[] = new byte[2];

		if (endianess == MemoryUtils.LITTLE_ENDIAN) {
			for (short j = 0; j < 2; j++) {
				buf[j] = new Integer(i >> j * 8).byteValue();
			}
			return buf;
		}
		for (short j = 1; j >= 0; j--) {
			buf[j] = new Integer(i >> (1 - j) * 8).byteValue();
		}
		return buf;
	}

	/**
	 * byte array to Hex string helper replaces the Integer.toHexString() which
	 * can't convert byte values properly (always pads with FFFFFF)
	 */
	static public String convertByteArrayToHexString(byte[] byteArray) {
		StringBuffer strBuffer = new StringBuffer();
		char charArray[];

		for (byte element : byteArray) {
			charArray = MemoryUtils.convertByteToCharArray(element);
			strBuffer.append(charArray);
		}

		return strBuffer.toString();
	}

	static public char[] convertByteToCharArray(byte aByte) {
		char charArray[] = new char[2];
		int val = aByte;
		if (val < 0)
			val += 256;
		charArray[0] = Character.forDigit(val / 16, 16);
		charArray[1] = Character.forDigit(val % 16, 16);

		return charArray;
	}

	/**
	 * Convert raw memory data to byte array
	 * 
	 * @param str
	 * @param numBytes
	 * @param numCharsPerByte
	 *            - number of characters per byte of data
	 * @return an array of byte, converted from a hex string
	 * @throws NumberFormatException
	 */
	public static byte[] convertHexStringToByteArray(String str, int numBytes, int numCharsPerByte)
			throws NumberFormatException {
		if (str.length() == 0)
			return null;

		StringBuffer buf = new StringBuffer(str);

		// pad string with zeros
		int requiredPadding = numBytes * numCharsPerByte - str.length();
		while (requiredPadding > 0) {
			buf.insert(0, "0"); //$NON-NLS-1$
			requiredPadding--;
		}

		byte[] bytes = new byte[numBytes];
		str = buf.toString();

		// set data in memory
		for (int i = 0; i < bytes.length; i++) {
			// convert string to byte
			String oneByte = str.substring(i * 2, i * 2 + 2);

			Integer number = Integer.valueOf(oneByte, 16);
			if (number.compareTo(Integer.valueOf(Byte.toString(Byte.MAX_VALUE))) > 0) {
				int temp = number.intValue();
				temp = temp - 256;

				String tempStr = Integer.toString(temp);

				Byte myByte = Byte.valueOf(tempStr);
				bytes[i] = myByte.byteValue();
			} else {
				Byte myByte = Byte.valueOf(oneByte, 16);
				bytes[i] = myByte.byteValue();
			}
		}

		return bytes;
	}

	public static String convertMemoryBytesToHexString(MemoryByte[] bytes) {
		StringBuffer strBuffer = new StringBuffer();
		char charArray[];

		for (MemoryByte b : bytes) {
			charArray = MemoryUtils.convertByteToCharArray(b.getValue());
			strBuffer.append(charArray);
		}

		return strBuffer.toString();
	}

	/**
	 * Convert raw memory data to byte array
	 * 
	 * @param str
	 * @param numBytes
	 * @param numCharsPerByte
	 *            - number of characters per byte of data
	 * @return an array of byte, converted from a hex string
	 * @throws NumberFormatException
	 */
	public static MemoryByte[] convertHexStringToMemoryBytes(String str, int numBytes, int numCharsPerByte)
			throws NumberFormatException {
		if (str.length() == 0)
			return null;

		StringBuffer buf = new StringBuffer(str);

		// pad string with zeros
		int requiredPadding = numBytes * numCharsPerByte - str.length();
		while (requiredPadding > 0) {
			buf.insert(0, "0"); //$NON-NLS-1$
			requiredPadding--;
		}

		MemoryByte[] bytes = new MemoryByte[numBytes];
		str = buf.toString();

		// set data in memory
		for (int i = 0; i < bytes.length; i++) {
			// convert string to byte
			String oneByte = str.substring(i * 2, i * 2 + 2);

			Integer number = Integer.valueOf(oneByte, 16);
			if (number.compareTo(Integer.valueOf(Byte.toString(Byte.MAX_VALUE))) > 0) {
				int temp = number.intValue();
				temp = temp - 256;

				String tempStr = Integer.toString(temp);

				Byte myByte = Byte.valueOf(tempStr);
				bytes[i] = new MemoryByte(myByte.byteValue());
			} else {
				Byte myByte = Byte.valueOf(oneByte, 16);
				bytes[i] = new MemoryByte(myByte.byteValue());
			}
		}

		return bytes;
	}
	
	public static class TypeCharacteristics {
		public int basicType = IBasicType.t_unspecified;
		public boolean isSigned = false;
		public boolean isShort = false;
		public boolean isLong = false;
		public boolean isLongLong = false;
		public boolean isComplex = false;
		
		public TypeCharacteristics(IType varType) {
			if (varType instanceof IBasicType) {
				IBasicType type = (IBasicType) varType;
				basicType = type.getBaseType();
				isSigned = type.isSigned();
				isShort = type.isShort();
				isLong = type.isLong();
				
				if (varType instanceof ICPPBasicType) {
					ICPPBasicType cppType = (ICPPBasicType) varType;
					isLongLong = cppType.isLongLong();
					isComplex  = cppType.isComplex();
				}
			} else if (varType instanceof IPointerType) {
				// treat pointer as an unsigned int
				basicType = IBasicType.t_int;
			} else if (varType instanceof IEnumerator){
				// treat enumerator as a signed int
				basicType = IBasicType.t_int;
				isSigned = true;
			} else {
				// treat unknown type as an unsigned int
				basicType = IBasicType.t_int;
			}
		}
	}

	public static BigInteger convertValueToMemory(IType varType, Number value) throws CoreException {
		BigInteger result = null;
		if (varType == null)
			throw EDCDebugger.newCoreException("Unknown type");
		int varSize = varType.getByteSize();
		if (varSize <= 0)
			throw EDCDebugger.newCoreException("Type has no size");
			
		TypeCharacteristics characteristics = new TypeCharacteristics(varType);
	
		// all other locations
		switch (characteristics.basicType) {
		case IBasicType.t_float:
		case IBasicType.t_double:
			if (varSize == 4) {
				result = BigInteger.valueOf(Float.floatToIntBits(value.floatValue()));
			} else if (varSize == 8) {
				result = BigInteger.valueOf(Double.doubleToLongBits(value.doubleValue()));
			}
			break;
	
		case ICPPBasicType.t_bool:
		case ICPPBasicType.t_wchar_t:
		case IBasicType.t_char:
		case IBasicType.t_int:
		case IBasicType.t_void:
			if (characteristics.isSigned) {
				// as needed, mask the value and sign-extend
				if (varSize == 4) {
					result = BigInteger.valueOf(value.intValue());
				} else if (varSize == 2) {
					int intResult = value.intValue() & 0xffff;
					if ((intResult & 0x00008000) != 0)
						intResult |= 0xffff0000;
					result = BigInteger.valueOf(intResult);
				} else if (varSize == 1) {
					int intResult = value.intValue() & 0xff;
					if ((intResult & 0x00000080) != 0)
						intResult |= 0xffffff00;
					result = BigInteger.valueOf(intResult);
				} else {
					// assume an 8-byte long is the default
					result = BigInteger.valueOf(value.longValue());
				}
			} else {
				if (varSize == 4) {
					result = BigInteger.valueOf(value.longValue() & 0xffffffffL);  // keep it unsigned
				} else if (varSize == 2) {
					result = BigInteger.valueOf(value.intValue() & 0xffff);
				} else if (varSize == 1) {
					result = BigInteger.valueOf(value.intValue() & 0xff);
				} else {
					// assume an 8-byte long is the default
					result = BigInteger.valueOf(value.longValue());
				}
			}
			break;
	
		case IBasicType.t_unspecified:
		default:
			assert false;
			throw EDCDebugger.newCoreException("Unhandled type");
		}
		return result;
	}
}

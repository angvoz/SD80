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

package org.eclipse.cdt.debug.edc.agent.gdbserver;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.edc.tcf.extension.AgentException;
import org.eclipse.cdt.debug.edc.tcf.extension.AgentUtils;

/**
 * A cache for a group of registers where the group can be GPR, FPR, or whatever
 * the target platform has. The cache may cache any number of registers, from
 * one to all, in the group.
 * 
 * @author LWang
 * 
 */
public abstract class RegisterCache {

	static class Register {
		public String name; // should be in sync with reg names defined in host
		// debugger
		public int size; // size in bytes
		public String value; // string representation of value in hexadecimal and in little-endian.
		public boolean valid;

		public Register(String name, int size) {
			this.name = name;
			this.size = size;
			value = null;
			valid = false;
		}

		@Override
		public String toString() {
			return name + ":" + size + ":" + value + ":" + valid;
		}
	}

	// value stored in little-endian as that's the endianness used by GDB remote
	// protocol on transmitting register values.
	private Register[] fCache;

	private Map<String, Integer> fName2IDMap = new HashMap<String, Integer>();

	public RegisterCache() {
		fCache = getCacheDefinition();

		for (int i = 0; i < fCache.length; i++)
			fName2IDMap.put(fCache[i].name, i);
	}

	/**
	 * Define what registers will be in the cache. This is target specific.
	 * 
	 * @return an array of Register objects.
	 */
	abstract protected Register[] getCacheDefinition();

	/**
	 * Update the cache from a gdbserver response packet for register read
	 * command "g".
	 * 
	 * @param p
	 * @throws AgentException
	 */
	abstract public void updateCache(GdbRemoteProtocol.Packet p) throws AgentException;

	/**
	 * Check if the cache has valid data.
	 * 
	 * @return true if all registers in the cache are valid, false otherwise.
	 */
	public boolean isCacheValid() {
		for (Register r : fCache)
			if (!r.valid)
				return false;

		return true;
	}

	/**
	 * Check if the register with given ID is cached (has valid value in the
	 * cache).
	 * 
	 * @param name
	 * @return
	 * @throws exception
	 *             if the given register is not in the cache.
	 */
	public boolean isRegisterCached(int id) throws AgentException {
		checkID(id);

		return fCache[id].valid;
	}

	/**
	 * Check if the register with given name is cached (has valid value in the
	 * cache).
	 * 
	 * @param name
	 * @return
	 */
	public boolean isRegisterCached(String name) throws AgentException {
		int id = checkName(name);
		return isRegisterCached(id);
	}

	public void invalidateCache() {
		for (Register r : fCache)
			r.valid = false;
	}

	/**
	 * Get cached value of the given register.
	 * 
	 * @param id
	 * @return String representation of the hex value in big-endian
	 * @throws AgentException
	 *             if the register is not part of the cache.
	 */
	public String getRegisterValue(int id) throws AgentException {
		checkID(id);

		return Swap4(fCache[id].value);
	}

	/**
	 * Get register value in big-endian byte array.
	 * E.g. with value 0x11223344, big-endian array is: [0x11, 0x22, 0x33, 0x44]
	 * while little-endian is: [0x44, 0x33, 0x22, 0x11]
	 * 
	 * @param id
	 * @return byte[] representation of the value in big-endian
	 * @throws AgentException
	 *             if the register is not part of the cache.
	 */
	public byte[] getRegisterValueAsBytes(int id) throws AgentException {
		return AgentUtils.hexStringToByteArray(getRegisterValue(id));
	}

	/**
	 * Get cached value of the given register.
	 * 
	 * @param id
	 * @return String representation of the hex value in big-endian
	 * @throws AgentException
	 *             if the register is not part of the cache.
	 */
	public String getRegisterValue(String regName) throws AgentException {
		int id = checkName(regName);
		return getRegisterValue(id);
	}

	/**
	 * Cache value of the given register.
	 * 
	 * @param id
	 * @param value
	 *            - hex string in little-endian.
	 * @throws AgentException
	 *             if the register is not part of the cache.
	 */
	public void cacheRegister(int id, String value) throws AgentException {
		checkID(id);

		fCache[id].value = value;
		fCache[id].valid = true;
	}

	/**
	 * Cache value of the given register.
	 * 
	 * @param id
	 * @param value
	 *            - hex string in little-endian.
	 * @throws AgentException
	 *             if the register is not part of the cache.
	 */
	public void cacheRegister(String regName, String value) throws AgentException {
		int id = checkName(regName);
		cacheRegister(id, value);
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();

		for (Register element : fCache)
			buf.append(element.value);

		return buf.toString();
	}

	/**
	 * @param str
	 *            - string representation of a 4-byte hexadecimal value, e.g.
	 *            "b0f0132a"
	 * @return a newly allocated string representing the swapted 4-byte value,
	 *         e.g. "2a13f0b0"
	 * @throws AgentException
	 */
	public static String Swap4(String str) throws AgentException {
		if (str.length() != 8)
			throw new AgentException("Argument string must be 8-char long for Swap4()");

		char[] a = str.toCharArray();
		char t;
		t = a[0];
		a[0] = a[6];
		a[6] = t;
		t = a[1];
		a[1] = a[7];
		a[7] = t;
		t = a[2];
		a[2] = a[4];
		a[4] = t;
		t = a[3];
		a[3] = a[5];
		a[5] = t;

		return new String(a);
	}

	protected void checkID(int id) throws AgentException {
		if (id < 0 || id >= fCache.length)
			throw new AgentException(MessageFormat.format("Register with ID ''{0}'' is not in the cache.", id));
	}

	protected int checkName(String regName) throws AgentException {
		Integer id = fName2IDMap.get(regName);
		if (id == null || id < 0 || id >= fCache.length)
			throw new AgentException(MessageFormat.format("Register with name ''{0}'' is not in the cache.", regName));

		return id.intValue();
	}
}

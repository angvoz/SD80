/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation  Feb, 2010
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.tcf.extension.agent;

import java.util.HashMap;
import java.util.Map;

/**
 * Place holder for static info about register or register group.
 * Dynamic info about a register or register group include register
 * value and TCF context ID. All other info are static.
 */
public interface IRegisterInfo {

	static class RegisterInfo {

		private Map<String, Object>	properties;

		/**
		 * @param name
		 *            name of the register, must be unique in its group.
		 * @param commonProps
		 *            properties common to all registers in the same group. Can be empty.
		 * @param specificProps
		 *            properties specific to the register.
		 */
		public RegisterInfo(Map<String, Object> commonProps, Map<String, Object> specificProps) {
			super();
			properties = new HashMap<String, Object>(commonProps);
			properties.putAll(specificProps);
		}

		public Map<String, Object> getProperties() {
			return properties;
		}
	}
	
	static class RegisterGroupInfo {
		/**
		 * Group specific properties
		 */
		private Map<String, Object>	properties;
		
		/**
		 * Registers in the group.
		 */
		private RegisterInfo[]	registers;

		public RegisterGroupInfo(Map<String, Object> properties,
				RegisterInfo[] registers) {
			super();
			this.properties = properties;
			this.registers = registers;
		}

		/**
		 * Group specific properties
		 */
		public Map<String, Object> getProperties() {
			return properties;
		}

		public RegisterInfo[] getRegisters() {
			return registers;
		}
	}
}

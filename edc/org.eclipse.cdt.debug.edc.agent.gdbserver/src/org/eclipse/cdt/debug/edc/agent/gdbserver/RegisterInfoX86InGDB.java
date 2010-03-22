/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nokia - Initial API and implementation. Mar, 2010
 *******************************************************************************/

package org.eclipse.cdt.debug.edc.agent.gdbserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.tcf.extension.agent.IRegisterInfo.RegisterInfo;
import org.eclipse.cdt.debug.edc.tcf.extension.agent.IRegisterInfo.RegisterGroupInfo;
import org.eclipse.tm.tcf.services.IRegisters;

/**
 * Singleton class holding static info of ARM registers supported by TRK.  
 */
public class RegisterInfoX86InGDB {
	/**
	 * Register ID in GDB remote protocol.
	 * This is position of the register (0, 1, 2, 3...) in the reply string
	 * of command "g".
	 * Value type: integer
	 */
	public final static String PROP_GDB_REGISTER_ID = "GDB_REGISTER_ID";

	public final static int PC_REGISTER_NO_IN_GDB = 8;	// EIP
	
	static private List<RegisterGroupInfo> sRegisterGroupInfos = null;
	
	static private RegisterInfoX86InGDB instance = null;
	
	private RegisterInfoX86InGDB() {};
	
	static public RegisterInfoX86InGDB getInstance() {
		if (instance == null)
			instance = new RegisterInfoX86InGDB();
		
		return instance;
	}
	
	public List<RegisterGroupInfo> getRegisterGroupInfo() {
		if (sRegisterGroupInfos == null)
			setupRegisterGroupInfo();
		
		return sRegisterGroupInfos;
	}
	
	static private RegisterInfo setupRegister(Map<String, Object> commProps, String name, int idInGDB, String role) {
		Map<String, Object> props = new HashMap<String, Object>();
		props.put(IRegisters.PROP_NAME, 	name);
		props.put(PROP_GDB_REGISTER_ID, 	new Integer(idInGDB));
		if (role != null)
			props.put(IRegisters.PROP_ROLE, 	role);
		
		return new RegisterInfo(commProps, props);
	}
	
	@SuppressWarnings("serial")
	private static RegisterGroupInfo setupRegGroup_UserMode() {
		// Properties common to all registers in the group
		//
		Map<String, Object> commonProps = new HashMap<String, Object>() {{
			put(IRegisters.PROP_SIZE, 		4);
			put(IRegisters.PROP_READBLE, 	true);
			put(IRegisters.PROP_WRITEABLE, 	true);
			put(IRegisters.PROP_BIG_ENDIAN, true);	// pass value in big-endian.

			// See RegistersProxy.Context for default values of other properties. 
		}};
		
		Map<String, Object> groupProps = new HashMap<String, Object>() {{
				put(IRegisters.PROP_NAME, 			"Basic");
				put(IRegisters.PROP_DESCRIPTION, 	"Basic program execution registers");
				put(IRegisters.PROP_ROLE, 	IRegisters.ROLE_CORE);
		}};
		
		RegisterInfo[] regs = new RegisterInfo[] {
			setupRegister(commonProps, "EAX", 0, null), 
			setupRegister(commonProps, "ECX", 1, null), 
			setupRegister(commonProps, "EDX", 2, null), 
			setupRegister(commonProps, "EBX", 3, null), 
			setupRegister(commonProps, "ESP", 4, IRegisters.ROLE_SP), 
			setupRegister(commonProps, "EBP", 5, null), 
			setupRegister(commonProps, "ESI", 6, null), 
			setupRegister(commonProps, "EDI", 7, null), 
			setupRegister(commonProps, "EIP", 8, IRegisters.ROLE_PC), 
			setupRegister(commonProps, "EFL", 9, null), 
			setupRegister(commonProps, "CS", 10, null), 
			setupRegister(commonProps, "SS", 11, null), 
			setupRegister(commonProps, "DS", 12, null), 
			setupRegister(commonProps, "ES",  13, null), 
			setupRegister(commonProps, "FS",  14, null), 
			setupRegister(commonProps, "GS",  15, null), 
		};
		
		return new RegisterGroupInfo(groupProps, regs);
	}

	/*
	 * Set up static info about all registers and register groups supported.
	 * Note this should be called only once.
	 */
	static private void setupRegisterGroupInfo() {
		sRegisterGroupInfos = new ArrayList<RegisterGroupInfo>();
		sRegisterGroupInfos.add(setupRegGroup_UserMode());
	}
}

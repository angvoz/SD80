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
package org.eclipse.cdt.debug.edc.internal.arm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.service.DsfSession;

public class ARMRegisters extends Registers {

	/**
	 * ARM User/System mode registers
	 */
	public final static String USER_MODE_REGISTERS = "User/System Mode Registers"; //$NON-NLS-1$

	public final static String R0 = "R0"; //$NON-NLS-1$
	public final static String R1 = "R1"; //$NON-NLS-1$
	public final static String R2 = "R2"; //$NON-NLS-1$
	public final static String R3 = "R3"; //$NON-NLS-1$
	public final static String R4 = "R4"; //$NON-NLS-1$
	public final static String R5 = "R5"; //$NON-NLS-1$
	public final static String R6 = "R6"; //$NON-NLS-1$
	public final static String R7 = "R7"; //$NON-NLS-1$
	public final static String R8 = "R8"; //$NON-NLS-1$
	public final static String R9 = "R9"; //$NON-NLS-1$
	public final static String R10 = "R10"; //$NON-NLS-1$
	public final static String R11 = "R11"; //$NON-NLS-1$
	public final static String R12 = "R12"; //$NON-NLS-1$
	public final static String SP = "SP"; //$NON-NLS-1$
	public final static String LR = "LR"; //$NON-NLS-1$
	public final static String PC = "PC"; //$NON-NLS-1$
	public final static String CPSR = "CPSR"; //$NON-NLS-1$

	/**
	 * ARM FIQ mode registers
	 */
	public final static String FIQ_MODE_REGISTERS = "FIQ Mode Registers"; //$NON-NLS-1$

	public final static String R8_fiq = "R8_fiq"; //$NON-NLS-1$
	public final static String R9_fiq = "R9_fiq"; //$NON-NLS-1$
	public final static String R10_fiq = "R10_fiq"; //$NON-NLS-1$
	public final static String R11_fiq = "R11_fiq"; //$NON-NLS-1$
	public final static String R12_fiq = "R12_fiq"; //$NON-NLS-1$
	public final static String SP_fiq = "SP_fiq"; //$NON-NLS-1$
	public final static String LR_fiq = "LR_fiq"; //$NON-NLS-1$
	public final static String SPSR_fiq = "SPSR_fiq"; //$NON-NLS-1$

	/**
	 * ARM IRQ mode registers
	 */
	public final static String IRQ_MODE_REGISTERS = "IRQ Mode Registers"; //$NON-NLS-1$

	public final static String SP_irq = "SP_irq"; //$NON-NLS-1$
	public final static String LR_irq = "LR_irq"; //$NON-NLS-1$
	public final static String SPSR_irq = "SPSR_irq"; //$NON-NLS-1$

	/**
	 * ARM supervisor mode registers
	 */
	public final static String SUPERVISOR_MODE_REGISTERS = "Supervisor Mode Registers"; //$NON-NLS-1$

	public final static String SP_svc = "SP_svc"; //$NON-NLS-1$
	public final static String LR_svc = "LR_svc"; //$NON-NLS-1$
	public final static String SPSR_svc = "SPSR_svc"; //$NON-NLS-1$

	/**
	 * ARM abort mode registers
	 */
	public final static String ABORT_MODE_REGISTERS = "Abort Mode Registers"; //$NON-NLS-1$

	public final static String SP_abt = "SP_abt"; //$NON-NLS-1$
	public final static String LR_abt = "LR_abt"; //$NON-NLS-1$
	public final static String SPSR_abt = "SPSR_abt"; //$NON-NLS-1$

	/**
	 * ARM undefined mode registers
	 */
	public final static String UNDEFINED_MODE_REGISTERS = "Undefined Mode Registers"; //$NON-NLS-1$

	public final static String SP_und = "SP_und"; //$NON-NLS-1$
	public final static String LR_und = "LR_und"; //$NON-NLS-1$
	public final static String SPSR_und = "SPSR_und"; //$NON-NLS-1$

	/**
	 * ARM system mode registers
	 */
	public final static String SYSTEM_MODE_REGISTERS = "System Mode Registers"; //$NON-NLS-1$

	public final static String SP_sys = "SP_sys"; //$NON-NLS-1$
	public final static String LR_sys = "LR_sys"; //$NON-NLS-1$
	public final static String SPSR_sys = "SPSR_sys"; //$NON-NLS-1$

	/**
	 * ARM single precision floating point registers
	 */
	public final static String SP_FLOATING_POINT_REGISTERS = "Single Precision Floating Point Registers"; //$NON-NLS-1$

	public final static String S0 = "S0"; //$NON-NLS-1$
	public final static String S1 = "S1"; //$NON-NLS-1$
	public final static String S2 = "S2"; //$NON-NLS-1$
	public final static String S3 = "S3"; //$NON-NLS-1$
	public final static String S4 = "S4"; //$NON-NLS-1$
	public final static String S5 = "S5"; //$NON-NLS-1$
	public final static String S6 = "S6"; //$NON-NLS-1$
	public final static String S7 = "S7"; //$NON-NLS-1$
	public final static String S8 = "S8"; //$NON-NLS-1$
	public final static String S9 = "S0"; //$NON-NLS-1$
	public final static String S10 = "S10"; //$NON-NLS-1$
	public final static String S11 = "S11"; //$NON-NLS-1$
	public final static String S12 = "S12"; //$NON-NLS-1$
	public final static String S13 = "S13"; //$NON-NLS-1$
	public final static String S14 = "S14"; //$NON-NLS-1$
	public final static String S15 = "S15"; //$NON-NLS-1$
	public final static String S16 = "S16"; //$NON-NLS-1$
	public final static String S17 = "S17"; //$NON-NLS-1$
	public final static String S18 = "S18"; //$NON-NLS-1$
	public final static String S19 = "S19"; //$NON-NLS-1$
	public final static String S20 = "S20"; //$NON-NLS-1$
	public final static String S21 = "S21"; //$NON-NLS-1$
	public final static String S22 = "S22"; //$NON-NLS-1$
	public final static String S23 = "S23"; //$NON-NLS-1$
	public final static String S24 = "S24"; //$NON-NLS-1$
	public final static String S25 = "S25"; //$NON-NLS-1$
	public final static String S26 = "S26"; //$NON-NLS-1$
	public final static String S27 = "S27"; //$NON-NLS-1$
	public final static String S28 = "S28"; //$NON-NLS-1$
	public final static String S29 = "S29"; //$NON-NLS-1$
	public final static String S30 = "S30"; //$NON-NLS-1$
	public final static String S31 = "S31"; //$NON-NLS-1$

	/**
	 * ARM double precision floating point registers
	 */
	public final static String DP_FLOATING_POINT_REGISTERS = "Double Precision Floating Point Registers"; //$NON-NLS-1$

	public final static String D0 = "D0"; //$NON-NLS-1$
	public final static String D1 = "D1"; //$NON-NLS-1$
	public final static String D2 = "D2"; //$NON-NLS-1$
	public final static String D3 = "D3"; //$NON-NLS-1$
	public final static String D4 = "D4"; //$NON-NLS-1$
	public final static String D5 = "D5"; //$NON-NLS-1$
	public final static String D6 = "D6"; //$NON-NLS-1$
	public final static String D7 = "D7"; //$NON-NLS-1$
	public final static String D8 = "D8"; //$NON-NLS-1$
	public final static String D9 = "D0"; //$NON-NLS-1$
	public final static String D10 = "D10"; //$NON-NLS-1$
	public final static String D11 = "D11"; //$NON-NLS-1$
	public final static String D12 = "D12"; //$NON-NLS-1$
	public final static String D13 = "D13"; //$NON-NLS-1$
	public final static String D14 = "D14"; //$NON-NLS-1$
	public final static String D15 = "D15"; //$NON-NLS-1$

	/**
	 * ARM common floating point registers
	 */
	public final static String FPSID = "FPSID"; //$NON-NLS-1$
	public final static String FPSCR = "FPSCR"; //$NON-NLS-1$
	public final static String FPEXC = "FPEXC"; //$NON-NLS-1$

	private final Map<String, List<String>> registerGroups = new HashMap<String, List<String>>();

	public ARMRegisters(DsfSession session) {
		super(session, new String[] { IRegisters.class.getName(), Registers.class.getName(),
				ARMRegisters.class.getName() });

		registerGroups.put(USER_MODE_REGISTERS, getUserModeRegisterNames());

		// TODO add these back in one we figure out how to let agents/services
		// decide which groups is supports
		/*
		 * registerGroups.put(FIQ_MODE_REGISTERS, getFIQModeRegisterNames());
		 * registerGroups.put(IRQ_MODE_REGISTERS, getIRQModeRegisterNames());
		 * registerGroups.put(SUPERVISOR_MODE_REGISTERS,
		 * getSupervisorModeRegisterNames());
		 * registerGroups.put(ABORT_MODE_REGISTERS,
		 * getAbortModeRegisterNames());
		 * registerGroups.put(UNDEFINED_MODE_REGISTERS,
		 * getUndefinedModeRegisterNames());
		 * registerGroups.put(SYSTEM_MODE_REGISTERS,
		 * getSystemModeRegisterNames());
		 * registerGroups.put(SP_FLOATING_POINT_REGISTERS,
		 * getSPFPRegisterNames());
		 * registerGroups.put(DP_FLOATING_POINT_REGISTERS,
		 * getDPFPRegisterNames());
		 */
	}

	@Override
	protected List<RegisterGroupDMC> createGroupsForContext(final IEDCExecutionDMC ctx) {

		List<RegisterGroupDMC> groups = super.createGroupsForContext(ctx);
		if (groups.size() > 0)
			return groups;

		// old way 
		groups = Collections.synchronizedList(new ArrayList<RegisterGroupDMC>());

		for (String groupName : registerGroups.keySet()) {
			groups.add(new RegisterGroupDMC(this, ctx, groupName, groupName, groupName));
		}

		return groups;
	}

	@Override
	protected List<RegisterDMC> createRegistersForGroup(RegisterGroupDMC registerGroupDMC) {

		List<RegisterDMC> registers = super.createRegistersForGroup(registerGroupDMC);
		if (registers.size() > 0)
			return registers;

		// old way 
		registers = new ArrayList<RegisterDMC>();

		List<String> registerNames = registerGroups.get(registerGroupDMC.getID());
		if (registerNames != null) {
			for (String registerName : registerNames) {
				registers.add(new RegisterDMC(registerGroupDMC.getExecutionDMC(), registerName, registerName,
						registerName));
			}
		}
		
		return registers;
	}

	private List<String> getUserModeRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(R0);
		registerNames.add(R1);
		registerNames.add(R2);
		registerNames.add(R3);
		registerNames.add(R4);
		registerNames.add(R5);
		registerNames.add(R6);
		registerNames.add(R7);
		registerNames.add(R8);
		registerNames.add(R9);
		registerNames.add(R10);
		registerNames.add(R11);
		registerNames.add(R12);
		registerNames.add(SP);
		registerNames.add(LR);
		registerNames.add(PC);
		registerNames.add(CPSR);

		return registerNames;
	}

/*	private List<String> getFIQModeRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(R8_fiq);
		registerNames.add(R9_fiq);
		registerNames.add(R10_fiq);
		registerNames.add(R11_fiq);
		registerNames.add(R12_fiq);
		registerNames.add(SP_fiq);
		registerNames.add(LR_fiq);
		registerNames.add(SPSR_fiq);

		return registerNames;
	}
*/
/*	private List<String> getIRQModeRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(SP_irq);
		registerNames.add(LR_irq);
		registerNames.add(SPSR_irq);

		return registerNames;
	}
*/
/*	private List<String> getSupervisorModeRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(SP_svc);
		registerNames.add(LR_svc);
		registerNames.add(SPSR_svc);

		return registerNames;
	}
*/
/*	private List<String> getAbortModeRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(SP_abt);
		registerNames.add(LR_abt);
		registerNames.add(SPSR_abt);

		return registerNames;
	}
*/
/*	private List<String> getUndefinedModeRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(SP_und);
		registerNames.add(LR_und);
		registerNames.add(SPSR_und);

		return registerNames;
	}
*/
/*	private List<String> getSystemModeRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(SP_sys);
		registerNames.add(LR_sys);
		registerNames.add(SPSR_sys);

		return registerNames;
	}
*/
/*	private List<String> getSPFPRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(S0);
		registerNames.add(S1);
		registerNames.add(S2);
		registerNames.add(S3);
		registerNames.add(S4);
		registerNames.add(S5);
		registerNames.add(S6);
		registerNames.add(S7);
		registerNames.add(S8);
		registerNames.add(S9);
		registerNames.add(S10);
		registerNames.add(S11);
		registerNames.add(S12);
		registerNames.add(S13);
		registerNames.add(S14);
		registerNames.add(S15);
		registerNames.add(S16);
		registerNames.add(S17);
		registerNames.add(S18);
		registerNames.add(S19);
		registerNames.add(S20);
		registerNames.add(S21);
		registerNames.add(S22);
		registerNames.add(S23);
		registerNames.add(S24);
		registerNames.add(S25);
		registerNames.add(S26);
		registerNames.add(S27);
		registerNames.add(S28);
		registerNames.add(S29);
		registerNames.add(S30);
		registerNames.add(S31);
		registerNames.add(FPSID);
		registerNames.add(FPSCR);
		registerNames.add(FPEXC);

		return registerNames;
	}
*/
/*	private List<String> getDPFPRegisterNames() {
		List<String> registerNames = new ArrayList<String>();

		registerNames.add(D0);
		registerNames.add(D1);
		registerNames.add(D2);
		registerNames.add(D3);
		registerNames.add(D4);
		registerNames.add(D5);
		registerNames.add(D6);
		registerNames.add(D7);
		registerNames.add(D8);
		registerNames.add(D9);
		registerNames.add(D10);
		registerNames.add(D11);
		registerNames.add(D12);
		registerNames.add(D13);
		registerNames.add(D14);
		registerNames.add(D15);
		registerNames.add(FPSID);
		registerNames.add(FPSCR);
		registerNames.add(FPEXC);

		return registerNames;
	}

*/	@Override
public String getRegisterNameFromCommonID(int id) {
		if (id < 16) {
			return registerGroups.get(USER_MODE_REGISTERS).get(id);
		}
		return null;
	}

}

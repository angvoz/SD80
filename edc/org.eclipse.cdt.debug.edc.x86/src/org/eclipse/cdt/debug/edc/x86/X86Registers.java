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
package org.eclipse.cdt.debug.edc.x86;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.debug.edc.services.IEDCExecutionDMC;
import org.eclipse.cdt.debug.edc.services.Registers;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;

public class X86Registers extends Registers {

	public static final int EBX = 3;
	public static final int ESP = 4;
	public static final int EBP = 5;
	public static final int ESI = 6;
	public static final int EDI = 7;
	
	public static String[] generalRegisterNames = { "EAX", "ECX", "EDX", "EBX", "ESP", "EBP", "ESI", "EDI", "GS", "FS",
			"ES", "DS", "EIP", "CS", "EFL", "SS" };
	public static String[] generalRegisterDescriptions = { "EAX", "ECX", "EDX", "EBX", "ESP", "EBP", "ESI", "EDI",
			"GS", "FS", "ES", "DS", "EIP", "CS", "EFL", "SS" };

	public X86Registers(DsfSession session) {
		super(session, new String[] { X86Registers.class.getName() });
	}

	@Override
	protected List<RegisterGroupDMC> createGroupsForContext(IEDCExecutionDMC ctx) {

		List<RegisterGroupDMC> groups = super.createGroupsForContext(ctx);
		if (groups.size() > 0)
			return groups;

		// old way
		groups = Collections.synchronizedList(new ArrayList<RegisterGroupDMC>());
		
		if (ctx instanceof IThreadDMContext)
			groups.add(new RegisterGroupDMC(this, ctx, "General", "General x86 Registers", "GPX"));

		return groups;
	}

	@Override
	protected List<RegisterDMC> createRegistersForGroup(RegisterGroupDMC registerGroupDMC) {

		List<RegisterDMC> registers = super.createRegistersForGroup(registerGroupDMC);
		if (registers.size() > 0)
			return registers;

		// old way.
		registers = new ArrayList<RegisterDMC>();

		String groupID = registerGroupDMC.getID();

		if (groupID.equals("GPX")) {
			for (int i = 0; i < generalRegisterNames.length; i++) {
				registers.add(new RegisterDMC(registerGroupDMC.getExecutionDMC(), generalRegisterNames[i],
						generalRegisterDescriptions[i], generalRegisterNames[i]));
			}
		}

		return registers;
	}

	@Override
	public String getRegisterNameFromCommonID(int id) {
		if (id < generalRegisterNames.length) {
			return generalRegisterNames[id];
		}
		return null;
	}

}

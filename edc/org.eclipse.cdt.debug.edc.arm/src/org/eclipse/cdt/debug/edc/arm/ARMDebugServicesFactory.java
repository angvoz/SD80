package org.eclipse.cdt.debug.edc.arm;

import org.eclipse.cdt.debug.edc.internal.arm.ARMDisassembly;
import org.eclipse.cdt.debug.edc.internal.arm.ARMRegisters;
import org.eclipse.cdt.debug.edc.internal.arm.ARMStack;
import org.eclipse.cdt.debug.edc.internal.arm.TargetEnvironmentARM;
import org.eclipse.cdt.debug.edc.launch.DebugServicesFactory;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;

public class ARMDebugServicesFactory extends DebugServicesFactory implements IDsfDebugServicesFactory {

	@Override
	protected IRegisters createRegistersService(DsfSession session) {
		return new ARMRegisters(session);
	}

	@Override
	protected IStack createStackService(DsfSession session) {
		return new ARMStack(session);
	}

	@Override
	protected ITargetEnvironment createTargetEnvironmentService(DsfSession session, ILaunch launch) {
		return new TargetEnvironmentARM(session, launch);
	}
	
	@Override
	protected IDisassembly createDisassemblyService(DsfSession session) {
		return new ARMDisassembly(session);
	}
}

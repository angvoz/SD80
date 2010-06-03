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

import org.eclipse.cdt.debug.edc.launch.DebugServicesFactory;
import org.eclipse.cdt.debug.edc.services.ITargetEnvironment;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IStack;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunch;

public class DebugServicesFactoryX86 extends DebugServicesFactory implements IDsfDebugServicesFactory {

	@Override
	protected IRegisters createRegistersService(DsfSession session) {
		return new X86Registers(session);
	}

	@Override
	protected IStack createStackService(DsfSession session) {
		return new X86Stack(session);
	}

	@Override
	protected ITargetEnvironment createTargetEnvironmentService(DsfSession session, ILaunch launch) {
		return new TargetEnvironmentX86(session, launch);
	}
}

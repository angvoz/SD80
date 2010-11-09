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
package org.eclipse.cdt.debug.edc.internal.ui;

import org.eclipse.cdt.debug.edc.internal.snapshot.Album;
import org.eclipse.cdt.dsf.debug.ui.actions.DsfSteppingModeTarget;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @since 2.0
 */
public class EDCSteppingModeTarget extends DsfSteppingModeTarget {

	private DsfSession session;

	public EDCSteppingModeTarget(DsfSession session) {
		this.session = session;
	}

	@Override
	public boolean supportsInstructionStepping() {
		if (Album.isSnapshotSession(session.getId()))
			return false;
		return super.supportsInstructionStepping();
	}

}

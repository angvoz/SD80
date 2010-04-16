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
package org.eclipse.cdt.debug.edc.internal.snapshot;

import org.eclipse.cdt.debug.edc.services.Stack.StackFrameDMC;
import org.eclipse.cdt.dsf.service.DsfSession;

public interface ISnapshotAlbumEventListener {

	public void snapshotCreated(Album album, Snapshot snapshot,
			DsfSession session, StackFrameDMC stackFrame);

	public void snapshotOpened(Snapshot snapshot);

	public void snapshotSessionEnded(Album album, DsfSession session);
	
}

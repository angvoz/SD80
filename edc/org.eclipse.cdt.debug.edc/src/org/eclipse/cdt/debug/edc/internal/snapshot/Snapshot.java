/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.snapshot;

import org.eclipse.cdt.debug.edc.EDCDebugger;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.service.IDsfService;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Element;

public class Snapshot {

	private final Element rootElement;

	public Snapshot(Element snapshotRoot) {
		rootElement = snapshotRoot;
	}

	private static String getServiceFilter(String sessionId) {
		return ("(" + IDsfService.PROP_SESSION_ID + "=" + sessionId + ")").intern(); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

	public void open(DsfSession session) {
		ServiceReference[] references;
		try {
			references = EDCDebugger.getBundleContext().getServiceReferences(ISnapshotContributor.class.getName(),
					getServiceFilter(session.getId()));
			for (ServiceReference serviceReference : references) {
				ISnapshotContributor sc = (ISnapshotContributor) EDCDebugger.getBundleContext().getService(
						serviceReference);
				sc.loadSnapshot(rootElement);
			}
		} catch (Exception e) {
			EDCDebugger.getMessageLogger().logError(null, e);
		}
	}

}

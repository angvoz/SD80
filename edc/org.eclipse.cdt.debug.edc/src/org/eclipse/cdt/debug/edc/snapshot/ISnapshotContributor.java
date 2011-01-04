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
package org.eclipse.cdt.debug.edc.snapshot;

import org.eclipse.core.runtime.IProgressMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The Interface ISnapshotContributor provides a way for objects to contribute data to
 * a debug snapshot.
 */
public interface ISnapshotContributor {

	/**
	 * Take a snapshot of this object's data.
	 *
	 * @param album the owning snapshot album
	 * @param document the xml document used to store the data
	 * @param monitor the progress monitor to use for reporting progress to the user. It is the caller's responsibility
        to call done() on the given monitor. Accepts null, indicating that no progress should be
        reported and that the operation cannot be canceled.
	 * @return the xml element containing the saved data.
	 * @since 2.0
	 */
	public Element takeSnapshot(IAlbum album, Document document, IProgressMonitor monitor) throws Exception;

	/**
	 * Load an object's data from a previously saved snapshot.
	 *
	 * @param element the xml element containing the data
	 * @throws Exception if anything goes wrong
	 */
	public void loadSnapshot(Element element) throws Exception;

}

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
package org.eclipse.cdt.debug.edc.internal.snapshot;

import org.eclipse.cdt.debug.edc.internal.EDCDebugger;
import org.eclipse.cdt.debug.edc.snapshot.IAlbum;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainerType;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainer;

public class AlbumSourceContainer extends AbstractSourceContainer {

	private IAlbum album;

	public static final String TYPE_ID = EDCDebugger.getUniqueIdentifier() + ".containerType.albumMapping"; //$NON-NLS-1$

	public AlbumSourceContainer(IAlbum album) {
		this.album = album;
	}

	public AlbumSourceContainer() {
	}

	public Object[] findSourceElements(String name) throws CoreException {
		// TODO Auto-generated method stub
		return new Object[0];
	}

	public String getName() {
		return album.getDisplayName();
	}

	public ISourceContainerType getType() {
		return getSourceContainerType(TYPE_ID);
	}

}

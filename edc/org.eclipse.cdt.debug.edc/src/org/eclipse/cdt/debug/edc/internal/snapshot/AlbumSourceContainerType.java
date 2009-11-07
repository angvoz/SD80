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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.AbstractSourceContainerTypeDelegate;

public class AlbumSourceContainerType extends AbstractSourceContainerTypeDelegate {

	public ISourceContainer createSourceContainer(String memento) throws CoreException {
		return new AlbumSourceContainer();
	}

	public String getMemento(ISourceContainer container) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

}

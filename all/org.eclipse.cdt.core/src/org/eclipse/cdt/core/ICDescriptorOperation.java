/*******************************************************************************
 * Copyright (c) 2002, 2003, 2004 QNX Software Systems Ltd. and others. All
 * rights reserved. This program and the accompanying materials are made
 * available under the terms of the Common Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - Initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public interface ICDescriptorOperation {
	
	void execute(ICDescriptor descriptor, IProgressMonitor monitor) throws CoreException;

}

/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.resources;


import org.eclipse.cdt.core.ConsoleOutputStream;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;


public interface IConsole {
	void start(IProject project);
    ConsoleOutputStream getOutputStream() throws CoreException;
    ConsoleOutputStream getInfoStream() throws CoreException;
    ConsoleOutputStream getErrorStream() throws CoreException;
}


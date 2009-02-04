/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jul 5, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.cdt.utils;

import org.eclipse.core.runtime.IPath;

/**
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IGnuToolFactory {

	Addr2line getAddr2line(IPath path);

	CPPFilt getCPPFilt();

	Objdump getObjdump(IPath path);

	NM getNM(IPath path);
}

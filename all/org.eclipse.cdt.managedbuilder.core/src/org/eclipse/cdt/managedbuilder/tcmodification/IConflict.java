/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.tcmodification;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.core.runtime.IPath;

public interface IConflict {
	int INCOMPATIBLE = 1;
	int SOURCE_EXT_CONFLICT = 1 << 1;
	
	int getConflictType();
	
	int getObjectType();
	
	IBuildObject getBuildObject();
	
	
	IPath[] getPaths();
}

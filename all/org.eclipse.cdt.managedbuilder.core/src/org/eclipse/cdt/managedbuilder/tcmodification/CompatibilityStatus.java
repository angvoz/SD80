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
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.internal.tcmodification.ConflictSet;
import org.eclipse.core.runtime.Status;


public final class CompatibilityStatus extends Status {

	public static final CompatibilityStatus OK_COMPATIBILITY_STATUS = new CompatibilityStatus(OK, ManagedBuilderCorePlugin.getUniqueIdentifier(), ""); //$NON-NLS-1$
	
	private ConflictSet fConflictSet;

//	private CompatibilityStatus(int severity, String pluginId, int code,
//			String message, Throwable exception) {
//		super(severity, pluginId, code, message, exception);
//	}

//	private CompatibilityStatus(int severity, String pluginId, String message,
//			Throwable exception) {
//		super(severity, pluginId, message, exception);
//	}

	private CompatibilityStatus(int severity, String pluginId, String message) {
		super(severity, pluginId, message);
	}
	
	public CompatibilityStatus(int severity, String message, ConflictSet cs) {
		this(severity, ManagedBuilderCorePlugin.getUniqueIdentifier(), message);
		
		fConflictSet = cs;
	}
	
	public IConflict[] getConflicts(){
		return fConflictSet != null ? fConflictSet.getConflicts() : ConflictSet.EMPTY_CONFLICT_ARRAY;
	}
	
	public IConflict[] getConflictsWith(int objectType){
		return fConflictSet != null ? fConflictSet.getConflictsWith(objectType) : ConflictSet.EMPTY_CONFLICT_ARRAY;
	}
	
	public IBuildObject[] getConflictingObjects(int objectType){
		return fConflictSet != null ? fConflictSet.getConflictingObjects(objectType) : ConflictSet.EMPTY_BO_ARRAY;
	}
	
	public IConflict getConflictWith(IBuildObject bo){
		return fConflictSet != null ? fConflictSet.getConflictWith(bo) : null;
	}

}

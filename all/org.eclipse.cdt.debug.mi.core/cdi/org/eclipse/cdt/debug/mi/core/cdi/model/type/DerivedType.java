/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.mi.core.cdi.model.type;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIDerivedType;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.cdt.debug.mi.core.cdi.SourceManager;

/**
 */
public abstract class DerivedType extends Type implements ICDIDerivedType {

	ICDIType derivedType;

	public DerivedType(ICDITarget target, String typename) {
		super(target, typename);
	}

	public void setComponentType(ICDIType dtype) {
		derivedType = dtype;
	}

	public void setComponentType(String name) {
		ICDITarget target = getTarget();
		Session session = (Session)(target.getSession());
		SourceManager sourceMgr = (SourceManager)session.getSourceManager();
		try {
			derivedType = sourceMgr.getType(target, name);
		} catch (CDIException e) {
			// Try after ptype.
			try {
				String ptype = sourceMgr.getDetailTypeName(name);
				derivedType = sourceMgr.getType(target, ptype);
			} catch (CDIException ex) {
			}
		}
		if (derivedType == null) {
			derivedType = new IncompleteType(getTarget(), name);
		}
	}
}

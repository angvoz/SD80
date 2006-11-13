/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCPPField extends PDOMCPPVariable implements ICPPField {
	
	public PDOMCPPField(PDOM pdom, PDOMCPPClassType parent, ICPPField field)
			throws CoreException {
		super(pdom, parent, field);
	}		

	public PDOMCPPField(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	// @Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	// @Override
	public int getNodeType() {
		return PDOMCPPLinkage.CPPFIELD;
	}
	
	public ICPPClassType getClassOwner() throws DOMException {
		try {
			return (ICPPClassType)getParentNode();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public int getVisibility() throws DOMException {
		return PDOMCPPAnnotation.getVisibility(getByte(record + ANNOTATIONS));
	}

	// @Override
	public boolean isMutable() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCPPAnnotation.MUTABLE_OFFSET);
	}

	// @Override
	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	// @Override
	public boolean isExtern() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}

	// @Override
	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false; 
	}
}

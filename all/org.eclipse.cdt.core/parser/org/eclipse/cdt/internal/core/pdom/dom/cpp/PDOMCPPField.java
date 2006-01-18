/*******************************************************************************
 * Copyright (c) 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;
import org.eclipse.cdt.internal.core.pdom.PDOMDatabase;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMember;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPField extends PDOMMember implements ICPPField {

	public PDOMCPPField(PDOMDatabase pdom, PDOMCPPClassType parent, IASTName name)
			throws CoreException {
		super(pdom, parent, name, PDOMCPPLinkage.CPPFIELD);
	}		

	public PDOMCPPField(PDOMDatabase pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	public ICPPClassType getClassOwner() throws DOMException {
		try {
			return (ICPPClassType)getMemberOwner();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	public String[] getQualifiedName() throws DOMException {
        return CPPVisitor.getQualifiedName( this );
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isGloballyQualified() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public int getVisibility() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IType getType() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isAuto() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isExtern() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isRegister() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isStatic() throws DOMException {
		throw new PDOMNotImplementedError();
	}

}

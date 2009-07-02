/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a c++ enumerator in the index.
 */
class PDOMCPPEnumerator extends PDOMCPPBinding implements IEnumerator {

	private static final int ENUMERATION = PDOMBinding.RECORD_SIZE + 0;
	private static final int NEXT_ENUMERATOR = PDOMBinding.RECORD_SIZE + 4;
	private static final int VALUE= PDOMBinding.RECORD_SIZE + 8;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 12;
		
	public PDOMCPPEnumerator(PDOMLinkage linkage, PDOMNode parent, IEnumerator enumerator, PDOMCPPEnumeration enumeration)
			throws CoreException {
		super(linkage, parent, enumerator.getNameCharArray());
		
		final Database db = getDB();
		db.putRecPtr(record + ENUMERATION, enumeration.getRecord());
		storeValue(db, enumerator);
		enumeration.addEnumerator(this);
	}

	public PDOMCPPEnumerator(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}
	
	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPENUMERATOR;
	}

	private void storeValue(final Database db, IEnumerator enumerator) throws CoreException {
		IValue value= enumerator.getValue();
		if (value != null) {
			Long val= value.numericalValue();
			db.putInt(record + VALUE, val == null ? -1 : val.intValue());
		}
	}
	
	@Override
	public void update(PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IEnumerator)
			storeValue(getDB(), (IEnumerator) newBinding);
	}

	public PDOMCPPEnumerator getNextEnumerator() throws CoreException {
		long value = getDB().getRecPtr(record + NEXT_ENUMERATOR);
		return value != 0 ? new PDOMCPPEnumerator(getLinkage(), value) : null;
	}
	
	public void setNextEnumerator(PDOMCPPEnumerator enumerator) throws CoreException {
		long value = enumerator != null ? enumerator.getRecord() : 0;
		getDB().putRecPtr(record + NEXT_ENUMERATOR, value);
	}
	
	public IType getType() throws DOMException {
		try {
			return new PDOMCPPEnumeration(getLinkage(), getDB().getRecPtr(record + ENUMERATION));
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
	
	public IValue getValue() {
		try {
			int val= getDB().getInt(record + VALUE);
			return Value.create(val);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return Value.UNKNOWN;
	}
}

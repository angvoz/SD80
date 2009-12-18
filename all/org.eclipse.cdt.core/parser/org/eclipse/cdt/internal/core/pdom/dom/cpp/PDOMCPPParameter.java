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
 *    IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for a parameter of a c++ function in the index.
 */
class PDOMCPPParameter extends PDOMNamedNode implements ICPPParameter, IPDOMBinding {

	private static final int NEXT_PARAM = PDOMNamedNode.RECORD_SIZE;
	private static final int ANNOTATIONS = NEXT_PARAM + Database.PTR_SIZE;
	private static final int FLAGS = ANNOTATIONS + 1;
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = FLAGS + 1;
	static {
		assert RECORD_SIZE <= 22; // 23 would yield a 32-byte block
	}
	
	private static final byte FLAG_DEFAULT_VALUE = 0x1;

	private final IType fType;
	public PDOMCPPParameter(PDOMLinkage linkage, long record, IType type) {
		super(linkage, record);
		fType= type;
	}

	public PDOMCPPParameter(PDOMLinkage linkage, PDOMNode parent, ICPPParameter param, PDOMCPPParameter next)
			throws CoreException {
		super(linkage, parent, param.getNameCharArray());
		fType= null;	// this constructor is used for adding parameters to the database, only.
		
		Database db = getDB();
		db.putByte(record + FLAGS, param.hasDefaultValue() ? FLAG_DEFAULT_VALUE : 0);
		db.putRecPtr(record + NEXT_PARAM, next == null ? 0 : next.getRecord());

		storeAnnotations(db, param);
	}

	private void storeAnnotations(Database db, ICPPParameter param) throws CoreException {
		try {
			byte annotations = PDOMCPPAnnotation.encodeAnnotation(param);
			db.putByte(record + ANNOTATIONS, annotations);
		} catch (DOMException e) {
			// ignore and miss out on some properties of the parameter
		}
	}

	public void update(ICPPParameter newPar) throws CoreException {
		final Database db = getDB();
		// Bug 297438: Don't clear the property of having a default value.
		if (newPar.hasDefaultValue()) {
			db.putByte(record + FLAGS, FLAG_DEFAULT_VALUE);
		} else if (newPar.isParameterPack()) {
			db.putByte(record + FLAGS, (byte) 0);
		} 
		storeAnnotations(db, newPar);
		
		final char[] newName = newPar.getNameCharArray();
		if (!CharArrayUtils.equals(newName, getNameCharArray())) {
			updateName(newName);
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPPARAMETER;
	}
	
	public String[] getQualifiedName() {
		return new String[] {getName()};
	}

	public char[][] getQualifiedNameCharArray() throws DOMException {
		return new char[][]{getNameCharArray()};
	}

	public boolean isGloballyQualified() {
		return false;
	}

	public boolean isMutable() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.8
		return false; 
	}

	public IType getType() {
		return fType;
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		byte flag = 1<<PDOMCAnnotation.AUTO_OFFSET;
		return hasFlag(flag, true, ANNOTATIONS);
	}

	public boolean isExtern() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.5
		return false; 
	}

	public boolean isExternC() {
		return false;
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		byte flag = 1<<PDOMCAnnotation.REGISTER_OFFSET;
		return hasFlag(flag, true, ANNOTATIONS);
	}

	public boolean isStatic() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.4
		return false; 
	}

	public String getName() {
		return new String(getNameCharArray());
	}

	public IIndexScope getScope() {
		return null;
	}

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public char[] getNameCharArray() {
		try {
			return super.getNameCharArray();
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new char[0];
		}
	}

	public boolean hasDefaultValue() {
		return hasFlag(FLAG_DEFAULT_VALUE, false, FLAGS);
	}

	public boolean isParameterPack() {
		return getType() instanceof ICPPParameterPackType;
	}

	private boolean hasFlag(byte flag, boolean defValue, int offset) {
		try {
			byte myflags= getDB().getByte(record + offset);
			return (myflags & flag) == flag;
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return defValue;
	}
	
	public IIndexFragment getFragment() {
		return getPDOM();
	}	
	
	public boolean hasDefinition() throws CoreException {
		// parameter bindings do not span index fragments
		return true;
	}

	public boolean hasDeclaration() throws CoreException {
		// parameter bindings do not span index fragments
		return true;
	}

	public int compareTo(Object arg0) {
		throw new PDOMNotImplementedError();
	}
	
	public int getBindingConstant() {
		return getNodeType();
	}

	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		long rec = getNextPtr();
		if (rec != 0) {
			new PDOMCPPParameter(linkage, rec, null).delete(linkage);
		}
		super.delete(linkage);
	}

	public long getNextPtr() throws CoreException {
		long rec = getDB().getRecPtr(record + NEXT_PARAM);
		return rec;
	}

	public boolean isFileLocal() throws CoreException {
		return false;
	}
	
	public IIndexFile getLocalToFile() throws CoreException {
		return null;
	}

	public IValue getInitialValue() {
		return null;
	}
}

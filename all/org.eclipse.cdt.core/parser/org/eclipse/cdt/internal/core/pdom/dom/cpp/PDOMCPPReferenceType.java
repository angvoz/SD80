/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.index.CPPReferenceTypeClone;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexType;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.core.runtime.CoreException;

class PDOMCPPReferenceType extends PDOMNode implements ICPPReferenceType, ITypeContainer, IIndexType {

	private static final int TYPE = PDOMNode.RECORD_SIZE + 0;
	
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMNode.RECORD_SIZE + 4;

	private IType targetType;
	
	public PDOMCPPReferenceType(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMCPPReferenceType(PDOMLinkage linkage, PDOMNode parent, ICPPReferenceType type) throws CoreException {
		super(linkage, parent);
		
		Database db = getDB();
		
		try {
			// type
			long typeRec = 0;
			if (type != null) {
				IType targetType = type.getType();
				PDOMNode targetTypeNode = getLinkage().addType(this, targetType);
				if (targetTypeNode != null)
					typeRec = targetTypeNode.getRecord();
			}
			db.putRecPtr(record + TYPE, typeRec);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_REFERENCE_TYPE;
	}

	public IType getType() {
		if (targetType == null)
			targetType= readType();
		
		return targetType;
	}

	private IType readType() {
		try {
			PDOMNode node = getLinkage().getNode(getDB().getRecPtr(record + TYPE));
			return node instanceof IType ? (IType)node : null;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}

	public boolean isSameType(IType type) {
		if (type instanceof ITypedef)
		    return type.isSameType(this);
		
		if (!(type instanceof ICPPReferenceType)) 
		    return false;
		
		ICPPReferenceType rhs = (ICPPReferenceType) type;
		try {
			IType type1= getType();
			if (type1 != null) {
				return type1.isSameType(rhs.getType());
			}
		} catch (DOMException e) {
		}
		return false;
	}

	public void setType(IType type) {
		throw new PDOMNotImplementedError();
	}

	@Override
	public Object clone() {
		return new CPPReferenceTypeClone(this);
	}
	
	@Override
	public void delete(PDOMLinkage linkage) throws CoreException {
		linkage.deleteType(getType(), record);
		super.delete(linkage);
	}

	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}

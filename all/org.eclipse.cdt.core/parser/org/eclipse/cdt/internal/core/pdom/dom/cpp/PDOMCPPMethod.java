/*******************************************************************************
 * Copyright (c) 2006, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    IBM Corporation
 *    Andrew Ferguson (Symbian)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 * 
 */
class PDOMCPPMethod extends PDOMCPPFunction implements ICPPMethod {

	/**
	 * Offset of remaining annotation information (relative to the beginning of
	 * the record).
	 */
	private static final int ANNOTATION1 = PDOMCPPFunction.RECORD_SIZE; // byte

	/**
	 * The size in bytes of a PDOMCPPMethod record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPFunction.RECORD_SIZE + 1;

	/**
	 * The bit offset of CV qualifier flags within ANNOTATION1.
	 */
	private static final int CV_OFFSET = PDOMCPPAnnotation.MAX_EXTRA_OFFSET + 1;

	private byte annotation1= -1;

	public PDOMCPPMethod(PDOMLinkage linkage, PDOMNode parent, ICPPMethod method) throws CoreException, DOMException {
		super(linkage, parent, method, true);

		Database db = getDB();

		try {
			annotation1= PDOMCPPAnnotation.encodeExtraAnnotation(method);
			db.putByte(record + ANNOTATION1, annotation1);
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
	}

	public PDOMCPPMethod(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof ICPPMethod) {
			ICPPMethod method= (ICPPMethod) newBinding;
			super.update(linkage, newBinding);
			annotation1= -1;
			try {
				final byte annot = PDOMCPPAnnotation.encodeExtraAnnotation(method);
				getDB().putByte(record + ANNOTATION1, annot);
				annotation1= annot;
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
		}
	}

	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPPMETHOD;
	}

	public boolean isVirtual() throws DOMException {
		return getBit(getAnnotation1(), PDOMCPPAnnotation.VIRTUAL_OFFSET);
	}

	protected byte getAnnotation1() {
		if (annotation1 == -1)
			annotation1= getByte(record + ANNOTATION1);
		return annotation1;
	}

	public boolean isPureVirtual() throws DOMException {
		return getBit(getAnnotation1(), PDOMCPPAnnotation.PURE_VIRTUAL_OFFSET);
	}

	public boolean isDestructor() {
		return getBit(getAnnotation1(), PDOMCPPAnnotation.DESTRUCTOR_OFFSET);
	}

	@Override
	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public boolean isImplicit() {
		return getBit(getAnnotation1(), PDOMCPPAnnotation.IMPLICIT_METHOD_OFFSET);
	}
	
	@Override
	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	@Override
	public boolean isExtern() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public boolean isExternC() {
		return false;
	}

	@Override
	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	@Override
	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 9.2.6
		return false;
	}

	public int getVisibility() throws DOMException {
		return PDOMCPPAnnotation.getVisibility(getAnnotation());
	}

	public ICPPClassType getClassOwner() throws DOMException {
		return (ICPPClassType) getOwner();
	}

	@Override
	public Object clone() {
		throw new PDOMNotImplementedError();
	}

	public boolean isConst() {
		return getBit(getAnnotation1(), PDOMCAnnotation.CONST_OFFSET + CV_OFFSET);
	}

	public boolean isVolatile() {
		return getBit(getAnnotation1(), PDOMCAnnotation.VOLATILE_OFFSET + CV_OFFSET);
	}

	@Override
	public int getAdditionalNameFlags(int standardFlags, IASTName name) {
		if ((standardFlags & PDOMName.IS_REFERENCE) == PDOMName.IS_REFERENCE) {
			IASTNode parent= name.getParent();
			if (parent instanceof ICPPASTFieldReference) {
				// the name is not qualified
				ICPPASTFieldReference fr= (ICPPASTFieldReference) parent;
				parent= parent.getParent();
				if (parent instanceof IASTFunctionCallExpression) {
					// v->member()
					if (fr.isPointerDereference()) {
						return PDOMName.COULD_BE_POLYMORPHIC_METHOD_CALL;
					}
					// v.member()
					IType type= fr.getFieldOwner().getExpressionType();
					if (type instanceof ICPPReferenceType) {
						return PDOMName.COULD_BE_POLYMORPHIC_METHOD_CALL;
					}
				}
			}
			// calling a member from within a member
			else if (parent instanceof IASTIdExpression) {
				if (parent.getParent() instanceof IASTFunctionCallExpression) {
					return PDOMName.COULD_BE_POLYMORPHIC_METHOD_CALL;
				}
			}
		}
		return 0;
	}
	
	@Override
	public IType[] getExceptionSpecification() throws DOMException {
		if (isImplicit()) {
			return ClassTypeHelper.getInheritedExceptionSpecification(this);
		}
		return super.getExceptionSpecification();
	}
}

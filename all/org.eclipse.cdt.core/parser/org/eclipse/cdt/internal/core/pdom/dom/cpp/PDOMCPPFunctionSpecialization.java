/*******************************************************************************
 * Copyright (c) 2007, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNotImplementedError;
import org.eclipse.cdt.internal.core.pdom.dom.c.PDOMCAnnotation;
import org.eclipse.core.runtime.CoreException;

/**
 * Binding for function specialization in the index. 
 */
class PDOMCPPFunctionSpecialization extends PDOMCPPSpecialization implements ICPPFunction {
	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	private static final int NUM_PARAMS = PDOMCPPSpecialization.RECORD_SIZE + 0;

	/**
	 * Offset of pointer to the first parameter of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FIRST_PARAM = PDOMCPPSpecialization.RECORD_SIZE + 4;

	/**
	 * Offset for type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FUNCTION_TYPE = PDOMCPPSpecialization.RECORD_SIZE + 8;	

	/**
	 * Offset of start of exception specification
	 */
	protected static final int EXCEPTION_SPEC = PDOMCPPSpecialization.RECORD_SIZE + 12; // int

	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	protected static final int ANNOTATION = PDOMCPPSpecialization.RECORD_SIZE + 16; // byte
	
	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	@SuppressWarnings("hiding")
	protected static final int RECORD_SIZE = PDOMCPPSpecialization.RECORD_SIZE + 17;
	
	public PDOMCPPFunctionSpecialization(PDOMLinkage linkage, PDOMNode parent, ICPPFunction function, PDOMBinding specialized) throws CoreException {
		super(linkage, parent, (ICPPSpecialization) function, specialized);
		
		Database db = getDB();
		try {
			IParameter[] params= function.getParameters();
			IType[] paramTypes= IType.EMPTY_TYPE_ARRAY;
			IFunctionType ft= function.getType();
			if (ft != null) {
				PDOMNode typeNode = getLinkage().addType(this, ft);
				if (typeNode != null) {
					db.putRecPtr(record + FUNCTION_TYPE, typeNode.getRecord());
					paramTypes= ((IFunctionType) typeNode).getParameterTypes();
				}
			}

			ICPPFunction sFunc= (ICPPFunction) ((ICPPSpecialization)function).getSpecializedBinding();
			IParameter[] sParams= sFunc.getParameters();
			IType[] sParamTypes= sFunc.getType().getParameterTypes();
			
			final int length= Math.min(sParams.length, params.length);
			db.putInt(record + NUM_PARAMS, length);
			for (int i=0; i<length; ++i) {
				final PDOMNode stype= linkage.addType(this, i<sParamTypes.length ? sParamTypes[i] : null);
				final long stypeRec= stype == null ? 0 : stype.getRecord();
				PDOMCPPParameter sParam = new PDOMCPPParameter(getLinkage(), this, sParams[i], stypeRec);

				long typeRecord= i<paramTypes.length && paramTypes[i]!=null ? ((PDOMNode)paramTypes[i]).getRecord() : 0;
				final ICPPParameter param = (ICPPParameter) params[i];
				setFirstParameter(new PDOMCPPParameterSpecialization(getLinkage(), this, param, sParam, typeRecord));
			}
			db.putByte(record + ANNOTATION, PDOMCPPAnnotation.encodeAnnotation(function));			
		} catch (DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
		try {
			long typelist= 0;
			if (function instanceof ICPPMethod && ((ICPPMethod) function).isImplicit()) {
				// don't store the exception specification, computed it on demand.
			} else {
				typelist = PDOMCPPTypeList.putTypes(this, function.getExceptionSpecification());
			}
			db.putRecPtr(record + EXCEPTION_SPEC, typelist);
		} catch (DOMException e) {
			// ignore problems in the exception specification
		}

	}

	public PDOMCPPFunctionSpecialization(PDOMLinkage linkage, long bindingRecord) {
		super(linkage, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_FUNCTION_SPECIALIZATION;
	}

	public PDOMCPPParameterSpecialization getFirstParameter() throws CoreException {
		long rec = getDB().getRecPtr(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCPPParameterSpecialization(getLinkage(), rec) : null;
	}

	public void setFirstParameter(PDOMCPPParameterSpecialization param) throws CoreException {
		if (param != null)
			param.setNextParameter(getFirstParameter());
		long rec = param != null ? param.getRecord() :  0;
		getDB().putRecPtr(record + FIRST_PARAM, rec);
	}
	
	public boolean isInline() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.INLINE_OFFSET);
	}

	public boolean isMutable() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IScope getFunctionScope() throws DOMException {
		throw new PDOMNotImplementedError();
	}

	public IParameter[] getParameters() throws DOMException {
		try {
			int n = getDB().getInt(record + NUM_PARAMS);
			IParameter[] params = new IParameter[n];
			PDOMCPPParameterSpecialization param = getFirstParameter();
			while (param != null) {
				params[--n] = param;
				param = param.getNextParameter();
			}
			return params;
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return new IParameter[0];
		}
	}

	public ICPPFunctionType getType() throws DOMException {		
		try {
			long offset= getDB().getRecPtr(record + FUNCTION_TYPE);
			return offset==0 ? null : new PDOMCPPFunctionType(getLinkage(), offset); 
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			return null;
		}
	}

	public boolean isAuto() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public boolean isExternC() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCPPAnnotation.EXTERN_C_OFFSET);
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 14882:2003 7.1.1.2
		return false; 
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean takesVarArgs() throws DOMException {
		return getBit(getByte(record + ANNOTATION), PDOMCAnnotation.VARARGS_OFFSET);
	}

	public boolean isConst() {
		// ISO/IEC 14882:2003 9.3.1.3
		// Only applicable to member functions
		return false; 
	}

	public boolean isVolatile() {
		// ISO/IEC 14882:2003 9.3.1.3
		// Only applicable to member functions
		return false; 
	}

	@Override
	public int pdomCompareTo(PDOMBinding other) {
		int cmp= super.pdomCompareTo(other);
		return cmp==0 ? PDOMCPPFunction.compareSignatures(this, other) : cmp;
	}

	public IType[] getExceptionSpecification() throws DOMException {
		try {
			final long rec = getPDOM().getDB().getRecPtr(record+EXCEPTION_SPEC);
			return PDOMCPPTypeList.getTypes(this, rec);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return null;
		}
	}
}

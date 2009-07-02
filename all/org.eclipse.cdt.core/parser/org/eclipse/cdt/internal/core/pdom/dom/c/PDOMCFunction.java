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

package org.eclipse.cdt.internal.core.pdom.dom.c;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.index.IIndexCBindingConstants;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
class PDOMCFunction extends PDOMBinding implements IFunction {
	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	public static final int NUM_PARAMS = PDOMBinding.RECORD_SIZE + 0;
	
	/**
	 * Offset of total number of function parameters (relative to the
	 * beginning of the record).
	 */
	public static final int FIRST_PARAM = PDOMBinding.RECORD_SIZE + 4;
	
	/**
	 * Offset for the type of this function (relative to
	 * the beginning of the record).
	 */
	private static final int FUNCTION_TYPE = PDOMBinding.RECORD_SIZE + 8;
	
	/**
	 * Offset of annotation information (relative to the beginning of the
	 * record).
	 */
	private static final int ANNOTATIONS = PDOMBinding.RECORD_SIZE + 12; // byte
	
	/**
	 * The size in bytes of a PDOMCPPFunction record in the database.
	 */
	@SuppressWarnings("hiding")
	public static final int RECORD_SIZE = PDOMBinding.RECORD_SIZE + 13;
	
	public PDOMCFunction(PDOMLinkage linkage, long record) {
		super(linkage, record);
	}

	public PDOMCFunction(PDOMLinkage linkage, PDOMNode parent, IFunction function) throws CoreException {
		super(linkage, parent, function.getNameCharArray());
		
		IFunctionType type;
		IParameter[] parameters;
		byte annotations;
		try {
			type = function.getType();
			parameters = function.getParameters();
			annotations = PDOMCAnnotation.encodeAnnotation(function);
		} catch(DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}
		setType(getLinkage(), type);
		setParameters(parameters);
		getDB().putByte(record + ANNOTATIONS, annotations);
	}

	@Override
	public void update(final PDOMLinkage linkage, IBinding newBinding) throws CoreException {
		if (newBinding instanceof IFunction) {
			IFunction func= (IFunction) newBinding;
			IFunctionType newType;
			IParameter[] newParams;
			byte newAnnotation;
			try {
				newType= func.getType();
				newParams = func.getParameters();
				newAnnotation = PDOMCAnnotation.encodeAnnotation(func);
			} catch (DOMException e) {
				throw new CoreException(Util.createStatus(e));
			}
				
			IFunctionType oldType= getType();
			setType(linkage, newType);
			PDOMCParameter oldParams= getFirstParameter();
			setParameters(newParams);
			if (oldType != null) {
				linkage.deleteType(oldType, record);
			}
			if (oldParams != null) {
				oldParams.delete(linkage);
			}
			getDB().putByte(record + ANNOTATIONS, newAnnotation);
		}
	}

	private void setType(PDOMLinkage linkage, IFunctionType ft) throws CoreException {
		long rec= 0;
		if (ft != null) {
			PDOMNode typeNode = linkage.addType(this, ft);
			if (typeNode != null) {
				rec= typeNode.getRecord();
			}
		}
		getDB().putRecPtr(record + FUNCTION_TYPE, rec);
	}

	private void setParameters(IParameter[] params) throws CoreException {
		getDB().putInt(record + NUM_PARAMS, params.length);
		getDB().putRecPtr(record + FIRST_PARAM, 0);
		for (int i = 0; i < params.length; ++i) {
			setFirstParameter(new PDOMCParameter(getLinkage(), this, params[i]));
		}
	}
	
	public PDOMCParameter getFirstParameter() throws CoreException {
		long rec = getDB().getRecPtr(record + FIRST_PARAM);
		return rec != 0 ? new PDOMCParameter(getLinkage(), rec) : null;
	}
	
	public void setFirstParameter(PDOMCParameter param) throws CoreException {
		if (param != null)
			param.setNextParameter(getFirstParameter());
		long rec = param != null ? param.getRecord() :  0;
		getDB().putRecPtr(record + FIRST_PARAM, rec);
	}

	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCBindingConstants.CFUNCTION;
	}

	public IFunctionType getType() {
		/*
		 * CVisitor binding resolution assumes any IBinding which is
		 * also an IType should be converted to a IProblemBinding in a
		 * route through the code that triggers errors here. This means
		 * we can't use the convenient idea of having PDOMCFunction implement
		 * both the IType and IBinding interfaces. 
		 */
		try {
			long offset= getDB().getRecPtr(record + FUNCTION_TYPE);
			return offset==0 ? null : new PDOMCFunctionType(getLinkage(), offset); 
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
			return null;
		}
	}

	public boolean isStatic() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.STATIC_OFFSET);
	}

	public boolean isExtern() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.EXTERN_OFFSET);
	}

	public IParameter[] getParameters() throws DOMException {
		try {
			int n = getDB().getInt(record + NUM_PARAMS);
			IParameter[] params = new IParameter[n];
			PDOMCParameter param = getFirstParameter();
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
	
	public boolean isAuto() throws DOMException {
		// ISO/IEC 9899:TC1 6.9.1.4
		return false;
	}

	public boolean isRegister() throws DOMException {
		// ISO/IEC 9899:TC1 6.9.1.4
		return false;
	}

	public boolean isInline() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.INLINE_OFFSET);
	}

	public boolean takesVarArgs() throws DOMException {
		return getBit(getByte(record + ANNOTATIONS), PDOMCAnnotation.VARARGS_OFFSET);
	}
	
	public IScope getFunctionScope() throws DOMException {
		return null;
	}
}

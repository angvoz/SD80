/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   IBM - Initial API and implementation
 *     Bryan Wilkinson (QNX)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import static org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil.getUltimateType;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDeferredTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * @author aniefer
 */
public class CPPClassInstance extends CPPInstance implements ICPPClassType, ICPPInternalBinding {
	private CPPClassSpecializationScope instanceScope;

	/**
	 * @param decl
	 * @param args
	 * @param arguments
	 */
	public CPPClassInstance(ICPPScope scope, IBinding decl, ObjectMap argMap, IType[] args) {
		super(scope, decl, argMap, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getBases()
	 */
	public ICPPBase[] getBases() throws DOMException {
		ICPPClassType cls = (ICPPClassType) getSpecializedBinding();
		if (cls != null) {
			ICPPBase[] result = null;
			ICPPBase[] bindings = cls.getBases();
			for (int i = 0; i < bindings.length; i++) {
				ICPPBase specBinding = (ICPPBase) ((ICPPInternalBase) bindings[i]).clone();
				IBinding base = bindings[i].getBaseClass();
				if (base instanceof IType) {
					IType specBase = CPPTemplates.instantiateType((IType) base, argumentMap);
					specBase = getUltimateType(specBase, false);
					if (specBase instanceof IBinding) {
						((ICPPInternalBase) specBinding).setBaseClass((IBinding) specBase);
					}
					result = (ICPPBase[]) ArrayUtil.append(ICPPBase.class, result, specBinding);
				}
			}
			return (ICPPBase[]) ArrayUtil.trim(ICPPBase.class, result);
		}
		return ICPPBase.EMPTY_BASE_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getFields()
	 */
	public IField[] getFields() throws DOMException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#findField(java.lang.String)
	 */
	public IField findField(String name) throws DOMException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredFields()
	 */
	public ICPPField[] getDeclaredFields() throws DOMException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getMethods()
	 */
	public ICPPMethod[] getMethods() throws DOMException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getAllDeclaredMethods()
	 */
	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getDeclaredMethods()
	 */
	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		CPPClassSpecializationScope scope = (CPPClassSpecializationScope) getCompositeScope();
		if (scope.isFullyCached())
			return scope.getDeclaredMethods();
		return ICPPMethod.EMPTY_CPPMETHOD_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getConstructors()
	 */
	public ICPPConstructor[] getConstructors() throws DOMException {
		CPPClassSpecializationScope scope = (CPPClassSpecializationScope) getCompositeScope();
		if (scope.isFullyCached())
			return scope.getConstructors();
		return ICPPConstructor.EMPTY_CONSTRUCTOR_ARRAY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType#getFriends()
	 */
	public IBinding[] getFriends() throws DOMException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getKey()
	 */
	public int getKey() throws DOMException {
		return ((ICPPClassType) getSpecializedBinding()).getKey();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.ICompositeType#getCompositeScope()
	 */
	public IScope getCompositeScope() {
		if (instanceScope == null) {
			instanceScope = new CPPClassSpecializationScope(this);
		}
		return instanceScope;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
	 */
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexType)
			return type.isSameType(this);
		if (type instanceof ICPPDeferredTemplateInstance && type instanceof ICPPClassType)
			return type.isSameType(this);  // the CPPDeferredClassInstance has some fuzziness

		if (type instanceof ICPPTemplateInstance) {
			ICPPClassType ct1= (ICPPClassType) getSpecializedBinding();
			ICPPClassType ct2= (ICPPClassType) ((ICPPTemplateInstance) type).getTemplateDefinition();
			if (!ct1.isSameType(ct2))
				return false;

			ObjectMap m1 = getArgumentMap();
			ObjectMap m2 = ((ICPPTemplateInstance) type).getArgumentMap();
			if (m1 == null || m2 == null || m1.size() != m2.size())
				return false;
			for (int i = 0; i < m1.size(); i++) {
				IType t1 = (IType) m1.getAt(i);
				IType t2 = (IType) m2.getAt(i);
				if (t1 == null || !t1.isSameType(t2))
					return false;
			}
			return true;
		}

		return false;
	}

	public ICPPClassType[] getNestedClasses() throws DOMException {
		return ICPPClassType.EMPTY_CLASS_ARRAY;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ICPPClassType && isSameType((ICPPClassType) obj);
	}
}

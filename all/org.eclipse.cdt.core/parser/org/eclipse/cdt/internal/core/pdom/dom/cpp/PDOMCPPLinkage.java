/*******************************************************************************
 * Copyright (c) 2005, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.Util;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.cdt.internal.core.pdom.dom.IPDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMCPPLinkage extends PDOMLinkage {
	public PDOMCPPLinkage(PDOM pdom, int record) {
		super(pdom, record);
	}

	public PDOMCPPLinkage(PDOM pdom) throws CoreException {
		super(pdom, CPP_LINKAGE_ID, CPP_LINKAGE_ID.toCharArray());
	}

	public String getID() {
		return CPP_LINKAGE_ID;
	}

	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	public int getNodeType() {
		return LINKAGE;
	}

	// Binding types
	public static final int CPPVARIABLE = PDOMLinkage.LAST_NODE_TYPE + 1;
	public static final int CPPFUNCTION = PDOMLinkage.LAST_NODE_TYPE + 2;
	public static final int CPPCLASSTYPE = PDOMLinkage.LAST_NODE_TYPE + 3;
	public static final int CPPFIELD = PDOMLinkage.LAST_NODE_TYPE + 4;
	public static final int CPPMETHOD = PDOMLinkage.LAST_NODE_TYPE + 5;
	public static final int CPPNAMESPACE = PDOMLinkage.LAST_NODE_TYPE + 6;
	public static final int CPPNAMESPACEALIAS = PDOMLinkage.LAST_NODE_TYPE + 7;
	public static final int CPPBASICTYPE = PDOMLinkage.LAST_NODE_TYPE + 8;
	public static final int CPPPARAMETER = PDOMLinkage.LAST_NODE_TYPE + 9;
	public static final int CPPENUMERATION = PDOMLinkage.LAST_NODE_TYPE + 10;
	public static final int CPPENUMERATOR = PDOMLinkage.LAST_NODE_TYPE + 11;
	public static final int CPPTYPEDEF = PDOMLinkage.LAST_NODE_TYPE + 12;
	public static final int CPP_POINTER_TO_MEMBER_TYPE= PDOMLinkage.LAST_NODE_TYPE + 13;
	public static final int CPP_CONSTRUCTOR= PDOMLinkage.LAST_NODE_TYPE + 14;
	public static final int CPP_REFERENCE_TYPE= PDOMLinkage.LAST_NODE_TYPE + 15;

	public ILanguage getLanguage() {
		return new GPPLanguage();
	}

	public PDOMBinding addBinding(IASTName name) throws CoreException {
		if (name == null || name instanceof ICPPASTQualifiedName)
			return null;

		// Check for null name
		char[] namechars = name.toCharArray();
		if (namechars == null || namechars.length == 0)
			return null;

		IBinding binding = name.resolveBinding();

		if (binding == null || binding instanceof IProblemBinding) {
			// Can't tell what it is
			return null;
		}

		if (binding instanceof IParameter)
			// Skip parameters (TODO and others I'm sure)
			return null;

		PDOMBinding pdomBinding = addBinding(binding);
		if (pdomBinding instanceof PDOMCPPClassType) {
			PDOMCPPClassType pdomClassType= (PDOMCPPClassType) pdomBinding;
			IASTNode baseNode= name.getParent();
			if (baseNode instanceof ICPPASTBaseSpecifier) 
				addBaseClasses(pdomClassType, (ICPPASTBaseSpecifier) baseNode);
			
			if (binding instanceof ICPPClassType && name.isDefinition()) {
				addImplicitMethods(pdomClassType, (ICPPClassType) binding);
			}
		}
		return pdomBinding;
	}

	private PDOMBinding addBinding(IBinding binding) throws CoreException {
		PDOMBinding pdomBinding = adaptBinding(binding);
		try {
			if (pdomBinding == null) {
				PDOMNode parent = getAdaptedParent(binding);
				if (parent == null)
					return null;
				pdomBinding = addBinding(parent, binding);
			}
		} catch(DOMException e) {
			throw new CoreException(Util.createStatus(e));
		}

		return pdomBinding;
	}

	private void addBaseClasses(PDOMCPPClassType pdomBinding, ICPPASTBaseSpecifier baseNode) throws CoreException {
		ICPPASTCompositeTypeSpecifier ownerNode = (ICPPASTCompositeTypeSpecifier)baseNode.getParent();
		IBinding ownerBinding = adaptBinding(ownerNode.getName().resolveBinding());
		if (ownerBinding != null && ownerBinding instanceof PDOMCPPClassType) {
			PDOMCPPClassType ownerClass = (PDOMCPPClassType)ownerBinding;
			PDOMCPPBase pdomBase = new PDOMCPPBase(pdom, pdomBinding,
					baseNode.isVirtual(), baseNode.getVisibility());
			ownerClass.addBase(pdomBase);
		}
	}

	private PDOMBinding addBinding(PDOMNode parent, IBinding binding) throws CoreException, DOMException {
		PDOMBinding pdomBinding= null;
		
		if (binding instanceof ICPPField && parent instanceof PDOMCPPClassType)
			pdomBinding = new PDOMCPPField(pdom, (PDOMCPPClassType)parent, (ICPPField) binding);
		else if (binding instanceof ICPPVariable && !(binding.getScope() instanceof CPPBlockScope)) {
			if (!(binding.getScope() instanceof CPPBlockScope)) {
				ICPPVariable var= (ICPPVariable) binding;
				if (!var.isStatic()) {  // bug 161216
					pdomBinding = new PDOMCPPVariable(pdom, parent, var);
				}
			}
		} else if (binding instanceof ICPPConstructor && parent instanceof PDOMCPPClassType) {
			pdomBinding = new PDOMCPPConstructor(pdom, parent, (ICPPConstructor)binding);
		} else if (binding instanceof ICPPMethod && parent instanceof PDOMCPPClassType) {
			pdomBinding = new PDOMCPPMethod(pdom, parent, (ICPPMethod)binding);
		} else if (binding instanceof ICPPFunction) {
			ICPPFunction func= (ICPPFunction) binding;
//			if (!func.isStatic()) {  // bug 161216
				pdomBinding = new PDOMCPPFunction(pdom, parent, func);
//			}
		} else if (binding instanceof ICPPClassType) {
			pdomBinding= new PDOMCPPClassType(pdom, parent, (ICPPClassType) binding);
		} else if (binding instanceof ICPPNamespaceAlias) {
			pdomBinding = new PDOMCPPNamespaceAlias(pdom, parent, (ICPPNamespaceAlias) binding);
		} else if (binding instanceof ICPPNamespace) {
			pdomBinding = new PDOMCPPNamespace(pdom, parent, (ICPPNamespace) binding);
		} else if (binding instanceof IEnumeration) {
			pdomBinding = new PDOMCPPEnumeration(pdom, parent, (IEnumeration) binding);
		} else if (binding instanceof IEnumerator) {
			IEnumeration enumeration = (IEnumeration)((IEnumerator)binding).getType();
			PDOMBinding pdomEnumeration = adaptBinding(enumeration);
			if (pdomEnumeration instanceof PDOMCPPEnumeration)
				pdomBinding = new PDOMCPPEnumerator(pdom, parent, (IEnumerator) binding,
						(PDOMCPPEnumeration)pdomEnumeration);
		} else if (binding instanceof ITypedef) {
			pdomBinding = new PDOMCPPTypedef(pdom, parent, (ITypedef)binding);
		}

		if(pdomBinding!=null) {
			parent.addChild(pdomBinding);
		}
		return pdomBinding;
	}

	private void addImplicitMethods(PDOMCPPClassType type, ICPPClassType binding) throws CoreException {
		try {
			IScope scope = binding.getCompositeScope();
			if (scope instanceof ICPPClassScope) {
				ICPPMethod[] implicit= ((ICPPClassScope) scope).getImplicitMethods();
				for (int i = 0; i < implicit.length; i++) {
					ICPPMethod method = implicit[i];
					addBinding(type, method);
				}
			}
		} catch (DOMException e) {
			CCorePlugin.log(e);
		}
	}

	public int getBindingType(IBinding binding) {
		if (binding instanceof ICPPTemplateDefinition)
			// this must be before class type
			return 0;
		else if (binding instanceof ICPPField)
			// this must be before variables
			return CPPFIELD;
		else if (binding instanceof ICPPVariable)
			return CPPVARIABLE;
		else if (binding instanceof ICPPConstructor)
			// before methods
			return CPP_CONSTRUCTOR;
		else if (binding instanceof ICPPMethod)
			// this must be before functions
			return CPPMETHOD;
		else if (binding instanceof ICPPFunction)
			return CPPFUNCTION;
		else if (binding instanceof ICPPClassType)
			return CPPCLASSTYPE;
		else if (binding instanceof ICPPNamespaceAlias)
			return CPPNAMESPACEALIAS;
		else if (binding instanceof ICPPNamespace)
			return CPPNAMESPACE;
		else if (binding instanceof IEnumeration)
			return CPPENUMERATION;
		else if (binding instanceof IEnumerator)
			return CPPENUMERATOR;
		else if (binding instanceof ITypedef)
			return CPPTYPEDEF;
		else
			return 0;
	}

	/**
	 * Find the equivalent binding, or binding placeholder within this PDOM
	 */
	public PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding == null || binding instanceof IProblemBinding)
			return null;

		if (binding instanceof PDOMBinding) {
			// there is no guarantee, that the binding is from the same PDOM object.
			PDOMBinding pdomBinding = (PDOMBinding) binding;
			if (pdomBinding.getPDOM() == getPDOM()) {
				return pdomBinding;
			}
			// so if the binding is from another pdom it has to be adapted. 
		}

		PDOMNode parent = getAdaptedParent(binding);

		if (parent == this) {
			return CPPFindBinding.findBinding(getIndex(), this, binding);
		} else if (parent instanceof IPDOMMemberOwner) {
			return CPPFindBinding.findBinding(parent, this, binding);
		} else if (parent instanceof PDOMCPPNamespace) {
			return CPPFindBinding.findBinding(((PDOMCPPNamespace)parent).getIndex(), this, binding);
		}

		return null;
	}

	public PDOMBinding resolveBinding(IASTName name) throws CoreException {
		IBinding binding= name.resolveBinding();
		if (binding != null) {
			return adaptBinding(binding);
		}
		return null;
	}

	/**
	 * Read type information from the AST or null if the types could not be determined
	 * @param paramExp the parameter expression to get types for (null indicates void function/method)
	 * @return an array of types or null if types could not be determined (because of missing semantic information in the AST)
	 */
	public static IType[] getTypes(IASTExpression paramExp) throws DOMException {
		IType[] types = null;

		if(paramExp==null) { // void function/method
			types = new IType[0]; 
		} else if(paramExp instanceof ICPPASTNewExpression) {
			// aftodo - I'm not 100% sure why a new expression doesn't
			// have a pointer type already
			ICPPASTNewExpression exp3 = (ICPPASTNewExpression) paramExp;
			IType type = exp3.getExpressionType();
			types = new IType[] {new CPPPointerType(type)};
		} else if(paramExp instanceof IASTExpressionList) {
			IASTExpressionList list = (IASTExpressionList) paramExp;
			IASTExpression[] paramExps = list.getExpressions();
			types = new IType[paramExps.length];
			for(int i=0; i<paramExps.length; i++) {
				types[i] = paramExps[i].getExpressionType();
			}
		} else {
			types = new IType[] {paramExp.getExpressionType()};
		}

		if(types!=null) { // aftodo - unit test coverage of this is low
			for(int i=0; i<types.length; i++) {
				// aftodo - assumed this always terminates
				while(types[i] instanceof ITypedef) {
					types[i] = ((ITypedef)types[i]).getType();
				}
				if(types[i] instanceof ProblemBinding)
					return null; 
			}
		}

		return types;
	}

	/*
	 * aftodo - I'm not confident I'm going through the correct AST routes here
	 * 
	 * (It does work though)
	 */
	public PDOMBinding resolveFunctionCall(IASTFunctionCallExpression callExp,
			IASTIdExpression id, IASTName name) throws CoreException,
			DOMException {
		IASTExpression paramExp = callExp.getParameterExpression();

		IType[] types = getTypes(paramExp);
		if (types != null) {
			IBinding parentBinding = id.getName().getBinding();

			if (parentBinding instanceof ICPPVariable) {

				ICPPVariable v = (ICPPVariable) parentBinding;
				IType type = v.getType();
				if (type instanceof PDOMBinding) {
					return CPPFindBinding.findBinding(
							((PDOMBinding) type),
							getPDOM(),
							name.toCharArray(),
							CPPMETHOD,
							types
					);
				}
			} else {
				IASTNode expPNode = callExp.getParent();
				if (expPNode instanceof IASTBinaryExpression) {
					IASTBinaryExpression bExp = (IASTBinaryExpression) expPNode;
					switch (bExp.getOperator()) {
					case ICPPASTBinaryExpression.op_pmarrow: /* fall through */
					case ICPPASTBinaryExpression.op_pmdot:
						IASTExpression left = bExp.getOperand1();
						IType t = CPPSemantics.getUltimateType(left.getExpressionType(), false);
						if (t instanceof PDOMCPPClassType) {
							return CPPFindBinding.findBinding(
									((PDOMCPPClassType) t),
									getPDOM(),
									name.toCharArray(),
									CPPMETHOD,
									types
							);
						}
					}
				} else { // filescope
					return CPPFindBinding.findBinding(
							getIndex(),
							getPDOM(),
							name.toCharArray(),
							CPPFUNCTION,
							types
					);
				}
			}
		}

		return null;
	}

	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof IProblemBinding) {
			return null;
		}
		if (type instanceof ICPPBasicType) {
			return new PDOMCPPBasicType(pdom, parent, (ICPPBasicType)type);
		}
		if (type instanceof ICPPClassType) {
			return addBinding((ICPPClassType) type);
		} 
		if (type instanceof IEnumeration) {
			return addBinding((IEnumeration) type);
		} 
		if (type instanceof ITypedef) {
			return addBinding((ITypedef) type);
		}
		if (type instanceof ICPPReferenceType) {
			return new PDOMCPPReferenceType(pdom, parent, (ICPPReferenceType)type);
		}
		if (type instanceof ICPPPointerToMemberType) {
			return new PDOMCPPPointerToMemberType(pdom, parent, (ICPPPointerToMemberType)type);
		}

		return super.addType(parent, type); 
	}

	public PDOMNode getNode(int record) throws CoreException {
		if (record == 0)
			return null;

		switch (PDOMNode.getNodeType(pdom, record)) {
		case CPPVARIABLE:
			return new PDOMCPPVariable(pdom, record);
		case CPPFUNCTION:
			return new PDOMCPPFunction(pdom, record);
		case CPPCLASSTYPE:
			return new PDOMCPPClassType(pdom, record);
		case CPPFIELD:
			return new PDOMCPPField(pdom, record);
		case CPP_CONSTRUCTOR:
			return new PDOMCPPConstructor(pdom, record);
		case CPPMETHOD:
			return new PDOMCPPMethod(pdom, record);
		case CPPNAMESPACE:
			return new PDOMCPPNamespace(pdom, record);
		case CPPNAMESPACEALIAS:
			return new PDOMCPPNamespaceAlias(pdom, record);
		case CPPBASICTYPE:
			return new PDOMCPPBasicType(pdom, record);
		case CPPENUMERATION:
			return new PDOMCPPEnumeration(pdom, record);
		case CPPENUMERATOR:
			return new PDOMCPPEnumerator(pdom, record);
		case CPPTYPEDEF:
			return new PDOMCPPTypedef(pdom, record);
		case CPP_POINTER_TO_MEMBER_TYPE:
			return new PDOMCPPPointerToMemberType(pdom, record);
		case CPP_REFERENCE_TYPE:
			return new PDOMCPPReferenceType(pdom, record);

		default:
			return super.getNode(record);
		}
	}

	public IBTreeComparator getIndexComparator() {
		return new CPPFindBinding.CPPBindingBTreeComparator(pdom);
	}
}

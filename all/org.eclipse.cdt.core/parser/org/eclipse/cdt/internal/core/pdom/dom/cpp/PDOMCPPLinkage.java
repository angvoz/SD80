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

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBlockScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPField;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespace;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNamespaceAlias;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFile;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMLinkage;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMember;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMMemberOwner;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMName;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNamedNode;
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

	public PDOMCPPLinkage(PDOM pdom)
			throws CoreException {
		super(pdom, GPPLanguage.ID, "C++".toCharArray());
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

	public ILanguage getLanguage() {
		return new GPPLanguage();
	}
	
	public PDOMNode getParent(IBinding binding) throws CoreException {
		PDOMNode parent = this;
		IScope scope = binding.getScope();
		if (scope != null) {
			IASTName scopeName = scope.getScopeName();
			if (scopeName != null) {
				IBinding scopeBinding = scopeName.resolveBinding();
				PDOMBinding scopePDOMBinding = adaptBinding(scopeBinding);
				if (scopePDOMBinding != null)
					parent = scopePDOMBinding;
			}
		}
		return parent;
	}
	
	public PDOMBinding addName(IASTName name, PDOMFile file) throws CoreException {
		if (name == null || name instanceof ICPPASTQualifiedName)
			return null;

		// Check for null name
		char[] namechars = name.toCharArray();
		if (namechars == null || namechars.length == 0)
			return null;
		
		IBinding binding = name.resolveBinding();
		if (binding == null || binding instanceof IProblemBinding)
			// Can't tell what it is
			return null;
		
		if (binding instanceof IParameter)
			// Skip parameters (TODO and others I'm sure)
			return null;
		
		PDOMBinding pdomBinding = adaptBinding(binding);
		if (pdomBinding == null) {
			PDOMNode parent = getParent(binding);

			if (binding instanceof CPPField && parent instanceof PDOMCPPClassType)
				pdomBinding = new PDOMCPPField(pdom, (PDOMCPPClassType)parent, name);
			else if (binding instanceof CPPVariable) {
				if (!(binding.getScope() instanceof CPPBlockScope))
					pdomBinding = new PDOMCPPVariable(pdom, parent, name);
			} else if (binding instanceof CPPMethod && parent instanceof PDOMCPPClassType) {
				pdomBinding = new PDOMCPPMethod(pdom, (PDOMCPPClassType)parent, name);
			} else if (binding instanceof CPPImplicitMethod) {
				// skip these for now
			} else if (binding instanceof CPPFunction) {
				pdomBinding = new PDOMCPPFunction(pdom, parent, name);
			} else if (binding instanceof CPPClassType) {
				pdomBinding = new PDOMCPPClassType(pdom, parent, name);
			} else if (binding instanceof CPPNamespaceAlias) {
				pdomBinding = new PDOMCPPNamespaceAlias(pdom, parent, name);
			} else if (binding instanceof CPPNamespace) {
				pdomBinding = new PDOMCPPNamespace(pdom, parent, name);
			}
		}
		
		// Add in the name
		if (pdomBinding != null)
			new PDOMName(pdom, name, file, pdomBinding);
			
		return pdomBinding;
	}
	
	private static final class FindBinding extends PDOMNamedNode.NodeFinder {
		PDOMBinding pdomBinding;
		final int desiredType;
		public FindBinding(PDOM pdom, char[] name, int desiredType) {
			super(pdom, name);
			this.desiredType = desiredType;
		}
		public boolean visit(int record) throws CoreException {
			if (record == 0)
				return true;
			PDOMBinding tBinding = pdom.getBinding(record);
			if (!tBinding.hasName(name))
				// no more bindings with our desired name
				return false;
			if (tBinding.getNodeType() != desiredType)
				// wrong type, try again
				return true;
			
			// got it
			pdomBinding = tBinding;
			return false;
		}
	}

	protected int getBindingType(IBinding binding) {
		if (binding instanceof ICPPVariable)
			return CPPVARIABLE;
		else if (binding instanceof ICPPFunction)
			return CPPFUNCTION;
		else if (binding instanceof ICPPClassType)
			return CPPCLASSTYPE;
		else if (binding instanceof ICPPField)
			return CPPFIELD;
		else if (binding instanceof ICPPMethod)
			return CPPMETHOD;
		else if (binding instanceof ICPPNamespaceAlias)
			return CPPNAMESPACEALIAS;
		else if (binding instanceof ICPPNamespace)
			return CPPNAMESPACE;
		else
			return 0;
	}
	
	public PDOMBinding adaptBinding(IBinding binding) throws CoreException {
		if (binding == null || binding instanceof IProblemBinding)
			return null;
		
		if (binding instanceof PDOMBinding)
			return (PDOMBinding)binding;
		
		PDOMNode parent = getParent(binding);
		if (parent == this) {
			FindBinding visitor = new FindBinding(pdom, binding.getNameCharArray(), getBindingType(binding));
			getIndex().accept(visitor);
			return visitor.pdomBinding;
		} else if (parent instanceof PDOMMemberOwner) {
			PDOMMemberOwner owner = (PDOMMemberOwner)parent;
			PDOMMember[] members = owner.findMembers(binding.getNameCharArray());
			if (members.length > 0)
				return members[0];
		} else if (parent instanceof PDOMCPPNamespace) {
			FindBinding visitor = new FindBinding(pdom, binding.getNameCharArray(), getBindingType(binding));
			((PDOMCPPNamespace)parent).getIndex().accept(visitor);
			return visitor.pdomBinding;
		}
		return null;
	}
	
	public PDOMBinding resolveBinding(IASTName name) throws CoreException {
		if (name instanceof ICPPASTQualifiedName) {
			IASTName lastName = ((ICPPASTQualifiedName)name).getLastName();
			return lastName != null ? resolveBinding(lastName) : null;
		}
		IASTNode parent = name.getParent();
		if (parent instanceof ICPPASTQualifiedName)
			parent = parent.getParent();
		if (parent instanceof IASTIdExpression) {
			// reference
			IASTNode eParent = parent.getParent();
			if (eParent instanceof IASTFunctionCallExpression) {
				FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CPPFUNCTION);
				getIndex().accept(visitor);
				return visitor.pdomBinding;
			} else {
				FindBinding visitor = new FindBinding(pdom, name.toCharArray(), 
						(name.getParent() instanceof ICPPASTQualifiedName
								&& ((ICPPASTQualifiedName)name.getParent()).getLastName() != name)
							? CPPNAMESPACE : CPPVARIABLE);
				getIndex().accept(visitor);
				return visitor.pdomBinding;
			}
		} else if (parent instanceof IASTNamedTypeSpecifier) {
			FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CPPCLASSTYPE);
			getIndex().accept(visitor);
			return visitor.pdomBinding;
		} else if (parent instanceof ICPPASTNamespaceAlias) {
			FindBinding visitor = new FindBinding(pdom, name.toCharArray(), CPPNAMESPACE);
			getIndex().accept(visitor);
			return visitor.pdomBinding;
		}
		
		return null;
	}
	
	public void findBindings(String pattern, List bindings) throws CoreException {
		MatchBinding visitor = new MatchBinding(pdom, pattern, bindings);
		getIndex().accept(visitor);
	}
	
	public PDOMNode addType(PDOMNode parent, IType type) throws CoreException {
		if (type instanceof ICPPBasicType)
			return new PDOMCPPBasicType(pdom, parent, (ICPPBasicType)type);
		else
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
		case CPPMETHOD:
			return new PDOMCPPMethod(pdom, record);
		case CPPNAMESPACE:
			return new PDOMCPPNamespace(pdom, record);
		case CPPNAMESPACEALIAS:
			return new PDOMCPPNamespaceAlias(pdom, record);
		case CPPBASICTYPE:
			return new PDOMCPPBasicType(pdom, record);
		}
		
		return super.getNode(record);
	}
	
}

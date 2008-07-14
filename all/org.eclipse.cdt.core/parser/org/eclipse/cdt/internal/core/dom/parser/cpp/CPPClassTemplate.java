/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Bryan Wilkinson (QNX)
 * Markus Schorn (Wind River Systems)
 * Andrew Ferguson (Symbian)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IIndexType;

/**
 * 
 * @author aniefer
 */
public class CPPClassTemplate extends CPPTemplateDefinition implements
		ICPPClassTemplate, ICPPClassType, ICPPInternalClassTemplate,
		ICPPInternalClassTypeMixinHost {

	private class FindDefinitionAction extends CPPASTVisitor {
		private char[] nameArray = CPPClassTemplate.this.getNameCharArray();
		public IASTName result = null;

		{
			shouldVisitNames          = true;
			shouldVisitDeclarations   = true;
			shouldVisitDeclSpecifiers = true;
			shouldVisitDeclarators    = true;
		}

		@Override
		public int visit(IASTName name) {
			if (name instanceof ICPPASTTemplateId || name instanceof ICPPASTQualifiedName)
				return PROCESS_CONTINUE;
			char[] c = name.toCharArray();
			if (name.getParent() instanceof ICPPASTTemplateId)
				name = (IASTName) name.getParent();
			if (name.getParent() instanceof ICPPASTQualifiedName) {
				IASTName[] ns = ((ICPPASTQualifiedName)name.getParent()).getNames();
				if (ns[ns.length - 1] != name)
					return PROCESS_CONTINUE;
				name = (IASTName) name.getParent();
			}

			if (name.getParent() instanceof ICPPASTCompositeTypeSpecifier &&
					CharArrayUtils.equals(c, nameArray)) {
				IBinding binding = name.resolveBinding();
				if (binding == CPPClassTemplate.this) {
					if (name instanceof ICPPASTQualifiedName) {
						IASTName[] ns = ((ICPPASTQualifiedName)name).getNames();
						name = ns[ns.length - 1];
					}
					result = name;
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE; 
		}

		@Override
		public int visit(IASTDeclaration declaration) { 
			if (declaration instanceof IASTSimpleDeclaration || declaration instanceof ICPPASTTemplateDeclaration)
				return PROCESS_CONTINUE;
			return PROCESS_SKIP; 
		}
		@Override
		public int visit(IASTDeclSpecifier declSpec) {
			return (declSpec instanceof ICPPASTCompositeTypeSpecifier) ? PROCESS_CONTINUE : PROCESS_SKIP; 
		}
		@Override
		public int visit(IASTDeclarator declarator) { return PROCESS_SKIP; }
	}

	private ICPPClassTemplatePartialSpecialization[] partialSpecializations = null;
	private ClassTypeMixin mixin;

	 public CPPClassTemplate(IASTName name) {
		super(name);
		this.mixin= new ClassTypeMixin(this);
	}

	@Override
	public ICPPSpecialization deferredInstance(ObjectMap argMap, IType[] arguments) {
		ICPPSpecialization instance = getInstance(arguments);
		if (instance == null) {
			instance = new CPPDeferredClassInstance(this, argMap, arguments);
			addSpecialization(arguments, instance);
		}
		return instance;
	}

	public void checkForDefinition() {
		FindDefinitionAction action = new FindDefinitionAction();
		IASTNode node = CPPVisitor.getContainingBlockItem(declarations[0]).getParent();
		while (node instanceof ICPPASTTemplateDeclaration)
			node = node.getParent();
		node.accept(action);
		definition = action.result;

		if (definition == null) {
			node.getTranslationUnit().accept(action);
			definition = action.result;
		}

		return;
	}
	
	public void addPartialSpecialization(ICPPClassTemplatePartialSpecialization spec) {
		partialSpecializations = (ICPPClassTemplatePartialSpecialization[]) ArrayUtil.append(
				ICPPClassTemplatePartialSpecialization.class, partialSpecializations, spec);
	}

	public ICPPASTCompositeTypeSpecifier getCompositeTypeSpecifier() {
		if (definition != null) {
			IASTNode node = definition.getParent();
			if (node instanceof ICPPASTQualifiedName)
				node = node.getParent();
			if (node instanceof ICPPASTCompositeTypeSpecifier)
				return (ICPPASTCompositeTypeSpecifier) node;
		}
		return null;
	}

	public IScope getCompositeScope() {
		if (definition == null) {
			checkForDefinition();
		}
		if (definition != null) {
			IASTNode parent = definition.getParent();
			while (parent instanceof IASTName)
				parent = parent.getParent();
			if (parent instanceof ICPPASTCompositeTypeSpecifier) {
				ICPPASTCompositeTypeSpecifier compSpec = (ICPPASTCompositeTypeSpecifier)parent;
				return compSpec.getScope();
			}
		}
		return null;
	}

	public int getKey() {
		if (definition != null) {
			ICPPASTCompositeTypeSpecifier cts= getCompositeTypeSpecifier();
			if (cts != null) {
				return cts.getKey();
			}
			IASTNode n= definition.getParent();
			if (n instanceof ICPPASTElaboratedTypeSpecifier) {
				return ((ICPPASTElaboratedTypeSpecifier)n).getKind();
			}
		}

		if (declarations != null && declarations.length > 0) {
			IASTNode n = declarations[0].getParent();
			if (n instanceof ICPPASTElaboratedTypeSpecifier) {
				return ((ICPPASTElaboratedTypeSpecifier)n).getKind();
			}
		}

		return ICPPASTElaboratedTypeSpecifier.k_class;
	}
	
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() {
		partialSpecializations = (ICPPClassTemplatePartialSpecialization[]) ArrayUtil.trim(ICPPClassTemplatePartialSpecialization.class, partialSpecializations);
		return partialSpecializations;
	}
	
	public boolean isSameType(IType type) {
		if (type == this)
			return true;
		if (type instanceof ITypedef || type instanceof IIndexType)
			return type.isSameType(this);
		return false;
	}
	
	public ICPPBase[] getBases() {
		return mixin.getBases();
	}

	public IField[] getFields() throws DOMException {
		return mixin.getFields();
	}

	public ICPPField[] getDeclaredFields() throws DOMException {
		return mixin.getDeclaredFields();
	}

	public ICPPMethod[] getMethods() throws DOMException {
		return CPPClassType.getMethods(this);
	}

	public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
		return mixin.getAllDeclaredMethods();
	}

	public ICPPMethod[] getDeclaredMethods() throws DOMException {
		return mixin.getDeclaredMethods();
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		return mixin.getConstructors();
	}

	public IBinding[] getFriends() {
		return mixin.getFriends();
	}
	
	public ICPPClassType[] getNestedClasses() {
		return mixin.getNestedClasses();
	}

	public IField findField(String name) throws DOMException {
		return mixin.findField(name);
	}
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
		}
		return null;
	}

	/* (non-Javadoc)
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		return ASTTypeUtil.getType(this);
	}
}

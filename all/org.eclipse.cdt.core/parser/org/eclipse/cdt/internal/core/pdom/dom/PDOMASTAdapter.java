/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.pdom.dom;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.core.runtime.CoreException;

public class PDOMASTAdapter {
	private static class AnonymousASTName implements IASTName {
		private IASTName fDelegate;
		private IASTFileLocation fLocation;

		public AnonymousASTName(IASTName name, final IASTFileLocation loc) {
			fDelegate= name;
			fLocation= new IASTFileLocation() {
				public int getEndingLineNumber() {
					return loc.getStartingLineNumber();
				}
				public String getFileName() {
					return loc.getFileName();
				}

				public int getStartingLineNumber() {
					return loc.getStartingLineNumber();
				}

				public IASTFileLocation asFileLocation() {
					return loc.asFileLocation();
				}

				public int getNodeLength() {
					return 0;
				}

				public int getNodeOffset() {
					return loc.getNodeOffset();
				}
				
			};
		}

		public boolean accept(ASTVisitor visitor) {
			return fDelegate.accept(visitor);
		}

		public boolean contains(IASTNode node) {
			return fDelegate.contains(node);
		}

		public IBinding getBinding() {
			return fDelegate.getBinding();
		}

		public IBinding getPreBinding() {
			return fDelegate.getPreBinding();
		}

		public String getContainingFilename() {
			return fLocation.getFileName();
		}

		public IASTFileLocation getFileLocation() {
			return fLocation;
		}

		public ILinkage getLinkage() {
			return fDelegate.getLinkage();
		}

		public IASTNodeLocation[] getNodeLocations() {
			return fDelegate.getNodeLocations();
		}

		public IASTNode getParent() {
			return fDelegate.getParent();
		}

		public IASTNode[] getChildren() {
			return fDelegate.getChildren();
		}

		public ASTNodeProperty getPropertyInParent() {
			return fDelegate.getPropertyInParent();
		}

		public String getRawSignature() {
			return fDelegate.getRawSignature();
		}

		public IASTTranslationUnit getTranslationUnit() {
			return fDelegate.getTranslationUnit();
		}

		public int getRoleOfName(boolean allowResolution) {
			return fDelegate.getRoleOfName(allowResolution);
		}

		public boolean isDeclaration() {
			return fDelegate.isDeclaration();
		}

		public boolean isDefinition() {
			return fDelegate.isDefinition();
		}

		public boolean isReference() {
			return fDelegate.isReference();
		}

		public IBinding resolveBinding() {
			return fDelegate.resolveBinding();
		}

		public IBinding resolvePreBinding() {
			return fDelegate.resolvePreBinding();
		}

		public IASTCompletionContext getCompletionContext() {
			return fDelegate.getCompletionContext();
		}

		public void setBinding(IBinding binding) {
			fDelegate.setBinding(binding);
		}

		public void setParent(IASTNode node) {
			fDelegate.setParent(node);
		}

		public void setPropertyInParent(ASTNodeProperty property) {
			fDelegate.setPropertyInParent(property);
		}

		public char[] toCharArray() {
			return fDelegate.toCharArray();
		}

		public char[] getSimpleID() {
			return fDelegate.getSimpleID();
		}
		
		public char[] getLookupKey() {
			return fDelegate.getLookupKey();
		}

		public IASTImageLocation getImageLocation() {
			return null;
		}

		public boolean isPartOfTranslationUnitFile() {
			return fLocation.getFileName().equals(fDelegate.getTranslationUnit().getFilePath());
		}
		
		@Override
		public String toString() {
			return fDelegate.toString();
		}

		public IASTName getLastName() {
			return this;
		}

		public IToken getSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getSyntax();
		}

		public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getLeadingSyntax();
		}

		public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException,
				UnsupportedOperationException {
			return fDelegate.getTrailingSyntax();
		}
		
		public boolean isFrozen() {
			return fDelegate.isFrozen();
		}
			
		public boolean isActive() {
			return fDelegate.isFrozen();
		}

		public IASTName copy() {
			throw new UnsupportedOperationException();
		}
	}

	private static class AnonymousEnumeration implements IEnumeration {
		private IEnumeration fDelegate;
		private char[] fName;

		public AnonymousEnumeration(char[] name, IEnumeration delegate) {
			fName= name;
			fDelegate= delegate;
		}

		@Override
		public Object clone() {
			throw new PDOMNotImplementedError();
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public IEnumerator[] getEnumerators() throws DOMException {
			return fDelegate.getEnumerators();
		}

		public ILinkage getLinkage() throws CoreException {
			return fDelegate.getLinkage();
		}

		public String getName() {
			return new String(fName);
		}

		public char[] getNameCharArray() {
			return fName;
		}

		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		public boolean isSameType(IType type) {
			return fDelegate.isSameType(type);
		}

		public IBinding getOwner() throws DOMException {
			return fDelegate.getOwner();
		}
	}

	private static class AnonymousCompositeType implements ICompositeType {
		protected ICompositeType fDelegate;
		private char[] fName;

		public AnonymousCompositeType(char[] name, ICompositeType delegate) {
			fName= name;
			fDelegate= delegate;
		}

		@Override
		public Object clone() {
			throw new PDOMNotImplementedError();
		}

		public IField findField(String name) throws DOMException {
			return fDelegate.findField(name);
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public IScope getCompositeScope() throws DOMException {
			return fDelegate.getCompositeScope();
		}

		public IField[] getFields() throws DOMException {
			return fDelegate.getFields();
		}

		public int getKey() throws DOMException {
			return fDelegate.getKey();
		}

		public ILinkage getLinkage() throws CoreException {
			return fDelegate.getLinkage();
		}

		public String getName() {
			return new String(fName);
		}

		public char[] getNameCharArray() {
			return fName;
		}

		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		public boolean isSameType(IType type) {
			return fDelegate.isSameType(type);
		}
		
		public IBinding getOwner() throws DOMException {
			return fDelegate.getOwner();
		}

		public boolean isAnonymous() throws DOMException {
			return fDelegate.isAnonymous();
		}
	}

	private static class AnonymousCPPBinding implements ICPPBinding {
		protected ICPPBinding fDelegate;
		private char[] fName;

		public AnonymousCPPBinding(char[] name, ICPPBinding delegate) {
			fName= name;
			fDelegate= delegate;
		}
		
		@Override
		public Object clone() {
			throw new PDOMNotImplementedError();
		}

		public String getName() {
			return new String(fName);
		}

		public char[] getNameCharArray() {
			return fName;
		}

		public String[] getQualifiedName() throws DOMException {
			String[] qn= fDelegate.getQualifiedName();
			if (qn.length < 1) {
				qn= new String[]{null};
			}
			qn[qn.length-1]= new String(fName);
			return qn;
		}

		public char[][] getQualifiedNameCharArray() throws DOMException {
			char[][] qn= fDelegate.getQualifiedNameCharArray();
			if (qn.length < 1) {
				qn= new char[][]{null};
			}
			qn[qn.length-1]= fName;
			return qn;
		}

		@SuppressWarnings("rawtypes")
		public Object getAdapter(Class adapter) {
			return fDelegate.getAdapter(adapter);
		}

		public ILinkage getLinkage() throws CoreException {
			return fDelegate.getLinkage();
		}

		public IScope getScope() throws DOMException {
			return fDelegate.getScope();
		}

		public boolean isGloballyQualified() throws DOMException {
			return fDelegate.isGloballyQualified();
		}

		public IBinding getOwner() throws DOMException {
			return fDelegate.getOwner();
		}
	}

	private static class AnonymousCPPEnumeration extends AnonymousCPPBinding implements IEnumeration {
		public AnonymousCPPEnumeration(char[] name, IEnumeration delegate) {
			super(name, (ICPPBinding) delegate);
		}

		public IEnumerator[] getEnumerators() throws DOMException {
			return ((IEnumeration) fDelegate).getEnumerators();
		}

		public boolean isSameType(IType type) {
			return ((IEnumeration) fDelegate).isSameType(type);
		}
	}

	private static class AnonymousClassType extends AnonymousCPPBinding implements ICPPClassType {
		public AnonymousClassType(char[] name, ICPPClassType delegate) {
			super(name, delegate);
		}
		
		public IField findField(String name) throws DOMException {
			return ((ICPPClassType) fDelegate).findField(name);
		}

		public ICPPMethod[] getAllDeclaredMethods() throws DOMException {
			return ((ICPPClassType) fDelegate).getAllDeclaredMethods();
		}

		public ICPPBase[] getBases() throws DOMException {
			return ((ICPPClassType) fDelegate).getBases();
		}

		public IScope getCompositeScope() throws DOMException {
			return ((ICPPClassType) fDelegate).getCompositeScope();
		}

		public ICPPConstructor[] getConstructors() throws DOMException {
			return ((ICPPClassType) fDelegate).getConstructors();
		}

		public ICPPField[] getDeclaredFields() throws DOMException {
			return ((ICPPClassType) fDelegate).getDeclaredFields();
		}

		public ICPPMethod[] getDeclaredMethods() throws DOMException {
			return ((ICPPClassType) fDelegate).getDeclaredMethods();
		}

		public IField[] getFields() throws DOMException {
			return ((ICPPClassType) fDelegate).getFields();
		}

		public IBinding[] getFriends() throws DOMException {
			return ((ICPPClassType) fDelegate).getFriends();
		}

		public int getKey() throws DOMException {
			return ((ICPPClassType) fDelegate).getKey();
		}

		public ICPPMethod[] getMethods() throws DOMException {
			return ((ICPPClassType) fDelegate).getMethods();
		}

		public ICPPClassType[] getNestedClasses() throws DOMException {
			return ((ICPPClassType) fDelegate).getNestedClasses();
		}

		public boolean isSameType(IType type) {
			return ((ICPPClassType) fDelegate).isSameType(type);
		}

		public boolean isAnonymous() throws DOMException {
			return ((ICPPClassType) fDelegate).isAnonymous();
		}
	}


	/**
	 * If the provided binding is anonymous, either an adapter is returned 
	 * that computes a name for the binding, or <code>null</code> if that
	 * is not appropriate (e.g. binding is not a type).
	 * Otherwise, if the binding has a name it is returned unchanged.
	 */
	public static IBinding getAdapterForAnonymousASTBinding(IBinding binding) {
		if (binding != null && !(binding instanceof IIndexBinding)) {
			char[] name = binding.getNameCharArray();
			if (name.length == 0) {
				if (binding instanceof IEnumeration) {
					name = ASTTypeUtil.createNameForAnonymous(binding);
					if (name != null) {
						if (binding instanceof ICPPBinding) {
							return new AnonymousCPPEnumeration(name, (IEnumeration) binding);
						}
						return new AnonymousEnumeration(name, (IEnumeration) binding);
					}
				} else if (binding instanceof ICPPClassType) {
					name = ASTTypeUtil.createNameForAnonymous(binding);
					if (name != null) {
						return new AnonymousClassType(name, (ICPPClassType) binding);
					}
				} else if (binding instanceof ICompositeType) {
					name = ASTTypeUtil.createNameForAnonymous(binding);
					if (name != null) {
						return new AnonymousCompositeType(name, (ICompositeType) binding);
					}
				} else if (binding instanceof ICPPTemplateParameter) {
					return binding;
				}
				return null;
			}
		}
		return binding;
	}

	/**
	 * If the name is empty and has no file location, either an adapter 
	 * that has a file location is returned, or <code>null</code> if that 
	 * is not possible (no parent with a file location).
	 * Otherwise if the provided name is not empty or has a file location, 
	 * it is returned unchanged.
	 */
	public static IASTName getAdapterIfAnonymous(IASTName name) {
		if (name.getLookupKey().length == 0) {
			if (name.getFileLocation() == null) {
				IASTNode parent= name.getParent();
				if (parent != null) {
					IASTFileLocation loc= parent.getFileLocation();
					if (loc != null) {
						return new AnonymousASTName(name, loc);
					}
				}
				return null;
			}
		}
		return name;
	}
}

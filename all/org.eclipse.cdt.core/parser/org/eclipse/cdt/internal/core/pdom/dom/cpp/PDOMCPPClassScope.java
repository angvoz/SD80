/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Doug Schaefer (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Andrew Ferguson (Symbian)
 *    Bryan Wilkinson (QNX)
 *    Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.core.parser.util.CharArrayMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassScope;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.cdt.internal.core.index.DeclaredBindingsFilter;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.BindingCollector;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * Represents the class scope for a class stored in the index.
 */
class PDOMCPPClassScope implements ICPPClassScope, IIndexScope {
	private static final IndexFilter CONVERSION_FILTER = new DeclaredBindingsFilter(ILinkage.CPP_LINKAGE_ID, true, false) {
		@Override
		public boolean acceptBinding(IBinding binding) throws CoreException {
			return binding instanceof ICPPMethod && 
				SemanticUtil.isConversionOperator(((ICPPMethod) binding)) &&
				super.acceptBinding(binding);
		}
	};
	
	private IPDOMCPPClassType fBinding;

	public PDOMCPPClassScope(IPDOMCPPClassType binding) {
		fBinding= binding;
	}
	
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	public ICPPClassType getClassType() {
		return fBinding;
	}

	public IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		return getBinding(name, resolve, null);
	}

	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup)	throws DOMException {
		return getBindings(name, resolve, prefixLookup, null);
	}

	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) throws DOMException {
		try {
		    final char[] nameChars = name.getSimpleID();
			if (CharArrayUtils.equals(fBinding.getNameCharArray(), nameChars)) {
		        if (CPPClassScope.isConstructorReference(name)){
		            return CPPSemantics.resolveAmbiguities(name, fBinding.getConstructors());
		        }
	            //9.2 ... The class-name is also inserted into the scope of the class itself
		        return getClassNameBinding();
		    }
			
			final IBinding[] candidates = getBindingsViaCache(fBinding, nameChars, IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE);
			return CPPSemantics.resolveAmbiguities(name, candidates);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	private IBinding getClassNameBinding() throws DOMException {
		if (fBinding instanceof ICPPClassTemplatePartialSpecialization)
			return ((ICPPClassTemplatePartialSpecialization) fBinding).getPrimaryClassTemplate();
		if (fBinding instanceof ICPPSpecialization)
			return ((ICPPSpecialization) fBinding).getSpecializedBinding();
		return fBinding;
	}
	
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) throws DOMException {
		try {
			if (name instanceof ICPPASTConversionName) {
				BindingCollector visitor = new BindingCollector(fBinding.getLinkage(), Keywords.cOPERATOR, CONVERSION_FILTER, true, true);
				acceptViaCache(fBinding, visitor, true);
				return visitor.getBindings();
			} 

			final char[] nameChars = name.getSimpleID();
			if (!prefixLookup) {
				if (CharArrayUtils.equals(fBinding.getNameCharArray(), nameChars)) {
			        if (CPPClassScope.isConstructorReference(name)){
			            return fBinding.getConstructors();
			        }
			        return new IBinding[] {getClassNameBinding()};
				}
			    return getBindingsViaCache(fBinding, nameChars, IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE);
			}
			
			// prefix lookup
			BindingCollector visitor = new BindingCollector(fBinding.getLinkage(), nameChars, IndexFilter.CPP_DECLARED_OR_IMPLICIT_NO_INSTANCE, prefixLookup, !prefixLookup);
			if (CharArrayUtils.equals(fBinding.getNameCharArray(), 0, nameChars.length, nameChars, true)) {
				// add the class itself, constructors will be found during the visit
		        visitor.visit((IPDOMNode) getClassNameBinding());
			}
			acceptViaCache(fBinding, visitor, true);
			return visitor.getBindings();
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
		return null;
	}

	public static IBinding[] getBindingsViaCache(IPDOMCPPClassType ct, final char[] name, IndexFilter filter) throws CoreException {
		CharArrayMap<List<PDOMBinding>> map = getBindingMap(ct);		
		List<PDOMBinding> cached= map.get(name);
		if (cached == null)
			return IBinding.EMPTY_BINDING_ARRAY;
		
		int i= 0;
		IBinding[] result= new IBinding[cached.size()];
		for (IBinding binding : cached) {
			if (filter.acceptBinding(binding)) {
				result[i++]= binding;
			}
		}
		if (i == result.length)
			return result;
		
		final IBinding[] bresult= new IBinding[i];
		System.arraycopy(result, 0, bresult, 0, i);
		return bresult;
	}

	/**
	 * Visit bindings via the cache.
	 */
	public static void acceptViaCache(IPDOMCPPClassType ct, IPDOMVisitor visitor, boolean includeNestedInAnonymous) throws CoreException {
		final long record= ct.getRecord();
		CharArrayMap<List<PDOMBinding>> map= getBindingMap(ct);
		for (List<PDOMBinding> list : map.values()) {
			for (PDOMBinding node : list) {
				if (includeNestedInAnonymous || node.getParentNodeRec() == record) {
					if (visitor.visit(node)) {
						node.accept(visitor);
					}
					visitor.leave(node);
				}
			}
		}
	}

	public static CharArrayMap<List<PDOMBinding>> getBindingMap(IPDOMCPPClassType ct) throws CoreException {
		final Long key= ct.getRecord() + PDOMCPPLinkage.CACHE_MEMBERS;
		final PDOM pdom = ct.getPDOM();
		@SuppressWarnings("unchecked")
		Reference<CharArrayMap<List<PDOMBinding>>> cached= (Reference<CharArrayMap<List<PDOMBinding>>>) pdom.getCachedResult(key);
		CharArrayMap<List<PDOMBinding>> map= cached == null ? null : cached.get();
		
		if (map == null) {
			// there is no cache, build it:
			final CharArrayMap<List<PDOMBinding>> result= new CharArrayMap<List<PDOMBinding>>();
			IPDOMVisitor visitor= new IPDOMVisitor() {
				public boolean visit(IPDOMNode node) throws CoreException {
					if (node instanceof PDOMBinding) {
						final PDOMBinding binding= (PDOMBinding) node;
						final char[] nchars = binding.getNameCharArray();
						List<PDOMBinding> list= result.get(nchars);
						if (list == null) {
							list= new ArrayList<PDOMBinding>();
							result.put(nchars, list);
						}
						list.add(binding);

						try {
							if (binding instanceof ICompositeType && ((ICompositeType) binding).isAnonymous()) {
								return true; // visit children
							}
						} catch (DOMException e) {
						}
					}
					return false;
				}
				public void leave(IPDOMNode node){}
			};
			
			visitor.visit(ct);
			ct.acceptUncached(visitor);
			map= result;
			pdom.putCachedResult(key, new SoftReference<CharArrayMap<?>>(map));
		}
		return map;
	}

	public IBinding[] find(String name) throws DOMException {
		return CPPSemantics.findBindings( this, name, false );
	}
	
	public IIndexBinding getScopeBinding() {
		return fBinding;
	}

	public ICPPMethod[] getImplicitMethods() {
		try {
			PDOMClassUtil.MethodCollector methods = new PDOMClassUtil.MethodCollector(true, false);
			acceptViaCache(fBinding, methods, false);
			return methods.getMethods();
		} catch (CoreException e) {
			return new ICPPMethod[0];
		}
	}

	public ICPPConstructor[] getConstructors() throws DOMException {
		return fBinding.getConstructors();
	}

	public IIndexScope getParent() {
		return fBinding.getScope();
	}

	public IIndexName getScopeName() {
		return fBinding.getScopeName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PDOMCPPClassScope)
			return fBinding.equals(((PDOMCPPClassScope) obj).fBinding);
		return false;
	}

	@Override
	public int hashCode() {
		return fBinding.hashCode();
	}
}

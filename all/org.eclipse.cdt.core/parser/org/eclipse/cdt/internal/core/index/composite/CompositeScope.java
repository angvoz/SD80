/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPCompositeBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUsingDeclaration;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;

public abstract class CompositeScope implements IIndexScope {
	/**
	 * The factory used for obtaining further composite bindings
	 */
	protected final ICompositesFactory cf;
	/**
	 * The representative binding for this composite binding. Most cases are simple
	 * enough that this becomes a delegate, some need to use it as a search key over fragments,
	 * and some ignore it as a representative binding from each fragment is needed to meet interface
	 * contracts.
	 */
	protected final IIndexFragmentBinding rbinding;

	public CompositeScope(ICompositesFactory cf, IIndexFragmentBinding rbinding) {
		if(cf==null || rbinding==null)
			throw new NullPointerException();
		this.cf = cf;
		this.rbinding = rbinding;
	}
	
	final public IScope getParent() throws DOMException {
		IIndexScope rscope = (IIndexScope) rbinding.getScope();
		if(rscope!=null) {
			return cf.getCompositeScope(rscope);
		}
		return null;
	}

	public IName getScopeName() throws DOMException {
		if(rbinding instanceof IScope)
			return ((IScope) rbinding).getScopeName();
		return null;
	}

	protected final void fail() {
		throw new CompositingNotImplementedError();
	}
	
	
	public IBinding getRawScopeBinding() {
		return rbinding;
	}
	
	/**
	 * For bindings that are not known statically to be index bindings, we must decide how to
	 * process them by run-time type. This method processes a single binding accordingly.
	 * @param binding
	 * @return
	 */
	protected final IBinding processUncertainBinding(IBinding binding) {
		if(binding instanceof IIndexFragmentBinding) {
			return cf.getCompositeBinding((IIndexFragmentBinding)binding);				
		} else if(binding instanceof ProblemBinding) {
			return binding;
		} else if(binding instanceof CPPCompositeBinding /* AST composite */) {
			return new CPPCompositeBinding(
				processUncertainBindings(((CPPCompositeBinding)binding).getBindings())
			);
		} else if(binding instanceof CPPUsingDeclaration) {
			return binding;
		} else if(binding == null) {
			return null;
		} else if(binding instanceof ICPPSpecialization) {
			return binding;
		}
		CCorePlugin.log("CompositeFactory unsure how to process: "+binding.getClass().getName()); //$NON-NLS-1$
		return binding;
	}
	
	/**
	 * A convenience method for processing an array of bindings with {@link CompositeScope#processUncertainBinding(IBinding)}
     * Returns an empty array if the input parameter is null
	 * @param frgBindings
	 * @return a non-null IBinding[] 
	 */
	protected final IBinding[] processUncertainBindings(IBinding[] frgBindings) {
		IBinding[] result= new IBinding[frgBindings==null ? 0 : frgBindings.length];
		for(int i=0; i<result.length; i++) {
			result[i]= processUncertainBinding(frgBindings[i]);
		}
		return result;
	}

	public final IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) throws DOMException {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}
}

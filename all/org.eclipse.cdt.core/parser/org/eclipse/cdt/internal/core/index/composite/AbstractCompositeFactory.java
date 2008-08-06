/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite;

import java.util.Comparator;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.DefaultFragmentBindingComparator;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBindingComparator;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMFragmentBindingComparator;
import org.eclipse.core.runtime.CoreException;

/**
 * Commonality between composite factories
 */
public abstract class AbstractCompositeFactory implements ICompositesFactory {	
	protected IIndex index;
	private Comparator<IIndexFragmentBinding> fragmentComparator;
	
	public AbstractCompositeFactory(IIndex index) {
		this.index= index;
		this.fragmentComparator= new FragmentBindingComparator( 
			new IIndexFragmentBindingComparator[] {
					new PDOMFragmentBindingComparator(), 
					new DefaultFragmentBindingComparator()
			}
		);
	}
	
	/*
	 * @see org.eclipse.cdt.internal.core.index.composite.ICompositesFactory#getCompositeBindings(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.internal.core.index.IIndexFragmentBinding[])
	 */
	public final IIndexBinding[] getCompositeBindings(IIndexFragmentBinding[] bindings) {
		IIndexBinding[] result = new IIndexBinding[bindings.length];
		for(int i=0; i<result.length; i++)
			result[i] = getCompositeBinding(bindings[i]);
		return result;
	}

	/* 
	 * @see org.eclipse.cdt.internal.core.index.composite.cpp.ICompositesFactory#getComposites(org.eclipse.cdt.core.index.IIndex, org.eclipse.cdt.internal.core.index.IIndexFragmentBinding[][])
	 */
	public final IIndexBinding[] getCompositeBindings(IIndexFragmentBinding[][] fragmentBindings) {
		return getCompositeBindings(mergeBindingArrays(fragmentBindings));
	}
	
	public final IIndexFragmentBinding[] findEquivalentBindings(IBinding binding) {
		CIndex cindex= (CIndex) index;
		try {
			return cindex.findEquivalentBindings(binding);
		} catch (CoreException e) {
			CCorePlugin.log(e);
			return IIndexFragmentBinding.EMPTY_INDEX_BINDING_ARRAY;
		}
	}
 
	/**
	 * Convenience method for taking a group of binding arrays, and returning a single array
	 * with the each binding appearing once
	 * @param fragmentBindings
	 * @return an array of unique bindings
	 */
	protected IIndexFragmentBinding[] mergeBindingArrays(IIndexFragmentBinding[][] fragmentBindings) {
		TreeSet<IIndexFragmentBinding> ts = new TreeSet<IIndexFragmentBinding>(fragmentComparator);
		for(int i=0; i<fragmentBindings.length; i++)
			for(int j=0; j<fragmentBindings[i].length; j++)
				ts.add(fragmentBindings[i][j]);
		return ts.toArray(new IIndexFragmentBinding[ts.size()]);
	}
	
	/**
	 * Convenience method for finding a binding with a definition (in the specified index
	 * context) which is equivalent to the specified binding. If no definition is found,
     * a declaration is returned if <code>allowDeclaration</code> is set, otherwise an
     * arbitrary binding is returned if available.
	 * @param binding the binding to find a representative for
	 * @param allowDeclaration whether declarations should be considered when a definition is
	 * unavailable
	 * @return the representative binding as defined above
	 */
	protected IIndexFragmentBinding findOneBinding(IBinding binding, boolean allowDeclaration) {
		try{
			IIndexFragmentBinding[] ibs= findEquivalentBindings(binding);
			IBinding def= null;
			IBinding dec= ibs.length>0 ? ibs[0] : null;
			for(int i=0; i<ibs.length; i++) {
				if(ibs[i].hasDefinition()) {
					def= ibs[i];
				} else if(allowDeclaration && ibs[i].hasDeclaration()) {
					dec= ibs[i];
				}
			}
			return (IIndexFragmentBinding) (def == null ? dec : def);
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		throw new CompositingNotImplementedError();
	}
	
	private static class FragmentBindingComparator implements Comparator<IIndexFragmentBinding> {
		private IIndexFragmentBindingComparator[] comparators;
		
		FragmentBindingComparator(IIndexFragmentBindingComparator[] comparators) {
			this.comparators= comparators;
		}
		
		public int compare(IIndexFragmentBinding f1, IIndexFragmentBinding f2) {
			for(int i=0; i<comparators.length; i++) {
				int cmp= comparators[i].compare(f1, f2);
				if(cmp!=Integer.MIN_VALUE) {
					return cmp;
				}
			}
			throw new IllegalArgumentException();
		}
	}
}

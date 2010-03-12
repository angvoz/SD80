/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.internal.core.dom.parser.cpp.semantics;

import java.util.BitSet;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;

/**
 * Cost for the entire function call
 */
class FunctionCost {
	private final IFunction fFunction;
	private final Cost[] fCosts;
	private final BitSet fSourceIsLValue;
	
	public FunctionCost(IFunction fn, int paramCount) {
		fFunction= fn;
		fCosts= new Cost[paramCount];
		fSourceIsLValue= new BitSet(paramCount);
	}
	
	public FunctionCost(IFunction fn, Cost cost) {
		fFunction= fn;
		fCosts= new Cost[] {cost};
		fSourceIsLValue= null; // no udc will be performed
	}

	public int getLength() {
		return fCosts.length;
	}
	
	public Cost getCost(int idx) {
		return fCosts[idx];
	}
	
	public void setCost(int idx, Cost cost, boolean sourceIsLValue) {
		fCosts[idx]= cost;
		fSourceIsLValue.set(idx, sourceIsLValue);
	}

	public IFunction getFunction() {
		return fFunction;
	}
	
	public boolean hasAmbiguousUserDefinedConversion() {
		for (Cost cost : fCosts) {
			if (cost.isAmbiguousUDC())
				return true;
		}
		return false;
	}

	public boolean hasDeferredUDC() {
		for (Cost cost : fCosts) {
			if (!cost.converts())
				return false;
			if (cost.isDeferredUDC())
				return true;
		}
		return false;
	}
	
	public boolean performUDC() throws DOMException {
		for (int i = 0; i < fCosts.length; i++) {
			Cost cost = fCosts[i];
			if (cost.isDeferredUDC()) {
				Cost udcCost = Conversions.checkUserDefinedConversionSequence(fSourceIsLValue.get(i),
						cost.source, cost.target, false, false);
				fCosts[i] = udcCost;
				if (!udcCost.converts()) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Compares this function call cost to another one.
	 */
	public int compareTo(LookupData data, FunctionCost other) throws DOMException {
		if (other == null)
			return -1;
		
		boolean haveWorse = false;
		boolean haveBetter = false;
		// In order for this function to be better than the previous best, it must
		// have at least one parameter match that is better that the corresponding
		// match for the other function, and none that are worse.
		int idx= getLength() - 1;
		int idxOther= other.getLength() - 1;
		for (; idx >= 0 && idxOther >= 0; idx--, idxOther--) {
			Cost cost= getCost(idx);
			if (!cost.converts()) {
				haveWorse = true;
				haveBetter = false;
				break;
			}

			int cmp = cost.compareTo(other.getCost(idxOther));
			haveWorse |= (cmp > 0);
			haveBetter |= (cmp < 0);
		}
	
		if (!haveWorse && !haveBetter) {
			// If they are both template functions, we can order them that way
			ICPPFunctionTemplate asTemplate= asTemplate(getFunction());
			ICPPFunctionTemplate otherAsTemplate= asTemplate(other.getFunction());
			final boolean isTemplate = asTemplate != null;
			final boolean otherIsTemplate = otherAsTemplate != null;
			
			// Prefer normal functions over template functions
			if (isTemplate && !otherIsTemplate) {
				haveWorse = true;
			} else if (!isTemplate && otherIsTemplate) {
				haveBetter = true;
			} else if (isTemplate && otherIsTemplate) {
				int order = CPPTemplates.orderTemplateFunctions(otherAsTemplate, asTemplate);
				if (order < 0) {
					haveBetter= true;	 				
				} else if (order > 0) {
					haveWorse= true;
				}
			} 
		}
		
		// if we are ambiguous at this point prefer non-index bindings
		if (haveBetter == haveWorse) {
			return -CPPSemantics.compareByRelevance(data, getFunction(), other.getFunction());
		}
		
		if (haveBetter) 
			return -1;
		
		return 1;
	}
	
	public boolean mustBeWorse(FunctionCost other) {
		if (other == null)
			return false;
		
		boolean haveWorse= false;
		int idx= getLength() - 1;
		int idxOther= other.getLength() - 1;
		for (; idx >= 0 && idxOther >= 0; idx--, idxOther--) {
			Cost cost= getCost(idx);
			if (!cost.converts()) 
				return true;
			
			Cost otherCost= other.getCost(idxOther);
			
			int cmp;
			if (cost.isDeferredUDC()) {
				cmp= cost.getRank().compareTo(otherCost.getRank());
			} else {
				cmp= cost.compareTo(otherCost);
			}
			
			if (cmp < 0)
				return false;
			if (cmp > 0)
				haveWorse= true;
		}
		
		return haveWorse;
	}

	private static ICPPFunctionTemplate asTemplate(IFunction function) {
		if (function instanceof ICPPSpecialization) {
			IBinding original= ((ICPPSpecialization) function).getSpecializedBinding();
			if (original instanceof ICPPFunctionTemplate) {
				return (ICPPFunctionTemplate) original;
			}
		}
		return null;
	}
}

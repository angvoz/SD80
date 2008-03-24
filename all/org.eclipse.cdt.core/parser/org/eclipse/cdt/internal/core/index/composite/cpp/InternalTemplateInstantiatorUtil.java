/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Bryan Wilkinson (QNX)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalTemplateInstantiator;
import org.eclipse.cdt.internal.core.index.CIndex;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;
import org.eclipse.core.runtime.CoreException;

public class InternalTemplateInstantiatorUtil {
	public static ICPPSpecialization deferredInstance(ObjectMap argMap, IType[] arguments, ICompositesFactory cf, IIndexBinding rbinding) {
		ICPPSpecialization spec= ((ICPPInternalTemplateInstantiator)rbinding).deferredInstance(argMap, arguments);
		if (spec instanceof IIndexFragmentBinding) {
			return (ICPPSpecialization) cf.getCompositeBinding((IIndexFragmentBinding) spec);
		} else {
			//can result in a non-index binding
			return spec;
		}
	}

	public static ICPPSpecialization getInstance(IType[] arguments, ICompositesFactory cf, CompositeCPPBinding cbinding) {		
		ICPPSpecialization preferredInstance= null;
		try {
			IIndexFragmentBinding[] bindings= ((CIndex)((CPPCompositesFactory)cf).getContext()).findEquivalentBindings(cbinding);
			
			for (int i = 0; i < bindings.length && !(preferredInstance instanceof IIndexFragmentBinding); i++) {
				ICPPInternalTemplateInstantiator instantiator= (ICPPInternalTemplateInstantiator) bindings[i];
				preferredInstance= instantiator.getInstance(arguments);
			}
		} catch(CoreException ce) {
			CCorePlugin.log(ce);
		}
		
		if (preferredInstance instanceof IIndexFragmentBinding) {
			return (ICPPSpecialization) cf.getCompositeBinding((IIndexFragmentBinding)preferredInstance);
		} else {
			// can result in a non-index binding
			return preferredInstance;
		}
	}

	public static IBinding instantiate(IType[] arguments, ICompositesFactory cf, IIndexBinding rbinding) {
		IBinding ins= ((ICPPInternalTemplateInstantiator)rbinding).instantiate(arguments);
		if (ins instanceof IIndexFragmentBinding) {
			return cf.getCompositeBinding((IIndexFragmentBinding)ins);
		} else {
			// can result in a non-index binding
			return ins;
		}
	}
}

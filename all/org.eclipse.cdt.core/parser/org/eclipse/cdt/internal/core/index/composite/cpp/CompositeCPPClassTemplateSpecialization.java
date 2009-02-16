/*******************************************************************************
 * Copyright (c) 2007, 2009 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferguson (Symbian) - Initial implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.composite.ICompositesFactory;

public class CompositeCPPClassTemplateSpecialization extends
CompositeCPPClassSpecialization implements ICPPClassTemplate, ICPPInstanceCache{

	public CompositeCPPClassTemplateSpecialization(ICompositesFactory cf, ICPPClassType rbinding) {
		super(cf, rbinding);
	}

	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations()
			throws DOMException {
		ICPPClassTemplatePartialSpecialization[] result= ((ICPPClassTemplate) rbinding).getPartialSpecializations();
		for (int i= 0; i < result.length; i++) {
			result[i]= (ICPPClassTemplatePartialSpecialization) cf.getCompositeBinding((IIndexFragmentBinding)result[i]);
		}
		return result;
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		return TemplateInstanceUtil.convert(cf, ((ICPPClassTemplate) rbinding).getTemplateParameters());
	}

	public ICPPTemplateInstance getInstance(ICPPTemplateArgument[] arguments) {
		return CompositeInstanceCache.getCache(cf, rbinding).getInstance(arguments);	
	}

	public void addInstance(ICPPTemplateArgument[] arguments, ICPPTemplateInstance instance) {
		CompositeInstanceCache.getCache(cf, rbinding).addInstance(arguments, instance);	
	}

	public ICPPTemplateInstance[] getAllInstances() {
		return CompositeInstanceCache.getCache(cf, rbinding).getAllInstances();
	}
	
	
	public ICPPDeferredClassInstance asDeferredInstance() throws DOMException  {
		CompositeInstanceCache cache= CompositeInstanceCache.getCache(cf, rbinding);
		synchronized (cache) {
			ICPPDeferredClassInstance dci= cache.getDeferredInstance();
			if (dci == null) {
				dci= createDeferredInstance();
				cache.putDeferredInstance(dci);
			}
			return dci;
		}
	}

	protected ICPPDeferredClassInstance createDeferredInstance() throws DOMException {
		ICPPTemplateArgument[] args = CPPTemplates.templateParametersAsArguments(getTemplateParameters());
		return new CPPDeferredClassInstance(this, args, getCompositeScope());
	}
}

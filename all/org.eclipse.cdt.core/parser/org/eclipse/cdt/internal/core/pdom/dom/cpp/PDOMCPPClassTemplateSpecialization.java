/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInstanceCache;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMNode;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Bryan Wilkinson
 * 
 */
class PDOMCPPClassTemplateSpecialization extends PDOMCPPClassSpecialization 
		implements ICPPClassTemplate, ICPPInstanceCache {

	public PDOMCPPClassTemplateSpecialization(PDOM pdom, PDOMNode parent, ICPPClassTemplate template, PDOMBinding specialized)
			throws CoreException {
		super(pdom, parent, template, specialized);
	}

	public PDOMCPPClassTemplateSpecialization(PDOM pdom, int bindingRecord) {
		super(pdom, bindingRecord);
	}
	
	@Override
	protected int getRecordSize() {
		return RECORD_SIZE;
	}

	@Override
	public int getNodeType() {
		return IIndexCPPBindingConstants.CPP_CLASS_TEMPLATE_SPECIALIZATION;
	}
		
	public ICPPClassTemplatePartialSpecialization[] getPartialSpecializations() throws DOMException {
		return ((ICPPClassTemplate)getSpecializedBinding()).getPartialSpecializations();
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPClassTemplate template = (ICPPClassTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	public ICPPTemplateInstance getInstance(IType[] arguments) {
		return PDOMInstanceCache.getCache(this).getInstance(arguments);	
	}

	public void addInstance(IType[] arguments, ICPPTemplateInstance instance) {
		PDOMInstanceCache.getCache(this).addInstance(arguments, instance);	
	}

	public ICPPTemplateInstance[] getAllInstances() {
		return PDOMInstanceCache.getCache(this).getAllInstances();	
	}
	
	@Override
	public boolean isSameType(IType type) {
		if( type == this )
			return true;

		if( type instanceof ITypedef )
			return type.isSameType( this );

		if (type instanceof PDOMNode) {
			PDOMNode node= (PDOMNode) type;
			if (node.getPDOM() == getPDOM()) {
				return node.getRecord() == getRecord();
			}
		}

		// require a class template specialization
		if (type instanceof ICPPClassSpecialization == false || 
				type instanceof ICPPTemplateDefinition == false || type instanceof IProblemBinding)
			return false;
		
		
		final ICPPClassSpecialization classSpec2 = (ICPPClassSpecialization) type;
		try {
			if (getKey() != classSpec2.getKey()) 
				return false;
			
			if (!CharArrayUtils.equals(getNameCharArray(), classSpec2.getNameCharArray()))
				return false;

			ICPPTemplateParameter[] params1= getTemplateParameters();
			ICPPTemplateParameter[] params2= ((ICPPClassTemplate) type).getTemplateParameters();

			if (params1 == params2)
				return true;

			if (params1 == null || params2 == null)
				return false;

			if (params1.length != params2.length)
				return false;

			for (int i = 0; i < params1.length; i++) {
				ICPPTemplateParameter p1= params1[i];
				ICPPTemplateParameter p2= params2[i];
				if (p1 instanceof IType && p2 instanceof IType) {
					IType t1= (IType) p1;
					IType t2= (IType) p2;
					if (!t1.isSameType(t2)) {
						return false;
					}
				} else if (p1 instanceof ICPPTemplateNonTypeParameter
						&& p2 instanceof ICPPTemplateNonTypeParameter) {
					IType t1= ((ICPPTemplateNonTypeParameter)p1).getType();
					IType t2= ((ICPPTemplateNonTypeParameter)p2).getType();
					if (t1 != t2) {
						if (t1 == null || t2 == null || !t1.isSameType(t2)) {
							return false;
						}
					}
				} else {
					return false;
				}
			}

			final IBinding owner1= getOwner();
			final IBinding owner2= classSpec2.getOwner();
			// for a specialization that is not an instance the owner has to be a class-type
			if (owner1 instanceof ICPPClassType == false || owner2 instanceof ICPPClassType == false)
				return false;

			return ((ICPPClassType) owner1).isSameType((ICPPClassType) owner2);
		} catch (DOMException e) {
			return false;
		}
	}

}

/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.parser.util.ObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * @author aniefer
 *
 */
public class CPPFunctionTemplateSpecialization extends CPPFunctionSpecialization
		implements ICPPFunctionTemplate, ICPPInternalTemplate {

	private ObjectMap instances = null;
	
	public CPPFunctionTemplateSpecialization(IBinding specialized, ICPPClassType owner, ObjectMap argumentMap) {
		super(specialized, owner, argumentMap);
	}

	public ICPPTemplateParameter[] getTemplateParameters() throws DOMException {
		ICPPFunctionTemplate template = (ICPPFunctionTemplate) getSpecializedBinding();
		return template.getTemplateParameters();
	}

	public final void addInstance(IType[] arguments, ICPPTemplateInstance instance) {
		if (instances == null)
			instances = new ObjectMap(2);
		instances.put(arguments, instance);
	}

	public final ICPPTemplateInstance getInstance(IType[] arguments) {
		if (instances != null) {
			loop: for (int i=0; i < instances.size(); i++) {
				IType[] args = (IType[]) instances.keyAt(i);
				if (args.length == arguments.length) {
					for (int j=0; j < args.length; j++) {
						if (!CPPTemplates.isSameTemplateArgument(args[j], arguments[j])) {
							continue loop;
						}
					}
					return (ICPPTemplateInstance) instances.getAt(i);
				}
			}
		}
		return null;
	}
	
	public ICPPTemplateInstance[] getAllInstances() {
		if (instances != null) {
			ICPPTemplateInstance[] result= new ICPPTemplateInstance[instances.size()];
			for (int i=0; i < instances.size(); i++) {
				result[i]= (ICPPTemplateInstance) instances.getAt(i);
			}
			return result;
		}
		return ICPPTemplateInstance.EMPTY_TEMPLATE_INSTANCE_ARRAY;
	}
}

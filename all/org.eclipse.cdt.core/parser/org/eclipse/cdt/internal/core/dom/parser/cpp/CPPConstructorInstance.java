/*******************************************************************************
 * Copyright (c) 2007, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bryan Wilkinson (QNX) - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;

/**
 * Instantiation of a constructor template
 */
public class CPPConstructorInstance extends CPPMethodInstance implements ICPPConstructor {

	public CPPConstructorInstance(ICPPClassType owner, ICPPConstructor orig, 
			CPPTemplateParameterMap tpmap, ICPPTemplateArgument[] args) {
		super(owner, orig, tpmap, args);
	}
	
	public boolean isExplicit() throws DOMException {
		return ((ICPPConstructor)getTemplateDefinition()).isExplicit();
	}
}

/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;

/**
 * @author jcamelon
 */
public interface ICPPASTTypeIdExpression extends IASTTypeIdExpression {

	public static final int op_typeid = IASTTypeIdExpression.op_typeid;

	/**
	 * @deprecated all constants should be declared in {@link IASTTypeIdExpression}
	 */
	@Deprecated
	public static final int op_last = IASTTypeIdExpression.op_last;
}

/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.gnu.cpp;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;

/**
 * G++ introduces additional operators.
 * 
 * @author jcamelon
 */
public interface IGPPASTBinaryExpression extends ICPPASTBinaryExpression {

	/**
	 * <code>op_max</code> represents >?
	 */
	public static final int op_max = ICPPASTBinaryExpression.op_last + 1;

	/**
	 * <code>op_min</code> represents <?
	 */
	public static final int op_min = ICPPASTBinaryExpression.op_last + 2;

	/**
	 * <code>op_last</code> provided for sub-interfaces to extend.
	 */
	public static final int op_last = op_min;

}

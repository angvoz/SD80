/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Camelon (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;

/**
 * This is a templated template parameter.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPASTTemplatedTypeTemplateParameter extends
		ICPPASTTemplateParameter, IASTNameOwner {

	/**
	 * PARAMETER
	 */
	public static final ASTNodeProperty PARAMETER = new ASTNodeProperty(
			"ICPPASTTemplateTypeTemplateParameter.PARAMETER - Template Parameter"); //$NON-NLS-1$

	/**
	 * Get all template parameters.
	 * 
	 * @return <code>ICPPASTTemplateParameter []</code>
	 */
	public ICPPASTTemplateParameter[] getTemplateParameters();

	/**
	 * Add a parameter.
	 * 
	 * @param parm
	 *            <code>ICPPASTTemplateParameter</code>
	 */
	public void addTemplateParamter(ICPPASTTemplateParameter parm);

	/**
	 * This parameter's name.
	 */
	public static final ASTNodeProperty PARAMETER_NAME = new ASTNodeProperty(
			"ICPPASTTemplateTypeTemplateParameter.PARAMETER_NAME - Template Parameter's Name"); //$NON-NLS-1$

	/**
	 * Get name.
	 * 
	 * @return <code>IASTName</code>
	 */
	public IASTName getName();

	/**
	 * Set name.
	 * 
	 * @param name
	 *            <code>IASTName</code>
	 */
	public void setName(IASTName name);

	/**
	 * DEFAULT_VALUE is an expession.
	 */
	public static final ASTNodeProperty DEFAULT_VALUE = new ASTNodeProperty(
			"ICPPASTTemplateTypeTemplateParameter.DEFAULT_VALUE - Default Value is an expression"); //$NON-NLS-1$

	/**
	 * Get default value for template type.
	 * 
	 * @return <code>IASTExpression</code>
	 */
	public IASTExpression getDefaultValue();

	/**
	 * Set default value for template type.
	 * 
	 * @param expression
	 *            <code>IASTExpression</code>
	 */
	public void setDefaultValue(IASTExpression expression);
	
	/**
	 * @since 5.1
	 */
	public ICPPASTTemplatedTypeTemplateParameter copy();
}

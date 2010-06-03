/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.services;

import java.util.concurrent.Executor;

import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.IStatus;

public interface IEDCExpression extends IExpressionDMContext, IEDCDMContext {

	public Executor getExecutor();
	public DsfServicesTracker getServiceTracker();

	/**
	 * Change the name of the expression that appears in the Variables view Name
	 * column or the Expressions view Expression column.  This is typically
	 * used to make the subexpressions of an expression show only the
	 * suffix of the expression relative to the parent expression and
	 * to differentiates children from each other (though it is not intended
	 * to have any syntactic significance when catenated to the parent).
	 * 
	 * Note: {@link #getExpression()} is always the full expression.
	 */
	public void setName(String name);
	
	public IFrameDMContext getFrame();

	public void evaluateExpression();

	public FormattedValueDMData getFormattedValue(FormattedValueDMContext dmc);

	public IVariableLocation getValueLocation();

	/** Get error during evaluation, or <code>null</code> if no error, or {@link #evaluateExpression()} has not been called */
	public IStatus getEvaluationError();
	/** Get numeric value after {@link #evaluateExpression()} */
	public Number getEvaluatedValue();
	/** Get string value after {@link #evaluateExpression()}, or the string value for
	 * a string expression, or the formatted value set by {@link #setEvaluatedValueString(String)} */
	public String getEvaluatedValueString();

	public void setEvaluatedValue(Number value);
	public void setEvaluatedValueString(String string);

	public IVariableLocation getEvaluatedLocation();

	public IType getEvaluatedType();

	public String getTypeName();

	public boolean hasChildren();

	/** Get any casting in effect.  May be <code>null</code>. */
	public CastInfo getCastInfo();


}
/*******************************************************************************
 * Copyright (c) 2009 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.formatter;

import org.eclipse.cdt.debug.edc.internal.services.dsf.Expressions.ExpressionDMC;
import org.eclipse.cdt.debug.edc.internal.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;

/**
 * Abstract class implementing IVariableValueConverter with summary and detail variants
 */
public abstract class AbstractVariableConverter implements IVariableValueConverter {

	protected IType type;
	private boolean forDetails;

	public AbstractVariableConverter(IType type, boolean forDetails) {
		this.forDetails = forDetails;
		this.type = type;
	}
	
	protected abstract String getDetailsValue(IExpressionDMContext variable);

	protected abstract String getSummaryValue(IExpressionDMContext variable);
	
	public String getValue(IExpressionDMContext variable) {
		if (forDetails)
			return getDetailsValue(variable);
		return getSummaryValue(variable);
	}

	public boolean canEditValue() {
		return false; // read-only implementation
	}
	
	public void setValue(IExpressionDMContext variable, String newValue) {
		// read-only implementation
	}
	
	protected String getMemberValue(IExpressionDMContext variable, IType type, String memberName) {
		return getMemberValue(variable, type, memberName, IExpressions.NATURAL_FORMAT);
	}

	protected String getMemberValue(IExpressionDMContext variable, IType type, String memberName, String format) {
		IExpressions expressions = ((ExpressionDMC)variable).getService();
		ExpressionDMC expression = 
			(ExpressionDMC) expressions.createExpression(variable, variable.getExpression()
					+ FormatUtils.getFieldAccessor(type) + memberName);
		FormattedValueDMContext fvc = expressions.getFormattedValueContext(expression, format);
		return expression.getFormattedValue(fvc).getFormattedValue();
	}
}

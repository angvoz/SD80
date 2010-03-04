package org.eclipse.cdt.debug.edc.services;

import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;

public interface IEDCExpression extends IExpressionDMContext, IEDCDMContext {

	// we need a property to hold the expression string when it differs from the
	// string we display in the Variables view Name column or the Expressions
	// view Expression column
	public final static String EXPRESSION_PROP = "Expression"; //$NON-NLS-1$

	public IFrameDMContext getFrame();

	public String getExpression();

	public void evaluateExpression();

	public FormattedValueDMData getFormattedValue(FormattedValueDMContext dmc);

	public Object getValueLocation();

	public Object getEvaluatedValue();

	public void setEvaluatedValue(Object value);

	public Object getEvaluatedLocation();

	public Object getEvaluatedType();

	public String getTypeName();

	public boolean hasChildren();

	public IExpressions getService();

}
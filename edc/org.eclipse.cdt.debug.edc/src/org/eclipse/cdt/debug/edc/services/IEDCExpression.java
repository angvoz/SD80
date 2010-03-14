package org.eclipse.cdt.debug.edc.services;

import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.debug.edc.symbols.IVariableLocation;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.core.runtime.IStatus;

public interface IEDCExpression extends IExpressionDMContext, IEDCDMContext {

	// we need a property to hold the expression string when it differs from the
	// string we display in the Variables view Name column or the Expressions
	// view Expression column
	public final static String EXPRESSION_PROP = "Expression"; //$NON-NLS-1$

	public IFrameDMContext getFrame();

	public String getExpression();

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

	public IExpressions getService();

}
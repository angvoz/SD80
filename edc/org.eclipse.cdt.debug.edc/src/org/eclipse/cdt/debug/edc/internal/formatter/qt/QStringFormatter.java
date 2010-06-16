/*******************************************************************************
 * Copyright (c) 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.edc.internal.formatter.qt;

import java.text.MessageFormat;

import org.eclipse.cdt.debug.edc.formatter.AbstractVariableConverter;
import org.eclipse.cdt.debug.edc.formatter.FormatUtils;
import org.eclipse.cdt.debug.edc.formatter.ITypeContentProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableFormatProvider;
import org.eclipse.cdt.debug.edc.formatter.IVariableValueConverter;
import org.eclipse.cdt.debug.edc.internal.symbols.IPointerType;
import org.eclipse.cdt.debug.edc.services.IEDCExpression;
import org.eclipse.cdt.debug.edc.symbols.IType;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.core.runtime.CoreException;

public class QStringFormatter implements IVariableFormatProvider {
	// TODO once the dwarf reader Bug 11422 -  EDC does not display the members of QString,
	// replace this implementation with one like QByteArrayFormatter
	
	private static final String TYPE_NAME = "QString"; //$NON-NLS-1$

	public class VariableConverter extends AbstractVariableConverter {

		private static final String EXPRESSION_FMT = 
			"reinterpret_cast<wchar_t *>((reinterpret_cast<Data*>({0}{1}))->data)";

		public VariableConverter(IType type, boolean forDetails) {
			super(type, forDetails);
		}

		@Override
		protected String getDetailsValue(IExpressionDMContext variable) throws CoreException {
			return getSummaryValue(variable);
		}

		@Override
		protected String getSummaryValue(IExpressionDMContext variable) throws CoreException {
			IEDCExpression variableExp = (IEDCExpression) variable;
			IExpressions expressions = variableExp.getServiceTracker().getService(IExpressions.class);
			String expressionString = MessageFormat.format(EXPRESSION_FMT, getDerefPrefix(variableExp.getEvaluatedType()), variableExp.getExpression());
			IEDCExpression convertedExp = 
				(IEDCExpression) expressions.createExpression(variableExp.getFrame(), expressionString);
			IVariableValueConverter valueConverter = FormatUtils.getCustomValueConverter(convertedExp);
			if (valueConverter != null)
				return valueConverter.getValue(convertedExp);
			return null;
		}

		private String getDerefPrefix(IType type) {
			StringBuffer sb = new StringBuffer();
			while (type instanceof IPointerType) {
				type = ((IType) type).getType();
				sb.append('*');
			}
			return sb.toString();
		}
	}

	public ITypeContentProvider getTypeContentProvider(IType type) {
		return null;
	}

	public IVariableValueConverter getVariableValueConverter(IType type) {
		if (FormatUtils.checkClassOrInheritanceByName(type, TYPE_NAME)) {
			return new VariableConverter(type, false);
		}
		return null;
	}

	public IVariableValueConverter getDetailValueConverter(IType type) {
		if (FormatUtils.checkClassOrInheritanceByName(type, TYPE_NAME)) {
			return new VariableConverter(type, true);
		}
		return null;
	}

}
